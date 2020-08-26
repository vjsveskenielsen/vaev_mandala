class Mandala {
  Group controlGroup;

  PImage[] g_array;
  int g; // what graphic to use from g_array[]

  float w_s; //wigglespeed
  float w_a; //wiggle amount
  float wiggle;

  int n; // iterations
  float divs; // n angle divisions on circle

  float scale; //master scale for all graphics

  float r_s; //rotation speed
  float r = 0; //rotation value

  PVector anchor; //Mandala anchor (point of origin)
  float distance;
  float d_norm = 0.5; //normalized distance of p (position) from anchor to d_m
  float d_s = 10; //speed of movement between anchor and d_max
  float d_max; //maximum distance possible within canvas
  float[] d_limits;

  // graphics, iterations, mandala rotation, graphic angle, wiggle amount,
  Mandala(String name, PImage[] _g_array) {
    divs = TWO_PI/n;
    g_array = _g_array;
    d_limits = noiseArray(n, 300);
    d_max = calcHypotenuse(c.width/2, c.height/2);
    anchor = c.center;

    controlGroup = cp5.addGroup(name)
    .setPosition(cp5A.getAnchor().x, cp5A.getAnchor().y)
    .setWidth(cp5A.groupwidth)
    .activateEvent(true)
    .setBackgroundColor(color(255, 80))
    .setLabel(name)
    ;

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "_" + "n")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(5, 50)
    .plugTo( this, "setN" )
    .setValue(15)
    .setLabel("n")
    .setGroup(controlGroup)
    ;

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "_" + "scale")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(0.0, 2.0 )
    .plugTo( this, "setScale" )
    .setValue( 1.0 )
    .setLabel("scale")
    .setGroup(controlGroup)
    ;

    cp5A.addXY(0, cp5A.margin+cp5A.sliderheight);
    cp5.addSlider(name + "_" + "rotation_speed")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(-0.01, 0.01 )
    .plugTo( this, "setRotationSpeed" )
    .setValue( 1.0 )
    .setLabel("rotation_speed")
    .setGroup(controlGroup)
    ;

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
    cp5.getController(name + "/" + "distance_speed").getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
    cp5.getController(name + "/" + "distance_speed").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);

    controlGroup.setBackgroundHeight(cp5A.groupheight);
    cp5A.setXY(0,0); //reset xy for next group of controls
    cp5A.goToNextAnchor(); //move to next anchor for next group of controls
  }

  void setScale(float input) {
    scale = input;
  }

  void setRotationSpeed(float input) {
    r_s = input;
  }

  void setDistanceSpeed(float input) {
    d_s = input;
  }

  void setN(int input) {
    n = input;
    divs = TWO_PI/n;
    d_limits = noiseArray(n, 0);
  }

  void update() {
    r = rollOver(r+r_s, 0, TWO_PI); //rotate mandala
    d_norm = rollOver(d_norm+d_s, 0.0, 1.0);
    distance = d_norm*d_max; //move mandala inwards/outwards
    //anchor = mapXYToCanvas(mouseX, mouseY, vp, c);
  }

  void display() {
    PVector p = new PVector(0, 0); //position of each graphic
    int g_i = 0; //counter for choosing graphic from g_array
    float s = scale; // graphics scale for each graphic
    //draw graphics on path
    for (int i = 0; i<n; i++) { //for every n
      float _d = distance; //create local distance value (for further math fuckery)
      //oscillate distance (positive only) value to create wavy mandalas
      //_d += abs(sin(i+r-1)*20);

      //scale each graphic down when near anchor or max_d
      if (_d < d_limits[i]) s *= _d/d_limits[i];
      else if (_d > d_max-d_limits[i]) s *= map(_d, d_max-d_limits[i], d_max, 1.0, 0.0);

      //calculate position of p on path, with local distance to anchor
      p = PVectorOnCircularPath(i*divs+r, _d);

      //anchor is added to p, as we need to calculate if p is inside the limits
      p.add(anchor);
      //graphics will only be processed if p is inside the limits
      if (c.isWithinLimits(p)) {
        //to calculate the angle of each graphic, we make another PVector with a slight offset on the path
        PVector pp = PVectorOnCircularPath(i*divs+r+.25, _d);
        pp.add(anchor);
        float angle = atan2(p.y-pp.y, p.x-pp.x);

        //draw graphics, orient along path
        c.pushMatrix();
        c.translate(p.x, p.y);
        c.rotate(angle);
        c.image(g_array[g_i], 0, 0, g_array[g_i].width*s, g_array[g_i].height*s);
        c.popMatrix();
      }
      /*
      the graphic displayed at each p of the mandala is chosen by g_i.
      Every time a new graphic has been put into the mandala, the counter increases by 1.
      There's a rollover g_i is larger than the number of items in g_array. */
      g_i = rollOver(g_i+1, 0, g_array.length);
    }
  }
}
