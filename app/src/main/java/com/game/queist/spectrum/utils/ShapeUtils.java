package com.game.queist.spectrum.utils;

import android.graphics.Color;

public class ShapeUtils {
   public static int LEVEL_OF_DETAIL = 160;

   public static float[] buildCylinderPositions(float radius, float width) {
      float[] vertices = new float[(LEVEL_OF_DETAIL + 1) * 2 * 3];
      for (int i = 0; i < LEVEL_OF_DETAIL + 1; i++) {
         vertices[6 * i    ] = (float)Math.cos(2 * Math.PI / LEVEL_OF_DETAIL * i) * radius;
         vertices[6 * i + 1] = (float)Math.sin(2 * Math.PI / LEVEL_OF_DETAIL * i) * radius;
         vertices[6 * i + 2] = width;
         vertices[6 * i + 3] = (float)Math.cos(2 * Math.PI / LEVEL_OF_DETAIL * i) * radius;
         vertices[6 * i + 4] = (float)Math.sin(2 * Math.PI / LEVEL_OF_DETAIL * i) * radius;
         vertices[6 * i + 5] = 0;
      }
      return vertices;
   }

   public static short[] buildCylinderIndices() {
      short[] indices = new short[LEVEL_OF_DETAIL * 2 * 3];
      for (int i = 0; i < LEVEL_OF_DETAIL; i++) {
         indices[6 * i    ] = (short) (2 * i    );
         indices[6 * i + 1] = (short) (2 * i + 1);
         indices[6 * i + 2] = (short) (2 * i + 2);
         indices[6 * i + 3] = (short) (2 * i + 2);
         indices[6 * i + 4] = (short) (2 * i + 1);
         indices[6 * i + 5] = (short) (2 * i + 3);
      }
      return indices;
   }

   public static float[] buildCylinderColors() {
      float[] colors = new float[(LEVEL_OF_DETAIL + 1) * 2 * 3];
      for (int i = 0; i < LEVEL_OF_DETAIL + 1; i++) {
         colors[6 * i    ] = 1.0f;
         colors[6 * i + 1] = 1.0f;
         colors[6 * i + 2] = 1.0f;
         colors[6 * i + 3] = 1.0f;
         colors[6 * i + 4] = 1.0f;
         colors[6 * i + 5] = 1.0f;
      }
      return colors;
   }

   public static float[] buildCylinderColors(int[] sideColors, float blendRate) {
      float[] colors = new float[(LEVEL_OF_DETAIL + 1) * 2 * 3];

      float[][] sideColorsRGB = new float[sideColors.length][3];
      for (int i = 0; i < sideColors.length; i++) {
         sideColorsRGB[i][0] = Color.red(sideColors[i]) / 255.f;
         sideColorsRGB[i][1] = Color.green(sideColors[i]) / 255.f;
         sideColorsRGB[i][2] = Color.blue(sideColors[i]) / 255.f;
      }

      for (int i = 0; i < LEVEL_OF_DETAIL; i++) {
         float rate = ((float) i * sideColors.length) / LEVEL_OF_DETAIL;
         int j = (int) Math.floor(rate);
         rate -= j;
         float blendR;
         float blendG;
         float blendB;
         if (blendRate <= rate && rate <= 1 - blendRate) {
            blendR = sideColorsRGB[j][0];
            blendG = sideColorsRGB[j][1];
            blendB = sideColorsRGB[j][2];
         }
         else if (blendRate > rate) {
            int before = (j + sideColors.length - 1) % sideColors.length;
            blendR = (sideColorsRGB[j][0] * (rate + blendRate) + sideColorsRGB[before][0] * (blendRate - rate)) / (2 * blendRate);
            blendG = (sideColorsRGB[j][1] * (rate + blendRate) + sideColorsRGB[before][1] * (blendRate - rate)) / (2 * blendRate);
            blendB = (sideColorsRGB[j][2] * (rate + blendRate) + sideColorsRGB[before][2] * (blendRate - rate)) / (2 * blendRate);
         }
         else {
            int next = (j + 1)%sideColors.length;
            blendR = (sideColorsRGB[j][0] * (1 - rate + blendRate) + sideColorsRGB[next][0] * (rate + blendRate - 1)) / (2 * blendRate);
            blendG = (sideColorsRGB[j][1] * (1 - rate + blendRate) + sideColorsRGB[next][1] * (rate + blendRate - 1)) / (2 * blendRate);
            blendB = (sideColorsRGB[j][2] * (1 - rate + blendRate) + sideColorsRGB[next][2] * (rate + blendRate - 1)) / (2 * blendRate);
         }
         colors[6 * i    ] = blendR;
         colors[6 * i + 1] = blendG;
         colors[6 * i + 2] = blendB;
         colors[6 * i + 3] = blendR;
         colors[6 * i + 4] = blendG;
         colors[6 * i + 5] = blendB;
      }
      colors[6 * LEVEL_OF_DETAIL    ] = colors[0];
      colors[6 * LEVEL_OF_DETAIL + 1] = colors[1];
      colors[6 * LEVEL_OF_DETAIL + 2] = colors[2];
      colors[6 * LEVEL_OF_DETAIL + 3] = colors[3];
      colors[6 * LEVEL_OF_DETAIL + 4] = colors[4];
      colors[6 * LEVEL_OF_DETAIL + 5] = colors[5];
      return colors;
   }

   public static float[] buildCylinderNormals() {
      float[] normals = new float[(LEVEL_OF_DETAIL + 1) * 2 * 3];
      for (int i = 0; i < LEVEL_OF_DETAIL + 1; i++) {
         normals[6 * i    ] = -(float)Math.cos(2 * Math.PI / LEVEL_OF_DETAIL * i);
         normals[6 * i + 1] = -(float)Math.sin(2 * Math.PI / LEVEL_OF_DETAIL * i);
         normals[6 * i + 2] = 0;
         normals[6 * i + 3] = -(float)Math.cos(2 * Math.PI / LEVEL_OF_DETAIL * i);
         normals[6 * i + 4] = -(float)Math.sin(2 * Math.PI / LEVEL_OF_DETAIL * i);
         normals[6 * i + 5] = 0;
      }
      return normals;
   }

   public static float[] buildCylinderTexCoords() {
      float[] texCoords = new float[(LEVEL_OF_DETAIL + 1) * 2 * 2];
      for (int i = 0; i < LEVEL_OF_DETAIL + 1; i++) {
         texCoords[4 * i    ] = 1.f / LEVEL_OF_DETAIL * i;
         texCoords[4 * i + 1] = 1.f;
         texCoords[4 * i + 2] = 1.f / LEVEL_OF_DETAIL * i;
         texCoords[4 * i + 3] = 0.f;
      }
      return texCoords;
   }

   public static float[] buildDiskPositions(float radius, float depth) {
      /*TODO*/
      float[] vertices = new float[(LEVEL_OF_DETAIL + 2) * 3];
      for (int i = 0; i < LEVEL_OF_DETAIL + 1; i++) {
         vertices[3 * i    ] = (float)Math.cos(2 * Math.PI / LEVEL_OF_DETAIL * i) * radius;
         vertices[3 * i + 1] = (float)Math.sin(2 * Math.PI / LEVEL_OF_DETAIL * i) * radius;
         vertices[3 * i + 2] = depth;
      }
      vertices[3 * LEVEL_OF_DETAIL + 3] = 0.f;
      vertices[3 * LEVEL_OF_DETAIL + 4] = 0.f;
      vertices[3 * LEVEL_OF_DETAIL + 5] = depth + radius;
      return vertices;
   }

   public static short[] buildDiskIndices() {
      short[] indices = new short[LEVEL_OF_DETAIL * 3];
      for (int i = 0; i < LEVEL_OF_DETAIL; i++) {
         indices[3 * i    ] = (short) (i                  );
         indices[3 * i + 1] = (short) (LEVEL_OF_DETAIL + 1);
         indices[3 * i + 2] = (short) (i               + 1);
      }
      return indices;
   }

   public static float[] buildDiskColors(int[] sideColors, float blendRate) {
      float[] colors = new float[(LEVEL_OF_DETAIL + 2) * 3];

      float[][] sideColorsRGB = new float[sideColors.length][3];
      for (int i = 0; i < sideColors.length; i++) {
         sideColorsRGB[i][0] = Color.red(sideColors[i]) / 255.f;
         sideColorsRGB[i][1] = Color.green(sideColors[i]) / 255.f;
         sideColorsRGB[i][2] = Color.blue(sideColors[i]) / 255.f;
      }

      for (int i = 0; i < LEVEL_OF_DETAIL; i++) {
         float rate = ((float) i * sideColors.length) / LEVEL_OF_DETAIL;
         int j = (int) Math.floor(rate);
         rate -= j;
         float blendR;
         float blendG;
         float blendB;
         if (blendRate <= rate && rate <= 1 - blendRate) {
            blendR = sideColorsRGB[j][0];
            blendG = sideColorsRGB[j][1];
            blendB = sideColorsRGB[j][2];
         }
         else if (blendRate > rate) {
            int before = (j + sideColors.length - 1) % sideColors.length;
            blendR = (sideColorsRGB[j][0] * (rate + blendRate) + sideColorsRGB[before][0] * (blendRate - rate)) / (2 * blendRate);
            blendG = (sideColorsRGB[j][1] * (rate + blendRate) + sideColorsRGB[before][1] * (blendRate - rate)) / (2 * blendRate);
            blendB = (sideColorsRGB[j][2] * (rate + blendRate) + sideColorsRGB[before][2] * (blendRate - rate)) / (2 * blendRate);
         }
         else {
            int next = (j + 1)%sideColors.length;
            blendR = (sideColorsRGB[j][0] * (1 - rate + blendRate) + sideColorsRGB[next][0] * (rate + blendRate - 1)) / (2 * blendRate);
            blendG = (sideColorsRGB[j][1] * (1 - rate + blendRate) + sideColorsRGB[next][1] * (rate + blendRate - 1)) / (2 * blendRate);
            blendB = (sideColorsRGB[j][2] * (1 - rate + blendRate) + sideColorsRGB[next][2] * (rate + blendRate - 1)) / (2 * blendRate);
         }
         colors[3 * i    ] = blendR;
         colors[3 * i + 1] = blendG;
         colors[3 * i + 2] = blendB;
      }
      colors[3 * LEVEL_OF_DETAIL    ] = colors[0];
      colors[3 * LEVEL_OF_DETAIL + 1] = colors[1];
      colors[3 * LEVEL_OF_DETAIL + 2] = colors[2];
      colors[3 * LEVEL_OF_DETAIL + 3] = .6f;
      colors[3 * LEVEL_OF_DETAIL + 4] = .6f;
      colors[3 * LEVEL_OF_DETAIL + 5] = .6f;
      return colors;
   }

   public static float[] buildDiskNormals() {
      float[] normals = new float[(LEVEL_OF_DETAIL + 2) * 3];
      for (int i = 0; i < LEVEL_OF_DETAIL + 1; i++) {
         normals[3 * i    ] = -(float)Math.cos(2 * Math.PI / LEVEL_OF_DETAIL * i);
         normals[3 * i + 1] = -(float)Math.sin(2 * Math.PI / LEVEL_OF_DETAIL * i);
         normals[3 * i + 2] = 0.f;
      }
      normals[3 * LEVEL_OF_DETAIL + 3] = 0.f;
      normals[3 * LEVEL_OF_DETAIL + 4] = 0.f;
      normals[3 * LEVEL_OF_DETAIL + 5] = -1.f;
      return normals;
   }

   public static float[] buildDiskTexCoords() {
      float[] texCoords = new float[(LEVEL_OF_DETAIL + 2) * 2];
      for (int i = 0; i < LEVEL_OF_DETAIL + 1; i++) {
         texCoords[2 * i    ] = (float)Math.cos(2 * Math.PI / LEVEL_OF_DETAIL * i) / 2.0f + 0.5f;
         texCoords[2 * i + 1] = (float)Math.sin(2 * Math.PI / LEVEL_OF_DETAIL * i) / 2.0f + 0.5f;
      }
      texCoords[2 * LEVEL_OF_DETAIL + 2] = 0.5f;
      texCoords[2 * LEVEL_OF_DETAIL + 3] = 0.5f;
      return texCoords;
   }
}
