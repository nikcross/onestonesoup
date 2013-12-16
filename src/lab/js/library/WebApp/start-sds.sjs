try{
out.println("Boot Loader Started");
out.println("Loading Objects");
js.runScript("library/OperatingSystem/OS.sjs");
js.runScript("library/Drive/Drive.sjs");
js.runScript("library/WebApp/WebApp.sjs");

webApp = new WebApp("bootLoader",OS.getLocalAddress(),80,"library/WebApp/boot-loader.html");
adminDrive = new Drive("user/admin");
webApp.setAuthentication(adminDrive,"users.json");

testDrive = new Drive("user/guest/Test");
drawDrive = new Drive("user/guest/Draw");

testDrive.createWebService(webApp,"testDriveService");
drawDrive.createWebService(webApp,"drawDriveService");

webApp.start();

} catch(e) {
	out.println( e+" on line "+e.lineNumber );
	js.exit();
}