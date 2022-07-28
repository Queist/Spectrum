package com.game.queist.spectrum.shape;

import com.game.queist.spectrum.activities.PlayScreen;
import com.game.queist.spectrum.utils.ShapeUtils;
import com.game.queist.spectrum.utils.Utility;

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
      int[] sideColors = new int[PlayScreen.SIDE_NUM];
      for (int i = 0; i < PlayScreen.SIDE_NUM; i++) {
         sideColors[i] = Utility.getRGB(i);
      }

      positions = ShapeUtils.buildCylinderPositions(radius, width);
      colors = ShapeUtils.buildCylinderColors(sideColors);
      normals = ShapeUtils.buildCylinderNormals();
      texCoords = ShapeUtils.buildCylinderTexCoords();

      vertices = ShapeUtils.mergeVertexAttributes(positions, colors, normals, texCoords);
      indices = ShapeUtils.buildCylinderIndices();
   }

   @Override
   protected void initShader() {

   }
}
