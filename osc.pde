void updateOSC(int p) {
  updateIP();
  oscP5 = new OscP5(this, p);
  cp5.getController("field_osc_port").setValue(p);
}

void updateIP() {
  ip = Server.ip();
  cp5.getController("button_ip").setLabel("ip: " + ip);
}

void oscEvent(OscMessage theOscMessage) {
  String str_in[] = split(theOscMessage.addrPattern(), '/');
  String txt = "got osc message: " + theOscMessage.addrPattern();
  if (log_osc) log.setText(txt);
  Controller con;
  if (str_in[1].equals(osc_address)) {
    // parse osc_address/controllername/value
    if (str_in.length == 3) {
      if (cp5.getController(str_in[2]) != null &&
      cp5.getController(str_in[2]).getId() != -1)
      {
        con = cp5.getController(str_in[2]);
        setControllerValueWithOSC(con, theOscMessage);
      }
    }
    // parse osc_address/groupname/controllername/value
    //stupid hotfixed way of going about this
    else if (str_in.length == 4) {
      String parsed_name = str_in[2] + "/" + str_in[3];
      if (cp5.getController(parsed_name) != null &&
      cp5.getGroup(str_in[2]).getController(parsed_name).getId() != -1)
      {
        con = cp5.getController(parsed_name);
        setControllerValueWithOSC(con, theOscMessage);
      }
    }
  }
}

void setControllerValueWithOSC(Controller con, OscMessage theOscMessage) {
  if (theOscMessage.checkTypetag("i")) {
    int value = theOscMessage.get(0).intValue();
    value = constrain(value, (int)con.getMin(), (int)con.getMax());
    con.setValue(value);
    log.appendText("int value: " + Integer.toString(value));
  }

  if (theOscMessage.checkTypetag("f")) {
    float value = theOscMessage.get(0).floatValue();
    value = constrain(value, con.getMin(), con.getMax());
    con.setValue(value);
    log.appendText(" float value: " + Float.toString(value));
  }
}
