package com.game.queist.spectrum.utils;

public class Vector {
   public static float[] add(float[] lhs, float[] rhs) {
      return new float[]{lhs[0] + rhs[0], lhs[1] + rhs[1], lhs[2] + rhs[2]};
   }

   public static float[] minus(float[] lhs, float[] rhs) {
      return new float[]{lhs[0] - rhs[0], lhs[1] - rhs[1], lhs[2] - rhs[2]};
   }

   public static float[] multiply(float scalar, float[] vector) {
      return new float[]{scalar * vector[0], scalar * vector[1], scalar * vector[2]};
   }

   public static float dotProduct(float[] lhs, float[] rhs) {
      return lhs[0] * rhs[0] + lhs[1] * rhs[1] + lhs[2] * rhs[2];
   }

   public static double length(float[] vector) {
      return Math.sqrt(dotProduct(vector, vector));
   }
}
