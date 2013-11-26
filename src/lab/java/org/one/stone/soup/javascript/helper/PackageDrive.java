package org.one.stone.soup.javascript.helper;

public class PackageDrive {

	public class PackageDetails {
		public String getName() {
			return name;
		}
		public String getVersion() {
			return version;
		}
		public String getHomeURL() {
			return homeURL;
		}
		public String[] getPackageDependencies() {
			return packageDependencies;
		}
		private String name;
		private String version;
		private String homeURL;
		private String[] packageDependencies;
	}
	
	//name.version	
	public String[] findPackages(String matcher) {
		//TODO
		return new String[]{};
	}
	
	public PackageDetails getPackage(String name) {
		//TODO
		return null;
	}
	
	public void installPackage(PackageDetails packageDetails) {
		//TODO
	}
	
	public PackageDetails createPackage(PackageDetails oldPackageDetails,String name,String version,String homeURL) {
		//TODO
		return null;
	}
}
