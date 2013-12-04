out.println("SJS Version 0.0.1 alpha");

function help(obj) {
	if(typeof(obj)=="undefined") {
		js.help();
		out.println("  Math");
		out.println("  String");
	} else {
		if(obj==Math) {
			out.println("Math!");
		} else if(obj==String) {
			out.println("String!");
		} else if(obj==Number) {
			out.println("Number!");
		} else if( typeof(obj)=='object' ) {
			if(obj.getClass) {
				js.help(obj);
			} else {
				for(var i in obj) { println("  "+obj[i]); };
			}
		} else if( typeof(obj)=='function' ) {
			out.println(""+obj);
		}
	}
}

NV = new function() {
	this.Math = Math;
	this.String = String;
	this.Number = Number;
	this.eval = eval;
}

function Test() {
	this.fn = function() {
		return new Date();
	}
}