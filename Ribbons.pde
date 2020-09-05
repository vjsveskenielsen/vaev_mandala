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
  float a_scale = 1.0; //scale to be animated on chooseGraphics()
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
    .setRange(0.0, 1.0 )
    .plugTo( this, "setScale" )
    .setValue( .5 )
    .setLabel("scale")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "scale");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "direction")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-10.0, 10.0 )
    .plugTo( this, "setDirection" )
    .setValue( 2.5 )
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

  void update() {
    offset.x = rollOver(offset.x+dir, 0, graphics_width);
  }

  void chooseGraphics(int input) {
    current_graphics = input;
  }

  void scaleDownChangeGraphics(int input) {
    //if the input different from current_graphics and no animation is in progress
    if (input != current_graphics) {
      //animate the scale and callback to scaleUp
      Ani.to(this, 1.0, "a_scale", 0.0, Ani.QUAD_IN, "onEnd:scaleUpChangeGraphics");
    }
  }
  void scaleUpChangeGraphics() {
    Ani.to(this, 1.0, "a_scale", 1.0, Ani.QUAD_IN);
    current_graphics = (int)cp5.getController(name + "/graphics").getValue();
  }

  void calculateAnchors() {
    for (int i = 0; i<4; i++) {
      switch(i) {
        case 0: ribbon_anchors[0] = new PVector(0,0);
        case 1: ribbon_anchors[1] = new PVector(c.width,0);
        case 2: ribbon_anchors[2] = new PVector(c.width, c.height);
        case 3: ribbon_anchors[3] = new PVector(0, c.height);
      }
    }
  }

  void calculateAngles() {
    for (int i = 0; i<4; i++) {
      ribbon_angles[i] = i*HALF_PI;
    }
  }

  void calculateRibbons() {
    PImage r = ribbon_graphics[current_graphics];
    graphics_width = r.width*m_scale;
    graphics_height = r.height*m_scale;
    for (int i = 0; i<4; i++) {
      ribbon_max_ns[i] = ceil(max(c.width, c.height)/graphics_width)+1;
    }
  }

  void setScale(float input) {
    m_scale = input;
    calculateRibbons();
  }

  void setYOffset(int input) {
    offset.y = input;
  }

  void setDirection(float input){
    dir = input;
  }

  void toggleRibbons(boolean theState) {
    if (!theState) Ani.to(this.offset, 5.0, "y", -offset.y-graphics_height, Ani.BACK_IN);
    else Ani.to(this.offset, 5.0, "y", cp5.getController(name+"/yoffset").getValue(), Ani.BACK_OUT);
  }

  void display(){
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
