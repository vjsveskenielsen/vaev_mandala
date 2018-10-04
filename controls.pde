void controlSetup() {
  cp5 = new ControlP5(this);
  int xoff = width-160, yoff = 100;
  int s_width = 100;
  int s_height = 20;
  
  slider1 = cp5.addSlider("ribbon speed")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(-3, 3)
    .setValue(1)
    ;
  slider1.getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
  slider1.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);

  yoff += 50;
  slider2 = cp5.addSlider("carrots speed")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(-3, 3)
    .setValue(1)
    ;
  slider2.getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
  slider2.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
  
  yoff += 50;
  slider3 = cp5.addSlider("leaves speed")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(-3, 3)
    .setValue(1)
    ;
  slider3.getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
  slider3.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
  
  yoff += 50;
  slider4 = cp5.addSlider("flowers speed")
    .setPosition(xoff, yoff)
    .setSize(s_width, s_height)
    .setRange(-3, 3)
    .setValue(1)
    ;
  slider4.getValueLabel().align(ControlP5.LEFT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
  slider4.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.BOTTOM_OUTSIDE).setPaddingX(0);
}

public void controlEvent(ControlEvent theEvent) {
  if (theEvent.isController()) {

    String name =theEvent.getController().getName();
    if (theEvent.getController().equals(slider1)) {
      setRibbonSpeed(slider1.getValue());
    }
    if (theEvent.getController().equals(slider2)) {
      for (Mandala m : mandalas) {
        if (m.g_array == carrots) m.r_s = slider2.getValue();
      }
    }
  }
}



void setRibbonSpeed(float value) {
  for (int i = 0; i<ribbons.length; i++) {
    ribbons[i].setSpeed(value);
  }
}
