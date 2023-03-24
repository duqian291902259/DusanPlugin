import * as path from 'path';
import * as vscode from 'vscode';
import { Disposable, disposeAll } from './dispose';
import { getNonce } from './utils';
import * as fs from 'fs';
/**
 * Define the type of edits used in paw draw files.
 */
interface PawDrawEdit {
	readonly color: string;
	readonly stroke: ReadonlyArray<[number, number]>;
}

interface PawDrawDocumentDelegate {
	getFileData(): Promise<Uint8Array>;
}

/**
 * Define the document (the data model) used for paw draw files.
 */
class PawDrawDocument extends Disposable implements vscode.CustomDocument {

	static async create(
		uri: vscode.Uri,
		backupId: string | undefined,
		delegate: PawDrawDocumentDelegate,
	): Promise<PawDrawDocument | PromiseLike<PawDrawDocument>> {
		// If we have a backup, read that. Otherwise read the resource from the workspace
		const dataFile = typeof backupId === 'string' ? vscode.Uri.parse(backupId) : uri;
		const fileData = await PawDrawDocument.readFile(dataFile);
		return new PawDrawDocument(uri, fileData, delegate);
	}

	private static async readFile(uri: vscode.Uri): Promise<Uint8Array> {
		if (uri.scheme === 'untitled') {
			return new Uint8Array();
		}
		return vscode.workspace.fs.readFile(uri);
	}

	private readonly _uri: vscode.Uri;

	private _documentData: Uint8Array;
	private _edits: Array<PawDrawEdit> = [];
	private _savedEdits: Array<PawDrawEdit> = [];

	private readonly _delegate: PawDrawDocumentDelegate;

	private constructor(
		uri: vscode.Uri,
		initialContent: Uint8Array,
		delegate: PawDrawDocumentDelegate
	) {
		super();
		this._uri = uri;
		this._documentData = initialContent;
		this._delegate = delegate;
	}

	public get uri() { return this._uri; }

	public get documentData(): Uint8Array { return this._documentData; }

	private readonly _onDidDispose = this._register(new vscode.EventEmitter<void>());
	/**
	 * Fired when the document is disposed of.
	 */
	public readonly onDidDispose = this._onDidDispose.event;

	private readonly _onDidChangeDocument = this._register(new vscode.EventEmitter<{
		readonly content?: Uint8Array;
		readonly edits: readonly PawDrawEdit[];
	}>());
	/**
	 * Fired to notify webviews that the document has changed.
	 */
	public readonly onDidChangeContent = this._onDidChangeDocument.event;

	private readonly _onDidChange = this._register(new vscode.EventEmitter<{
		readonly label: string,
		undo(): void,
		redo(): void,
	}>());
	/**
	 * Fired to tell VS Code that an edit has occured in the document.
	 * 
	 * This updates the document's dirty indicator.
	 */
	public readonly onDidChange = this._onDidChange.event;

	/**
	 * Called by VS Code when there are no more references to the document.
	 * 
	 * This happens when all editors for it have been closed.
	 */
	dispose(): void {
		this._onDidDispose.fire();
		super.dispose();
	}

	/**
	 * Called when the user edits the document in a webview.
	 * 
	 * This fires an event to notify VS Code that the document has been edited.
	 */
	makeEdit(edit: PawDrawEdit) {
		this._edits.push(edit);

		this._onDidChange.fire({
			label: 'Stroke',
			undo: async () => {
				this._edits.pop();
				this._onDidChangeDocument.fire({
					edits: this._edits,
				});
			},
			redo: async () => {
				this._edits.push(edit);
				this._onDidChangeDocument.fire({
					edits: this._edits,
				});
			}
		});
	}

	/**
	 * Called by VS Code when the user saves the document.
	 */
	async save(cancellation: vscode.CancellationToken): Promise<void> {
		await this.saveAs(this.uri, cancellation);
		this._savedEdits = Array.from(this._edits);
	}

	/**
	 * Called by VS Code when the user saves the document to a new location.
	 */
	async saveAs(targetResource: vscode.Uri, cancellation: vscode.CancellationToken): Promise<void> {
		const fileData = await this._delegate.getFileData();
		if (cancellation.isCancellationRequested) {
			return;
		}
		await vscode.workspace.fs.writeFile(targetResource, fileData);
	}

	/**
	 * Called by VS Code when the user calls `revert` on a document.
	 */
	async revert(_cancellation: vscode.CancellationToken): Promise<void> {
		const diskContent = await PawDrawDocument.readFile(this.uri);
		this._documentData = diskContent;
		this._edits = this._savedEdits;
		this._onDidChangeDocument.fire({
			content: diskContent,
			edits: this._edits,
		});
	}

	/**
	 * Called by VS Code to backup the edited document.
	 * 
	 * These backups are used to implement hot exit.
	 */
	async backup(destination: vscode.Uri, cancellation: vscode.CancellationToken): Promise<vscode.CustomDocumentBackup> {
		await this.saveAs(destination, cancellation);

		return {
			id: destination.toString(),
			delete: async () => {
				try {
					await vscode.workspace.fs.delete(destination);
				} catch {
					// noop
				}
			}
		};
	}
}

/**
 * Provider for paw draw editors.
 * 
 * Paw draw editors are used for `.pawDraw` files, which are just `.png` files with a different file extension.
 * 
 * This provider demonstrates:
 * 
 * - How to implement a custom editor for binary files.
 * - Setting up the initial webview for a custom editor.
 * - Loading scripts and styles in a custom editor.
 * - Communication between VS Code and the custom editor.
 * - Using CustomDocuments to store information that is shared between multiple custom editors.
 * - Implementing save, undo, redo, and revert.
 * - Backing up a custom editor.
 */
export class SvgaFileEditorProvider implements vscode.CustomEditorProvider<PawDrawDocument> {

	public static register(context: vscode.ExtensionContext): vscode.Disposable {
		return vscode.window.registerCustomEditorProvider(
			SvgaFileEditorProvider.viewType,
			new SvgaFileEditorProvider(context),
			{
				webviewOptions: {
					retainContextWhenHidden: true,
				},
				supportsMultipleEditorsPerDocument: false,
			});
	}

	private static readonly viewType = 'svga.preview';

	/**
	 * Tracks all known webviews
	 */
	private readonly webviews = new WebviewCollection();

	constructor(
		private readonly _context: vscode.ExtensionContext
	) { }

	//#region CustomEditorProvider

	async openCustomDocument(
		uri: vscode.Uri,
		openContext: { backupId?: string },
		_token: vscode.CancellationToken
	): Promise<PawDrawDocument> {
		const document: PawDrawDocument = await PawDrawDocument.create(uri, openContext.backupId, {
			getFileData: async () => {
				const webviewsForDocument = Array.from(this.webviews.get(document.uri));
				if (!webviewsForDocument.length) {
					throw new Error('Could not find webview to save for');
				}
				const panel = webviewsForDocument[0];
				const response = await this.postMessageWithResponse<number[]>(panel, 'getFileData', {});
				return new Uint8Array(response);
			}
		});

		const listeners: vscode.Disposable[] = [];

		listeners.push(document.onDidChange(e => {
			// Tell VS Code that the document has been edited by the use.
			this._onDidChangeCustomDocument.fire({
				document,
				...e,
			});
		}));

		listeners.push(document.onDidChangeContent(e => {
			// Update all webviews when the document changes
			for (const webviewPanel of this.webviews.get(document.uri)) {
				this.postMessage(webviewPanel, 'update', {
					edits: e.edits,
					content: e.content,
				});
			}
		}));

		document.onDidDispose(() => disposeAll(listeners));

		return document;
	}

	async resolveCustomEditor(
		document: PawDrawDocument,
		webviewPanel: vscode.WebviewPanel,
		_token: vscode.CancellationToken
	): Promise<void> {
		this.webviews.add(document.uri, webviewPanel);
		webviewPanel.webview.options = {
			enableScripts: true,
		};
		webviewPanel.webview.html = this.getHtmlForWebview(webviewPanel.webview);


		webviewPanel.webview.onDidReceiveMessage(e => this.onMessage(document, e));

		webviewPanel.webview.onDidReceiveMessage(e => {
			console.log("document.documentData");
			if (e.type === 'ready') {
				console.log("read to play"),
				this.postMessage(webviewPanel, 'init', {
					value: document.documentData
				});
			}
		});
	}

	private readonly _onDidChangeCustomDocument = new vscode.EventEmitter<vscode.CustomDocumentEditEvent<PawDrawDocument>>();
	public readonly onDidChangeCustomDocument = this._onDidChangeCustomDocument.event;

	public saveCustomDocument(document: PawDrawDocument, cancellation: vscode.CancellationToken): Thenable<void> {
		return document.save(cancellation);
	}

	public saveCustomDocumentAs(document: PawDrawDocument, destination: vscode.Uri, cancellation: vscode.CancellationToken): Thenable<void> {
		return document.saveAs(destination, cancellation);
	}

	public revertCustomDocument(document: PawDrawDocument, cancellation: vscode.CancellationToken): Thenable<void> {
		return document.revert(cancellation);
	}

	public backupCustomDocument(document: PawDrawDocument, context: vscode.CustomDocumentBackupContext, cancellation: vscode.CancellationToken): Thenable<vscode.CustomDocumentBackup> {
		return document.backup(context.destination, cancellation);
	}
	private getHtmlForWebview(webview: vscode.Webview): string {
		const scriptPath = vscode.Uri.file(
			path.join(this._context.extensionPath, 'media', 'svga.perview.js')
		);
		const scriptUri = webview.asWebviewUri(scriptPath);

		const svgaFilePath = vscode.Uri.file(
			path.join(this._context.extensionPath, 'media', 'svga.lite.min.js')
		);
		const svgaFile = webview.asWebviewUri(svgaFilePath);

		const htmlPath = vscode.Uri.file(
			path.join(this._context.extensionPath, 'media', 'svga_new.html')
		);
		const nonce = getNonce();

		/* const htmlContent1 = this.getHtmlContent(nonce, svgaFile, scriptUri);
		console.log("htmlContent1=" + htmlContent1);

		var htmlContent = fs.readFileSync(htmlPath.path, 'utf8');

		htmlContent = htmlContent.replace("{nonce}", nonce).replace("{nonce}", nonce).replace("{svgaFile}", svgaFile + "").replace("{scriptUri}", scriptUri + ""); */
		const htmlContent = this.getHtmlContentNew(nonce, svgaFile, scriptUri);
		//console.log("htmlContent=" + htmlContent);
		return htmlContent;
	}

	private _requestId = 1;
	private readonly _callbacks = new Map<number, (response: any) => void>();


	private getHtmlContentNew(nonce: string, svgaFile: vscode.Uri, scriptUri: vscode.Uri): string {
		return `<!DOCTYPE html>
		<html lang="en">
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
			<meta name="description" content="SVGAPlayer by 杜小菜，duqian2010@gmail.com">
			<meta http-equiv="X-UA-Compatible" content="IE=edge">
			<meta name="renderer" content="webkit">
			<title>SVGAPlayer</title>
			<style>
				
				html {
					background: transparent;
					width: 100%;
					height: 100%;
					margin: 0;
					padding: 0;
				}
		
				body {
					background-color: rgb(60, 63, 65);
					overflow: hidden;
					width: 100%;
					height: 100%;
					margin: 0;
					padding-top: 30px;
					box-sizing: border-box;
					overflow-x: scroll;
					overflow-y: auto;
				}
		
				.container_header {
					width: 60%;
					height: 80px;
					margin: 10px auto 0px;
				}
		
				.title {
					height: 30px;
					line-height: 30px;
					margin: 0;
				}
		
				#svgaTitle {
					width: 50%;
					height: 30px;
					margin: 10px auto 0px;
					color: #ffffff;
					display: flex;
					text-align: center;
					min-width: 550px;
				}
		
				button {
					width: 50px;
					height: 30px;
					padding: 5px 0px;
					margin-left: 10px;
					text-align: center;
					border-radius: 4px;
					border: none;
				}
		
				#topDiv {
					height: 70px;
					min-height: 30px;
					line-height: 30px;
					background: rgb(60, 63, 65);
					position: relative;
					margin-left:100px;
				}
		
				.switch-bg-btn {
					width: 14px;
					height: 14px;
					cursor: pointer;
					float: left;
					margin-left: 8px;
					margin-top: 8px;
					box-sizing: border-box;
					border: 1px solid rgb(50, 50, 50);
				}
		
				#switch-bg-none {
					background-size: 12px 12px;
					background-image: url(data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPHN2ZyB2ZXJzaW9uPSIxLjEiIGlkPSJiYWNrZ3JvdW5kSW1hZ2VfYnlfbW9reSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB3aWR0aD0iMTAiIGhlaWdodD0iMTAiPgogICAgdmlld0JveD0iMCAwIDEwIDEwIiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCAxMCAxMDsiIHhtbDpzcGFjZT0icHJlc2VydmUiPgogICAgPHN0eWxlIHR5cGU9InRleHQvY3NzIj4KICAgICAgICAuc3Qwe2ZpbGw6I0ZGRkZGRjt9CiAgICAgICAgLnN0MXtmaWxsOiNDMEMwQzA7fQogICAgPC9zdHlsZT4KICAgIDxyZWN0IGNsYXNzPSJzdDAiIHdpZHRoPSIxMCIgaGVpZ2h0PSIxMCIvPgogICAgPHJlY3QgY2xhc3M9InN0MSIgd2lkdGg9IjUiIGhlaWdodD0iNSIvPgogICAgPHJlY3QgeD0iNSIgeT0iNSIgY2xhc3M9InN0MSIgd2lkdGg9IjUiIGhlaWdodD0iNSIvPgo8L3N2Zz4K);
				}
		
				#switch-bg-black {
					background-color: black;
				}
		
				#switch-bg-white {
					background-color: white;
				}
		
				#switch-bg-blue {
					background-color: #3498DB;
				}
		
				#switch-bg-green {
					background-color: #30CC71;
				}
		
				#switch-bg-yellow {
					background-color: #F1C40D;
				}
		
				#switch-bg-red {
					background-color: #C0392B;
				}
		
				#infoDiv {
					height: 30px;
					line-height: 30px;
					width: auto;
					position: absolute;
					font-family: '.AppleSystemUIFont', serif;
					font-size: 12px;
					color: rgb(255, 255, 255);
					cursor: default;
				}
			
				#content-body {
					width: 70%;
					height: 100%;
					margin:20px auto;
					position: relative;
					overflow-x: scroll;
					overflow-y: auto;
				}

				#playerCanvas {
					max-width:70%;
					max-height:70%;
					overflow-x: scroll;
					overflow-y: auto;
					position: absolute;
					left: 50%;
					transform: translate(-50%,0%);
					border: 1px solid #c0c0c0;
					background-image: url(data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPHN2ZyB2ZXJzaW9uPSIxLjEiIGlkPSJiYWNrZ3JvdW5kSW1hZ2VfYnlfbW9reSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB3aWR0aD0iMTAiIGhlaWdodD0iMTAiPgogICAgdmlld0JveD0iMCAwIDEwIDEwIiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCAxMCAxMDsiIHhtbDpzcGFjZT0icHJlc2VydmUiPgogICAgPHN0eWxlIHR5cGU9InRleHQvY3NzIj4KICAgICAgICAuc3Qwe2ZpbGw6I0ZGRkZGRjt9CiAgICAgICAgLnN0MXtmaWxsOiNDMEMwQzA7fQogICAgPC9zdHlsZT4KICAgIDxyZWN0IGNsYXNzPSJzdDAiIHdpZHRoPSIxMCIgaGVpZ2h0PSIxMCIvPgogICAgPHJlY3QgY2xhc3M9InN0MSIgd2lkdGg9IjUiIGhlaWdodD0iNSIvPgogICAgPHJlY3QgeD0iNSIgeT0iNSIgY2xhc3M9InN0MSIgd2lkdGg9IjUiIGhlaWdodD0iNSIvPgo8L3N2Zz4K);
				}
			</style>
		</head>
		
		<body oncontextmenu="return false;" onselectstart="return false;">
			<div class="container_header">
				<div id="svgaTitle">
					<h5 class="title">SVGAPlayer,Click color-button to change the background color</h5>
					<button onclick="startAnimation()">Play</button>
					<button onclick="pauseAnimation()">Pause</button>
					<button onclick="stopAnimation()">Clear</button>
				</div>
				<div id="topDiv">
					<div class="switch-bg-btn" id="switch-bg-none" onclick="onSwitchBackground(this)"></div>
					<div class="switch-bg-btn" id="switch-bg-black" onclick="onSwitchBackground(this)"></div>
					<div class="switch-bg-btn" id="switch-bg-white" onclick="onSwitchBackground(this)"></div>
					<div class="switch-bg-btn" id="switch-bg-blue" onclick="onSwitchBackground(this)"></div>
					<div class="switch-bg-btn" id="switch-bg-green" onclick="onSwitchBackground(this)"></div>
					<div class="switch-bg-btn" id="switch-bg-yellow" onclick="onSwitchBackground(this)"></div>
					<div class="switch-bg-btn" id="switch-bg-red" onclick="onSwitchBackground(this)"></div>
					<div id="infoDiv"></div>
				</div>
			</div>
			<div id="content_body">
				<div id="content-div">
					<canvas id="playerCanvas"></canvas>
					<script nonce="${nonce}" src="${svgaFile}"></script>
					<!-- <script nonce="${nonce}" src="${scriptUri}"></script>
					<!-- <script nonce="WRjNwz1vANRYI2atJBqF1gIllCxZpg90"
						src="https://file%2B.vscode-resource.vscode-cdn.net/Users/duqian/Documents/DuQian/MyGithub/MyPlugins/VSCodePlugins/duqian/media/svga.lite.min.js"></script>  -->
				</div>
			</div>
		
			<script type="text/javascript">
				window.addEventListener('mousewheel', function (event) {
					if (event.ctrlKey === true || event.metaKey) {
						event.preventDefault();
					}
				}, { passive: false });
		
				window.addEventListener('DOMMouseScroll', function (event) {
					if (event.ctrlKey === true || event.metaKey) {
						event.preventDefault();
					}
				}, { passive: false });
		
		
				console.log("init1");
				const {
					Downloader,
					Parser,
					Player
				} = SVGA;
				let parser = new SVGA.Parser();
				let player = new SVGA.Player('#playerCanvas');
				let lastData = undefined;
				(function () {
					const vscode = acquireVsCodeApi();
					const svgaFn = (data) => {
						const downloader = new Downloader();
						// 默认调用 WebWorker 线程解析
						// 可配置 new Parser({ disableWorker: true }) 禁止
						console.log("init play");
				
						(async () => {
							const svgaData = await parser.do(data);
							await player.mount(svgaData);
							player
								// 开始动画事件回调
								.$on('start', () => console.log('event start'))
								// 暂停动画事件回调
								.$on('pause', () => console.log('event pause'))
								// 停止动画事件回调
								.$on('stop', () => console.log('event stop'))
								// 动画结束事件回调
								.$on('end', () => console.log('event end'))
								// 清空动画事件回调
								.$on('clear', () => console.log('event clear'))
								// 动画播放中事件回调
								.$on('process', () => console.log('event process', player.progress));
							// 开始播放动画
							player.start();
						})();
					};
				
					// // Handle messages from the extension
					window.addEventListener('message', async e => {
						const {
							type,
							body,
						} = e.data;
						switch (type) {
							case 'init': {
								const originData = body.value.data;
								const data = new Uint8Array(originData);
								console.log("originData=" + originData);
								console.log("data=" + data);
								lastData = data;
								svgaFn(data);
							}
						}
					});
					vscode.postMessage({
						type: 'ready'
					});
				}());
		
				//播放
				function startAnimation() {
					parser = new SVGA.Parser();
				    player = new SVGA.Player('#playerCanvas');
					console.log("startAnimation"+lastData);
					(async () => {
						const svgaData = await parser.do(lastData);
						await player.mount(svgaData);
						// 开始播放动画
						player.start();
					})();
				}
		
				//暂停
				function pauseAnimation() {
					console.log("pauseAnimation");
					player.pause();
				}
		
				//停止播放动画，如果 clearsAfterStop === true，将会清空画布
				function stopAnimation() {
					console.log("stopAnimation");
					player.stop();
				}
		
				function onSwitchBackground(target) {
					document.getElementById('playerCanvas').style.backgroundImage = getComputedStyle(target, null).backgroundImage;
					document.getElementById('playerCanvas').style.backgroundColor = getComputedStyle(target, null).backgroundColor;
					if (target.id === 'switch-bg-none') {
						document.getElementById('playerCanvas').style.borderWidth = '1px';
					} else {
						document.getElementById('playerCanvas').style.borderWidth = '0';
					}
				}
			</script>
		</body>
		
		</html>`;
	}

	private getHtmlContent(nonce: string, svgaFile: vscode.Uri, scriptUri: vscode.Uri): string {
		return `<!DOCTYPE html>
				<html lang="en">
				<head>
					<meta charset="UTF-8">
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<title>SVGA-Player</title>
				</head>
				<style>
					body,html {
						width:100%;
						height: 100%;
						display: flex;
						justify-content: center;
						align-items: center;
						overflow: hidden;
						color:#fff;
					}
				</style>	
				<body>
					<canvas id="playerCanvas"></canvas>
					<script nonce="${nonce}" src="${svgaFile}"></script>
					<script nonce="${nonce}" src="${scriptUri}"></script>
				</body>
				</html>`;
	}

	private postMessageWithResponse<R = unknown>(panel: vscode.WebviewPanel, type: string, body: any): Promise<R> {
		const requestId = this._requestId++;
		const p = new Promise<R>(resolve => this._callbacks.set(requestId, resolve));
		panel.webview.postMessage({ type, requestId, body });
		return p;
	}

	private postMessage(panel: vscode.WebviewPanel, type: string, body: any): void {
		panel.webview.postMessage({ type, body });
	}

	private onMessage(document: PawDrawDocument, message: any) {
		switch (message.type) {
			case 'stroke':
				document.makeEdit(message as PawDrawEdit);
				return;

			case 'response':
				{
					const callback = this._callbacks.get(message.requestId);
					callback?.(message.body);
					return;
				}
		}
	}
}

/**
 * Tracks all webviews.
 */
class WebviewCollection {

	private readonly _webviews = new Set<{
		readonly resource: string;
		readonly webviewPanel: vscode.WebviewPanel;
	}>();

	/**
	 * Get all known webviews for a given uri.
	 */
	public *get(uri: vscode.Uri): Iterable<vscode.WebviewPanel> {
		const key = uri.toString();
		for (const entry of this._webviews) {
			if (entry.resource === key) {
				yield entry.webviewPanel;
			}
		}
	}

	/**
	 * Add a new webview to the collection.
	 */
	public add(uri: vscode.Uri, webviewPanel: vscode.WebviewPanel) {
		const entry = { resource: uri.toString(), webviewPanel };
		this._webviews.add(entry);

		webviewPanel.onDidDispose(() => {
			this._webviews.delete(entry);
		});
	}
}