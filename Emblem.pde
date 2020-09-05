class Emblem {
  Group controlGroup;
  String name;
  int current_graphics = 0;

  float m_scale; //master scale for all graphics
  float a_scale = 1.0; //scale to be animated on chooseGraphics()

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
    .setRange(0.0, 2.0 )
    .plugTo( this, "setScale" )
    .setValue( 1.0 )
    .setLabel("scale")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "scale");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "r0_rotation_speed")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-0.01, 0.01 )
    .plugTo( this, "r0_s" )
    .setValue( 1.0 )
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
    .setRange(-0.01, 0.01 )
    .plugTo( this, "r1_s" )
    .setValue( 1.0 )
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

  void setScale(float input) {
    m_scale = input;
  }

  void chooseGraphics(int input) {
    current_graphics = input;
  }
  void moveInOut() {
    if (anchor_offset > 0) {
      moveIn();
    }
    else {
      moveOut();
    }
  }

  void moveOut() {
    Ani.to(this, 3.0, "anchor_offset", c.height, Ani.QUAD_IN);
  }

  void moveIn() {
    Ani.to(this, 3.0, "anchor_offset", 0, Ani.QUAD_OUT);
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

  void update() {
    m_scale = cp5.getController(name + "/" + "scale").getValue()*a_scale;
    r0 = rollOver(r0+r0_s, 0, TWO_PI);
    r1 = rollOver(r1+r1_s, 0, TWO_PI);
  }

  void display() {
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
        else c.rotate(sin(r0)*.25);
        break;
        case 1:
        if (r1_rotate_wiggle) c.rotateZ(r1);
        else c.rotate(sin(r1)*.25);
        break;
        case 2:
        if (r0_rotate_wiggle) c.rotateZ(r0);
        else c.rotate(sin(r0)*.25);
        break;
      }
      c.image(img, 0,0, img.width*m_scale, img.height*m_scale);
      c.popMatrix();
    }
    c.popMatrix();
  }
}
