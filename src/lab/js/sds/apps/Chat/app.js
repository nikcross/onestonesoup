
if( typeof(chatApp)=="undefined") {
	chatApp = "Loaded";
	function print(message) {
		JSON.get("service","mqS.sendMessage","values="+message).go();
	}
	
	setInterval("checkForMessages()",1000,1000);
	var lastCheck = 0;
	
	function checkForMessages() {
		JSON.get("service","mqS.getMessages","values="+lastCheck).onSuccess(processMessages).go();
		lastCheck = new Date().getTime();
	}
	
	function processMessages(response) {
		for(i in response.value) {
			append("console",decodeURIComponent(response.value[i])+"<br/>\n");
		}
	}
	
	function sendMessage() {
		print(message);
		message="";
	}
	function get(id) {
		return document.getElementById(id);
	}
	
	function set(id,html) {
		document.getElementById(id).innerHTML = html;
	}
	
	function append(id,html) {
		document.getElementById(id).innerHTML += html;
	}
	
	print("Chat started");
}