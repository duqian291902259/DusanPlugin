class EncodeDecode {

  static b64EncodeUnicode(str: any) {
    // first we use encodeURIComponent to get percent-encoded UTF-8,
    // then we convert the percent encodings into raw bytes which
    // can be fed into btoa.
    return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g,
      // function toSolidBytes(match, p1) {
      (match, p1) => {
        // console.debug('match: ' + match);
        return String.fromCharCode(("0x" + p1) as any);
      }));
  }

  static b64DecodeUnicode(str: string) {
    // Going backwards: from bytestream, to percent-encoding, to original string.
    return decodeURIComponent(atob(str).split('').map(function (c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
  }
}


export function getNonce() {
  let text = '';
  const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  for (let i = 0; i < 32; i++) {
    text += possible.charAt(Math.floor(Math.random() * possible.length));
  }
  return text;
}


export function isJsonString(str: string) {
  try {
    let lottieObject = JSON.parse(str);
    if (typeof lottieObject === "object") {
      return lottieObject.hasOwnProperty("v") && lottieObject.hasOwnProperty("w") && lottieObject.hasOwnProperty("h");
    }
  } catch (e) {
  }
  return false;
}


export function getErrorPage(originData:string){
		return `<!DOCTYPE html>
				<html lang="en">
				<head>
					<meta charset="UTF-8">
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<title>Lottie-Player</title>
				</head>
				<style>
					body,html {
						width:100%;
						height: 100%;
						display: block;
						justify-content: center;
						align-items: center;
						overflow: hidden;
						color:#fff;
					}
				</style>	
				<body>
					<h1>Sorry,it's not lottie json:\n \n</h1>
          <h1></h1>
          </br>
        
          <h4 style="display:block;">${originData}</h4>
				</body>
				</html>`;
}