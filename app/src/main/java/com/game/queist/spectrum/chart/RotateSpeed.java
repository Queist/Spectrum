package com.game.queist.spectrum.chart;

public class RotateSpeed extends BitObject {
   private double value;

   public RotateSpeed(double value, double bit) {
      super(bit);
      this.value = value;
   }

   public double getValue() {
      return value;
   }
}
