OpenDevice = new function() {
	var controller = js.mountJar("controller","src/lab/js/library/OpenDevice/open-device-core.jar,src/lab/js/library/OpenDevice/rxtx.jar","org.one.stone.soup.open.device.controller.OpenDeviceController");
	
	this.getAvailableSerialPorts = function() {
		return controller.getAvailableSerialPorts();
	}
	
	this.getUsedSerialPorts = function() {
		return controller.getUsedPorts();
	}
	
	this.connectToNewDevice = function(deviceClass,deviceAlias,portName){
		return controller.connectNewDevice(deviceClass,deviceAlias,portName);
	}
	
	this.getDevice = function(deviceAlias) {
		return controller.getDevice(deviceAlias);
	}
	
	this.getDevices = function() {
		return controller.getDevices();
	}
	
	this.connectToCurrentCost = function(portName) {
		return controller.connectNewDevice("org.one.stone.soup.open.device.smart.meter.current.cost.CurrentCostPowerMonitor","current cost",portName);
	}
	
	this.connectToWeatherStation = function(portName) {
		return controller.connectNewDevice("org.one.stone.soup.open.device.weather.ws2350.WS2350","weather station",portName);
	}
}

function CurrentCost(device) {
	var device = device;
	
	this.getTemperature = function() {
		device.getDataLog();
		return device.getTemperature();
	}
	
	this.getPower = function(sensor) {
		var sensor = device.getSensors().get(sensor);
		sensor.getDataLog();
		return sensor.getPower();
	}
	
	this.getUnits = function(sensor) {
		var sensor = device.getSensors().get(sensor);
		sensor.getDataLog();
		return sensor.getUnits();
	}
	
	this.createWebService = function(webApp,serviceName) {
		this.serviceName = serviceName;
		webApp.createGlobalService(
				serviceName, function() {
					self = this;
					this.getTemperature = function() {
						return this.getTemperature();
					}
					this.getPower = function(sensor) {
						return this.getPower(sensor);
					}
					this.getUnits = function(sensor) {
						return this.getUnits(sensor);
					}
				
					this.load = function(fileName) {
						return tfs.load(decodeURIComponent(fileName));
					}
					this.save = function(data,fileName) {
						return tfs.save(decodeURIComponent(fileName),decodeURIComponent(data));
					}
					this.listDirectories = function(directoryName) {
						return tfs.listDirectories(decodeURIComponent(directoryName));
					}
					this.listFiles = function(directoryName) {
						return tfs.listFiles(decodeURIComponent(directoryName));
					}
					this.getTree = function(directoryName) {
						return self.getJSONTree(decodeURIComponent(directoryName));
					}
				}	
			);
	}
}