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
  float a; // angle

  float r_s; //normalized rotation speed
  float r = 0; //rotation value

  PVector anchor; //Mandala anchor (point of origin)
  float d = 0; //normalized distance of p (position) from anchor to d_m
  float d_s = 10; //speed of movement between anchor and d_max
  float d_max; //maximum distance possible within canvas
  float[] d_limits;

  // graphics, iterations, mandala rotation, graphic angle, wiggle amount,
  Mandala(String name, PImage[] _g_array, int _n, float _r_s, float _a, float _w_a, float _w_s, int _d, float _g_s) {
    n = _n;
    w_s = _w_s;
    w_a = _w_a;
    divs = TWO_PI/n;
    a = _a;
    d = _d;
    r_s = _r_s;
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

    cp5A.goToNextAnchor();
    cp5A.addXY(0, cp5A.margin);
    cp5.addSlider(name + "_" + "scale")
    .setPosition(cp5A.x, cp5A.y)
    .setRange(0.0, 2.0 )
    .plugTo( this, "setScale" )
    .setValue( 1.0 )
    .setLabel("scale")
    .setGroup(controlGroup)
    ;
    cp5A.addXY(0, cp5A.sliderheight);

    controlGroup.setBackgroundHeight(cp5A.groupheight);
    cp5A.setXY(0,0); //reset xy for next group of controls
  }

  void setScale(float input) {
    scale = input;
  }

  void update() {
    r = rollOver(r+r_s, 0, TWO_PI); //rotate mandala
    d = rollOver(d+d_s, 0.0, d_max); //move mandala inwards/outwards
    //anchor = mapXYToCanvas(mouseX, mouseY, vp, c);
  }

  void display() {
    PVector p = new PVector(0, 0); //position of each graphic
    int g_i = 0; //counter for choosing graphic from g_array
    float s = scale; // graphics scale for each graphic
    //draw graphics on path
    for (int i = 0; i<n; i++) { //for every n
      float _d = d; //create local distance value (for further math fuckery)
      //oscillate distance (positive only) value to create wavy mandalas
      _d += abs(sin(i+r-1)*20);

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
        PVector pp = PVectorOnCircularPath(i*divs+r+.25, d);
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
