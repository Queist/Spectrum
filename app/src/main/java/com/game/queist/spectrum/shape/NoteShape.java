package com.game.queist.spectrum.shape;

import com.game.queist.spectrum.utils.ShapeUtils;

import androidx.annotation.IntRange;

public class NoteShape extends Shape {
   private float radius;
   private float width;

   public NoteShape(float radius, float width) {
      super();
      this.radius = radius;
      this.width = width;
   }

   public NoteShape() {
      super();
      this.radius = 10.f;
      this.width = 1.f;
   }

   @Override
   protected void initBufferResources() {
      positions = ShapeUtils.buildCylinderPositions(radius, width);
      colors = ShapeUtils.buildCylinderColors();
      normals = ShapeUtils.buildCylinderNormals();
      texCoords = ShapeUtils.buildCylinderTexCoords();

      indices = ShapeUtils.buildCylinderIndices();
   }

   @Override
   protected void initShader() {

   }

   public void draw(@IntRange(from=0, to=3) int quadrant, double start, double end) {
      if (quadrant%2 == 0) {
         draw(
                 (int)(((indices.length - 6) / 24) * quadrant + ((indices.length - 6) / 240) * (10 - end)) * 6,
                 (int)(((indices.length - 6) / 240) * (end - start)) * 6 + 6);
      }
      else {
         draw(
                 (int)(((indices.length - 6) / 24) * quadrant + ((indices.length - 6) / 240) * start) * 6,
                 (int)(((indices.length - 6) / 240) * (end - start)) * 6 + 6);
      }
   }
}
