OpenForum.loadScript("/library/giraffe/giraffe.js");
//OpenForum.loadScript("/apps/Draw/CompassFace.object.js");
//OpenForum.loadScript("/apps/Draw/Protractor.object.js");
var pen = null;
function initGiraffe() {	
	canvas = new Canvas("canvas");
  	Giraffe.Interactive.setInteractive(canvas);
  	Giraffe.setAnimated(canvas);
	canvas.startAnimation(15,100,true);
	
	createGrid();
//	createCompass();
//	createProtractor();
	
	pen = new Pen();
	data="pen.moveTo(200,200);";
}

function initDrive() {
	drawDrive = new Drive("drawDrive");
	drawDrive.setFileListListener( function(newFiles) {
		files = newFiles;
	});
}
OpenForum.loadScript("/library/Drive.js");

function run() {
	eval(data);
}

function clear() {
	canvas.graphicsObjects = [];
	newPen = new Pen();
	newPen.x = pen.x;
	newPen.y = pen.y;
}

function createGrid() {
	grid = new Composite(0,0);
	grid.visible = false;
	canvas.add(grid);
	for(var x=0;x<400;x+=10) {
		line = new Line(x,0.5,0.5,400).setColor("grey");
		grid.add(line);
	}
	for(var y=0;y<400;y+=10) {
		line = new Line(0.5,y,400,0.5).setColor("grey");
		grid.add(line);
	}
}

function createCompass() {
	compass = new CompassFace(200,200);
	compass.scaleX=0.5;
	compass.scaleY=0.5;
	compass.visible = false;
	canvas.add(compass);
}

function createProtractor() {
	protractor = new Protractor(200,200);
	protractor.scaleX=0.5;
	protractor.scaleY=0.5;
	protractor.visible = false;
	canvas.add(protractor);
}

function showProtractor() {
	protractor.visible = true;
}

function hideProtractor() {
	protractor.visible = false;
}

function showCompass() {
	compass.visible = true;
}

function hideCompass() {
	compass.visible = false;
}

function showGrid() {
	grid.visible=true;
}
function hideGrid() {
	grid.visible=false;
}

function Pen() {
	var x=200;
	var y=200;
	var direction=0;
	var penState="up";
	var color="cyan";
	var points=[];
	
	var pointer = new Polygon(x,y);
	pointer.addPoint(0,0);
	pointer.addPoint(-5,-5);
	pointer.addPoint(10,0);
	pointer.addPoint(-5,5);
	pointer.setColor(color);
	canvas.add(pointer);
	
	var updatePointer = function() {
		pointer.setRotation(Giraffe.DEG_TO_RAD*direction);

		if(penState=="down") {
			pointer.setFillColor("green");
		} else {
			pointer.setFillColor(null);
		}
		if(pointer.x!=x || pointer.y!=y) {
			if(penState=="down") {
				var line = new Line(pointer.x,pointer.y,x-pointer.x,y-pointer.y).setColor(color);
				canvas.add(line);
				
				points[points.length] = [x,y];
			}
		}
		
		pointer.x=x;
		pointer.y=y;
		pointer.setColor(color);
	}
	
	this.up = function() {
		penState="up";
		updatePointer();
	}
	this.down = function() {
		penState="down";
		updatePointer();
	}
	this.moveTo = function(x1,y1) {
		x=x1;
		y=y1;
		updatePointer();
	}
	this.moveBy = function(x1,y1) {
		x+=x1;
		y+=y1;
		updatePointer();
	}
	this.setColor = function(newColor) {
		color=newColor;
		updatePointer();
	}
	this.turnLeft = function(degrees) {
		direction-=degrees;
		direction = direction%360;
		updatePointer();
	}
	this.turnRight = function(degrees) {
		direction+=degrees;
		direction = direction%360;
		updatePointer();
	}
	this.setHeading = function(heading) {
		direction=heading;
		direction = direction%360;
		updatePointer();
	}
	this.moveForward = function(v) {
		var vx = Math.cos( direction*Giraffe.DEG_TO_RAD )*v;
		var vy = Math.sin( direction*Giraffe.DEG_TO_RAD )*v;
		x+=vx;
		y+=vy;
		updatePointer();
	}
	this.moveBack = function(v) {
		var vx = Math.cos( direction*Giraffe.DEG_TO_RAD )*v;
		var vy = Math.sin( direction*Giraffe.DEG_TO_RAD )*v;
		x-=vx;
		y-=vy;
		updatePointer();
	}
	this.close = function() {
		var poly = new Polygon();
		for(i in points) {
			point = points[i];
			poly.addPoint(point[0],point[1]);
		}
		point = points[0];
		poly.addPoint(point[0],point[1]);
		poly.setColor(color);
		poly.setFillColor(color);
		canvas.add(poly);
		
		points = [];
		updatePointer();
	}
}
/*
clear, print, load, save, 

showCompass, hideCompass, showProtractor, hideProtractor, showHelper
*/