(function () {
	const {
		Downloader,
		Parser,
		Player
	} = SVGA;
	const vscode = acquireVsCodeApi();
	const svgaFn = (data) => {
		const downloader = new Downloader();
		// 默认调用 WebWorker 线程解析
		// 可配置 new Parser({ disableWorker: true }) 禁止
		const parser = new Parser();
		// #canvas 是 HTMLCanvasElement
		const player = new Player('#playerCanvas')

			;
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
				svgaFn(data);
			}
		}
	});
	vscode.postMessage({
		type: 'ready'
	});
}());