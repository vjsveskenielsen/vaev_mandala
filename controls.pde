void controlSetup() {
  cp5 = new ControlP5(this);
  int xoff = width-160, yoff = 20;
  int s_width = 100;
  int s_height = 20;

  cp5.addSlider("ribbons_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(-3, 3)
    .setValue(1)
    .setLabel("ribbon speed")
    ;
    cp5.getController("ribbons_s").getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
    cp5.getController("ribbons_s").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);

  yoff += 50;
  cp5.addSlider("carrots_r_s")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(-.5, .5)
    .setValue(0.0)
    .setLabel("carrot rotation")
    ;
    cp5.getController("carrots_r_s").getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
    cp5.getController("carrots_r_s").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);

    yoff += 50;
    cp5.addSlider("carrots_w_a")
      .setPosition(xoff, yoff)
      .setSize(s_width, s_height)
      .setRange(0., TWO_PI)
      .setValue(0.0)
      .setLabel("carrot wiggle amount")
      ;
      cp5.getController("carrots_w_a").getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
      cp5.getController("carrots_w_a").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
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



void ribbons_s(float value) {
  for (int i = 0; i<ribbons.length; i++) {
    ribbons[i].setSpeed(value);
  }
}

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
