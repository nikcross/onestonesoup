function load() {
	data = Ajax.doGet("service?action=testDriveService.load&values="+fileName);
}

function save() {
	params = new Post();
	params.addItem("data",data);
	params.addItem("fileName",fileName);
	params.addItem("values","$data,$fileName");
	Ajax.doPost("service?action=testDriveService.save",params);
	//JSON.post("service","testDriveService.save","values="+data+","+fileName).go();
}
