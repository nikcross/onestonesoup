function WebApp(alias,address,port,page) {
	js.mount(alias+"Server","org.one.stone.soup.sds.SimpleDeviceServer");
	var server = eval(alias+"Server");
	server.setPort(port);
	server.setAddress(address);
	server.setPageFile(page);
	
	this.start = function() {
		server.start();
	}
	this.stop = function() {
		server.stop();
	}
	this.getAddress = function() {
		return server.getAddress();
	}
	this.getPort = function() {
		return server.getPort();
	}
	this.registerService = function(alias,thing) {
		server.registerService(alias,thing);
	}
	this.createGlobalService = function(alias,newService) {
		var service = eval( alias+"= new newService();" );
		
		var factoryFn = {
			newInstance: function() {
				return service;
			}
		}
		
		var serviceFactory = new org.one.stone.soup.sds.SimpleDeviceServer.Factory(factoryFn);
		this.registerService(alias,serviceFactory);
		
		return serviceFactory;
	}
	this.createUserService = function(alias,newService) {
		var service = newService;
		var factoryFn = {
			newInstance: function() {
				return new service();
			}
		}
		
		var serviceFactory = new org.one.stone.soup.sds.SimpleDeviceServer.Factory(factoryFn);
		this.registerService(alias,serviceFactory);
		
		return serviceFactory;
	}
}