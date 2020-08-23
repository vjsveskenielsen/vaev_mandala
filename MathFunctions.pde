
//rollover of float value between a lower and upper value
float rollOver(float input, float edge0, float edge1) {
  float out = input;
  if (input > edge1) out = edge0 + input -edge1;
  else if(input <= edge0) out = edge1 + input -edge0;
  return out;
}

int rollOver(int input, int edge0, int edge1) {
  int out = input;
  if (input >= edge1) out = edge0 + input - edge1;
  else if(input <= edge0) out = edge1 + input - edge0;
  return out;
}

PVector PVectorOnCircularPath(float angle, float distance) {
  float x = cos(angle) * distance;
  float y = sin(angle) * distance;
  return new PVector(x, y);
}

float calcHypotenuse(float a, float b) {
  float c = sqrt(pow(a, 2) + pow(b, 2));
  return c;
}

//returns array of n normalized perlin noise values
float[] noiseArray(int _n) {
  float[] array = new float[_n];
  for (int i = 0; i<_n; i++) array[i] = noise(i);
  return array;
}
//same, with multiplying value
float[] noiseArray(int _n, float multiplier) {
  float[] array = noiseArray(_n);
  for (int i = 0; i<_n; i++) array[i] *= multiplier;
  return array;
}

float wiggleFloat(float amount, float speed) {
  return sin(millis()*speed)*amount;
}

int wiggleInt(float amount, float speed) {
  return round(sin(millis()*speed)*amount);
}

int[] scaleToFill(int in_w, int in_h, int dest_w, int dest_h) {
  PVector in = new PVector((float)in_w, (float)in_h); //vector of input dimensions
  PVector dest = new PVector((float)dest_w, (float)dest_h); //vector of destination dimensions
  /*
  calculate the scaling ratios for both axis, and choose the largest for scaling
  the output dimensions to FILL the destination
  */
  float scale = max(dest.x/in.x, dest.y/in.y);
  int out_w = round(in_w *scale);
  int out_h = round(in_h *scale);
  int off_x = (dest_w - out_w) / 2;
  int off_y = (dest_h - out_h) / 2;

  int[] out = {off_x, off_y, out_w, out_h};
  return out;
}

int[] scaleToFit(int in_w, int in_h, int dest_w, int dest_h) {
  PVector in = new PVector((float)in_w, (float)in_h); //vector of input dimensions
  PVector dest = new PVector((float)dest_w, (float)dest_h); //vector of destination dimensions
  /*
  calculate the scaling ratios for both axis, and choose the SMALLEST for scaling
  the output dimensions to FIT the destination
  */
  float scale = min(dest.x/in.x, dest.y/in.y);
  int out_w = round(in_w *scale);
  int out_h = round(in_h *scale);
  int off_x = (dest_w - out_w) / 2;
  int off_y = (dest_h - out_h) / 2;
  println("offset x:", off_x, "offset y:", off_y);

  int[] out = {off_x, off_y, out_w, out_h};
  return out;
}
