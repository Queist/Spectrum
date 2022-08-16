package com.game.queist.spectrum.shape;

import android.content.Context;

import com.game.queist.spectrum.R;
import com.game.queist.spectrum.utils.ShapeUtils;

public class EffectShape extends Shape {
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
}
