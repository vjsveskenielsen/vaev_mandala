float anim1;
Mandala m;
Baand b;

PImage[] graphics = new PImage[2];
PImage baand;
void settings() {
  size(600, 600, P3D);
}

void setup() {
  rectMode(CENTER);
  imageMode(CENTER);
  graphics[0] = loadImage("gulerod1.png");
  graphics[1] = loadImage("gulerod2.png");
  baand = loadImage("baand.png");
  m = new Mandala(16, .1*PI, .0003, 0);
  b = new Baand(HALF_PI, 2.);
}

float wiggleFloat(float amount, float speed, float offset) {
  return offset + sin(millis()*speed)*amount;
}

int wiggleInt(float amount, float speed, float offset) {
  return round(offset + sin(millis()*speed)*amount);
}
void draw() {
  background(255, 224, 74);
  m.display();
  String txt_fps = String.format(getClass().getName()+ "   [size %d/%d]   [fps %6.2f]", width, height, frameRate);
  surface.setTitle(txt_fps);
  b.update();
  b.display();
}

class Baand {
  float p; //progress along x axis
  float a; //angle
  float s; // speed
  Baand(float _a, float _s) {
    s = _s;
    a = _a;
  }
  void update() {
    if (p >= baand.width) p = 0;
    else p+=s;
  }

  void display() {
    imageMode(CORNER);
    translate(baand.height, 0);
    rotate(a);
    image(baand, p, 0);
    image(baand, p-baand.width, 0);

  }
}

class Mandala {
  float w_s; //wigglespeed
  float w_a; //wiggle amount
  float s; //scale
  float wiggle;
  int n; // iterations
  int g; // what graphic to use from graphics[]
  float a;

  Mandala(int _n, float _w_a, float _w_s, int _g) {
    n = _n;
    w_s = _w_s;
    w_a = _w_a;
    g = _g;
    a = TWO_PI/n; // set angle offset
  }

  void update() {
  }

  void display() {
    pushMatrix();
    translate(width*.5, height*.5);
    int offset = 120;
    rotate(millis()*.0001);
    for (int i = 0; i<n; i++) {
      pushMatrix();
      translate(cos(i*a)*offset, sin(i*a)*offset);
      rotate(a*i); //rotate around center
      rotate(wiggleFloat(w_a, w_s, w_a/n*i)); //wiggle rotation
      rotate(0.25*PI); //nudge
      //fill(255);
      //rect(0, 0, 40, 40);
      if (i%2 == 0) image(graphics[0], 0, 0, 100, 100);
      else image(graphics[1], 0, 0, 100, 100);
      popMatrix();
    }
    popMatrix();
  }
}

void keyPressed() {
  if (keyCode == '0') m.g = 0;
  if (keyCode == '1') m.g = 1;
}
