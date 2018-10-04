import codeanticode.syphon.*;
import controlP5.*;

ControlP5 cp5;

Slider slider1, slider2, slider3, slider4;

float anim1;
ArrayList<Mandala> mandalas = new ArrayList();
Ribbon[] ribbons = new Ribbon[4];
Ribbon stripes;
Bushel b1;
PImage[] carrots = new PImage[2];
PImage[] leaves = new PImage[2];
PImage[] bushels = new PImage[2];
PImage[] flowers = new PImage[4];
PImage ribbon, logo, bushel;

PGraphics c;
PVector cc; //canvas center

SyphonServer server;

void settings() {
  size(960, 540, P3D);
}

void setup() {
  //create syphon output
  c = createGraphics(1920, 1080, P3D);
  cc = new PVector(c.width/2, c.height/2);
  server = new SyphonServer(this, "Vaev Mandala");

  controlSetup();

  carrots[0] = loadImage("carrot01.png");
  carrots[1] = loadImage("carrot02.png");
  leaves[0] = loadImage("leaf01.png");
  leaves[1] = loadImage("leaf02.png");
  bushels[0] = loadImage("bushel01.png");
  bushels[1] = loadImage("bushel02.png");
  flowers[0] = loadImage("flower01.png");
  flowers[1] = loadImage("flower02.png");
  flowers[2] = loadImage("flower03.png");
  flowers[3] = loadImage("flower04.png");

  b1 = new Bushel(new PVector(0,0));

  ribbon = loadImage("ribbon.png");
  logo = loadImage("vaevlogo.png");
  mandalas.add(new Mandala(carrots, 28, -.2, TWO_PI, .3*PI, .0003, 350, .8));
  mandalas.add(new Mandala(leaves, 40, .3, .0, .3*PI, .0003, 200, 1.));
  mandalas.add(new Mandala(leaves, 80, .3, .0, .3*PI, .0003, 500, 1.));
  mandalas.add(new Mandala(flowers, 80, .3, .0, .3*PI, .0003, 600, 1.));
  int c = 0;
  for (int i = 0; i<ribbons.length; i++) {
    Ribbon r = new Ribbon(HALF_PI*i, 1., new PVector(10, 10));
    ribbons[i] = r;
  }
}

float wiggleFloat(float amount, float speed) {
  return sin(millis()*speed)*amount;
}

int wiggleInt(float amount, float speed) {
  return round(sin(millis()*speed)*amount);
}
void draw() {
  c.beginDraw();
  c.background(255, 224, 74);
  c.imageMode(CENTER);
  c.image(logo, c.width/2, c.height/2);

  for (Mandala m : mandalas){
    m.display();
  }
  for (int i = 0; i<ribbons.length; i++) {
    ribbons[i].update();
    ribbons[i].display();
  }

  b1.display();
  c.endDraw();

  image(c, 0,0, width, height);
  fill(80, 200);
  noStroke();
  rect(width-180, 0, 180, height);
  server.sendImage(c);
  displayFrameRate();
}

void displayFrameRate(){
  String txt_fps = String.format(getClass().getName()+ "   [size %d/%d]   [fps %6.2f]", width, height, frameRate);
  surface.setTitle(txt_fps);
}
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
     if (p > ribbon.width*sc*max_n) p = 0;
     else if (p < 0) p = ribbon.width*sc*max_n;
     else p+=s;
  }

  void setSpeed(float input) {s = input;}

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

class Mandala {
  PImage[] g_array;
  float w_s; //wigglespeed
  float w_a; //wiggle amount
  float s; //scale
  float wiggle;
  int n; // iterations
  int g; // what graphic to use from carrots[]
  float g_s; //normalized graphics scale
  float a_o; // angle offset for each graphic
  float a; // angle
  int d; //distance to center
  float r_s; //normalized rotation speed

  Mandala(PImage[] _g_array, int _n, float _r_s, float _a, float _w_a, float _w_s, int _d, float _g_s) {
    n = _n;
    w_s = _w_s;
    w_a = _w_a;
    a_o = TWO_PI/n; // set angle offset
    a = _a;
    d = _d;
    g_s = _g_s;
    r_s = _r_s;
    g_array = _g_array;
  }

  void update() {
  }

  void display() {
    c.pushMatrix();
    c.translate(c.width*.5, c.height*.5); // move to center of screen
    c.rotate(millis()*(.001*r_s));

    int g_i = 0; //index for choosing graphics from g_array
    for (int i = 0; i<n; i++) {
      c.pushMatrix();
      c.translate(cos(i*a_o)*d, sin(i*a_o)*d); // move away from center
      c.rotate(a_o*i); //rotate around center
      c.rotate(wiggleFloat(w_a, w_s)); //wiggle rotation
      c.rotate(a*PI); //set initial angle

      //the graphic displayed at each step in the mandala is chosen
      // by g_i. Every time a new graphic has been put into the mandala,
      // the counter increases by 1.
      if (g_i>=g_array.length) g_i = 0; //cycle through the available graphics
      PImage img = g_array[g_i];
      c.image(img, 0, 0, img.width*g_s, img.height*g_s);
      g_i++;

      c.popMatrix();
    }
    c.popMatrix();
  }
}

class Bushel {
  PVector pos;
  float a;
  PImage b;
  Bushel(PVector _pos){
    pos = _pos;
    a = PVector.angleBetween(pos, cc);
    println(a);
    int r = round(random(1));
    b = bushels[r];
  }

  void display(){
    c.imageMode(CORNER);
    c.rectMode(CORNER);
    PVector mouse = new PVector(mouseX, mouseY);
    //println(a);
    c.pushMatrix();
    c.translate(pos.x-10, pos.y-10);
    c.rotate(a);
    c.image(b, 0,0, b.width*2, b.height*2);
    c.popMatrix();
  }
}
