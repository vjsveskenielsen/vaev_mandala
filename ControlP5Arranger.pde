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
  int groupheight = 150;

  void style1(String con_name) {
    Controller con = cp5.getController(con_name);
    if con.
    con.setHeight(cp5A.sliderheight);
    con.setWidth(sliderwidth);
    con.setId(0);
    con.getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
    con.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
  }

  //anchors[0] x, anchors[0] y, n anchors on x axis, n anchors on y axis
  ControlP5Arranger(int ax, int ay, int nx, int ny) {
    anchors = generateAnchors(nx, groupwidth+margin, ny, groupheight+margin, ax, ay);
  }

  void goToNextAnchor() {
    anchor_index++;
  }

  PVector getAnchor() {
    if (anchor_index >= 0 && anchor_index < anchors.length) return anchors[anchor_index];
    else return new PVector(0, 0);
  }

  PVector[] generateAnchors(int nx, int w, int ny, int h, int xoff, int yoff) {
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
  void setX(int input) {
    x = input;
  }
  void setY(int input) {
    y = input;
  }
  void setXY(int inputx, int inputy) {
    x = inputx;
    y = inputy;
  }

  void addXY(int inputx, int inputy) {
    x += inputx;
    y += inputy;
  }
}
