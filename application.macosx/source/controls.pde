void controlSetup() {
  cp5 = new ControlP5(this);
  int xoff = width-160, yoff = 10, yoffdef = 30;
  float def = 0.0;
  float r_s_min = -0.02, r_s_max = 0.02;
  float w_s_max = .02;

  int s_width = 130;
  int s_height = 10;

  cp5.addSlider("master_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(-1., 1.)
    .setValue(1.)
    .setLabel("master speed")
    ;
  posLabel("master_s");

  yoff += yoffdef;
  cp5.addSlider("ribbons_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(-3, 3)
    .setValue(0)
    .setLabel("ribbon speed")
    ;
  posLabel("ribbons_s");

  yoff += yoffdef;
  cp5.addSlider("corners_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0, r_s_max)
    .setValue(def)
    .setLabel("corner wiggle")
    ;
  posLabel("corners_s");

// CARROTS
  yoff += yoffdef+20;
  cp5.addSlider("carrots_r_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(r_s_min, r_s_max)
    .setValue(def)
    .setLabel("carrot rotation")
    ;
  posLabel("carrots_r_s");

  yoff += yoffdef;
  cp5.addSlider("carrots_w_a")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0., TWO_PI)
    .setValue(def)
    .setLabel("carrot wiggle amount")
    ;
  posLabel("carrots_w_a");

  yoff += yoffdef;
  cp5.addSlider("carrots_w_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0., w_s_max)
    .setValue(def)
    .setLabel("carrot wiggle speed")
    ;
  posLabel("carrots_w_s");

// BUSHELS
  yoff += yoffdef+20;
  cp5.addSlider("bushels_r_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(r_s_min, r_s_max)
    .setValue(def)
    .setLabel("bushel rotation")
    ;
  posLabel("bushels_r_s");

  yoff += yoffdef;
  cp5.addSlider("bushels_w_a")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0., TWO_PI)
    .setValue(def)
    .setLabel("bushel wiggle amount")
    ;
  posLabel("bushels_w_a");

  yoff += yoffdef;
  cp5.addSlider("bushels_w_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0., w_s_max)
    .setValue(def)
    .setLabel("bushel wiggle speed")
    ;
  posLabel("bushels_w_s");

// LEAVES

  yoff += yoffdef+20;
  cp5.addSlider("leaves_r_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(r_s_min, r_s_max)
    .setValue(def)
    .setLabel("leaves rotation")
    ;
  posLabel("leaves_r_s");

yoff += yoffdef;
  cp5.addSlider("leaves_w_a")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0., TWO_PI)
    .setValue(def)
    .setLabel("leaves wiggle amount")
    ;
  posLabel("leaves_w_a");

  yoff += yoffdef;
  cp5.addSlider("leaves_w_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0., w_s_max)
    .setValue(def)
    .setLabel("leaves wiggle speed")
    ;
  posLabel("leaves_w_s");

// FLOWERS

  yoff += yoffdef+20;
  cp5.addSlider("flowers_r_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(r_s_min, r_s_max)
    .setValue(0.0)
    .setLabel("flowers rotation")
    ;
  posLabel("flowers_r_s");

  yoff += yoffdef;

  cp5.addSlider("flowers_w_a")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0., TWO_PI)
    .setValue(0.0)
    .setLabel("flowers wiggle amount")
    ;
  posLabel("flowers_w_a");

  yoff += yoffdef;
  cp5.addSlider("flowers_w_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(0., w_s_max)
    .setValue(def)
    .setLabel("flowers wiggle speed")
    ;
  posLabel("flowers_w_s");
}

public void controlEvent(ControlEvent theEvent) {
  if (theEvent.isController()) {

    String name =theEvent.getController().getName();
    /*
    if (theEvent.getController().equals(slider1)) {
      setRibbonSpeed(slider1.getValue());
    }
    */
  }
}

void posLabel(String con) {
  cp5.getController(con).getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
  cp5.getController(con).getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
}

void master_s(float value) {
  m_s = value;
}

void ribbons_s(float value) {
  for (Ribbon r : ribbons) {
    r.s = value;
  }
}

void corners_s(float value){
  for (Corner c : corners) {
    c.s = value;
  }
}
// CARROTS
void carrots_r_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == carrots) m.r_s = value;
  }
}

void carrots_w_a(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == carrots) m.w_a = value;
  }
}

void carrots_w_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == carrots) m.w_s = value;
  }
}

// BUSHELS
void bushels_r_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == bushels) m.r_s = value;
  }
}

void bushels_w_a(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == bushels) m.w_a = value;
  }
}

void bushels_w_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == bushels) m.w_s = value;
  }
}

// LEAVES

void leaves_r_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == leaves) m.r_s = value;
  }
}

void leaves_w_a(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == leaves) m.w_a = value;
  }
}

void leaves_w_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == leaves) m.w_s = value;
  }
}

// FLOWERS
void flowers_r_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == flowers) m.r_s = value;
  }
}

void flowers_w_a(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == flowers) m.w_a = value;
  }
}

void flowers_w_s(float value){
  for (Mandala m : mandalas) {
    if (m.g_array == flowers) m.w_s = value;
  }
}
