import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import codeanticode.syphon.*; 
import controlP5.*; 
import themidibus.*; 
import oscP5.*; 
import netP5.*; 
import processing.net.*; 
import java.util.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class vaev_mandala extends PApplet {

/*
Vaev app for Assens
*/








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
int cw = 1920, ch = 1080;

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

public void settings() {
  size(960, 540, P3D);
}

public void setup() {
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

  ribbon = loadImage("ribbon.png");
  logo = loadImage("vaevlogo.png");
  mandalas.add(new Mandala(carrots, 34, -.2f, PI, .1f*PI, .0003f, 700, .8f));
  mandalas.add(new Mandala(leaves, 40, .3f, .0f, .3f*PI, .0003f, 200, 1.f));
  mandalas.add(new Mandala(leaves, 80, .3f, .0f, .3f*PI, .0003f, 500, 1.f));
  mandalas.add(new Mandala(bushels, 68, .3f, .0f, .3f*PI, .0003f, 300, 1.f));
  mandalas.add(new Mandala(flowers, 80, .3f, .0f, .3f*PI, .0003f, 600, 1.f));

  for (int i = 0; i<ribbons.length; i++) {
    Ribbon r = new Ribbon(HALF_PI*i, 1.f, new PVector(10, 10));
    ribbons[i] = r;
  }
}

public void draw() {
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

public void drawGraphics() {
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

public float wiggleFloat(float amount, float speed) {
  return sin(millis()*speed)*amount;
}

public int wiggleInt(float amount, float speed) {
  return round(sin(millis()*speed)*amount);
}

public void displayFrameRate(){
  String txt_fps = String.format(getClass().getName()+ "   [size %d/%d]   [fps %6.2f]", width, height, frameRate);
  surface.setTitle(txt_fps);
}

public PVector mapXYToCanvas(int x_in, int y_in, Viewport viewport, PGraphics pg) {
  int x_min = round(viewport.position.x + viewport.canvas_offset.x);
  int x_max = x_min + viewport.canvas_width;
  int y_min = round(viewport.position.y + viewport.canvas_offset.y);
  int y_max = y_min + viewport.canvas_height;
  PVector out = new PVector(-1, -1);
  if (x_in >= x_min && x_in <= x_max && y_in >= y_min && y_in <= y_max) {
    float x = map(x_in, x_min, x_max, 0.0f, pg.width);
    float y = map(y_in, y_min, y_max, 0.0f, pg.height);
    out = new PVector(x,y);
  }
  return out;
}
class Corner {
  PVector pos;
  PVector head; //heading - the direction of the bushel
  float a;
  PImage b = bushels[3];
  float sc = 1.5f;
  Corner(PVector _pos, PVector _head){
    pos = _pos;
    head = _head;
    println(a);
    int r = round(bushels.length-1);
    a = 0;
  }

  public void display(){
    c.imageMode(CORNER);
    c.pushMatrix();
    // position bushel with small offset
    c.translate(pos.x+(head.x*-1*25*sc), pos.y+(head.y*-1*25*sc));
    c.rotate(a+wiggleFloat(.05f, .001f));
    c.scale(head.x, head.y);
    c.image(b, 0,0, b.width*sc, b.height*sc);
    c.popMatrix();
  }
}
class Log {
  String current_log;
  int counter;
  Log() {
    current_log = "No new events";
    counter = 30;
  }

  public void update() {
    fill(5);
    text(current_log, 10, height-10);
  }

  public void setText(String input) {
    String time = zeroFormat(hour()) + ":" + zeroFormat(minute()) + ":" + zeroFormat(second());
    current_log = time + " " + input;
  }
}
//function for formatting int values as strings: 1 becomes "01", 2 becomes "02"
public String zeroFormat(int input) {
  String output = Integer.toString(input);
  if (input < 10) output = "0" + output;
  return output;
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
  float r = 0;
  // graphics, iterations, mandala rotation, graphic angle, wiggle amount,
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

  public void update() {
    if (r >= TWO_PI) r = 0;
    else if (r < 0) r = TWO_PI;
    else r += r_s;
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
class Ribbon {
  float p; //progress along x axis
  float a; //angle
  float s; // speed
  PVector pos; //position
  PVector offset; //ribbon offset from corner
  float sc = .4f; //scaling
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
  public void update() {
     if (p > ribbon.width*sc*max_n) p = 0;
     else if (p < 0) p = ribbon.width*sc*max_n;
     else p+=s;
  }

  public void setSpeed(float input) {s = input;}

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
class Viewport {
  int canvas_width;
  int canvas_height;
  int size; //viewport size
  PVector position;
  PVector canvas_offset = new PVector(0,0); //canvas pos within viewport
  PGraphics bg; //background customized for canvas

  Viewport(PGraphics pg, int vsize, int vpx, int vpy) {
    size = vsize;
    position = new PVector(vpx, vpy);
  }

  public void display(PGraphics pg) {
    pushMatrix();
    translate(position.x, position.y);
    noFill();
    stroke(100);
    rect(0, 0, size, size);
    noStroke();
    fill(255);
    drawPointers();

    if (viewport_show_alpha) image(bg, canvas_offset.x, canvas_offset.y, canvas_width, canvas_height);
    else {
      fill(0);
      rect(canvas_offset.x, canvas_offset.y, canvas_width, canvas_height);
    }
    image(pg, canvas_offset.x, canvas_offset.y, canvas_width, canvas_height);
    popMatrix();
  }

  public void resize(PGraphics pg) {
    int[] dims = scaleToFit(pg.width, pg.height, size, size);
    canvas_offset = new PVector(dims[0], dims[1]);
    canvas_width = dims[2];
    canvas_height =dims[3];
    bg = createAlphaBackground(canvas_width, canvas_height);
  }

  public PGraphics createAlphaBackground(int w, int h) {

    PGraphics abg = createGraphics(w, h, P2D);
    int s = 10; // size of square
    abg.beginDraw();
    abg.background(127+50);
    abg.noStroke();
    abg.fill(127-50);
    for (int x = 0; x < w; x+=s+s) {
      for (int y = 0; y < h; y+=s+s) {
        abg.rect(x, y, s, s);
      }
    }
    for (int x = s; x < w; x+=s+s) {
      for (int y = s; y < h; y+=s+s) {
        abg.rect(x, y, s, s);
      }
    }
    abg.endDraw();
    return abg;
  }

  public void drawPointers() {
    float x = canvas_offset.x;
    float y = canvas_offset.y;
    triangle(x, y, x-5, y, x, y-5);
    x += bg.width;
    triangle(x, y, x+5, y, x, y-5);
    y += bg.height;
    triangle(x, y, x+5, y, x, y+5);
    x = canvas_offset.x;
    triangle(x, y, x-5, y, x, y+5);
  }
}

public void updateCanvas() {
  c = createGraphics(cw, ch, P3D);
  vp.resize(c);
}

public void updateCanvas(int w, int h) {
  c = createGraphics(w, h, P3D);
  c = createGraphics(w, h, P3D);
  vp.resize(c);
}

public int[] scaleToFill(int in_w, int in_h, int dest_w, int dest_h) {
  PVector in = new PVector((float)in_w, (float)in_h); //vector of input dimensions
  PVector dest = new PVector((float)dest_w, (float)dest_h); //vector of destination dimensions
  /*
  calculate the scaling ratios for both axis, and choose the largest for scaling
  the output dimensions to FILL the destination
  */
  float scale = max(dest.x/in.x, dest.y/in.y);
  int out_w = round(in_w *scale);
  int out_h = round(in_h *scale);
  int off_x = (dest_w - out_w) / 2;
  int off_y = (dest_h - out_h) / 2;

  int[] out = {off_x, off_y, out_w, out_h};
  return out;
}

public int[] scaleToFit(int in_w, int in_h, int dest_w, int dest_h) {
  PVector in = new PVector((float)in_w, (float)in_h); //vector of input dimensions
  PVector dest = new PVector((float)dest_w, (float)dest_h); //vector of destination dimensions
  /*
  calculate the scaling ratios for both axis, and choose the SMALLEST for scaling
  the output dimensions to FIT the destination
  */
  float scale = min(dest.x/in.x, dest.y/in.y);
  int out_w = round(in_w *scale);
  int out_h = round(in_h *scale);
  int off_x = (dest_w - out_w) / 2;
  int off_y = (dest_h - out_h) / 2;
  println("offset x:", off_x, "offset y:", off_y);

  int[] out = {off_x, off_y, out_w, out_h};
  return out;
}
public void controlSetup() {
  cp5 = new ControlP5(this);
  int xoff = 10;
  int yoff = 20;

  cb = new CallbackListener() {
    public void controlEvent(CallbackEvent theEvent) {
      switch(theEvent.getAction()) {
        case(ControlP5.ACTION_ENTER):
        cursor(HAND);
        break;
        case(ControlP5.ACTION_LEAVE):
        case(ControlP5.ACTION_RELEASEDOUTSIDE):
        cursor(ARROW);
        break;
      }
    }
  };

  cp5.getTab("default")
  .setAlwaysActive(true)
  .hideBar()
  .setWidth(-3)
  ;
  //hide default bar
  cp5.addTab("output/syphon").setActive(true);

  cp5.addTab("osc/midi")
  ;

  field_cw = cp5.addTextfield("field_cw")
  .setPosition(xoff, yoff)
  .setSize(30, 20)
  .setAutoClear(false)
  .setText(Integer.toString(cw))
  .setLabel("width")
  .setId(-1)
  .moveTo("output/syphon")
  ;

  xoff += field_cw.getWidth() + 10;
  field_ch = cp5.addTextfield("field_ch")
  .setPosition(xoff, yoff)
  .setSize(30, 20)
  .setAutoClear(false)
  .setText(Integer.toString(ch))
  .setLabel("height")
  .setId(-1)
  .moveTo("output/syphon")
  ;

  xoff += field_ch.getWidth() + 10;
  xoff += cp5.getController("field_ch").getWidth() + 10;
  field_syphon_name = cp5.addTextfield("field_syphon_name")
  .setPosition(xoff, yoff)
  .setSize(60, 20)
  .setAutoClear(false)
  .setText(syphon_name)
  .setLabel("syphon name")
  .setId(-1)
  .moveTo("output/syphon")
  ;

  xoff += field_syphon_name.getWidth() + 10;
  toggle_view_bg = cp5.addToggle("viewport_show_alpha")
  .setPosition(xoff, yoff)
  .setSize(50, 20)
  .setValue(viewport_show_alpha)
  .setLabel("alpha / none")
  .setMode(ControlP5.SWITCH)
  .setId(-1)
  .moveTo("output/syphon")
  ;

  xoff = 10; //reset position for tab "osc/midi"
  button_ip = cp5.addButton("button_ip")
  .setPosition(xoff, yoff)
  .setSize(70, 20)
  .setLabel("ip: " + ip)
  .setSwitch(false)
  .setId(-1)
  .moveTo("osc/midi")
  ;

  xoff += button_ip.getWidth() + 10;
  field_osc_port = cp5.addTextfield("field_osc_port")
  .setPosition(xoff, yoff)
  .setSize(30, 20)
  .setAutoClear(false)
  .setText(Integer.toString(port))
  .setLabel("osc port")
  .setId(-1)
  .moveTo("osc/midi")
  ;

  xoff += field_osc_port.getWidth() + 10;
  field_osc_address = cp5.addTextfield("field_osc_address")
  .setPosition(xoff, yoff)
  .setSize(50, 20)
  .setAutoClear(false)
  .setText(syphon_name)
  .setLabel("osc address")
  .setId(-1)
  .moveTo("osc/midi")
  ;

  xoff += field_osc_address.getWidth() + 10;
  toggle_log_osc = cp5.addToggle("log_osc")
  .setPosition(xoff, yoff)
  .setSize(30, 20)
  .setLabel("log osc")
  .setValue(true)
  .setId(-1)
  .moveTo("osc/midi")
  ;

  xoff += toggle_log_osc.getWidth() + 10;
  dropdown_midi = cp5.addScrollableList("dropdown_midi")
  .setPosition(xoff, yoff)
  .setSize(200, 100)
  .setOpen(false)
  .setBarHeight(20)
  .setItemHeight(20)
  .addItems(Arrays.asList(midi_devices))
  .setLabel("MIDI INPUT")
  .setId(-1)
  .moveTo("osc/midi")
  .setType(ScrollableList.LIST) // currently supported DROPDOWN and LIST
  ;

  xoff += dropdown_midi.getWidth() + 10;
  toggle_log_midi = cp5.addToggle("log_midi")
  .setPosition(xoff, yoff)
  .setSize(30, 20)
  .setLabel("log midi")
  .setValue(true)
  .setId(-1)
  .moveTo("osc/midi")
  ;

  /*  CUSTOM CONTROLS
  Add your own controls below. Use .setId(-1) to make controller
  unreachable by OSC.
  */
  xoff = 500;
  yoff = 100;
  int s_width = 100;
  int s_height = 20;

  cp5.addSlider("ribbons_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(-3, 3)
    .setValue(1)
    .setLabel("ribbon speed")
    ;
    cp5.getController("ribbons_s").getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
    cp5.getController("ribbons_s").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);

  yoff += 50;
  cp5.addSlider("carrots_r_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(-.5f, .5f)
    .setValue(0.0f)
    .setLabel("carrot rotation")
    ;
  cp5.getController("carrots_r_s").getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
  cp5.getController("carrots_r_s").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);

  yoff += 50;
  cp5.addSlider("carrots_w_a")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0.f, TWO_PI)
    .setValue(0.0f)
    .setLabel("carrot wiggle amount")
    ;
  cp5.getController("carrots_w_a").getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
  cp5.getController("carrots_w_a").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);

}

// checks if input is 4 digits
public int evalFieldInput1(String in, int current, Controller con) {
  String name = con.getLabel();
  int out = -1;
  char[] ints = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
  char[] input = in.toCharArray();

  String txt = "value not int between 1 and 9999";
  if (input.length < 5) {
    int check = 0;
    for (char ch : input) {
      for (char i : ints) {
        if (ch == i) check++;
      }
    }

    if (input.length == check) {
      int verified_int = Integer.parseInt(in);
      txt = name + " changed from " + current + " to " + verified_int;
      if (verified_int < 1) {
        verified_int = 1;
        txt = name + " was lower than 0 and defaults to " + verified_int;
      }
      if (verified_int == current) txt = "value is not different from " + current;
      else {
        out = verified_int;
      }
    }
  }
  log.setText(txt);

  return out;
}

// checks if input is valid string for osc path
public boolean evalFieldInput2(String in, String current, Controller con) {
  String name = con.getLabel();
  String txt = "input to " + name + " is unchanged";
  boolean out = true;
  char[] illegal_chars = {'/', ',', '.', '(', ')', '[', ']',
  '{', '}', ' '};
  char[] input = in.toCharArray();
  if (!in.equals(current)) {
    if (input.length > 0) {
      for (char ch : input) {
        for (char i : illegal_chars) {
          if (ch == i) {
            txt = "input to " + name + " contained illegal character and was reset";
            out = false;
          }
        }
      }
    }
  }

  log.setText(txt);

  return out;
}

public void field_cw(String theText) {
  int value = evalFieldInput1(theText, cw, cp5.getController("field_cw"));
  if (value > 0) {
    cw = value;
    updateCanvas();
  }
}
public void field_ch(String theText) {
  int value = evalFieldInput1(theText, ch, cp5.getController("field_ch"));
  if (value > 0) {
    ch = value;
    updateCanvas();
  }
}

public void field_syphon_name(String input) {
  if (evalFieldInput2(input, syphon_name, field_syphon_name)) {
    syphon_name = input;
    field_osc_address.setText(input);
    osc_address = input;
    log.setText("syphon name and osc address set to " + input);
  }
  else field_syphon_name.setText(syphon_name);
}

public void field_osc_address(String input) {
  if (evalFieldInput2(input, osc_address, field_osc_address)) {
    syphon_name = input;
    log.setText("osc address set to " + input);
  }
  else field_osc_address.setText(osc_address);
}

public void dropdown_midi(int n) {
  updateMIDI(n);
  println("added " + midi_devices[n], n);
}

public void log_midi(boolean state) {
  log_midi = state;
  if (state) log.setText("started logging midi input");
  else log.setText("stopped logging midi input");
}

public void field_osc_port(String theText) {
  int value = evalFieldInput1(theText, port, field_osc_port);
  if (value > 0) {
    port = value;
    updateOSC(port);
  }
}

public void button_ip() {
  updateIP();
  log.setText("ip adress has been updated to " + ip);
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

/*
Custom control functions
*/


public void ribbons_s(float value) {
  for (int i = 0; i<ribbons.length; i++) {
    ribbons[i].setSpeed(value);
  }
}

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
public void loadGraphics() {
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
}
public void noteOn(int channel, int pitch, int velocity) {
  if (log_midi) log.setText("Note On // Channel:"+channel + " // Pitch:"+pitch + " // Velocity:"+velocity);
}

public void noteOff(int channel, int pitch, int velocity) {
  if (log_midi) log.setText("Note Off // Channel:"+channel + " // Pitch:"+pitch + " // Velocity:"+velocity);
}

public void controllerChange(int channel, int number, int value) {
  if (log_midi) log.setText("Slider // Channel:"+channel + " // Number:" +number + " // Value: "+value);
}

public void changeSlider(String name, int value) {
  Controller con = cp5.getController(name);
  con.setValue(map(value, 0, 127, con.getMin(), con.getMax()));
}

public void updateMIDI(int n) {
 log.setText("added midi device " + midi_devices[n]);
 midi = new MidiBus(this, n, -1);
}
public void updateOSC(int p) {
  updateIP();
  oscP5 = new OscP5(this, p);
  cp5.getController("field_osc_port").setValue(p);
}

public void updateIP() {
  ip = Server.ip();
  cp5.getController("button_ip").setLabel("ip: " + ip);
}

public void oscEvent(OscMessage theOscMessage) {
  String str_in[] = split(theOscMessage.addrPattern(), '/');
  String txt = "got osc message: " + theOscMessage.addrPattern();
  if (str_in.length == 3) {
    if (str_in[1].equals(osc_address) &&
    cp5.getController(str_in[2]) != null &&
    cp5.getController(str_in[2]).getId() != -1)
    {
      Controller con = cp5.getController(str_in[2]);

      if (theOscMessage.checkTypetag("i")) {
        int value = theOscMessage.get(0).intValue();
        value = constrain(value, (int)con.getMin(), (int)con.getMax());
        con.setValue(value);
        txt += " int value: " + Integer.toString(value);
      }

      else if (theOscMessage.checkTypetag("f")) {
        float value = theOscMessage.get(0).floatValue();
        value = constrain(value, con.getMin(), con.getMax());
        con.setValue(value);
        txt += " float value: " + Float.toString(value);
      }
    }
  }
  if (log_osc) log.setText(txt);
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
