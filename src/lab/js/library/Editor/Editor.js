OpenForum.getObject("dataNew").addListener(
		function(obj) {
			if(jsEditor.codeMirror.getValue()!=obj.getValue()) {
				jsEditor.codeMirror.setValue(obj.getValue());
			}
		}
	);
OpenForum.getObject("jsEditor.codeMirror.getValue()").addListener(
		function(obj) {
			if(dataCurrent!=obj.getValue()) {
				dataCurrent=obj.getValue();
			}
		}
	);

OpenForum.loadScript("/Drive/Drive.js");
function initDrive() {
	drawDrive = new Drive("testDrive");
	drawDrive.setFileListListener( function(newFiles) {
		files = newFiles;
	});
}

var jsEditor = null;
var dataCurrent = "";
var dataNew = "";
function initJSEditor() {
	jsEditor = new JSEditor("code");
}
OpenForum.loadScript("/Editor/JSEditor.js");

function showDrive() {
	document.getElementById("drive").style.display="block";
}
function hideDrive() {
	document.getElementById("drive").style.display="none";
}
