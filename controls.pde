void controlSetup() {
  cp5 = new ControlP5(this);
  int xoff = width-140, yoff = 100;
  int slider_width = 100;
  slider1 = cp5.addSlider("ribbon speed")
    .setPosition(xoff, yoff)
    .setSize(slider_width, 20)
    .setRange(-3, 3)
    .setValue(1)
    ;
}

public void controlEvent(ControlEvent theEvent) {
  if (theEvent.isController()) {

    String name =theEvent.getController().getName();
    if (theEvent.getController().equals(slider1)) {
      for (int i = 0; i<ribbons.length; i++) {
        ribbons[i].setSpeed(slider1.getValue());
      }
    }

  }
}
