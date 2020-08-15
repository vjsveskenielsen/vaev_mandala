class Corner {
  PVector pos;
  PVector head; //heading - the direction of the bushel
  float a;
  PImage b = bushels[3];
  float sc = 1.5;
  Corner(PVector _pos, PVector _head){
    pos = _pos;
    head = _head;
    println(a);
    int r = round(bushels.length-1);
    a = 0;
  }

  void display(){
    c.imageMode(CORNER);
    c.pushMatrix();
    // position bushel with small offset
    c.translate(pos.x+(head.x*-1*25*sc), pos.y+(head.y*-1*25*sc));
    c.rotate(a+wiggleFloat(.05, .001));
    c.scale(head.x, head.y);
    c.image(b, 0,0, b.width*sc, b.height*sc);
    c.popMatrix();
  }
}
