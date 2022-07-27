package com.game.queist.spectrum.utils;

import android.graphics.Color;

public class ShapeUtils {
   public static int LEVEL_OF_DETAIL = 40;
   public static float[] buildCylinderPositions(float radius, float width) {
      float[] vertices = new float[(LEVEL_OF_DETAIL + 1) * 2 * 3];
      for (int i = 0; i < LEVEL_OF_DETAIL + 1; i++) {
         vertices[6 * i    ] = (float)Math.cos(2 * Math.PI / LEVEL_OF_DETAIL * i) * radius;
         vertices[6 * i + 1] = (float)Math.sin(2 * Math.PI / LEVEL_OF_DETAIL * i) * radius;
         vertices[6 * i + 2] = width/2;
         vertices[6 * i + 3] = (float)Math.cos(2 * Math.PI / LEVEL_OF_DETAIL * i) * radius;
         vertices[6 * i + 4] = (float)Math.sin(2 * Math.PI / LEVEL_OF_DETAIL * i) * radius;
         vertices[6 * i + 5] = -width/2;
      }
      return vertices;
   }

   public static short[] buildCylinderIndices() {
      short[] indices = new short[(LEVEL_OF_DETAIL + 1) * 2 * 3];
      for (int i = 0; i < LEVEL_OF_DETAIL + 1; i++) {
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

   public static float[] buildCylinderColors(int[] sideColors) {
      float[] colors = new float[(LEVEL_OF_DETAIL + 1) * 2 * 3];
      for (int i = 0; i < LEVEL_OF_DETAIL; i++) {
         int j = i * sideColors.length / LEVEL_OF_DETAIL;
         colors[6 * i    ] = Color.red(sideColors[j]) / 255.f;
         colors[6 * i + 1] = Color.green(sideColors[j]) / 255.f;
         colors[6 * i + 2] = Color.blue(sideColors[j]) / 255.f;
         colors[6 * i + 3] = Color.red(sideColors[j]) / 255.f;
         colors[6 * i + 4] = Color.green(sideColors[j]) / 255.f;
         colors[6 * i + 5] = Color.blue(sideColors[j]) / 255.f;
      }
      colors[6 * LEVEL_OF_DETAIL    ] = Color.red(sideColors[0]) / 255.f;
      colors[6 * LEVEL_OF_DETAIL + 1] = Color.green(sideColors[0]) / 255.f;
      colors[6 * LEVEL_OF_DETAIL + 2] = Color.blue(sideColors[0]) / 255.f;
      colors[6 * LEVEL_OF_DETAIL + 3] = Color.red(sideColors[0]) / 255.f;
      colors[6 * LEVEL_OF_DETAIL + 4] = Color.green(sideColors[0]) / 255.f;
      colors[6 * LEVEL_OF_DETAIL + 5] = Color.blue(sideColors[0]) / 255.f;
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

   public static float[] buildDiskVertex() {
      /*TODO*/
      return new float[0];
   }

   public static float[] mergeVertexAttributes(float[] positions, float[] colors, float[] normals, float[] texCoords) {
      /*TODO*/
      return new float[0];
   }
}
