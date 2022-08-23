package com.game.queist.spectrum.shape;

import android.content.Context;
import android.opengl.Matrix;

public class BackgroundQuad extends Shape {
   private int resourceID;

   public BackgroundQuad(Context context, int resourceID) {
      super(context);
      this.resourceID = resourceID;
   }

   @Override
   protected void initBufferResources() {
      positions = new float[]{
              -1, 1, 1,
              -1, -1, 1,
              1, -1, 1,
              1, 1, 1,
      };
      normals = new float[]{
              0, 0, -1,
              0, 0, -1,
              0, 0, -1,
              0, 0, -1,
      };
      texCoords = new float[]{
              0, 0,
              0, 1,
              1, 1,
              1, 0,
      };

      indices = new short[]{
              0, 1, 2,
              2, 0, 3,
      };

      createTexture("Background", resourceID);
   }

   @Override
   protected void initShader() { //TODO
      setVertexShader("note_v");
      setFragmentShader("note_f");
   }

   public void draw() {
      float[][] worlds = new float[1][16];
      Matrix.setIdentityM(worlds[0], 0);
      setWorlds(worlds);
      setTexTransforms(worlds);
      setTextures(new String[]{"Background"});
      draw(1, new int[]{0}, new int[]{indices.length});
   }
}
