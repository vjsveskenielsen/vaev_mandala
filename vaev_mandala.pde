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
import de.looksgood.ani.*;

MidiBus midi;
String[] midi_devices;

OscP5 oscP5;
ControlP5Arranger cp5A; //custom ControlP5Arranger object
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

Layer c;
int cw = 1920, ch = 1080; //canvas dimensions

SyphonServer syphonserver;
SyphonClient[] syphon_clients;
int syphon_clients_index; //current syphon client
String syphon_name = "vaev", osc_address = syphon_name;
Log log;

ArrayList<Mandala> mandalas = new ArrayList();
Ribbons ribbons;
Corners corners;
Emblem emblem;

PImage ribbon;
PImage skovdyr_emblem, skovdyr_ring;
PImage logo_name, logo_star;
PImage rummelpot_emblem, rummelpot_ring;
PImage jomfru, hollaender, potflag;
PImage mia_ring;

PImage[] carrots = new PImage[2];
PImage[] leaves = new PImage[2];
PImage[] bushels = new PImage[4];
PImage[] flowers = new PImage[4];
PImage[] mexiko = new PImage[2];
PImage[] fish = new PImage[2];
PImage[] members = new PImage[2];
PImage[] marius = new PImage[1];
PImage[] mia = new PImage[1];
PImage[] skovdyr = new PImage[2];
PImage[] logo = new PImage[2];
PImage[] rummelpot = new PImage[2];
PImage[] rummelpotjomfru = new PImage[3];
PImage[] mia_emblem = new PImage[2];
PImage[] sparrows = new PImage[2];

PImage[][] mandala_graphics = {carrots, leaves, bushels, flowers, mexiko, fish, members, marius, mia, sparrows};
PImage[][] emblem_graphics = {logo, skovdyr, rummelpot, rummelpotjomfru, mia_emblem};

void settings() {
  size(1500, 540, P3D);
}

void setup() {
  loadGraphics();
  Ani.init(this);

  log = new Log();

  midi_devices = midi.availableInputs();
  cp5A = new ControlP5Arranger(500, 70, 6, 2); //new Arranger with grid of x by y anchors
  controlSetup();

  updateOSC(port);
  c = new Layer(cw, ch);
  c.setLimits(-200);
  vp = new Viewport(400, 50, 70);
  vp.update(c);

  syphonserver = new SyphonServer(this, syphon_name);

  mandalas.add(new Mandala("Mandala1"));
  mandalas.add(new Mandala("Mandala2"));
  mandalas.add(new Mandala("Mandala3"));
  mandalas.get(1).m_scale = 0.0;
  mandalas.get(2).m_scale = 0.0;
  cp5A.setColorScheme(0);
  ribbons = new Ribbons("Ribbons");
  corners = new Corners("Corners");
  emblem = new Emblem("Emblem");
}

void draw() {
  background(127);
  noStroke();
  fill(100);
  rect(0, 0, width, 55);
  fill(cp5.getTab("output/syphon").getColor().getBackground());
  rect(0, 0, width, cp5.getTab("OUTPUT/syphon").getHeight());


  drawGraphics();
  vp.display(c);
  syphonserver.sendImage(c);
  //interface helpers for mandalas
  int colorindex = 1;
    for (Mandala m : mandalas) {
      int x = (int)map(m.anchor.x, 0, c.width, vp.position.x, vp.position.x+vp.size);
      int y = (int)map(m.anchor.y, 0, c.height, vp.position.y, vp.position.y+vp.size);
      stroke(cp5A.foregroundColors[colorindex]);
      noFill();
      circle(x, y, m.d_norm/2*m.d_max);
      colorindex++;
    }

  log.update();
  displayFrameRate();
}

void drawGraphics() {
  c.beginDraw();
  c.background(255, 224, 74);
  c.imageMode(CENTER);
  //c.image(logo, c.width/2, c.height/2);

  for (Mandala m : mandalas){
    m.update();
    m.display();
  }

  ribbons.update();
  ribbons.display();

  corners.update();
  corners.display();

  emblem.update();
  emblem.display();

  c.endDraw();
}

void displayFrameRate(){
  String txt_fps = String.format(getClass().getName()+ "   [size %d/%d]   [fps %6.2f]", c.width, c.height, frameRate);
  surface.setTitle(txt_fps);
}

//take an xy position over the viewport, map it to a canvas_offset
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
