/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function CloseAndExit() {
    if (confirm("Close the program?")) {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open("GET", "close", false); // false for synchronous request
        xmlHttp.send(null);
        window.close('', '_parent', '');
    }
}
