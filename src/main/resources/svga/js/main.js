window.addEventListener('mousewheel', function (event) {
    if (event.ctrlKey === true || event.metaKey) {
        event.preventDefault();
    }
}, {passive: false});

window.addEventListener('DOMMouseScroll', function (event) {
    if (event.ctrlKey === true || event.metaKey) {
        event.preventDefault();
    }
}, {passive: false});

function onPageLoaded() {
    let player = new SVGA.Player('#playerCanvas');
    let parser = new SVGA.Parser('#playerCanvas');
    parser.load('{SVGA_DATA_STUFF}', function (videoItem) {
            document.getElementById('playerCanvas').style.width = ''.concat(videoItem.videoSize.width, 'px');
            document.getElementById('playerCanvas').style.height = ''.concat(videoItem.videoSize.height, 'px');
            player.setVideoItem(videoItem);
            player.startAnimation();
            processSvgaInfo(videoItem);

            onSwitchBackground(document.getElementById('switch-bg-blue'))
        }
    );
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

function processSvgaInfo(videoItem) {
    let bc = 0;
    for (let key in videoItem.images) {
        if (videoItem.images.hasOwnProperty(key)) {
            let n = getImageSizeFromBase64Data(videoItem.images[key]);
            bc += n.width * n.height * 4;
        }
    }
    document.getElementById('infoDiv').innerHTML = videoItem.videoSize.width + '×' +
        videoItem.videoSize.height + '\xa0\xa0SVGA/' + videoItem.version + '\xa0\xa0FPS:\xa0' +
        videoItem.FPS + '\xa0\xa0Frames:\xa0' + videoItem.frames + '\xa0\xa0Memory: ' +
        processFileSizeText(bc) + '\xa0\xa0File: {FILE_SIZE_STUFF}';
}

function getImageSizeFromBase64Data(base64) {
    let dec = window.atob(base64),
        length = dec.length,
        array = new Uint8Array(new ArrayBuffer(length)), i;
    for (i = 0; i < length; i++) array[i] = dec.charCodeAt(i);
    return {width: 256 * array[18] + array[19], height: 256 * array[22] + array[23]}
}

function processFileSizeText(bc) {
    if (bc < 1024) {
        return bc + 'B';
    } else if (bc < 1048576) {
        return (Math.round(bc * 1.0 / 1024 * 10) / 10.0) + 'K';
    } else {
        return (Math.round(bc * 1.0 / 1048576 * 100) / 100.0) + 'M';
    }
}
