package com.game.queist.spectrum.shape;

import android.content.Context;
import android.opengl.GLES30;

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
   }

   public NoteShape(Context context) {
      super(context);
      this.radius = 10.f;
      this.width = 1.f;
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

   public void draw(@IntRange(from=0, to=3) int[] quadrant, double[] start, double[] end, double[] z) {
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
