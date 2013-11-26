
var driveIndex = 0;
function Drive(newRoot) {
	var index = driveIndex;
	driveIndex++;
	var tfs = js.mount("drive"+index,"org.one.stone.soup.javascript.helper.TextDrive");
	tfs.setRoot(newRoot);
	var serviceName = null;
	var root = newRoot;
	
	this.createWebService = function(webApp,serviceName) {
		this.serviceName = serviceName;
		webApp.createGlobalService(
				serviceName, function() {
					this.load = function(fileName) {
						return tfs.load(decodeURIComponent(fileName));
					}
					this.save = function(data,fileName) {
						return tfs.save(decodeURIComponent(fileName),decodeURIComponent(data));
					}
				}	
			);
	}
	
	this.load = function(fileName) {
		return tfs.load(fileName);
	}
	
	this.save = function(data,fileName) {
		tfs.save(data,fileName);
	}
}

//test
testDrive = new Drive("src/lab/js/drive/Test","testDriveService");

testData = "test data "+new Date();
testDrive.save("test.txt",testData);
data = testDrive.load("test.txt");
if(data!=testData) {
	throw "error in root Drive test "+data+" != "+testData;
}