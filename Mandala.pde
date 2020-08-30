class Mandala {
  Group controlGroup;
  String name;
  int current_graphics = 0;

  float w_s; //wigglespeed
  float w_a; //wiggle amount
  float wiggle;

  int max_n = 50;
  int n; // iterations
  float divs; // n angle divisions on circle

  float m_scale; //master scale for all graphics
  float a_scale = 1.0; //scale to be animated on chooseGraphics()

  float r_s; //rotation speed
  float r = 0; //rotation value
  float orientation;

  PVector anchor; //Mandala anchor (point of origin)
  float distance;
  float d_norm = 0.5; //normalized distance of p (position) from anchor to d_m
  float d_s = 10; //speed of movement between anchor and d_max
  float d_max; //maximum distance possible within canvas
  float d_limit;
  float[] d_limits;
  float mod_freq, mod_amount, mod_time = 0, mod_rate;

  MyControlListener listener;
  RadioButton radio;

  // graphics, iterations, mandala rotation, graphic angle, wiggle amount,
  Mandala(String _name) {
    name = _name;
    divs = TWO_PI/n;
    d_limits = noiseArray(n, 300);
    d_max = calcHypotenuse(c.width/2, c.height/2);
    anchor = c.center;

    listener = new MyControlListener();

    controlGroup = cp5.addGroup(name)
    .setPosition(cp5A.getAnchor().x, cp5A.getAnchor().y)
    .setWidth(cp5A.groupwidth)
    .activateEvent(true)
    .setBackgroundColor(color(255, 80))
    .setLabel(name)
    ;

    cp5A.addXY(5, 5);
    cp5.addScrollableList(name + "/" + "graphics")
    .setPosition(cp5A.x, cp5A.y)
    .addItem("carrots", 0)
    .addItem("leaves", 1)
    .addItem("bushels", 2)
    .addItem("flowers", 3)
    .setValue(0)
    .plugTo(this, "scaleDownChangeGraphics")
    .setLabel("choose graphics")
    .setGroup(controlGroup)
    .setType(ControlP5.LIST)
    .open()
    ;
    //cp5A.style1(name + "/" + "graphics");

    cp5A.addXY(0, cp5A.margin + 60);
    cp5.addSlider(name + "/" + "n")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(5, max_n-1)
    .plugTo( this, "setN" )
    .setValue(15)
    .setLabel("n")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "n");


    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
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
    cp5.addSlider(name + "/" + "orientation")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-PI, PI )
    .plugTo( this, "setOrientation" )
    .setValue( 0.0 )
    .setLabel("orientation")
    .setGroup(controlGroup)
    ;
    cp5A.style1(name + "/" + "orientation");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "distance_speed")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-0.01, 0.01 )
    .plugTo( this, "setDistanceSpeed" )
    .setValue( 0.0 )
    .setLabel("distance_speed")
    .setGroup(controlGroup)
    .setId(0)
    ;
    cp5A.style1(name + "/" + "distance_speed");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "distance_limit")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(0.0, 0.75 )
    .plugTo( this, "setDistanceLimit" )
    .setValue( 0.25 )
    .setLabel("distance_limit")
    .setGroup(controlGroup)
    .setId(0)
    ;
    cp5A.style1(name + "/" + "distance_limit");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "mod_freq")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(0.0, 10 )
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
    .setRange(0.0, 0.5 )
    .plugTo( this, "setModulationAmount" )
    .setValue( 0.0 )
    .setLabel("mod_amount")
    .setGroup(controlGroup)
    .setId(0)
    ;
    cp5A.style1(name + "/" + "mod_amount");

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "/" + "mod_rate")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-0.5, 0.5 )
    .plugTo( this, "setModulationRate" )
    .setValue( 0.0 )
    .setLabel("mod_rate")
    .setGroup(controlGroup)
    .setId(0)
    ;
    cp5A.style1(name + "/" + "mod_rate");

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

  void setOrientation(float input) {
    orientation = input;
  }

  void setDistanceSpeed(float input) {
    d_s = input;
  }

  void setDistanceLimit(float input) {
    d_limit = input*d_max;
    d_limits = noiseArray(max_n, d_limit*.5, d_limit);
  }

  void setN(int input) {
    n = input + (input%mandala_graphics[current_graphics].length); //adds remainder needed to maintain alternating pattern
    divs = TWO_PI/n;
  }

  void setModulationFrequency(int input) {
    if (input != mod_freq) Ani.to(this, abs(input-mod_freq), "mod_freq", input, Ani.SINE_IN_OUT);
  }

  void setModulationAmount(float input) {
    mod_amount = input*d_max;
  }

  void setModulationRate(float input) {
    mod_rate = input;
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
    r = rollOver(r+r_s, 0, TWO_PI); //rotate mandala
    //move mandala "ring" inwards/outwards
    d_norm = rollOver(d_norm+d_s, 0.0, 1.0);
    mod_time = rollOver(mod_time+mod_rate, 0, TWO_PI);
    //anchor = mapXYToCanvas(mouseX, mouseY, vp, c);
    m_scale = cp5.getController(name + "/" + "scale").getValue()*a_scale;
  }

  void display() {
    if (m_scale > 0) {
      PVector p = new PVector(0, 0); //position of each graphic
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
          else if (pda > d_max-d_limits[i]) s *= map(pda, d_max-d_limits[i], d_max, 1., 0.);

          //graphics will only be processed if p is inside the limits
          if (c.isWithinLimits(p)) {
            //to calculate the angle of each graphic, we make another PVector with a slight offset on the path
            PVector pp = PVectorOnCircularPath(i*divs+r+.25, d);
            pp.add(anchor);
            float angle = atan2(p.y-pp.y, p.x-pp.x);

            //draw graphics, orient along path
            c.pushMatrix();
            c.translate(p.x, p.y);
            c.rotate(angle+orientation);
            c.image(mandala_graphics[current_graphics][g_i], 0, 0, mandala_graphics[current_graphics][g_i].width*s, mandala_graphics[current_graphics][g_i].height*s);
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
// hotfix for radio button that wont plug to shit
class MyControlListener implements ControlListener {
  public void controlEvent(ControlEvent theEvent) {
    println(theEvent.getController().getValue());
  }

}
