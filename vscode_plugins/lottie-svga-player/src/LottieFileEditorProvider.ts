import * as path from 'path';
import * as vscode from 'vscode';
import { Disposable, disposeAll } from './dispose';
import { isJsonString, getErrorPage } from './utils';
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
export class LottieFileEditorProvider implements vscode.CustomEditorProvider<PawDrawDocument> {

	public static register(context: vscode.ExtensionContext): vscode.Disposable {
		return vscode.window.registerCustomEditorProvider(
			LottieFileEditorProvider.viewType,
			new LottieFileEditorProvider(context),
			{
				webviewOptions: {
					retainContextWhenHidden: true,
				},
				supportsMultipleEditorsPerDocument: false,
			});
	}

	private static readonly viewType = 'lottie.preview';

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
				console.log("openCustomDocument " + uri);
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
			let json = document.documentData + "";
			console.log("document.documentData:" + json);
			if (e.type === 'ready') {

				let checkLottie = isJsonString(json);
				console.log("read to play,checkLottie=" + checkLottie);

				if (checkLottie === true) {
					const htmlContent = this.getHtmlContent(json);
					//send msg to start play
					this.postMessage(webviewPanel, 'init', {
						value: document.documentData
					});
					webviewPanel.webview.html = htmlContent;
				} else {
					vscode.commands.executeCommand('workbench.action.closeActiveEditor');
					console.log("not lottie file=" + json);

					vscode.commands.executeCommand(
						"vscode.openWith",
						document.uri,
						"default",
						webviewPanel.viewColumn
					);
					// Not loading HTML into the webview
					//webviewPanel.webview.html = getErrorPage(json);
				}
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
		const lottie = vscode.Uri.file(
			path.join(this._context.extensionPath, 'demo', 'lottie.json')
		);
		let jsonString = fs.readFileSync(lottie.path, 'utf8');
		const htmlContent = this.getHtmlContent("{}");
		//console.log("htmlContent=" + htmlContent);
		return htmlContent;
	}

	private _requestId = 1;
	private readonly _callbacks = new Map<number, (response: any) => void>();


	private getHtmlContent(json: string): string {
		return `<!DOCTYPE html>
		<html>
		<head>
			<meta charset="UTF-8"/>
			<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
			<meta name="description" content="LottiePlayer by 杜小菜，duqian2010@gmail.com">
			<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
			<title>LottiePlayer</title>
			<script src="https://cdnjs.cloudflare.com/ajax/libs/lottie-web/5.9.4/lottie.min.js"></script>
			<style>
			  body{
				 background-color:#3C3F41;
			  }
			  #app {
				width: 500px;
				height: 500px;
				margin:10px auto;
			  }
			  #lottieTitle {
				width: 300px;
				margin:60px auto;
				display:flex;
			  }
			  button {
				width:50px;height:30px;padding:5px 0px;margin-left:10px;text-align:center;
				border-radius: 4px;
				border: none;
				color:#000000;
			  }
			  .title{
				height: 30px;
				line-height: 30px;
				margin: 0;
				color:#66FFFFFF;
			  }
		
			</style>
		</head>
		<body>
		<div id="lottieTitle">
			<h4 class="title">LottiePlayer </h4>
			<button onclick="startAnimation()"> Play</button>
			<button onclick="pauseAnimation()"> Pause</button>
			<!-- <button onclick="stopAnimation()"> Clear</button> -->
		</div>
		<div id="app"></div>
		</body>
		<script>
			let animationData = ${json};
			let lastData = ${json};
			(function () {
				const vscode = acquireVsCodeApi();
				//Handle messages from the extension
				window.addEventListener('message', async e => {
					const {
						type,
						body,
					} = e.data;
					switch (type) {
						case 'init': {
							console.log("ini player");
							const originData = body.value.data+"";
							console.log("originData=" + originData);
							animationData = originData;
							//lastData = originData;
							initPlayer();
						}
					}
				});
				vscode.postMessage({
					type: 'ready'
				});
			}());
			
			var player;
			initPlayer();
			function initPlayer(){
			   if(player){
				console.log("retrun init play="+player);
				return;
			   }
			   player = lottie.loadAnimation({
				  container: document.querySelector("#app"),
				  //path: "https://assets10.lottiefiles.com/packages/lf20_l3qxn9jy.json",
				  animationData: animationData, // the animation data
				  loop: true,
				  autoplay: true,
				  renderer: "svg",
				});
			}
			//play
			function startAnimation(){
				console.log("start play="+player);
				animationData = lastData;
				initPlayer();
				player.play();
			}
		
			//pause
			function pauseAnimation(){
				initPlayer();
				player.pause();
			}
		
			//clear or stop
			function stopAnimation(){
				//player.stop();
				player.destroy();
				player = null;
			}
		
		</script>
		</html>
		`;
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