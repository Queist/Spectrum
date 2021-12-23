#pragma version(1)
#pragma rs java_package_name(com.game.queist.spectrum)
#pragma rs_fp_relaxed

float saturation;

uchar4 RS_KERNEL retouch(uchar4 in) {
  uchar4 out = in;
  if (in.r == in.g && in.g == in.b) {
    return out;
  }
  if (in.r >= in.g && in.r >= in.b) {
    if (in.g > in.b) {
        out.r = in.r;
        out.b = in.r - (char) (saturation * (in.r - in.b));
        out.g = out.b + (out.r - out.b) * (in.g - in.b) / (in.r - in.b);
    }
    else {
        out.r = in.r;
        out.g = in.r - (char) (saturation * (in.r - in.g));
        out.b = out.g + (out.r - out.g) * (in.b - in.g) / (in.r - in.g);
    }
  }
  else if (in.g >= in.r && in.g >= in.b) {
    if (in.r > in.b) {
        out.g = in.g;
        out.b = in.g - (char) (saturation * (in.g - in.b));
        out.r = out.b + (out.g - out.b) * (in.r - in.b) / (in.g - in.b);
    }
    else {
        out.g = in.g;
        out.r = in.g - (char) (saturation * (in.g - in.r));
        out.b = out.r + (out.g - out.r) * (in.b - in.r) / (in.g - in.r);
    }
  }
  else {
    if (in.r > in.g) {
        out.b = in.b;
        out.g = in.b - (char) (saturation * (in.b - in.g));
        out.r = out.g + (out.b - out.g) * (in.r - in.g) / (in.b - in.g);
    }
    else {
        out.b = in.b;
        out.r = in.b - (char) (saturation * (in.b - in.r));
        out.g = out.r + (out.b - out.r) * (in.g - in.r) / (in.b - in.r);
    }
  }
  return out;
}