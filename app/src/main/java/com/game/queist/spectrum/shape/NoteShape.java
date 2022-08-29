package com.game.queist.spectrum.shape;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.game.queist.spectrum.R;
import com.game.queist.spectrum.activities.PlayScreen;
import com.game.queist.spectrum.chart.LongNote;
import com.game.queist.spectrum.chart.Note;
import com.game.queist.spectrum.utils.ShapeUtils;
import com.game.queist.spectrum.utils.Utility;

import java.util.ArrayList;

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
      this.thickness = 1.f;
      setMaterial(0.4f, new float[]{0.5f, 0.5f, 0.5f});
   }

   @Override
   protected void initBufferResources() {
      positions = ShapeUtils.buildCylinderPositions(radius, thickness);
      normals = ShapeUtils.buildCylinderNormals();
      texCoords = ShapeUtils.buildCylinderTexCoords();

      indices = ShapeUtils.buildCylinderIndices();

      createTexture(Note.TAB, R.drawable.tab_note_texture);
      createTexture(Note.SLIDE, R.drawable.slide_note_texture);
      createTexture(Note.LONG, R.drawable.long_note_texture);
      createTexture("SubLine", R.drawable.sub_line);
      createTexture(PlayScreen.SPEED_UP, R.drawable.speed_up);
      createTexture(PlayScreen.SPEED_DOWN, R.drawable.speed_down);
   }

   @Override
   protected void initShader() {
      setVertexShader("note_v");
      setFragmentShader("note_f");
   }

   //Draw Note
   public void draw(int count, ArrayList<Note> notes, double[] z, double rotateAngle) {
      int[] startOffset = new int[count];
      int[] length = new int[count];

      float[][] worlds = new float[count][16];
      float[][] colors = new float[count][4];
      float[][] texTransform = new float[count][16];
      String[] textures = new String[count];

      for (int i = 0; i < count; i++) {
         float start = (float) notes.get(i).getStart();
         float range = (float) notes.get(i).getRange();

         startOffset[i] = 0;
         length[i] = (int)((indices.length / 6) * (range / 360)) * 6;

         Matrix.setIdentityM(worlds[i], 0);
         Matrix.rotateM(worlds[i], 0, (float) (Math.toDegrees(rotateAngle) + start), 0, 0, 1);
         Matrix.translateM(worlds[i], 0, 0, 0, (float) z[i]);
         if (notes.get(i).getKind().equals(Note.LONG)) {
            LongNote longNote = (LongNote) notes.get(i);
            if (longNote.getWorldWidth() > 0) Matrix.scaleM(worlds[i], 0, 1, 1, (float) (longNote.getWorldWidth() / thickness));
            else Matrix.scaleM(worlds[i], 0, 1, 1, 0.f);
         }

         colors[i][0] = Color.red(Utility.getNoteRGB(notes.get(i).getColor())) / 255.f;
         colors[i][1] = Color.green(Utility.getNoteRGB(notes.get(i).getColor())) / 255.f;
         colors[i][2] = Color.blue(Utility.getNoteRGB(notes.get(i).getColor())) / 255.f;
         colors[i][3] = 1.f; //TODO

         Matrix.setIdentityM(texTransform[i], 0);
         Matrix.scaleM(texTransform[i], 0, 1 / texCoords[length[i] * 4 / 6], 1, 1);

         textures[i] = notes.get(i).getKind();
      }

      setWorlds(worlds);
      setColors(colors);
      setTextures(textures);
      setTexTransforms(texTransform);

      draw(count, startOffset, length);
   }

   //Draw Equivalence Line
   public void draw(int count, double[] z) {
      int[] startOffset = new int[count];
      int[] length = new int[count];

      float[][] worlds = new float[count][16];
      float[][] colors = new float[count][4];
      float[][] texTransform = new float[count][16];
      String[] textures = new String[count];

      for (int i = 0; i < count; i++) {
         startOffset[i] = 0;
         length[i] = indices.length;

         Matrix.setIdentityM(worlds[i], 0);
         Matrix.translateM(worlds[i], 0, 0, 0, (float) z[i]);
         Matrix.scaleM(worlds[i], 0, 1, 1, 0.2f);

         colors[i][0] = 1.f;
         colors[i][1] = 1.f;
         colors[i][2] = 1.f;
         colors[i][3] = 0.5f;
         Matrix.setIdentityM(texTransform[i], 0);
         textures[i] = "SubLine";
      }

      setWorlds(worlds);
      setColors(colors);
      setTextures(textures);
      setTexTransforms(texTransform);

      draw(count, startOffset, length);
   }

   //Draw Rotate Speed Line
   public void draw(int count, String[] rotateSpeedLine, double[] z) {
      int[] startOffset = new int[count];
      int[] length = new int[count];

      float[][] worlds = new float[count][16];
      float[][] colors = new float[count][4];
      float[][] texTransform = new float[count][16];
      String[] textures = new String[count];

      for (int i = 0; i < count; i++) {
         startOffset[i] = 0;
         length[i] = indices.length;

         Matrix.setIdentityM(worlds[i], 0);
         Matrix.translateM(worlds[i], 0, 0, 0, (float) z[i]);
         Matrix.scaleM(worlds[i], 0, 1, 1, 3.f);

         colors[i][0] = 1.f;
         colors[i][1] = 1.f;
         colors[i][2] = 1.f;
         colors[i][3] = 0.7f;

         Matrix.setIdentityM(texTransform[i], 0);
         textures[i] = rotateSpeedLine[i];
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
