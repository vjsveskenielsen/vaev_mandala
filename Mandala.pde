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
  float d; //distance to center
  float r_s; //normalized rotation speed
  float r = 0;

  PVector anchor;
  float divs; // angle offset for each graphic
  float max_d;
  float dir = 0;
  PVector p = new PVector(0, 0);


  // graphics, iterations, mandala rotation, graphic angle, wiggle amount,
  Mandala(PImage[] _g_array, int _n, float _r_s, float _a, float _w_a, float _w_s, int _d, float _g_s) {
    n = _n;
    w_s = _w_s;
    w_a = _w_a;
    divs = TWO_PI/n; // set angle offset
    a = _a;
    d = _d;
    g_s = _g_s;
    r_s = _r_s;
    g_array = _g_array;
  }

  void update() {
    if (r >= TWO_PI) r = 0;
    else if (r < 0) r = TWO_PI;
    else r += r_s;
  }

  void display() {
    /*
    c.pushMatrix();
    c.translate(c.width*.5, c.height*.5); // move to center of screen
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
    */

    anchor = new PVector(c.width/2, c.height/2);
    for (int i = 0; i<n; i++) {
      d = 200+sin(i+r)*50; //oscillate distance value to create wavy mandalas
      p = PVectorOnCircularPath(i*divs+r, d); //calculate path with p
      //anchor is added to p, as we need to calculate if
      p.add(anchor);
      //graphics will only be processed if inside the limit area
      if (p.x >= lim_l && p.x <= lim_r && p.y >= lim_t && p.y <= lim_b) {
        //to calculate the angle, we make another PVector with a slight offset on the path
        PVector pp = PVectorOnCircularPath(i*divs+dir+.25, d);
        pp.add(anchor);
        float angle = atan2(p.y-pp.y, p.x-pp.x);

        //draw graphics, orient along path
        c.pushMatrix();
        c.translate(p.x, p.y);
        c.rotate(angle);
        c.fill(255);
        c.rect(0, 0, 100, 100);
        c.point(0, 15);
        c.popMatrix();
      }
    }
  }
}

PVector PVectorOnCircularPath(float angle, float distance) {
  float x = cos(angle) * distance;
  float y = sin(angle) * distance;
  return new PVector(x, y);
}

float calcHypotenuse(float a, float b) {
  float c = sqrt(pow(a, 2) + pow(b, 2));
  return c;
}
