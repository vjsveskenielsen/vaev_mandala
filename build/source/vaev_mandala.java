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
import de.looksgood.ani.*; 
import processing.core.PApplet; 
import processing.core.PGraphics; 
import codeanticode.syphon.*; 

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

public void settings() {
  size(1500, 540, P3D);
}

public void setup() {
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
  mandalas.get(1).m_scale = 0.0f;
  mandalas.get(2).m_scale = 0.0f;
  cp5A.setColorScheme(0);
  ribbons = new Ribbons("Ribbons");
  corners = new Corners("Corners");
  emblem = new Emblem("Emblem");
}

public void draw() {
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

public void drawGraphics() {
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

public void displayFrameRate(){
  String txt_fps = String.format(getClass().getName()+ "   [size %d/%d]   [fps %6.2f]", c.width, c.height, frameRate);
  surface.setTitle(txt_fps);
}

//take an xy position over the viewport, map it to a canvas_offset
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
class ControlP5Arranger {
  int x = 0;
  int y = 0;
  int sliderwidth = 100;
  int sliderheight = 20;
  int knobsize = 50;
  int margin = 15;
  int groupwidth = 120;
  PVector[] anchors;
  int anchor_index = 0;
  int groupheight = 550;
  int[] foregroundColors = {color(150), color(255, 0, 0, 100),  color(0, 200, 50, 100),  color(0, 0, 255, 100)};
  int[] backgroundColors = {color(150), color(255, 0, 0, 50),   color(0, 200, 50, 50),   color(0, 0, 255, 50)};
  int[] activeConColors =  {color(150), color(255, 0, 0, 255),  color(0, 200, 50, 255),  color(0, 0, 255, 255)};
  int colorSchemeIndex = 0;

  public void style1(String con_name) {
    Controller con = cp5.getController(con_name);
    con.setHeight(cp5A.sliderheight);
    con.setWidth(sliderwidth);
    con.setId(0);
    con.getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
    con.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
    //con.setColorForeground(cp5A.getForegroundColor());
    //con.setColorBackground(cp5A.getBackgroundColor());
    //con.setColorActive(cp5A.getActiveConColor());
  }

  //anchors[0] x, anchors[0] y, n anchors on x axis, n anchors on y axis
  ControlP5Arranger(int ax, int ay, int nx, int ny) {
    anchors = generateAnchors(nx, groupwidth+margin, ny, groupheight+margin, ax, ay);
  }

  public void goToNextAnchor() {
    anchor_index++;
  }

  public PVector getAnchor() {
    if (anchor_index >= 0 && anchor_index < anchors.length) return anchors[anchor_index];
    else return new PVector(0, 0);
  }

  public PVector[] generateAnchors(int nx, int w, int ny, int h, int xoff, int yoff) {
    PVector[] out = new PVector[nx*ny];
    int index = 0;
    for (int i = 0; i<ny; i++) { //for every rows of ny rows
      for (int j = 0; j<nx; j++) { // and every column of nx columns
        out[index] = new PVector(j*w+xoff, i*h+yoff); //add coordinates to anchors[] at index
        index++; //increase index
      }
    }
    return out;
  }
  public void setX(int input) {
    x = input;
  }
  public void setY(int input) {
    y = input;
  }
  public void setXY(int inputx, int inputy) {
    x = inputx;
    y = inputy;
  }

  public void addXY(int inputx, int inputy) {
    x += inputx;
    y += inputy;
  }

  public int getForegroundColor() {
    return foregroundColors[colorSchemeIndex];
  }
  public int getBackgroundColor() {
    return activeConColors[colorSchemeIndex];
  }
  public int getActiveConColor() {
    return activeConColors[colorSchemeIndex];
  }

  public void goToNextColorScheme() {
    colorSchemeIndex = rollOver(colorSchemeIndex+1, 0, foregroundColors.length);
  }
  public void setColorScheme(int input) {
    colorSchemeIndex = input;
  }
}
class Corners {
  String name;
  Group controlGroup;
  PVector[] corner_anchors = new PVector[4];
  float[] corner_angles = new float[4];
  float scale;
  float graphics_width;
  float graphics_height;

  Corners(String _name){
    name = _name;
    calculateAnchors();
    calculateAngles();

    controlGroup = cp5.addGroup(name)
    .setPosition(cp5A.getAnchor().x, cp5A.getAnchor().y)
    .setWidth(cp5A.groupwidth)
    .activateEvent(true)
    .setBackgroundColor(color(255, 80))
    .setLabel(name)
    ;

    cp5A.addXY(5, 5);
    cp5.addSlider(name + "/" + "scale")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(0.0f, 2.0f )
    .plugTo( this, "setScale" )
    .setValue( scale )
    .setLabel("scale")
    .setGroup(controlGroup);
    ;
    controlGroup.setBackgroundHeight(cp5A.groupheight);
    cp5A.setXY(0,0); //reset xy for next group of controls
    cp5A.goToNextAnchor(); //move to next anchor for next group of controls
  }

  public void setScale(float input){
    scale = input;
  }
  public void calculateAnchors() {
    for (int i = 0; i<4; i++) {
      int margin =50;
      switch(i) {
        case 0: corner_anchors[0] = new PVector(-margin,-margin);
        case 1: corner_anchors[1] = new PVector(c.width+margin,-margin);
        case 2: corner_anchors[2] = new PVector(c.width+margin, c.height+margin);
        case 3: corner_anchors[3] = new PVector(-margin, c.height+margin);
      }
    }
  }

  public void calculateAngles() {
    float a = 0;
    for (int i = 0; i<corner_angles.length; i++) {
      corner_angles[i] = a + HALF_PI*i;
    }
  }

  public void update() {

  }

  public void display(){
    c.imageMode(CORNER);
    for (int i = 0; i<corner_anchors.length; i++) {
      c.pushMatrix();
      c.translate(corner_anchors[i].x, corner_anchors[i].y);
      c.rotate(corner_angles[i]);
      for (int j = 1; j<bushels.length+1; j++){
        int index = bushels.length-j;
        c.rotate(-0.30f + 0.15f*i);
        c.rotate(wiggleFloat(.05f, .001f*index));
        c.image(bushels[index], 0, 0, bushels[index].width*scale, bushels[index].height*scale);
      }
      c.popMatrix();
    }
  }
}
class Emblem {
  Group controlGroup;
  String name;
  int current_graphics = 0;

  float m_scale; //master scale for all graphics
  float a_scale = 1.0f; //scale to be animated on chooseGraphics()

  float r0_s; //rotation speed
  float r0 = 0; //rotation value
  float r1_s; //rotation speed
  float r1 = 0; //rotation value
  float orientation;
  PVector anchor;
  float anchor_offset = 0;

  boolean r0_rotate_wiggle, r1_rotate_wiggle;

  // graphics, iterations, mandala rotation, graphic angle, wiggle amount,
  Emblem(String _name) {
    name = _name;
    anchor = c.center;

    controlGroup = cp5.addGroup(name)
    .setPosition(cp5A.getAnchor().x, cp5A.getAnchor().y)
    .setWidth(cp5A.groupwidth)
    .activateEvent(true)
    .setBackgroundColor(color(255, 80))
    .setLabel(name)
    ;

    cp5A.addXY(5, 5);

    cp5.addSlider(name + "/" + "scale")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(0.0f, 2.0f )
    .plugTo( this, "setScale" )
    .setValue( 1.0f )
    .setLabel("scale")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "scale");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "r0_rotation_speed")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-0.01f, 0.01f )
    .plugTo( this, "r0_s" )
    .setValue( 1.0f )
    .setLabel("r0_rotation_speed")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "r0_rotation_speed");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addToggle(name + "/" + "r0_rotate_wiggle")
    .setPosition(cp5A.x, cp5A.y)
    .plugTo( this, "r0_rotate_wiggle" )
    .setValue(false)
    .setMode(ControlP5.SWITCH)
    .setLabel("r0_rotate_wiggle")
    .setGroup(controlGroup)
    ;

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "r1_rotation_speed")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-0.01f, 0.01f )
    .plugTo( this, "r1_s" )
    .setValue( 1.0f )
    .setLabel("r1_rotation_speed")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "r1_rotation_speed");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addToggle(name + "/" + "r1_rotate_wiggle")
    .setPosition(cp5A.x, cp5A.y)
    .plugTo( this, "r1_rotate_wiggle" )
    .setValue(true)
    .setMode(ControlP5.SWITCH)
    .setLabel("r1_rotate_wiggle")
    .setGroup(controlGroup)
    ;

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addBang(name + "/" + "emblem_toggle")
    .setPosition(cp5A.x, cp5A.y)
    .plugTo( this, "moveInOut" )
    .setLabel("emblem_toggle")
    .setGroup(controlGroup)
    ;

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addScrollableList(name + "/" + "graphics")
    .setPosition(cp5A.x, cp5A.y)
    .addItem("vaev_logo", 0)
    .addItem("skovdyr", 1)
    .addItem("rummelpot", 2)
    .addItem("rummelpotjomfru", 3)
    .addItem("mia", 4)
    .setValue(0)
    .plugTo(this, "scaleDownChangeGraphics")
    .setLabel("choose graphics")
    .setGroup(controlGroup)
    .setType(ControlP5.LIST)
    .open()
    ;
    //cp5A.style1(name + "/" + "graphics");

    controlGroup.setBackgroundHeight(cp5A.groupheight);
    cp5A.setXY(0,0); //reset xy for next group of controls
    cp5A.goToNextAnchor(); //move to next anchor for next group of controls
  }

  public void setScale(float input) {
    m_scale = input;
  }

  public void chooseGraphics(int input) {
    current_graphics = input;
  }
  public void moveInOut() {
    if (anchor_offset > 0) {
      moveIn();
    }
    else {
      moveOut();
    }
  }

  public void moveOut() {
    Ani.to(this, 3.0f, "anchor_offset", c.height, Ani.QUAD_IN);
  }

  public void moveIn() {
    Ani.to(this, 3.0f, "anchor_offset", 0, Ani.QUAD_OUT);
  }
  public void scaleDownChangeGraphics(int input) {
    //if the input different from current_graphics and no animation is in progress
    if (input != current_graphics) {
      //animate the scale and callback to scaleUp
      Ani.to(this, 1.0f, "a_scale", 0.0f, Ani.QUAD_IN, "onEnd:scaleUpChangeGraphics");
    }
  }
  public void scaleUpChangeGraphics() {
    Ani.to(this, 1.0f, "a_scale", 1.0f, Ani.QUAD_IN);
    current_graphics = (int)cp5.getController(name + "/graphics").getValue();
  }

  public void update() {
    m_scale = cp5.getController(name + "/" + "scale").getValue()*a_scale;
    r0 = rollOver(r0+r0_s, 0, TWO_PI);
    r1 = rollOver(r1+r1_s, 0, TWO_PI);
  }

  public void display() {
    c.pushMatrix();
    c.translate(anchor.x, anchor.y+anchor_offset);
    c.imageMode(CENTER);
    PImage img;
    for (int i = emblem_graphics[current_graphics].length-1; i>-1; i--) {
      img = emblem_graphics[current_graphics][i];
      //println(current_graphics, i);
      c.pushMatrix();
      switch(i) {
        case 0:
        if (r0_rotate_wiggle) c.rotateZ(r0);
        else c.rotate(sin(r0)*.25f);
        break;
        case 1:
        if (r1_rotate_wiggle) c.rotateZ(r1);
        else c.rotate(sin(r1)*.25f);
        break;
        case 2:
        if (r0_rotate_wiggle) c.rotateZ(r0);
        else c.rotate(sin(r0)*.25f);
        break;
      }
      c.image(img, 0,0, img.width*m_scale, img.height*m_scale);
      c.popMatrix();
    }
    c.popMatrix();
  }
}
/* Example of a custom Layer class
 The Layer class extends a PGraphics3D object with nice stuff like
  - variable limits for optimizing drawing stuff within the canvas
  - variable for center of canvas
 */






public class Layer extends PGraphics3D {
  /* this class was made with no idea of whats going on, lifting from
   https://forum.processing.org/two/discussion/5238/#Comment_18116
   */

  Limits limits = new Limits();
  PVector center;

  public Layer(int w, int h) {
    final PApplet p = getEnclosingPApplet();
    initialize(w, h, p, p.dataPath(""));
  }

  public Layer(int w, int h, PApplet p) {
    initialize(w, h, p, p.dataPath(""));
  }

  public Layer(int w, int h, PApplet p, String s) {
    initialize(w, h, p, s);
  }

  public void initialize(int w, int h, PApplet p, String s) {
    setParent(p);
    setPrimary(false);
    setPath(s);
    setSize(w, h);
    limits.setLeft(0);
    limits.setRight(w);
    limits.setTop(0);
    limits.setBottom(h);
    center = new PVector(this.width/2, this.height/2);
  }

  protected PApplet getEnclosingPApplet() {
    try {
      return (PApplet) getClass()
        .getDeclaredField("this$0").get(this);
    }

    catch (ReflectiveOperationException cause) {
      throw new RuntimeException(cause);
    }
  }

  @Override public String toString() {
    return "Width: " + width + "\t Height: " + height
      + "\nPath:  " + path;
  }

  public boolean isWithinLimits(PVector p) {
    if (p.x >= limits.left && p.x <= limits.right && p.y >= limits.top && p.y <= limits.bottom) return true;
    else return false;
  }

  public boolean isWithinLimits(int px, int py) {
    if (px >= limits.left && px <= limits.right && py >= limits.top && py <= limits.bottom) return true;
    else return false;
  }

  public boolean isWithinLimits(float px, float py) {
    if (px >= limits.left && px <= limits.right && py >= limits.top && py <= limits.bottom) return true;
    else return false;
  }

  public void setLimits(int margin) {
    //add margin to all limits (negative value expands, positive contracts)
    limits.setLeft(margin);
    limits.setTop(margin);
    limits.setRight(this.width-margin);
    limits.setBottom(this.height-margin);
  }
}

class Limits {
  int left, right, top, bottom;

  Limits() {
    left = 0;
    right = 0;
    top = 0;
    bottom = 0;
  }
  public void setLeft(int l) {
    left = l;
  }
  public void setRight(int r) {
    right = r;
  }
  public void setTop(int t) {
    top = t;
  }
  public void setBottom(int b) {
    bottom = b;
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
  public void appendText(String input) {
    current_log += " " + input;
  }
}
//function for formatting int values as strings: 1 becomes "01", 2 becomes "02"
public String zeroFormat(int input) {
  String output = Integer.toString(input);
  if (input < 10) output = "0" + output;
  return output;
}
class Mandala {
  Group controlGroup;
  String name;
  int current_graphics = 0;

  float w_s; //wigglespeed
  float w_a; //wiggle amount
  float wiggle;

  int max_n = 50;
  int n = 15; // iterations
  float divs; // n angle divisions on circle

  float m_scale; //master scale for all graphics
  float a_scale = 1.0f; //scale to be animated on chooseGraphics()

  float r_s; //rotation speed
  float r = 0; //rotation value
  float orientation;

  PVector anchor; //Mandala anchor (point of origin)
  float distance;
  float d_norm = 0.5f; //normalized distance of p (position) from anchor to d_m
  float d_s = 10; //speed of movement between anchor and d_max
  float d_max; //maximum distance possible within canvas
  float d_limit;
  float[] d_limits;
  float mod_freq, mod_amount, mod_time = 0, mod_rate;

  // graphics, iterations, mandala rotation, graphic angle, wiggle amount,
  Mandala(String _name) {
    name = _name;
    d_limits = noiseArray(n, 300);
    d_max = calcHypotenuse(c.width/2, c.height/2);
    anchor = c.center;
    divs = TWO_PI/(float)n;

    cp5A.goToNextColorScheme();
    controlGroup = cp5.addGroup(name)
    .setPosition(cp5A.getAnchor().x, cp5A.getAnchor().y)
    .setWidth(cp5A.groupwidth)
    .activateEvent(true)
    .setBackgroundColor(cp5A.getBackgroundColor())
    .setLabel(name)
    ;

    cp5A.addXY(5, 5);
    cp5.addSlider(name + "/" + "n")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(5, max_n-1)
    .plugTo( this, "setN" )
    .setValue(n)
    .setLabel("n")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "n");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "scale")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(0.0f, 2.0f )
    .plugTo( this, "setScale" )
    .setValue( 1.0f )
    .setLabel("scale")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "scale");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "rotation_speed")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-100, 100 )
    .plugTo( this, "setRotationSpeed" )
    .setValue( 0 )
    .setLabel("rotation_speed")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "rotation_speed");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "orientation")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-PI, PI )
    .plugTo( this, "setOrientation" )
    .setValue( 0.0f )
    .setLabel("orientation")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "orientation");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "distance_speed")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-100, 100 )
    .plugTo( this, "setDistanceSpeed" )
    .setValue( 0 )
    .setLabel("distance_speed")
    .setGroup(controlGroup)
    .setId(0)
    ;
    cp5A.style1(name + "/" + "distance_speed");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "distance_limit")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(0.0f, 0.75f )
    .plugTo( this, "setDistanceLimit" )
    .setValue( 0.25f )
    .setLabel("distance_limit")
    .setGroup(controlGroup)
    .setId(0)
    ;
    cp5A.style1(name + "/" + "distance_limit");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "mod_freq")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(0.0f, 10 )
    .plugTo( this, "setModulationFrequency" )
    .setValue( 0 )
    .setLabel("mod_freq")
    .setGroup(controlGroup)
    .setId(0)
    ;
    cp5A.style1(name + "/" + "mod_freq");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "mod_amount")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(0.0f, 0.5f )
    .plugTo( this, "setModulationAmount" )
    .setValue( 0.0f )
    .setLabel("mod_amount")
    .setGroup(controlGroup)
    .setId(0)
    ;
    cp5A.style1(name + "/" + "mod_amount");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "mod_rate")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-0.5f, 0.5f )
    .plugTo( this, "setModulationRate" )
    .setValue( 0.0f )
    .setLabel("mod_rate")
    .setGroup(controlGroup)
    .setId(0)
    ;
    cp5A.style1(name + "/" + "mod_rate");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addScrollableList(name + "/" + "graphics")
    .setPosition(cp5A.x, cp5A.y)
    .addItem("carrots", 0)
    .addItem("leaves", 1)
    .addItem("bushels", 2)
    .addItem("flowers", 3)
    .addItem("mexiko", 4)
    .addItem("fish", 5)
    .addItem("members", 6)
    .addItem("marius", 7)
    .addItem("mia", 7)
    .addItem("sparrows", 8)
    .setValue(0)
    .plugTo(this, "scaleDownChangeGraphics")
    .setLabel("choose graphics")
    .setGroup(controlGroup)
    .setType(ControlP5.LIST)
    .open()
    ;
    //cp5A.style1(name + "/" + "graphics");


    controlGroup.setBackgroundHeight(cp5A.groupheight);
    cp5A.setXY(0,0); //reset xy for next group of controls
    cp5A.goToNextAnchor(); //move to next anchor for next group of controls
  }

  public void setScale(float input) {
    m_scale = input;
  }

  public void setRotationSpeed(int input) {
    r_s = map(input,-100.f, 100.f, -0.01f, 0.01f);
  }

  public void setOrientation(float input) {
    orientation = input;
  }

  public void setDistanceSpeed(int input) {
    d_s = map(input,-100.f, 100.f, -0.01f, 0.01f);
  }
  public void setDistanceLimit(float input) {
    d_limit = input*d_max;
    d_limits = noiseArray(max_n, d_limit*.5f, d_limit);
  }

  public void setN(int input) {
    n = input + (input%mandala_graphics[current_graphics].length); //adds remainder needed to maintain alternating pattern
    divs = TWO_PI/(float)n;
  }

  public void setModulationFrequency(int input) {
    if (input != mod_freq) Ani.to(this, abs(input-mod_freq), "mod_freq", input, Ani.SINE_IN_OUT);
  }

  public void setModulationAmount(float input) {
    mod_amount = input*d_max;
  }

  public void setModulationRate(float input) {
    mod_rate = input;
  }

  public void chooseGraphics(int input) {
    current_graphics = input;
  }

  public void scaleDown() {
    Ani.to(this, 2.0f, "a_scale", 0.0f, Ani.QUAD_IN, "onEnd:scaleUp");
  }
  public void scaleDownChangeGraphics(int input) {
    //if the input different from current_graphics and no animation is in progress
    if (input != current_graphics) {
      //animate the scale and callback to scaleUp
      Ani.to(this, 1.0f, "a_scale", 0.0f, Ani.QUAD_IN, "onEnd:scaleUpChangeGraphics");
    }
  }

  public void scaleUp() {
    Ani.to(this, 1.0f, "a_scale", 1.0f, Ani.QUAD_IN);
  }
  public void scaleUpChangeGraphics() {
    Ani.to(this, 1.0f, "a_scale", 1.0f, Ani.QUAD_IN);
    current_graphics = (int)cp5.getController(name + "/graphics").getValue();
  }

  public void update() {
    r = rollOver(r+r_s, 0, TWO_PI); //rotate mandala
    //move mandala "ring" inwards/outwards
    d_norm = rollOver(d_norm+d_s, 0.0f, 1.0f);
    mod_time = rollOver(mod_time+mod_rate, 0, TWO_PI);
    //anchor = mapXYToCanvas(mouseX, mouseY, vp, c);
    m_scale = cp5.getController(name + "/" + "scale").getValue()*a_scale;
  }

  public void display() {
    if (m_scale > 0) {
      PVector p = new PVector(0, 0, 0); //position of each graphic
      int g_i = 0; //counter for choosing graphic from g_array
      float s; // graphics scale for each graphic
      float d; //distance value for each graphic
      float pda; //distance from p to anchor

      //draw graphics on path
      for (int i = 0; i<n; i++) { //for every n
        s = m_scale; //set to original scale
        d = d_norm*d_max; //set original distance
        d += sin(i*divs*mod_freq + mod_time) *mod_amount; //modulate distance
        d = rollOver(d, 0, d_max); //rollover value so it stays within d_max

        //calculate position of p on path
        p = PVectorOnCircularPath(i*divs+r, d);

        //anchor is added to p, as we need to calculate if p is inside the limits
        p.add(anchor);

        //scale each graphic down when near anchor or max_d
        pda = p.dist(anchor);

        if (pda>5){ //if graphic is more than 15 pixels {
          if (pda < d_limits[i]) s *= pda/d_limits[i];
          else if (pda > d_max-d_limits[i]) s *= map(pda, d_max-d_limits[i], d_max, 1.f, 0.f);

          //graphics will only be processed if p is inside the limits
          if (c.isWithinLimits(p)) {
            //to calculate the angle of each graphic, we make another PVector with a slight offset on the path
            PVector pp = PVectorOnCircularPath(i*divs+r+.25f, d);
            pp.add(anchor);
            float angle = atan2(p.y-pp.y, p.x-pp.x);

            //draw graphics, orient along path
            c.pushMatrix();
            c.translate(p.x, p.y);
            c.rotateZ(angle+orientation);
            PImage img = mandala_graphics[current_graphics][g_i];
            c.image(img, 0, 0, img.width*s, img.height*s);
            c.popMatrix();
          }
        }
        /*
        the graphic displayed at each p of the mandala is chosen by g_i.
        Every time a new graphic has been put into the mandala, the counter increases by 1.
        There's a rollover g_i is larger than the number of items in g_array. */
        g_i = rollOver(g_i+1, 0, mandala_graphics[current_graphics].length);
      }
    }
  }
}

//rollover of float value between a lower and upper value
public float rollOver(float input, float edge0, float edge1) {
  float out = input;
  if (input > edge1) out = edge0 + input -edge1;
  else if(input <= edge0) out = edge1 + input -edge0;
  return out;
}

public int rollOver(int input, int edge0, int edge1) {
  int out = input;
  if (input >= edge1) out = edge0 + input - edge1;
  else if(input <= edge0) out = edge1 + input - edge0;
  return out;
}

public PVector PVectorOnCircularPath(float angle, float distance) {
  float x = cos(angle) * distance;
  float y = sin(angle) * distance;
  return new PVector(x, y);
}

public float calcHypotenuse(float a, float b) {
  float c = sqrt(pow(a, 2) + pow(b, 2));
  return c;
}

//returns array of n normalized perlin noise values
public float[] noiseArray(int _n) {
  float[] array = new float[_n];
  for (int i = 0; i<_n; i++) array[i] = noise(i);
  return array;
}
//same, with multiplying value
public float[] noiseArray(int _n, float multiplier) {
  float[] array = noiseArray(_n);
  for (int i = 0; i<_n; i++) array[i] *= multiplier;
  return array;
}

public float[] noiseArray(int _n, float edge0, float edge1) {
  float[] array = noiseArray(_n);
  for (int i = 0; i<_n; i++) array[i] = map(array[i], 0, 1, edge0, edge1);
  return array;
}

public float wiggleFloat(float amount, float speed) {
  return sin(millis()*speed)*amount;
}

public int wiggleInt(float amount, float speed) {
  return round(sin(millis()*speed)*amount);
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
class Ribbons {
  String name;
  PImage[] ribbon_graphics;
  int current_graphics = 0;
  float[] ribbon_angles = new float[4];
  PVector[] ribbon_anchors = new PVector[4];
  PVector offset = new PVector(0,0);
  int[] ribbon_max_ns = new int[4];
  float dir;

  float m_scale;
  float a_scale = 1.0f; //scale to be animated on chooseGraphics()
  float graphics_width;
  float graphics_height;

  Group controlGroup;

  Ribbons(String _name) {
    name = _name;
    PImage r1 = loadImage("ribbon01.png");
    PImage r2 = loadImage("ribbon02.png");
    PImage r3 = loadImage("ribbon03.png");
    PImage[] _ribbon_graphics = { r1, r2, r3 };
    ribbon_graphics = _ribbon_graphics;
    calculateRibbons();
    calculateAnchors();
    calculateAngles();

    controlGroup = cp5.addGroup(name)
    .setPosition(cp5A.getAnchor().x, cp5A.getAnchor().y)
    .setWidth(cp5A.groupwidth)
    .activateEvent(true)
    .setBackgroundColor(color(255, 80))
    .setLabel(name)
    ;

    cp5A.addXY(5, 5);
    cp5.addSlider(name + "/" + "scale")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(0.0f, 1.0f )
    .plugTo( this, "setScale" )
    .setValue( .5f )
    .setLabel("scale")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "scale");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "direction")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-10.0f, 10.0f )
    .plugTo( this, "setDirection" )
    .setValue( 2.5f )
    .setLabel("direction")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "direction");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "yoffset")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(0, 200 )
    .plugTo( this, "setYOffset" )
    .setValue( 100 )
    .setLabel("yoffset")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "yoffset");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addToggle(name + "/"+ "toggle")
    .setPosition(cp5A.x, cp5A.y)
    .setSize(50,20)
    .setValue(true)
    .setLabel("toggle_ribbons")
    .setMode(ControlP5.SWITCH)
    .plugTo(this, "toggleRibbons")
    .setGroup(controlGroup)
    ;

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addScrollableList(name + "/" + "graphics")
    .setPosition(cp5A.x, cp5A.y)
    .addItem("flowers", 0)
    .addItem("tangents", 1)
    .addItem("roses", 2)
    .setValue(0)
    .plugTo(this, "scaleDownChangeGraphics")
    .setLabel("choose graphics")
    .setGroup(controlGroup)
    .setType(ControlP5.LIST)
    .open()
    ;

    controlGroup.setBackgroundHeight(cp5A.groupheight);
    cp5A.setXY(0,0); //reset xy for next group of controls
    cp5A.goToNextAnchor(); //move to next anchor for next group of controls
  }

  public void update() {
    offset.x = rollOver(offset.x+dir, 0, graphics_width);
  }

  public void chooseGraphics(int input) {
    current_graphics = input;
  }

  public void scaleDownChangeGraphics(int input) {
    //if the input different from current_graphics and no animation is in progress
    if (input != current_graphics) {
      //animate the scale and callback to scaleUp
      Ani.to(this, 1.0f, "a_scale", 0.0f, Ani.QUAD_IN, "onEnd:scaleUpChangeGraphics");
    }
  }
  public void scaleUpChangeGraphics() {
    Ani.to(this, 1.0f, "a_scale", 1.0f, Ani.QUAD_IN);
    current_graphics = (int)cp5.getController(name + "/graphics").getValue();
  }

  public void calculateAnchors() {
    for (int i = 0; i<4; i++) {
      switch(i) {
        case 0: ribbon_anchors[0] = new PVector(0,0);
        case 1: ribbon_anchors[1] = new PVector(c.width,0);
        case 2: ribbon_anchors[2] = new PVector(c.width, c.height);
        case 3: ribbon_anchors[3] = new PVector(0, c.height);
      }
    }
  }

  public void calculateAngles() {
    for (int i = 0; i<4; i++) {
      ribbon_angles[i] = i*HALF_PI;
    }
  }

  public void calculateRibbons() {
    PImage r = ribbon_graphics[current_graphics];
    graphics_width = r.width*m_scale;
    graphics_height = r.height*m_scale;
    for (int i = 0; i<4; i++) {
      ribbon_max_ns[i] = ceil(max(c.width, c.height)/graphics_width)+1;
    }
  }

  public void setScale(float input) {
    m_scale = input;
    calculateRibbons();
  }

  public void setYOffset(int input) {
    offset.y = input;
  }

  public void setDirection(float input){
    dir = input;
  }

  public void toggleRibbons(boolean theState) {
    if (!theState) Ani.to(this.offset, 5.0f, "y", -offset.y-graphics_height, Ani.BACK_IN);
    else Ani.to(this.offset, 5.0f, "y", cp5.getController(name+"/yoffset").getValue(), Ani.BACK_OUT);
  }

  public void display(){
    for (int i = 0; i<4; i++){
      c.pushMatrix();
      c.translate(ribbon_anchors[i].x, ribbon_anchors[i].y);
      c.rotate(ribbon_angles[i]);
      for (int n = -1; n<ribbon_max_ns[i]; n++) {
        c.image(ribbon_graphics[current_graphics],graphics_width*n+offset.x,offset.y, graphics_width, graphics_height*a_scale);
      }
      c.popMatrix();
    }
    c.pushMatrix();
    c.translate(ribbon_anchors[0].x, ribbon_anchors[0].y);
    c.rotate(ribbon_angles[0]);//kind of redundant
    c.image(ribbon_graphics[current_graphics], offset.x, offset.y, graphics_width, graphics_height*a_scale);
    c.image(ribbon_graphics[current_graphics], -graphics_width+offset.x, offset.y, graphics_width, graphics_height*a_scale);
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

  Viewport(int vsize, int vpx, int vpy) {
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

  public void update(PGraphics pg) {
    int[] dims = scaleToFit(pg.width, pg.height, size, size);
    canvas_offset = new PVector(dims[0], dims[1]);
    canvas_width = dims[2];
    canvas_height = dims[3];
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
public void controlSetup() {
  cp5 = new ControlP5(this);
  int xoff = 10;
  int yoff = 20;

  //cp5.setColorForeground(cp5A.getForegroundColor());
  //cp5.setColorBackground(cp5A.getBackgroundColor());
  //cp5.setColorActive(cp5A.getActiveConColor());

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
    c = new Layer(cw, ch);
    vp.update(c);
  }
}
public void field_ch(String theText) {
  int value = evalFieldInput1(theText, ch, cp5.getController("field_ch"));
  if (value > 0) {
    ch = value;
    c = new Layer(cw, ch);
    vp.update(c);
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
/*
void keyPressed() {
  // default properties load/save key combinations are
  // alt+shift+l to load properties
  // alt+shift+s to save properties
  if (key=='1') {
    cp5.saveProperties(("hello.properties"));
  }
  else if (key=='2') {
    cp5.loadProperties(("hello.properties"));
  }
}
*/
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

  mexiko[0] = loadImage("mexiko01.png");
  mexiko[1] = loadImage("mexiko02.png");

  fish[0] = loadImage("fish01.png");
  fish[1] = loadImage("fish02.png");

  members[0] = loadImage("Lendal.png");
  members[1] = loadImage("Mondrup.png");

  marius[0] = loadImage("Marius.png");

  mia[0] = loadImage("mia.png");

  mia_ring = loadImage("mia_ring.png");
  mia_emblem[0] = mia[0];
  mia_emblem[1] = mia_ring;

  logo_name = loadImage("vaev_logo.png");
  logo_star = loadImage("vaev_logo_star.png");
  logo[0] = logo_name;
  logo[1] = logo_star;

  skovdyr_emblem = loadImage("skovdyr_emblem.png");
  skovdyr_ring = loadImage("skovdyr_ring.png");
  skovdyr[0] = skovdyr_emblem;
  skovdyr[1] = skovdyr_ring;

  rummelpot_emblem = loadImage("rummelpot_emblem.png");
  rummelpot_ring = loadImage("rummelpot_ring.png");
  rummelpot[0] = rummelpot_emblem;
  rummelpot[1] = rummelpot_ring;

  jomfru = loadImage("rummelpot_jomfru.png");
  hollaender = loadImage("rummelpot_hollaender.png");
  potflag = loadImage("rummelpot_potflag.png");
  rummelpotjomfru[0] = hollaender;
  rummelpotjomfru[1] = jomfru;
  rummelpotjomfru[2] = potflag;

  sparrows[0] = loadImage("sparrow01.png");
  sparrows[1] = loadImage("sparrow02.png");
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
  if (log_osc) log.setText(txt);
  Controller con;
  if (str_in[1].equals(osc_address)) {
    // parse osc_address/controllername/value
    if (str_in.length == 3) {
      if (cp5.getController(str_in[2]) != null &&
      cp5.getController(str_in[2]).getId() != -1)
      {
        con = cp5.getController(str_in[2]);
        setControllerValueWithOSC(con, theOscMessage);
      }
    }
    // parse osc_address/groupname/controllername/value
    //stupid hotfixed way of going about this
    else if (str_in.length == 4) {
      String parsed_name = str_in[2] + "/" + str_in[3];
      if (cp5.getController(parsed_name) != null &&
      cp5.getGroup(str_in[2]).getController(parsed_name).getId() != -1)
      {
        con = cp5.getController(parsed_name);
        setControllerValueWithOSC(con, theOscMessage);
      }
    }
  }
}

public void setControllerValueWithOSC(Controller con, OscMessage theOscMessage) {
  if (theOscMessage.checkTypetag("i")) {
    int value = theOscMessage.get(0).intValue();
    value = constrain(value, (int)con.getMin(), (int)con.getMax());
    con.setValue(value);
    log.appendText("int value: " + Integer.toString(value));
  }

  if (theOscMessage.checkTypetag("f")) {
    float value = theOscMessage.get(0).floatValue();
    value = constrain(value, con.getMin(), con.getMax());
    con.setValue(value);
    log.appendText(" float value: " + Float.toString(value));
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
