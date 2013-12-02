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
	
	this.setAuthentication = function(drive,file) {
		var users = eval(drive.load(file));
		var authenticatorFn = {
			usersDrive: drive,
			usersFile: file,
			users: users,
			canAccess: function(header,socket) {
				if(header.getChild("Authorization")==null) {
					return false;
				}
				var part = org.one.stone.soup.core.StringHelper.decodeBase64( header.getChild("Authorization").getValue().substring(7) ).split(":");
				var userName = part[0];
				var password = part[1];
				
				var user = users.accounts[userName];
				if( typeof(user)!="undefined" ) {
					if(user.password==password) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			},
			whoIs: function(header) {
				if(header.getChild("Authorization")==null) {
					return null;
				}
				var part = org.one.stone.soup.core.StringHelper.decodeBase64( header.getChild("Authorization").getValue() ).split(":");
				return part[0];
			}
		}
		
		var authenticator = new org.one.stone.soup.sds.SimpleDeviceServer.Authenticator(authenticatorFn);
		server.setAuthenticator(authenticator);
	}
}