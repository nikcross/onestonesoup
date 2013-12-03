
var driveIndex = 0;
function Drive(newRoot) {
	var index = driveIndex;
	driveIndex++;
	var tfs = js.mount("drive"+index,"org.one.stone.soup.javascript.helper.TextDrive");
	tfs.setRoot(newRoot);
	var serviceName = null;
	var root = newRoot;
	var self = this;
	
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
	
	this.listDirectories = function(directory) {
		return tfs.listDirectories(directory);
	}
	
	this.listFiles = function(directory) {
		return tfs.listFiles(directory);
	}
	
	this.getJSONTree = function(directory) {
		data="";
		out.println("Directory:"+directory+" Data:" +data);
		data = addDirectoryTreeNode(data,directory);
		return data;
	}
	
	var addDirectoryTreeNode = function(data,directory) {
		data+="{";
		data+="name: \""+directory+"\",";
		
		data+="leaves: ["
		files = tfs.listFiles(directory);
		for(var i in files) {
			addFileTreeNode(data,files[i]);
			out.println("Data:" +data);
		}
		
		directories = tfs.listDirectories(directory);
		for(var i in directories) {
			addDirectoryTreeNode(data,directories[i]);
			out.println("Data:" +data);
		}
		data+="]},";
		
		return data;
	}
	
	var addFileTreeNode = function(data,file) {
		data+="{";
		data+="name: \""+file+"\"";
		data+="},";
		return data;
	}
	
	this.load = function(fileName) {
		return ""+tfs.load(fileName);
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
