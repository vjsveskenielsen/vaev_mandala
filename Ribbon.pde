class Ribbon {
  float p; //progress along x axis
  float a; //angle
  float s; // speed
  PVector pos; //position
  PVector offset; //ribbon offset from corner
  float sc = .4; //scaling
  int max; //the side of the canvas that ribbon scrolls across
  int max_n; //maximum number of ribbons needed to cover screen

  //angle,
  Ribbon(float _a, float _s, PVector _offset) {
    s = _s;
    a = _a;
    offset = _offset;
    //set pos according to orientation
    //
    if (a>0 && a<=HALF_PI){ pos = new PVector(c.width, 0); max = c.height; }
    else if (a>HALF_PI && a<=PI){ pos = new PVector(c.width, c.height);max = c.width;}
    else if (a>PI && a<=3*HALF_PI){ pos = new PVector(0, c.height); max = c.height;}
    else {pos = new PVector(0, 0);max = c.width;}
    //println(a);
    max_n = ceil(max/(ribbon.width*sc));
  }
  void update() {
     p = rollOver(p+s, 0, ribbon.width*sc*max_n);
  }

  void setSpeed(float input) {s = input;}

  void overlap() {
    c.pushMatrix();
    c.translate(pos.x, pos.y);
    c.translate(offset.x, offset.y);
    int i = -max_n/2;
    c.image(ribbon, p+(i*ribbon.width*sc), 0, ribbon.width*sc, ribbon.height*sc);
    c.popMatrix();
  }

  void display() {
    c.imageMode(CORNER);
    c.pushMatrix();
    c.translate(pos.x, pos.y);
    c.rotate(a);
    c.translate(offset.x, offset.y);

    for (int i = -max_n; i<max_n; i++) {
      c.image(ribbon, p+(i*ribbon.width*sc), 0, ribbon.width*sc, ribbon.height*sc);
    }
    c.popMatrix();
  }
}
