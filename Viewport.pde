class Viewport {
  int canvas_width;
  int canvas_height;
  int size; //viewport size
  PVector position;
  PVector canvas_offset = new PVector(0,0); //canvas pos within viewport
  PGraphics bg; //background customized for canvas

  Viewport(int vsize, int vpx, int vpy) {
    size = vsize;
    position = new PVector(vpx, vpy);
  }

  void display(PGraphics pg) {
    pushMatrix();
    translate(position.x, position.y);
    noFill();
    stroke(100);
    rect(0, 0, size, size);
    noStroke();
    fill(255);
    drawPointers();

    if (viewport_show_alpha) image(bg, canvas_offset.x, canvas_offset.y, canvas_width, canvas_height);
    else {
      fill(0);
      rect(canvas_offset.x, canvas_offset.y, canvas_width, canvas_height);
    }
    image(pg, canvas_offset.x, canvas_offset.y, canvas_width, canvas_height);
    popMatrix();
  }

  void update(PGraphics pg) {
    int[] dims = scaleToFit(pg.width, pg.height, size, size);
    canvas_offset = new PVector(dims[0], dims[1]);
    canvas_width = dims[2];
    canvas_height = dims[3];
    bg = createAlphaBackground(canvas_width, canvas_height);
  }

  PGraphics createAlphaBackground(int w, int h) {
    PGraphics abg = createGraphics(w, h, P2D);
    int s = 10; // size of square
    abg.beginDraw();
    abg.background(127+50);
    abg.noStroke();
    abg.fill(127-50);
    for (int x = 0; x < w; x+=s+s) {
      for (int y = 0; y < h; y+=s+s) {
        abg.rect(x, y, s, s);
      }
    }
    for (int x = s; x < w; x+=s+s) {
      for (int y = s; y < h; y+=s+s) {
        abg.rect(x, y, s, s);
      }
    }
    abg.endDraw();
    return abg;
  }

  void drawPointers() {
    float x = canvas_offset.x;
    float y = canvas_offset.y;
    triangle(x, y, x-5, y, x, y-5);
    x += bg.width;
    triangle(x, y, x+5, y, x, y-5);
    y += bg.height;
    triangle(x, y, x+5, y, x, y+5);
    x = canvas_offset.x;
    triangle(x, y, x-5, y, x, y+5);
  }
}
