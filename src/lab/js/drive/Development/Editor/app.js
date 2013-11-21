function load() {
	data = Ajax.doGet("service?action=testDriveService.load&values="+fileName);
}

function save() {
	JSON.get("service","testDriveService.save","values="+data+","+fileName).go();
}