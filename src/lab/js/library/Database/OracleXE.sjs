function OracleDB() {
	var odb = js.mount("odb","org.one.stone.soup.sds.service.Database");
	odb.registerDriver("oracle.jdbc.driver.OracleDriver","","C:/oraclexe/app/oracle/product/11.2.0/server/jdbc/lib/ojdbc5.jar");
	
	this.registerSchema = function(schema,user,password) {
		odb.createConnection(schema,"jdbc:oracle:thin:@//localhost:1521/XE",user,password);
	}

	this.query = function(schema,query) {
		return odb.query(schema,query);
	}
}