// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';
import { Base64 } from 'js-base64';
import { PawDrawEditorProvider } from './pawDrawEditor';
import { getNonce } from './utils';

const myProvider = class implements vscode.TextDocumentContentProvider {
	provideTextDocumentContent(uri: vscode.Uri): string {
		//文本内容
		return uri.path;
	}
};

// 追踪当前webview面板
let currentPanel: vscode.WebviewPanel | undefined = undefined;
// This method is called when your extension is activated
// Your extension is activated the very first time the command is executed
export function activate(context: vscode.ExtensionContext) {


	// This line of code will only be executed once when your extension is activated
	console.log('Congratulations, your extension "duqian" is now active!');

	// The command has been defined in the package.json file
	// Now provide the implementation of the command with registerCommand

	let disposable = vscode.commands.registerCommand('duqian.helloflat', () => {

		vscode.window.showInformationMessage('Hello Flat, from DQVSCodePlugin!' + context.extensionUri);

		//vscode.workspace.registerTextDocumentContentProvider("flat", myProvider);

		currentPanel = undefined;
		currentPanel = initWebView(context, "FlatWeb");
		// 获取磁盘上的资源路径
		const onDiskPath = vscode.Uri.file(
			path.join(context.extensionPath, 'media', 'ic_launcher_round.png')
		);

		// 获取在webview中使用的特殊URI
		const vsSrc = onDiskPath.with({ scheme: 'vscode-resource' });

		const stylesResetUri = currentPanel.webview.asWebviewUri(onDiskPath);
		// 设置HTML内容
		currentPanel.webview.html = getWebviewContent1(stylesResetUri);
		console.log(context.extensionUri);

		//stylesResetUri=https://file%2B.vscode-resource.vscode-cdn.net/Users/duqian/Documents/DuQian/xxx.png
		console.log("stylesResetUri=" + stylesResetUri);
		console.log(vsSrc);

		//open file
		//let uri = vscode.Uri.file("/Users/duqian/Documents/DuQian/MyGithub/MyPluginLib");
		//let success = vscode.commands.executeCommand('vscode.openFolder', uri);
	});

	context.subscriptions.push(disposable);

	//lottie-player
	context.subscriptions.push(vscode.commands.registerCommand('duqian.lottie', async (fileUri) => {
		handleSelectedFile(context, fileUri);
	}));

	//svga-player
	context.subscriptions.push(vscode.commands.registerCommand('duqian.svga', async (fileUri) => {
		//handleSelectedFile(context, fileUri);
		startPlaySvga(context, fileUri);
	}));

	//svga-preivew
	context.subscriptions.push(vscode.commands.registerCommand('svga.preview', async (fileUri) => {
		handleSelectedFile(context, fileUri);
		console.log("svga.preview");
	}));

	//svga-editor
	context.subscriptions.push(PawDrawEditorProvider.register(context));
}

//  "when": "explorerResourceIsFolder","when": "resourceLangId == json"
function handleSelectedFile(context: vscode.ExtensionContext, fileUri: any) {
	console.log("duqian.fileUri=" + fileUri);
	const curFilePath = fileUri.path;
	currentPanel = undefined; //重新创建
	let name = curFilePath.substring(curFilePath.lastIndexOf("/") + 1, curFilePath.length);
	console.log("duqian.name =" + name);

	currentPanel = initWebView(context, name);

	var index = curFilePath.lastIndexOf(".svga");
	console.log("duqian.index=" + index);
	const htmlName = index > 0 ? "svga-kiss.html" : "lottie.html";
	const htmlPath = vscode.Uri.file(
		path.join(context.extensionPath, 'media', htmlName)
	);

	let animContent = fs.readFileSync(curFilePath, 'utf8');
	let htmlContent = fs.readFileSync(htmlPath.path, 'utf8');
	console.log("animContent=" + animContent);
	let htmlContent2 = htmlContent.replace("{animationData}", animContent);
	currentPanel.webview.html = htmlContent2;
}

function initWebView(context: vscode.ExtensionContext, name: string) {
	if (currentPanel) {
		const columnToShowIn = vscode.window.activeTextEditor
			? vscode.window.activeTextEditor.viewColumn
			: undefined;

		// 如果我们已经有了一个面板，那就把它显示到目标列布局中
		currentPanel.reveal(columnToShowIn);
	} else {
		//webview
		currentPanel = vscode.window.createWebviewPanel(
			'FlatWeb', // webview id
			name, // title
			vscode.ViewColumn.One, // 给新的webview面板一个编辑器视图
			{
				// 只允许webview加载我们插件的`media`目录下的资源
				localResourceRoots: [vscode.Uri.file(path.join(context.extensionPath, 'media'))],
				// 在webview中启用脚本
				enableScripts: true
			}
		);
		console.log('init webview!');
	}

	// 当前面板被关闭后重置
	currentPanel.onDidDispose(
		() => {
			currentPanel = undefined;
		},
		null,
		context.subscriptions
	);
	return currentPanel;
}

function getWebviewContent1(srcPath: vscode.Uri) {
	console.log("srcPath:" + srcPath);
	return `
		  <!DOCTYPE html>
		  <html lang="en">
		  <head>
			  <meta charset="UTF-8">
			  <meta name="viewport" content="width=device-width, initial-scale=1.0">
			  <title>Dusan Coding</title>
		  </head>
		  <body>
			  <!--<img src="https://media.giphy.com/media/JIX9t2j0ZTN9S/giphy.gif" width="300" />-->
			  <img src="https://img1.kchuhai.com/ueditor/image/20210810/6376419298690944692274851.png" width="500"/>
			  <br/>
			  <img src="${srcPath}" width="200" />
		  </body>
		  </html>
	  `;
}
// This method is called when your extension is deactivated
export function deactivate() { }

function postMessage(panel: vscode.WebviewPanel, type: string, body: any): void {
	panel.webview.postMessage({ type, body });
}

function startPlaySvga(context: vscode.ExtensionContext, fileUri: any) {
	console.log("duqian.svga fileUri=" + fileUri);
	const curFilePath = fileUri.path;
	let name = curFilePath.substring(curFilePath.lastIndexOf("/") + 1, curFilePath.length);
	currentPanel = initWebView(context, name);

	var index = curFilePath.lastIndexOf(".svga");
	console.log("duqian.index=" + index);
	const htmlName = index > 0 ? "svga2.html" : "lottie.html";
	const htmlPath = vscode.Uri.file(
		path.join(context.extensionPath, 'media', htmlName)
	);

	let animContent = fs.readFileSync(curFilePath, 'utf8');
	console.log("animContent=" + animContent);

	//animContent = Buffer.from(animContent, 'utf-8').toString('base64');

	//animContent = EncodeDecode.b64EncodeUnicode(animContent); 
	//animContent =  Base64.encode(animContent); 
	//console.log("animContent1=" + animContent);

	let htmlContent = fs.readFileSync(htmlPath.path, 'utf8');
	//let htmlContent2 = htmlContent.replace("{animationData}", animContent);

	const svgaFilePath = vscode.Uri.file(
		path.join(context.extensionPath, 'media', 'svga.lite.min.js')
	);
	const svgaFile = currentPanel.webview.asWebviewUri(svgaFilePath);

	const scriptPath = vscode.Uri.file(
		path.join(context.extensionPath, 'media', 'svgaPerview.js')
	);
	const scriptUri = currentPanel.webview.asWebviewUri(scriptPath);

	const nonce = getNonce();

	htmlContent = htmlContent.replace("${nonce}", nonce)
		.replace("${nonce}", nonce)
		.replace("${svgaFile}", svgaFile + "")
		.replace("{scriptUri}", scriptUri + "")
		.replace("$lastDataStub", animContent);
	console.log("htmlContent2=" + htmlContent);
	currentPanel.webview.html = htmlContent;

	postMessage(currentPanel, "init", animContent);
	setTimeout(function () {
		console.log("init player delay=");
		//postMessage(currentPanel, "init", animContent);
	}, 2000);

}

