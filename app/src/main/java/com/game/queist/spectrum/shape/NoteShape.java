package com.game.queist.spectrum.shape;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.game.queist.spectrum.R;
import com.game.queist.spectrum.chart.Note;
import com.game.queist.spectrum.utils.ShapeUtils;
import com.game.queist.spectrum.utils.Utility;

import java.util.ArrayList;

import androidx.annotation.IntRange;

public class NoteShape extends Shape {
   private float radius;
   private float width;

   private float[][] colors;

   public NoteShape(Context context, float radius, float width) {
      super(context);
      this.radius = radius;
      this.width = width;
      setMaterial(0.4f, new float[]{0.5f, 0.5f, 0.5f});
   }

   public NoteShape(Context context) {
      super(context);
      this.radius = 10.f;
      this.width = 2.f;
      setMaterial(0.4f, new float[]{0.5f, 0.5f, 0.5f});
   }

   @Override
   protected void initBufferResources() {
      positions = ShapeUtils.buildCylinderPositions(radius, width);
      normals = ShapeUtils.buildCylinderNormals();
      texCoords = ShapeUtils.buildCylinderTexCoords();

      indices = ShapeUtils.buildCylinderIndices();

      createTexture(R.drawable.note);
   }

   @Override
   protected void initShader() {
      setVertexShader("note_v");
      setFragmentShader("note_f");
   }

   public void draw(int count, int[] quadrant, ArrayList<Note> note, double[] z) {
      int[] startOffset = new int[count];
      int[] length = new int[count];

      float[][] worlds = new float[count][16];
      float[][] colors = new float[count][3];
      float[][] texTransform = new float[count][16];

      for (int i = 0; i < count; i++) {
         float start = (float) note.get(i).getPosition1();
         float end = (float) note.get(i).getPosition2();
         if (start > end) {
            float t = start;
            start = end;
            end = t;
         }
         if (quadrant[i]%2 == 0) {
            startOffset[i] = (int)((indices.length / 24) * quadrant[i] + (indices.length / 240) * (10 - end)) * 6;
            length[i] = (int)((indices.length / 240) * (end - start)) * 6;
         }
         else {
            startOffset[i] = (int)((indices.length / 24) * quadrant[i] + (indices.length / 240) * start) * 6;
            length[i] = (int)((indices.length / 240) * (end - start)) * 6;
         }

         Matrix.setIdentityM(worlds[i], 0);
         Matrix.translateM(worlds[i], 0, 0, 0, (float) z[i]);
         colors[i][0] = Color.red(Utility.getNoteRGB(note.get(i).getColor())) / 255.f;
         colors[i][1] = Color.green(Utility.getNoteRGB(note.get(i).getColor())) / 255.f;
         colors[i][2] = Color.blue(Utility.getNoteRGB(note.get(i).getColor())) / 255.f;
         Matrix.setIdentityM(texTransform[i], 0);
         Matrix.scaleM(texTransform[i], 0, 1 / (end / 40.0f - start / 40.0f), 1, 1);
      }

      setWorlds(worlds);
      setColors(colors);
      setTexTransform(texTransform);

      draw(count, startOffset, length);
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
