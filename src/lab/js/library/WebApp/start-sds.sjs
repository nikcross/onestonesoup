try{
out.println("Boot Loader Started");
out.println("Loading Objects");
js.runScript("src/lab/js/library/Drive/Drive.sjs");
js.runScript("src/lab/js/library/WebApp/WebApp.sjs");

webApp = new WebApp("bootLoader","localhost",8888,"src/lab/js/library/WebServer/boot-loader.html");
adminDrive = new Drive("src/lab/js/user/admin");
webApp.setAuthentication(adminDrive,"users.json");

testDrive = new Drive("src/lab/js/user/guest/Test");
drawDrive = new Drive("src/lab/js/user/guest/user");

testDrive.createWebService(webApp,"testDriveService");
drawDrive.createWebService(webApp,"drawDriveService");

webApp.start();

} catch(e) {
	out.println( e+" on line "+e.lineNumber );
	js.exit();
}