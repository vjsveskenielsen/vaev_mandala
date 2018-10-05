import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import codeanticode.syphon.*; 
import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class vaev_mandala extends PApplet {




ControlP5 cp5;
float anim1;
ArrayList<Mandala> mandalas = new ArrayList();
ArrayList<Corner> corners = new ArrayList();
ArrayList<Ribbon> ribbons = new ArrayList();
PImage[] carrots = new PImage[2];
PImage[] leaves = new PImage[2];
PImage[] bushels = new PImage[4];
PImage[] flowers = new PImage[4];
PImage ribbon, logo, bushel;

float m_s; //master speed
PGraphics c;
PVector cc; //canvas center

SyphonServer server;

public void settings() {
  size(960, 540, P3D);
}

public void setup() {
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
  bushels[2] = loadImage("bushel03.png");
  bushels[3] = loadImage("bushel04.png");
  flowers[0] = loadImage("flower01.png");
  flowers[1] = loadImage("flower02.png");
  flowers[2] = loadImage("flower03.png");
  flowers[3] = loadImage("flower04.png");

  corners.add(new Corner(new PVector(0,0), new PVector(1,1)));
  corners.add(new Corner(new PVector(c.width,0), new PVector(-1,1)));
  corners.add(new Corner(new PVector(c.width,c.height), new PVector(-1,-1)));
  corners.add(new Corner(new PVector(0,c.height), new PVector(1,-1)));

  ribbon = loadImage("ribbon.png");
  logo = loadImage("vaevlogo.png");
  //                       graphics, iterations, graphic start angle, distance, graphic scale
  mandalas.add(new Mandala(carrots, 34, PI, round(cc.x*.5f), .5f));
  mandalas.add(new Mandala(leaves,  40, .0f, round(cc.x*.35f), 1.f));
  mandalas.add(new Mandala(leaves,  80, .0f, round(cc.x*.61f), 1.f));
  mandalas.add(new Mandala(bushels, 80, .0f, round(cc.x*.97f), 2.f));
  mandalas.add(new Mandala(bushels, 80, .0f, round(cc.x*.85f), 1.5f));
  mandalas.add(new Mandala(flowers, 80, .0f, round(cc.x*.68f), 1.f));

  ribbons.add(new Ribbon(0, new PVector(10, 10)));
  ribbons.add(new Ribbon(HALF_PI, new PVector(10, 10)));
  ribbons.add(new Ribbon(PI, new PVector(10, 10)));
  ribbons.add(new Ribbon(HALF_PI*3, new PVector(10, 10)));
}

public float wiggleFloat(float amount, float time) {
  return sin(time)*amount;
}

public int wiggleInt(float amount, float speed) {
  return round(sin(millis()*speed)*amount);
}
public void draw() {
  c.beginDraw();
  c.background(255, 224, 74);

  for (Mandala m : mandalas){
    m.update();
    m.display();
  }
  for (Ribbon r : ribbons) {
    r.update();
    r.display();
  }
  for (Corner cnr : corners) {
    cnr.update();
    cnr.display();
  }
  c.imageMode(CENTER);
  c.image(logo, cc.x, cc.y);
  c.endDraw();

  image(c, 0,0, width, height);
  fill(80, 200);
  noStroke();
  rect(width-180, 0, 180, height);
  server.sendImage(c);
  displayFrameRate();
}

public void displayFrameRate(){
  String txt_fps = String.format(getClass().getName()+ "   [size %d/%d]   [fps %6.2f]", width, height, frameRate);
  surface.setTitle(txt_fps);
}
class Ribbon {
  float p; //progress along x axis
  float a; //angle
  float s;
  PVector pos; //position
  PVector offset; //ribbon offset from corner
  float sc = .4f; //scaling
  int max; //the side of the canvas that ribbon scrolls across
  int max_n; //maximum number of ribbons needed to cover screen

  Ribbon(float _a, PVector _offset) {
    a = _a;
    offset = _offset;
    //set pos according to orientation
    if (a>0 && a<=HALF_PI){ pos = new PVector(c.width, 0); max = c.height; }
    else if (a>HALF_PI && a<=PI){ pos = new PVector(c.width, c.height);max = c.width;}
    else if (a>PI && a<=3*HALF_PI){ pos = new PVector(0, c.height); max = c.height;}
    else {pos = new PVector(0, 0);max = c.width;}
     s = cp5.getController("ribbons_s").getValue();
    max_n = ceil(max/(ribbon.width*sc));
  }

  public void setSpeed(float v){
    s = v;
  }
  public void update() {
     if (p > ribbon.width*sc*max_n) p = 0.f;
     else if (p < 0.f) p = ribbon.width*sc*max_n;
     else p += s*m_s;
  }

  public void display() {
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
  float r = 0; //rotation
  float w_t = 0; //local wiggle time
  float w;

  // graphics, iterations, graphic start angle, distance, graphic scale
  Mandala(PImage[] _g_array, int _n, float _a, int _d, float _g_s) {
    n = _n;
    a_o = TWO_PI/n; // set angle offset
    a = _a;
    d = _d;
    g_s = _g_s;
    g_array = _g_array;
    r_s = initRotationSpeed()*m_s;
    w_s = initWiggleSpeed();
    w_a = initWiggleAmount();
  }

  public float initRotationSpeed(){
    if (g_array == carrots) return cp5.getController("carrots_r_s").getValue();
    else if (g_array == leaves) return cp5.getController("leaves_r_s").getValue();
    else if (g_array == bushels) return cp5.getController("bushels_r_s").getValue();
    else if (g_array == flowers) return cp5.getController("flowers_r_s").getValue();
    else return .0f;
  }

  public float initWiggleSpeed(){
    if (g_array == carrots) return cp5.getController("carrots_w_s").getValue();
    else if (g_array == leaves) return cp5.getController("leaves_w_s").getValue();
    else if (g_array == bushels) return cp5.getController("bushels_w_s").getValue();
    else if (g_array == flowers) return cp5.getController("flowers_w_s").getValue();
    else return .0f;
  }
  public float initWiggleAmount(){
    if (g_array == carrots) return cp5.getController("carrots_w_a").getValue();
    else if (g_array == leaves) return cp5.getController("leaves_w_a").getValue();
    else if (g_array == bushels) return cp5.getController("bushels_w_a").getValue();
    else if (g_array == flowers) return cp5.getController("flowers_w_a").getValue();
    else return .0f;
  }

  public void update() {
    r += r_s*m_s;
    if (r >= TWO_PI) r = 0;
    else if (r < 0) r = TWO_PI;

    w_t += w_s*m_s;
    w = sin(w_t)*w_a;
  }
  public void display() {
    c.pushMatrix();
    c.translate(c.width*.5f, c.height*.5f); // move to center of screen
    c.rotate(r);

    int g_i = 0; //index for choosing graphics from g_array
    for (int i = 0; i<n; i++) {
      c.pushMatrix();
      c.translate(cos(i*a_o)*d, sin(i*a_o)*d); // move away from center
      c.rotate(a_o*i); //distribute around center
      c.rotate(w); //wiggle rotation
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

class Corner {
  PVector pos;
  PVector head; //heading - the direction of the bushel
  float a;
  PImage b = bushels[3];
  float sc = 2.f;
  float w_t = 0;
  float w_a = .3f;
  float s = cp5.getController("corners_s").getValue();

  Corner(PVector _pos, PVector _head){
    pos = _pos;
    head = _head;
    int r = round(bushels.length-1);
    a = 0;
  }
  public void update() {
    w_t += s*m_s;
  }

  public void display(){
    c.imageMode(CORNER);
    c.pushMatrix();
    // position bushel with small offset
    c.translate(pos.x+(head.x*-1*25*sc), pos.y+(head.y*-1*25*sc));
    c.rotate(a+wiggleFloat(w_a, w_t));
    c.scale(head.x, head.y);
    c.image(b, 0,0, b.width*sc, b.height*sc);
    c.popMatrix();
  }
}
public void controlSetup() {
  cp5 = new ControlP5(this);
  int xoff = width-160, yoff = 10, yoffdef = 30;
  float def = 0.0f;
  float r_s_min = -0.02f, r_s_max = 0.02f;
  float w_s_max = .02f;

  int s_width = 130;
  int s_height = 10;

  cp5.addSlider("master_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(-1.f, 1.f)
    .setValue(1.f)
    .setLabel("master speed")
    ;
  posLabel("master_s");

  yoff += yoffdef;
  cp5.addSlider("ribbons_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(-3, 3)
    .setValue(0)
    .setLabel("ribbon speed")
    ;
  posLabel("ribbons_s");

  yoff += yoffdef;
  cp5.addSlider("corners_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0, r_s_max)
    .setValue(def)
    .setLabel("corner wiggle")
    ;
  posLabel("corners_s");

// CARROTS
  yoff += yoffdef+20;
  cp5.addSlider("carrots_r_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(r_s_min, r_s_max)
    .setValue(def)
    .setLabel("carrot rotation")
    ;
  posLabel("carrots_r_s");

  yoff += yoffdef;
  cp5.addSlider("carrots_w_a")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0.f, TWO_PI)
    .setValue(def)
    .setLabel("carrot wiggle amount")
    ;
  posLabel("carrots_w_a");

  yoff += yoffdef;
  cp5.addSlider("carrots_w_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0.f, w_s_max)
    .setValue(def)
    .setLabel("carrot wiggle speed")
    ;
  posLabel("carrots_w_s");

// BUSHELS
  yoff += yoffdef+20;
  cp5.addSlider("bushels_r_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(r_s_min, r_s_max)
    .setValue(def)
    .setLabel("bushel rotation")
    ;
  posLabel("bushels_r_s");

  yoff += yoffdef;
  cp5.addSlider("bushels_w_a")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0.f, TWO_PI)
    .setValue(def)
    .setLabel("bushel wiggle amount")
    ;
  posLabel("bushels_w_a");

  yoff += yoffdef;
  cp5.addSlider("bushels_w_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0.f, w_s_max)
    .setValue(def)
    .setLabel("bushel wiggle speed")
    ;
  posLabel("bushels_w_s");

// LEAVES

  yoff += yoffdef+20;
  cp5.addSlider("leaves_r_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(r_s_min, r_s_max)
    .setValue(def)
    .setLabel("leaves rotation")
    ;
  posLabel("leaves_r_s");

yoff += yoffdef;
  cp5.addSlider("leaves_w_a")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0.f, TWO_PI)
    .setValue(def)
    .setLabel("leaves wiggle amount")
    ;
  posLabel("leaves_w_a");

  yoff += yoffdef;
  cp5.addSlider("leaves_w_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0.f, w_s_max)
    .setValue(def)
    .setLabel("leaves wiggle speed")
    ;
  posLabel("leaves_w_s");

// FLOWERS

  yoff += yoffdef+20;
  cp5.addSlider("flowers_r_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(r_s_min, r_s_max)
    .setValue(0.0f)
    .setLabel("flowers rotation")
    ;
  posLabel("flowers_r_s");

  yoff += yoffdef;

  cp5.addSlider("flowers_w_a")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0.f, TWO_PI)
    .setValue(0.0f)
    .setLabel("flowers wiggle amount")
    ;
  posLabel("flowers_w_a");

  yoff += yoffdef;
  cp5.addSlider("flowers_w_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0.f, w_s_max)
    .setValue(def)
    .setLabel("flowers wiggle speed")
    ;
  posLabel("flowers_w_s");
}

public void controlEvent(ControlEvent theEvent) {
  if (theEvent.isController()) {

    String name =theEvent.getController().getName();
    /*
    if (theEvent.getController().equals(slider1)) {
      setRibbonSpeed(slider1.getValue());
    }
    */
  }
}

public void posLabel(String con) {
  cp5.getController(con).getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
  cp5.getController(con).getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
}

public void master_s(float value) {
  m_s = value;
}

public void ribbons_s(float value) {
  for (Ribbon r : ribbons) {
    r.s = value;
  }
}

public void corners_s(float value){
  for (Corner c : corners) {
    c.s = value;
  }
}
// CARROTS
public void carrots_r_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == carrots) m.r_s = value;
  }
}

public void carrots_w_a(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == carrots) m.w_a = value;
  }
}

public void carrots_w_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == carrots) m.w_s = value;
  }
}

// BUSHELS
public void bushels_r_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == bushels) m.r_s = value;
  }
}

public void bushels_w_a(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == bushels) m.w_a = value;
  }
}

public void bushels_w_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == bushels) m.w_s = value;
  }
}

// LEAVES

public void leaves_r_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == leaves) m.r_s = value;
  }
}

public void leaves_w_a(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == leaves) m.w_a = value;
  }
}

public void leaves_w_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == leaves) m.w_s = value;
  }
}

// FLOWERS
public void flowers_r_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == flowers) m.r_s = value;
  }
}

public void flowers_w_a(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == flowers) m.w_a = value;
  }
}

public void flowers_w_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == flowers) m.w_s = value;
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "vaev_mandala" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
