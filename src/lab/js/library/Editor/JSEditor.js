OpenForum.loadCSS("/Editor/code-mirror-2.3/codemirror.css");
OpenForum.loadCSS("/Editor/code-mirror-2.3/theme/rubyblue.css");
OpenForum.loadCSS("/Editor/code-mirror-2.3/util/dialog.css");


OpenForum.loadScript("/Editor/code-mirror-2.3/codemirror.js",function() { loaded(); });

var count = 0;
function loaded() {
	count++;
	if(count==1) {
		OpenForum.loadScript("/Editor/code-mirror-2.3/mode/javascript/javascript.js",function() { loaded(); });
		OpenForum.loadScript("/Editor/code-mirror-2.3/util/search.js",function() { loaded(); });
		OpenForum.loadScript("/Editor/code-mirror-2.3/util/searchcursor.js",function() { loaded(); });
		OpenForum.loadScript("/Editor/code-mirror-2.3/util/foldcode.js",function() { loaded(); });
		OpenForum.loadScript("/Editor/code-mirror-2.3/util/dialog.js",function() { loaded(); });
	} else if(count==6) {
		if(initJSEditor) {
			initJSEditor(); 
		}		
	}
}

function JSEditor(id) {
	this.foldFunc = CodeMirror.newFoldFunction(CodeMirror.braceRangeFinder);
	this.codeMirror = CodeMirror.fromTextArea(
		document.getElementById(id),
		{ 
			theme: 'rubyblue',
			lineNumbers: true,
			matchBrackets: true,
			onGutterClick: this.foldFunc
		}
	);
	CodeMirror.commands.autocomplete = function (cm) {
	     CodeMirror.showHint(cm, CodeMirror.hint.javascript);
	 };
}