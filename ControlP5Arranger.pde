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
  color[] foregroundColors = {color(150), color(255, 0, 0, 100),  color(0, 200, 50, 100),  color(0, 0, 255, 100)};
  color[] backgroundColors = {color(150), color(255, 0, 0, 50),   color(0, 200, 50, 50),   color(0, 0, 255, 50)};
  color[] activeConColors =  {color(150), color(255, 0, 0, 255),  color(0, 200, 50, 255),  color(0, 0, 255, 255)};
  int colorSchemeIndex = 0;

  void style1(String con_name) {
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

  color getForegroundColor() {
    return foregroundColors[colorSchemeIndex];
  }
  color getBackgroundColor() {
    return activeConColors[colorSchemeIndex];
  }
  color getActiveConColor() {
    return activeConColors[colorSchemeIndex];
  }

  void goToNextColorScheme() {
    colorSchemeIndex = rollOver(colorSchemeIndex+1, 0, foregroundColors.length);
  }
  void setColorScheme(int input) {
    colorSchemeIndex = input;
  }
}
