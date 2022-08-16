package com.game.queist.spectrum.shape;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.game.queist.spectrum.R;
import com.game.queist.spectrum.chart.LongNote;
import com.game.queist.spectrum.chart.Note;
import com.game.queist.spectrum.utils.ShapeUtils;
import com.game.queist.spectrum.utils.Utility;

import java.util.ArrayList;
import java.util.Objects;

public class NoteShape extends Shape {
   private float radius;
   private float thickness;

   private float[][] colors;

   public NoteShape(Context context, float radius, float thickness) {
      super(context);
      this.radius = radius;
      this.thickness = thickness;
      setMaterial(0.4f, new float[]{0.5f, 0.5f, 0.5f});
   }

   public NoteShape(Context context) {
      super(context);
      this.radius = 10.5f;
      this.thickness = 2.f;
      setMaterial(0.4f, new float[]{0.5f, 0.5f, 0.5f});
   }

   @Override
   protected void initBufferResources() {
      positions = ShapeUtils.buildCylinderPositions(radius, thickness);
      normals = ShapeUtils.buildCylinderNormals();
      texCoords = ShapeUtils.buildCylinderTexCoords();

      indices = ShapeUtils.buildCylinderIndices();

      createTexture(Note.TAB, R.drawable.note);
      createTexture(Note.SLIDE, R.drawable.note_v2);
      createTexture(Note.LONG, R.drawable.note_v2);
   }

   @Override
   protected void initShader() {
      setVertexShader("note_v");
      setFragmentShader("note_f");
   }

   public void draw(int count, int quadrant, ArrayList<Note> note, double[] z, double rotateAngle) {
      int[] startOffset = new int[count];
      int[] length = new int[count];

      float[][] worlds = new float[count][16];
      float[][] colors = new float[count][4];
      float[][] texTransform = new float[count][16];
      String[] textures = new String[count];

      for (int i = 0; i < count; i++) {
         float start = (float) note.get(i).getPosition1();
         float end = (float) note.get(i).getPosition2();
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
         if (note.get(i).getKind().equals(Note.LONG)) {
            LongNote longNote = (LongNote) note.get(i);
            Matrix.translateM(worlds[i], 0, 0, 0, (float) (z[i] + longNote.getWorldWidth()/2 - thickness/2));
            Matrix.scaleM(worlds[i], 0, 0, 0, (float) (longNote.getWorldWidth() / thickness));
         }
         else Matrix.translateM(worlds[i], 0, 0, 0, (float) z[i]);

         colors[i][0] = Color.red(Utility.getNoteRGB(note.get(i).getColor())) / 255.f;
         colors[i][1] = Color.green(Utility.getNoteRGB(note.get(i).getColor())) / 255.f;
         colors[i][2] = Color.blue(Utility.getNoteRGB(note.get(i).getColor())) / 255.f;
         colors[i][3] = 1.f; //TODO

         Matrix.setIdentityM(texTransform[i], 0);
         Matrix.scaleM(texTransform[i], 0, 1 / texCoords[length[i] * 4 / 6], 1, 1);
         Matrix.translateM(texTransform[i], 0, -texCoords[startOffset[i] * 4 / 6], 0, 0);

         textures[i] = note.get(i).getKind();
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
