pen.moveTo(200,200);
pen.down();
pen.setColor("yellow");
for(i=0;i<360;i+=10) {
  pen.moveForward(150);
  pen.turnRight(190);
}