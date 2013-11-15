js.runScript("src/lab/js/library/WebServer.js");
js.runScript("src/lab/js/library/OS.js");

js.mount("mq","org.one.stone.soup.core.container.TransientMessageQueue");
mq.postMessage("mq started");

webApp = new WebApp("bootLoader",OS.getLocalAddress(),9000,"src/lab/resources/sds/boot-loader.html");

webApp.createUserService(
		"testService1",function() {
			var count = 1;
			this.getTheMagic = function() {
				count++;
				return "Magic1 321321"+count;
			}
		}
);

webApp.createGlobalService(
		"testService2",function() {
			var count = 1;
			this.getTheMagic = function() {
				count++;
				return "Magic1 321321"+count;
			}
		}
);

webApp.createGlobalService(
		"js",function() {
			this.run = function(script) {
				return eval(script);
			}
		}
);

webApp.createGlobalService(
		"mq",function() {
			this.sendMessage = function() {
				count++;
				return "Magic1 321321"+count;
			}
		}
);

webApp.start();

OS.openWebApp(webApp);