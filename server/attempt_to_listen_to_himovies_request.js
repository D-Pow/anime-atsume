let OrigXHR = XMLHttpRequest;
XMLHttpRequest = function() {
    let xhr = new XMLHttpRequest.OrigXHR();
    xhr.origonreadystatechange = xhr.onreadystatechange || (() => {});
    xhr.onreadystatechange = function(...args) {
        console.log(xhr.response);
        xhr.origonreadystatechange(...args);
    }
    Object.defineProperty(xhr, 'onreadystatechange', {
        get: () => xhr.origonreadystatechange,
        set: func => xhr.orignonreadystatechange = func
    });
    return xhr;
}
XMLHttpRequest.OrigXHR = OrigXHR;

let x = new XMLHttpRequest(); x.open('GET', window.location.href); x.send()