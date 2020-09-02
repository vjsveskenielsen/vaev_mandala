class Emblem {
  Group controlGroup;
  String name;
  int current_graphics = 0;

  float m_scale; //master scale for all graphics
  float a_scale = 1.0; //scale to be animated on chooseGraphics()

  float r_s; //rotation speed
  float r = 0; //rotation value
  float orientation;
  PVector anchor;

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
    cp5.addSlider(name + "/" + "rotation_speed")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-0.01, 0.01 )
    .plugTo( this, "setRotationSpeed" )
    .setValue( 1.0 )
    .setLabel("rotation_speed")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "rotation_speed");


    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addScrollableList(name + "/" + "graphics")
    .setPosition(cp5A.x, cp5A.y)
    .addItem("skovdyr", 0)
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

  void setRotationSpeed(float input) {
    r_s = input;
  }

  void chooseGraphics(int input) {
    current_graphics = input;
  }

  void scaleDown() {
    Ani.to(this, 2.0, "a_scale", 0.0, Ani.QUAD_IN, "onEnd:scaleUp");
  }
  void scaleDownChangeGraphics(int input) {
    //if the input different from current_graphics and no animation is in progress
    if (input != current_graphics) {
      //animate the scale and callback to scaleUp
      Ani.to(this, 1.0, "a_scale", 0.0, Ani.QUAD_IN, "onEnd:scaleUpChangeGraphics");
    }
  }

  void scaleUp() {
    Ani.to(this, 1.0, "a_scale", 1.0, Ani.QUAD_IN);
  }
  void scaleUpChangeGraphics() {
    Ani.to(this, 1.0, "a_scale", 1.0, Ani.QUAD_IN);
    current_graphics = (int)cp5.getController(name + "/graphics").getValue();
  }

  void update() {
    m_scale = cp5.getController(name + "/" + "scale").getValue()*a_scale;
    r = rollOver(r+r_s, 0, TWO_PI); //rotate mandala
  }

  void display() {
    c.pushMatrix();
    c.translate(anchor.x, anchor.y);
    c.imageMode(CENTER);
    PImage img;
    for (int i = emblem_graphics[current_graphics].length-1; i>-1; i--) {
      println(i);
      img = emblem_graphics[current_graphics][i];
      c.image(img, 0,0, img.width*m_scale, img.height*m_scale);
    }
    c.popMatrix();
  }

}
