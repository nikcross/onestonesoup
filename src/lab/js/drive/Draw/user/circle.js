pen.moveTo(200,200);

pen.down();
for(i=0;i<360;i+=10) {
  pen.moveForward(5);
  pen.turnRight(10);
}