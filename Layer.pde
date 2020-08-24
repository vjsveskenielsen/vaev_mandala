/* Example of a custom Layer class
 The Layer class extends a PGraphics3D object with nice stuff like
  - variable limits for optimizing drawing stuff within the canvas
  - variable for center of canvas
 */


import processing.core.PApplet;
import processing.core.PGraphics;
import codeanticode.syphon.*;

public class Layer extends PGraphics3D {
  /* this class was made with no idea of whats going on, lifting from
   https://forum.processing.org/two/discussion/5238/#Comment_18116
   */

  Limits limits = new Limits();
  PVector center;

  public Layer(int w, int h) {
    final PApplet p = getEnclosingPApplet();
    initialize(w, h, p, p.dataPath(""));
  }

  public Layer(int w, int h, PApplet p) {
    initialize(w, h, p, p.dataPath(""));
  }

  public Layer(int w, int h, PApplet p, String s) {
    initialize(w, h, p, s);
  }

  public void initialize(int w, int h, PApplet p, String s) {
    setParent(p);
    setPrimary(false);
    setPath(s);
    setSize(w, h);
    limits.setLeft(0);
    limits.setRight(w);
    limits.setTop(0);
    limits.setBottom(h);
    center = new PVector(this.width/2, this.height/2);
  }

  protected PApplet getEnclosingPApplet() {
    try {
      return (PApplet) getClass()
        .getDeclaredField("this$0").get(this);
    }

    catch (ReflectiveOperationException cause) {
      throw new RuntimeException(cause);
    }
  }

  @ Override public String toString() {
    return "Width: " + width + "\t Height: " + height
      + "\nPath:  " + path;
  }

  boolean isWithinLimits(PVector p) {
    if (p.x >= limits.left && p.x <= limits.right && p.y >= limits.top && p.y <= limits.bottom) return true;
    else return false;
  }

  boolean isWithinLimits(int px, int py) {
    if (px >= limits.left && px <= limits.right && py >= limits.top && py <= limits.bottom) return true;
    else return false;
  }

  boolean isWithinLimits(float px, float py) {
    if (px >= limits.left && px <= limits.right && py >= limits.top && py <= limits.bottom) return true;
    else return false;
  }

  void setLimits(int margin) {
    //add margin to all limits (negative value expands, positive contracts)
    limits.setLeft(margin);
    limits.setTop(margin);
    limits.setRight(this.width-margin);
    limits.setBottom(this.height-margin);
  }
}

class Limits {
  int left, right, top, bottom;

  Limits() {
    left = 0;
    right = 0;
    top = 0;
    bottom = 0;
  }
  void setLeft(int l) {
    left = l;
  }
  void setRight(int r) {
    right = r;
  }
  void setTop(int t) {
    top = t;
  }
  void setBottom(int b) {
    bottom = b;
  }
}
