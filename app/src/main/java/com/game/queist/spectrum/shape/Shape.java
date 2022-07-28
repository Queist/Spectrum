package com.game.queist.spectrum.shape;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.game.queist.spectrum.utils.Utility;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

public abstract class Shape {

    private int program;

    private String vertexShader;
    private String fragmentShader;

    private FloatBuffer positionBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer texCoordsBuffer;
    private ShortBuffer indexBuffer;

    protected float[] positions;
    protected float[] colors;
    protected float[] normals;
    protected float[] texCoords;
    protected short[] indices;

    protected float[][] worlds;

    private static float[] view;
    private static float[] proj;

    public Shape() {
        initBufferResources();
        initShader();

        ByteBuffer vbb = ByteBuffer.allocateDirect(positions.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        positionBuffer = vbb.asFloatBuffer();
        positionBuffer.put(positions);
        positionBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        colorBuffer = cbb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        ByteBuffer nbb = ByteBuffer.allocateDirect(normals.length * 4);
        nbb.order(ByteOrder.nativeOrder());
        normalBuffer = nbb.asFloatBuffer();
        normalBuffer.put(normals);
        normalBuffer.position(0);

        ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        texCoordsBuffer = tbb.asFloatBuffer();
        texCoordsBuffer.put(texCoords);
        texCoordsBuffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);


        int vertexShaderIndex = Utility.loadShader(GLES30.GL_VERTEX_SHADER,
                vertexShader);
        int fragmentShaderIndex = Utility.loadShader(GLES30.GL_FRAGMENT_SHADER,
                fragmentShader);

        program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vertexShaderIndex);
        GLES30.glAttachShader(program, fragmentShaderIndex);
        GLES30.glLinkProgram(program);

        GLES30.glUseProgram(program);
        GLES30.glFrontFace(GLES30.GL_CCW);
        GLES30.glEnable(GL10.GL_CULL_FACE);
    }

    /**
     * initialize buffer-array
     */
    protected abstract void initBufferResources();

    /**
     * initialize shader
     */
    protected abstract void initShader();

    public static void setCamara(float[] camPosition, float[] target) {
        Matrix.setLookAtM(view, 0,
                camPosition[0], camPosition[1], camPosition[2],
                target[0], target[1], target[2],
                0, 1, 0);
    }

    public static void setProj(float fovy, float aspect, float near, float far) {
        Matrix.perspectiveM(proj, 0, fovy, aspect, near, far);
    }

    public void setWorlds(float[][] worlds) {
        this.worlds = worlds;
    }

    private void bindVerticesAndIndices() {
        int positionHandle = GLES30.glGetAttribLocation(program, "position");
        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 3 * Float.BYTES, positionBuffer);

        int colorHandle = GLES30.glGetAttribLocation(program, "color");
        GLES30.glEnableVertexAttribArray(colorHandle);
        GLES30.glVertexAttribPointer(colorHandle, 3, GLES30.GL_FLOAT, false, 3 * Float.BYTES, colorBuffer);

        int normalHandle = GLES30.glGetAttribLocation(program, "normal");
        GLES30.glEnableVertexAttribArray(normalHandle);
        GLES30.glVertexAttribPointer(normalHandle, 3, GLES30.GL_FLOAT, false, 3 * Float.BYTES, normalBuffer);

        int texCoordsHandle = GLES30.glGetAttribLocation(program, "texCoords");
        GLES30.glEnableVertexAttribArray(texCoordsHandle);
        GLES30.glVertexAttribPointer(texCoordsHandle, 2, GLES30.GL_FLOAT, false, 2 * Float.BYTES, texCoordsBuffer);
    }

    protected void bindObjectPerCB(int i) {
        int worldHandle = GLES30.glGetUniformLocation(program, "world");
        GLES30.glUniformMatrix4fv(worldHandle, 1, false, worlds[i], 0);
    }

    private void bindMainPassCB() {
        int viewHandle = GLES30.glGetUniformLocation(program, "view");
        GLES30.glUniformMatrix4fv(viewHandle, 1, false, view, 0);

        int projHandle = GLES30.glGetUniformLocation(program, "proj");
        GLES30.glUniformMatrix4fv(projHandle, 1, false, proj, 0);
    }

    public void draw(int count, int[] startOffset, int[] length) {

        bindVerticesAndIndices();
        bindMainPassCB();

        for (int i = 0; i < count; i++) {
            bindObjectPerCB(i);
            //TODO : refactor to binding buffer
            GLES30.glDrawRangeElements(GLES30.GL_TRIANGLES, startOffset[i], startOffset[i] + length[i], length[i] / 3, GLES30.GL_SHORT, indexBuffer);
        }

        //TODO : unbindVerticesAndIndices();
    }


    protected void setVertexShader(String vertexShader) {
        this.vertexShader = vertexShader;
    }

    protected void setFragmentShader(String fragmentShader) {
        this.fragmentShader = fragmentShader;
    }
}
