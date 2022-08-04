package com.game.queist.spectrum.shape;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.game.queist.spectrum.utils.ShapeUtils;

import androidx.annotation.IntRange;

public class NoteShape extends Shape {
   private float radius;
   private float width;

   private float[][] colors;

   public NoteShape(Context context, float radius, float width) {
      super(context);
      this.radius = radius;
      this.width = width;
      setMaterial(1.f, new float[]{0.9f, 0.9f, 0.9f});
   }

   public NoteShape(Context context) {
      super(context);
      this.radius = 10.f;
      this.width = 2.f;
      setMaterial(1.f, new float[]{0.9f, 0.9f, 0.9f});
   }

   @Override
   protected void initBufferResources() {
      positions = ShapeUtils.buildCylinderPositions(radius, width);
      normals = ShapeUtils.buildCylinderNormals();
      texCoords = ShapeUtils.buildCylinderTexCoords();

      indices = ShapeUtils.buildCylinderIndices();
   }

   @Override
   protected void initShader() {
      setVertexShader("note_v");
      setFragmentShader("note_f");
   }

   public void draw(int count, @IntRange(from=0, to=3) int[] quadrant, double[] start, double[] end, double[] z) {
      int[] startOffset = new int[count];
      int[] length = new int[count];

      for (int i = 0; i < count; i++) {
         if (quadrant[i]%2 == 0) {
            startOffset[i] = (int)((indices.length / 24) * quadrant[i] + (indices.length / 240) * (10 - end[i])) * 6;
            length[i] = (int)((indices.length / 240) * (end[i] - start[i])) * 6;
         }
         else {
            startOffset[i] = (int)((indices.length / 24) * quadrant[i] + (indices.length / 240) * start[i]) * 6;
            length[i] = (int)((indices.length / 240) * (end[i] - start[i])) * 6;
         }
      }

      float[][] worlds = new float[count][16];
      float[][] colors = new float[count][3];

      for (int i = 0; i < count; i++) {
         Matrix.setIdentityM(worlds[i], 0);
         Matrix.translateM(worlds[i], 0, 0, 0, (float) z[i]);
         colors[i][0] = colors[i][1] = colors[i][2] = 1.f; //temporal
      }

      setWorlds(worlds);
      setColors(colors);

      draw(count, startOffset, length);
      /*if (quadrant%2 == 0) {
         draw(
                 (int)(((indices.length - 6) / 24) * quadrant + ((indices.length - 6) / 240) * (10 - end)) * 6,
                 (int)(((indices.length - 6) / 240) * (end - start)) * 6 + 6);
      }
      else {
         draw(
                 (int)(((indices.length - 6) / 24) * quadrant + ((indices.length - 6) / 240) * start) * 6,
                 (int)(((indices.length - 6) / 240) * (end - start)) * 6 + 6);
      }*/
   }

   @Override
   protected void bindObjectPerCB(int i) {
      super.bindObjectPerCB(i);
      int colorHandle = GLES30.glGetUniformLocation(program, "color");
      GLES30.glUniform3fv(colorHandle, 1, colors[i], 0);
   }

   public float[][] getColors() {
      return colors;
   }

   public void setColors(float[][] colors) {
      this.colors = colors;
   }
}
