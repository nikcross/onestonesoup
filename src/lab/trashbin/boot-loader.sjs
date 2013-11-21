server.setPageFile("src/lab/resources/sds/boot-loader.html");

server.print( "Server starting on "+server.getAddress()+":"+server.getPort() );
server.print( "Application page file:"+server.getPageFile() );

server.registerService( "file","org.one.stone.soup.sds.service.TextFileService" );
sjs.mount( "file",server.getService("file") );
file.setRoot("src/lab/resources/sds");