package com.game.queist.spectrum.shape;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.game.queist.spectrum.R;
import com.game.queist.spectrum.chart.EffectFlag;
import com.game.queist.spectrum.utils.ShapeUtils;
import com.game.queist.spectrum.utils.Utility;

import java.util.ArrayList;

public class EffectShape extends Shape {
   private float[][] colors;

   float outerRadius;
   float innerRadius;
   float depth;

   public EffectShape(Context context, float innerRadius, float outerRadius, float depth) {
      super(context);
      this.outerRadius = outerRadius;
      this.innerRadius = innerRadius;
      this.depth = depth;
   }

   public EffectShape(Context context) {
      super(context);
      this.outerRadius = 11.f;
      this.innerRadius = 10.f;
      this.depth = 0;
   }

   @Override
   protected void initBufferResources() {
      positions = ShapeUtils.buildRingPositions(outerRadius, innerRadius, depth);
      normals = ShapeUtils.buildRingNormals();
      texCoords = ShapeUtils.buildRingTexCoords();

      indices = ShapeUtils.buildRingIndices();

      createTexture("Effect", R.drawable.note);
   }

   @Override
   protected void initShader() {
      setVertexShader("note_v");
      setFragmentShader("note_f");
   }

   public void draw(int count, ArrayList<EffectFlag> effects) {
      int[] startOffset = new int[count];
      int[] length = new int[count];

      float[][] worlds = new float[count][16];
      float[][] colors = new float[count][4];
      float[][] texTransform = new float[count][16];
      String[] textures = new String[count];

      for (int i = 0; i < count; i++) {
         float start = (float) effects.get(i).getPosition1();
         float end = (float) effects.get(i).getPosition2();
         float lifeRatio = (float) (effects.get(i).getLifeTime() / EffectFlag.TOTAL_LIFETIME);
         String judge = effects.get(i).getEffect();
         double rotateAngle = effects.get(i).getRotateAngle();

         int quadrant = effects.get(i).getQuadrant();
         if (start > end) {
            float t = start;
            start = end;
            end = t;
         }
         if (quadrant%2 == 0) {
            startOffset[i] = (int)((indices.length / 24) * quadrant + (indices.length / 240) * (10 - end)) * 6;
            length[i] = (int)((indices.length / 240) * (end - start)) * 6;
         }
         else {
            startOffset[i] = (int)((indices.length / 24) * quadrant + (indices.length / 240) * start) * 6;
            length[i] = (int)((indices.length / 240) * (end - start)) * 6;
         }

         Matrix.setIdentityM(worlds[i], 0);
         Matrix.rotateM(worlds[i], 0, (float) (Math.toDegrees(rotateAngle)), 0, 0, 1);

         colors[i][0] = Utility.judgeToInteger(judge) / 3.f;
         colors[i][1] = Utility.judgeToInteger(judge) / 3.f;
         colors[i][2] = Utility.judgeToInteger(judge) / 3.f;
         colors[i][3] = lifeRatio; //TODO

         Matrix.setIdentityM(texTransform[i], 0);
         Matrix.scaleM(texTransform[i], 0, 1 / texCoords[length[i] * 4 / 6], 1, 1);
         Matrix.translateM(texTransform[i], 0, -texCoords[startOffset[i] * 4 / 6], 0, 0);

         textures[i] = "Effect";
      }

      setWorlds(worlds);
      setColors(colors);
      setTextures(textures);
      setTexTransforms(texTransform);

      draw(count, startOffset, length);
   }

   @Override
   protected void bindObjectPerCB(int i) {
      super.bindObjectPerCB(i);
      int colorHandle = GLES30.glGetUniformLocation(program, "color");
      GLES30.glUniform4fv(colorHandle, 1, colors[i], 0);
   }

   public float[][] getColors() {
      return colors;
   }

   public void setColors(float[][] colors) {
      this.colors = colors;
   }
}
