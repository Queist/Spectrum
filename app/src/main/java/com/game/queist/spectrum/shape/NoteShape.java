package com.game.queist.spectrum.shape;

import com.game.queist.spectrum.utils.ShapeUtils;

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
      vertices = ShapeUtils.buildCylinderVertices(radius, width);
      indices = ShapeUtils.buildCylinderIndices();
      colors = ShapeUtils.buildCylinderColors();
      normals = ShapeUtils.buildCylinderNormals();
      texCoords = ShapeUtils.buildCylinderTexCoords();
   }
}
