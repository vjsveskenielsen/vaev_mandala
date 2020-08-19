/*
Vaev app for Assens
*/
import codeanticode.syphon.*;
import controlP5.*;
import themidibus.*;
import oscP5.*;
import netP5.*;
import processing.net.*;
import java.util.*;

MidiBus midi;
String[] midi_devices;
OscP5 oscP5;
ControlP5 cp5;
CallbackListener cb;
Textfield field_cw, field_ch, field_syphon_name, field_osc_port, field_osc_address;
Button button_ip;
ScrollableList dropdown_midi, dropdown_syphon_client;
Toggle toggle_log_osc, toggle_log_midi, toggle_view_bg;
Viewport vp;
boolean viewport_show_alpha = false;
boolean log_midi = true, log_osc = true;

int port = 9999;
String ip;

PGraphics c;
int cw = 1920, ch = 1080; //canvas dimensions
float lim_l, lim_r, lim_t, lim_b; //limits for graphics to be drawn within

SyphonServer syphonserver;
SyphonClient[] syphon_clients;
int syphon_clients_index; //current syphon client
String syphon_name = "vaev", osc_address = syphon_name;
Log log;

float anim1;
ArrayList<Mandala> mandalas = new ArrayList();
ArrayList<Corner> corners = new ArrayList();
Ribbon[] ribbons = new Ribbon[4];
Ribbon stripes;
PImage[] carrots = new PImage[2];
PImage[] leaves = new PImage[2];
PImage[] bushels = new PImage[4];
PImage[] flowers = new PImage[4];
PImage ribbon, logo, bushel;

PVector cc; //canvas center

void settings() {
  size(960, 540, P3D);
}

void setup() {
  log = new Log();

  midi_devices = midi.availableInputs();
  controlSetup();
  updateOSC(port);
  c = createGraphics(cw, ch, P3D);
  vp = new Viewport(c, 400, 50, 70);
  vp.resize(c);
  syphonserver = new SyphonServer(this, syphon_name);

  //create syphon output
  cc = new PVector(c.width/2, c.height/2);

  loadGraphics();

  corners.add(new Corner(new PVector(0,0), new PVector(1,1)));
  corners.add(new Corner(new PVector(c.width,0), new PVector(-1,1)));
  corners.add(new Corner(new PVector(c.width,c.height), new PVector(-1,-1)));
  corners.add(new Corner(new PVector(0,c.height), new PVector(1,-1)));

// graphics, iterations, mandala rotation, graphic angle, wiggle amount,
  mandalas.add(new Mandala(carrots, 34, -.2, PI, .1*PI, .0003, 700, .8));
  //mandalas.add(new Mandala(leaves, 40, .3, .0, .3*PI, .0003, 200, 1.));
  //mandalas.add(new Mandala(leaves, 80, .3, .0, .3*PI, .0003, 500, 1.));
  //mandalas.add(new Mandala(bushels, 68, .3, .0, .3*PI, .0003, 300, 1.));
  //mandalas.add(new Mandala(flowers, 80, .3, .0, .3*PI, .0003, 600, 1.));

  for (int i = 0; i<ribbons.length; i++) {
    Ribbon r = new Ribbon(HALF_PI*i, 1., new PVector(10, 10));
    ribbons[i] = r;
  }
}

void draw() {
  background(127);
  noStroke();
  fill(100);
  rect(0, 0, width, 55);
  fill(cp5.getTab("output/syphon").getColor().getBackground());
  rect(0, 0, width, cp5.getTab("output/syphon").getHeight());


  drawGraphics();
  vp.display(c);
  syphonserver.sendImage(c);
  log.update();
    displayFrameRate();
}

void drawGraphics() {
  c.beginDraw();
  c.background(255, 224, 74);
  c.imageMode(CENTER);
  c.image(logo, c.width/2, c.height/2);

  for (Mandala m : mandalas){
    m.update();
    m.display();
  }
  for (int i = 0; i<ribbons.length; i++) {
    ribbons[i].update();
    ribbons[i].display();
  }
  for (Corner cnr : corners) {
    cnr.display();
  }
  c.endDraw();
}

float wiggleFloat(float amount, float speed) {
  return sin(millis()*speed)*amount;
}

int wiggleInt(float amount, float speed) {
  return round(sin(millis()*speed)*amount);
}

void displayFrameRate(){
  String txt_fps = String.format(getClass().getName()+ "   [size %d/%d]   [fps %6.2f]", width, height, frameRate);
  surface.setTitle(txt_fps);
}

PVector mapXYToCanvas(int x_in, int y_in, Viewport viewport, PGraphics pg) {
  int x_min = round(viewport.position.x + viewport.canvas_offset.x);
  int x_max = x_min + viewport.canvas_width;
  int y_min = round(viewport.position.y + viewport.canvas_offset.y);
  int y_max = y_min + viewport.canvas_height;
  PVector out = new PVector(-1, -1);
  if (x_in >= x_min && x_in <= x_max && y_in >= y_min && y_in <= y_max) {
    float x = map(x_in, x_min, x_max, 0.0, pg.width);
    float y = map(y_in, y_min, y_max, 0.0, pg.height);
    out = new PVector(x,y);
  }
  return out;
}
