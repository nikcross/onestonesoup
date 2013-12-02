function Drive(driveName) {
	var self = this;
	var driveName = driveName;
	var files;
	var fileListListener = null;
	
	this.load = function(fileName) {
		return Ajax.doGet("service?action="+driveName+"Service.load&values="+fileName,true, new function() {
			self.loaded(fileName);
		});
	}
	
	this.save = function(data,fileName) {
		var params = new Post();
		params.addItem("data",data);
		params.addItem("fileName",fileName);
		params.addItem("values","$data,$fileName");
		Ajax.doPost("service?action="+driveName+"Service.save",params,true, new function() {
			self.saved(fileName);
		});
	}
	
	this.loaded = function(fileName) {
		//Popup.notify( "File "+fileName+" saved." );
		setStatus("Loaded "+fileName+" on "+new Date());
	}
	this.saved = function(fileName) {
		//Popup.notify( "File "+fileName+" saved." );
		setStatus("Saved "+fileName+" on "+new Date());
	}
	
	this.getFilesList = function() {
		JSON.get("service",driveName+"Service.listFiles","values=/").onSuccess(listFiles).go();
	}
	
	var listFiles = function(response) {
		files = response.value;
		if(fileListListener!=null) {
			fileListListener(files);
		}
	}
	
	this.setFileListListener = function( fn ){
		fileListListener = fn;
	}
	
	setInterval(
			function() {
				self.getFilesList();
			}
	,15000);
	this.getFilesList();
}

if(initDrive) {
	initDrive(); 
}