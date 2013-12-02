OpenDevice = new function() {
	var controller = js.mountJar("controller","src/lab/js/library/open-device-core.jar,src/lab/js/library/rxtx-2.1.7.jar","org.one.stone.soup.open.device.controller.OpenDeviceController");
	
	this.getAvailableSerialPorts = function() {
		return controller.getAvailableSerialPorts();
	}
}