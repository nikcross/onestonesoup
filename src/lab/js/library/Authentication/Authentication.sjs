Authentication = new function() {
		var users = eval(""+FileHelper.loadFileAsString("user/admin/users.json"));
		var currentLocalUser = null;
		
		var authenticatorFn = {
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
		
		this.login = function(userName,password) {
			var user = users.accounts[userName];
			if(user.password==password) {
				var drive = org.one.stone.soup.javascript.helper.Drive("library","user/"+userName);
				js.mountObject("Drive",drive);
				
				currentLocalUser = user;
				return true;
			} else {
				return false;
			}
		}
		
		this.logout = function() {
			currentLocalUser = null;
		}
		
		this.getLocalUser = function() {
			return currentLocalUser;
		}
}

js.mountObject("Authentication",Authentication);