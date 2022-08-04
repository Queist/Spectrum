package com.game.queist.spectrum.shape;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.game.queist.spectrum.activities.PlayScreen;
import com.game.queist.spectrum.utils.ShapeUtils;
import com.game.queist.spectrum.utils.Utility;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class LaneShape extends Shape {

   private FloatBuffer colorBuffer;

   protected float[] colors;

   protected float radius;
   protected float width;
   protected float blendRate;

   private int colorAttrIndex;

   public LaneShape(Context context, float radius, float width, float blendRate) {
      super(context);
      this.radius = radius;
      this.width = width;
      this.blendRate = blendRate;
      setMaterial(.4f, new float[]{0.04f, 0.04f, 0.04f});
   }

   public LaneShape(Context context) {
      super(context);
      this.radius = 10.5f;
      this.width = 20.f;
      this.blendRate = .166667f;
      setMaterial(.4f, new float[]{0.04f, 0.04f, 0.04f});
   }

   @Override
   protected void initBufferResources() {
      int[] sideColors = new int[PlayScreen.SIDE_NUM];
      for (int i = 0; i < PlayScreen.SIDE_NUM; i++) {
         sideColors[i] = Utility.getBGRGB(i);
      }

      positions = ShapeUtils.buildCylinderPositions(radius, width);
      colors = ShapeUtils.buildCylinderColors(sideColors, blendRate);
      normals = ShapeUtils.buildCylinderNormals();
      texCoords = ShapeUtils.buildCylinderTexCoords();

      indices = ShapeUtils.buildCylinderIndices();
   }

   @Override
   protected void initShader() {
      setVertexShader("lane_v");
      setFragmentShader("lane_f");
   }

   @Override
   protected void generateBuffer() {
      ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
      cbb.order(ByteOrder.nativeOrder());
      colorBuffer = cbb.asFloatBuffer();
      colorBuffer.put(colors);
      colorBuffer.position(0);
      super.generateBuffer();
   }

   @Override
   protected void generateVerticesAndIndices() {
      int[] vertexBufferIndex = new int[1];
      GLES30.glGenBuffers(1, vertexBufferIndex, 0);
      colorAttrIndex = vertexBufferIndex[0];
      GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBufferIndex[0]);
      GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, colors.length * Float.BYTES, colorBuffer, GLES30.GL_STATIC_DRAW);

      super.generateVerticesAndIndices();
   }

   @Override
   protected void bindVerticesAndIndices() {
      super.bindVerticesAndIndices();

      int colorHandle = GLES30.glGetAttribLocation(program, "color");
      GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, colorAttrIndex);
      GLES30.glEnableVertexAttribArray(colorHandle);
      GLES30.glVertexAttribPointer(colorHandle, 3, GLES30.GL_FLOAT, false, 3 * Float.BYTES, 0);
   }

   @Override
   protected void unBindVerticesAndIndices() {
      super.unBindVerticesAndIndices();

      int colorHandle = GLES30.glGetAttribLocation(program, "color");
      GLES30.glDisableVertexAttribArray(colorHandle);
   }

   public void draw() {
      float[][] worlds = new float[1][16];
      Matrix.setIdentityM(worlds[0], 0);
      setWorlds(worlds);
      draw(1, new int[]{0}, new int[]{indices.length});
   }
}
