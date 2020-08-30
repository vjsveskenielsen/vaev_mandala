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
    .setRange(0.0, 2.0 )
    .plugTo( this, "setScale" )
    .setValue( scale )
    .setLabel("scale")
    .setGroup(controlGroup);
    ;
    controlGroup.setBackgroundHeight(cp5A.groupheight);
    cp5A.setXY(0,0); //reset xy for next group of controls
    cp5A.goToNextAnchor(); //move to next anchor for next group of controls
  }

  void setScale(float input){
    scale = input;
  }
  void calculateAnchors() {
    for (int i = 0; i<4; i++) {
      switch(i) {
        case 0: corner_anchors[0] = new PVector(-50,-50);
        case 1: corner_anchors[1] = new PVector(c.width+50,-50);
        case 2: corner_anchors[2] = new PVector(c.width+50, c.height+50);
        case 3: corner_anchors[3] = new PVector(-50, c.height+50);
      }
    }
  }

  void calculateAngles() {
    float a = 0;
    for (int i = 0; i<corner_angles.length; i++) {
      corner_angles[i] = a + HALF_PI*i;
    }
  }

  void update() {

  }

  void display(){
    c.imageMode(CORNER);
    for (int i = 0; i<corner_anchors.length; i++) {
      c.pushMatrix();
      c.translate(corner_anchors[i].x, corner_anchors[i].y);
      c.rotate(corner_angles[i]);
      for (int j = 1; j<bushels.length+1; j++){
        int index = bushels.length-j;
        c.rotate(-0.1 + 0.05*i);
        c.rotate(wiggleFloat(.05, .001*index));
        c.image(bushels[index], 0, 0, bushels[index].width*scale, bushels[index].height*scale);
      }
      c.popMatrix();
    }
  }
}
