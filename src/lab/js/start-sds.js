try{
out.println("Boot Loader Started");
out.println("Loading Objects");
js.runScript("src/lab/js/library/Drive.js");
js.runScript("src/lab/js/library/WebApp.js");

webApp = new WebApp("bootLoader","localhost",8888,"src/lab/js/sds/boot-loader.html");
adminDrive = new Drive("src/lab/js/sds/settings");
webApp.setAuthentication(adminDrive,"users.json");

testDrive = new Drive("src/lab/js/drive/Test");
drawDrive = new Drive("src/lab/js/drive/Draw/user");

testDrive.createWebService(webApp,"testDriveService");
drawDrive.createWebService(webApp,"drawDriveService");

webApp.start();

} catch(e) {
	out.println( e+" on line "+e.lineNumber );
	js.exit();
}