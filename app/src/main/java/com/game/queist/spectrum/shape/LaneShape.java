package com.game.queist.spectrum.shape;

import com.game.queist.spectrum.utils.ShapeUtils;

public class LaneShape extends Shape {
   private float radius;
   private float width;

   public LaneShape(float radius, float width) {
      super();
      this.radius = radius;
      this.width = width;
   }

   public LaneShape() {
      super();
      this.radius = 10.f;
      this.width = 100.f;
   }

   @Override
   protected void initBufferResources() {
      vertices = ShapeUtils.buildCylinderVertices(radius, width);
      indices = ShapeUtils.buildCylinderIndices();
      colors = ShapeUtils.buildCylinderColors();
      normals = ShapeUtils.buildCylinderNormals();
      texCoords = ShapeUtils.buildCylinderTexCoords();
   }

   @Override
   protected void initShader() {

   }
}
