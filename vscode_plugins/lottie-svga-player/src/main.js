// Convert file to base64 string
export const fileToBase64 = (filename, filepath) => {
    return new Promise(resolve => {
        var file = new File([filename], filepath);
        var reader = new FileReader();
        // Read file content on file loaded event
        reader.onload = function (event) {
            const result = event.target.result;
            console.log("result=" + result);
            resolve(result);
        };

        // Convert data to base64 
        reader.readAsDataURL(file);
    });
};

export const stringToBase64 = (content) => {
    return window.btoa(content);
};

function contentToBase64(content) {
    return window.btoa(content);
}


async function getAsByteArray(file) {
    return new Uint8Array(await readFile(file));
}

function readFile(file) {
    return new Promise((resolve, reject) => {
        let reader = new FileReader();
        reader.addEventListener("loadend", e => resolve(e.target.result));
        reader.addEventListener("error", reject);
        reader.readAsArrayBuffer(file);
    });
}

exports.readFile = readFile;
//const byteFile = await getAsByteArray(file)

const toBase64 = file => new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
});
exports.toBase64 = toBase64;
/* async function Main() {
   const file = document.querySelector('#myfile').files[0];
   console.log(await toBase64(file));
}

Main(); */

exports.main = main;