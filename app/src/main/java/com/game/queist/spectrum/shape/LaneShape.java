package com.game.queist.spectrum.shape;

import android.content.Context;
import android.opengl.GLES30;

import com.game.queist.spectrum.activities.PlayScreen;
import com.game.queist.spectrum.utils.ShapeUtils;
import com.game.queist.spectrum.utils.Utility;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class LaneShape extends Shape {

   private FloatBuffer colorBuffer;

   protected float[] colors;

   private float radius;
   private float width;

   public LaneShape(Context context, float radius, float width) {
      super(context);
      this.radius = radius;
      this.width = width;
   }

   public LaneShape(Context context) {
      super(context);
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
   protected void bindVerticesAndIndices() {
      int[] vertexBufferIndex = new int[1];
      GLES30.glGenBuffers(1, vertexBufferIndex, 0);
      GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBufferIndex[0]);
      GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, colors.length * Float.BYTES, colorBuffer, GLES30.GL_STATIC_DRAW);

      super.bindVerticesAndIndices();

      int colorHandle = GLES30.glGetAttribLocation(program, "color");
      GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBufferIndex[0]);
      GLES30.glEnableVertexAttribArray(colorHandle);
      GLES30.glVertexAttribPointer(colorHandle, 3, GLES30.GL_FLOAT, false, 3 * Float.BYTES, 0);
   }
}
