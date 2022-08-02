package com.game.queist.spectrum.shape;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.game.queist.spectrum.utils.Utility;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public abstract class Shape {
    private Context context;

    protected int program;

    private int vertexShader;
    private int fragmentShader;

    private FloatBuffer positionBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer texCoordsBuffer;
    private ShortBuffer indexBuffer;

    protected float[] positions;
    protected float[] normals;
    protected float[] texCoords;
    protected short[] indices;

    protected float[][] worlds;

    private static float[] view = new float[16];
    private static float[] proj = new float[16];

    private int indexBufferIndex;
    private int vertexBufferIndex;

    private int positionAttrIndex;
    private int normalAttrIndex;
    private int texCoordsAttrIndex;

    public Shape(Context context) {
        this.context = context;
    }

    /**
     * must be called after constructor
     */
    public void initialize() {
        initBufferResources();
        initShader();
        generateBuffer();
        generateVerticesAndIndices();
    }

    /**
     * initialize buffer-array
     */
    protected abstract void initBufferResources();

    /**
     * initialize shader
     */
    protected abstract void initShader();

    protected void generateBuffer() {
        ByteBuffer vbb = ByteBuffer.allocateDirect(positions.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        positionBuffer = vbb.asFloatBuffer();
        positionBuffer.put(positions);
        positionBuffer.position(0);

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

        program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);
    }

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

    protected void generateVerticesAndIndices() {
        int[] vertexBufferIndex = new int[3];
        GLES30.glGenBuffers(3, vertexBufferIndex, 0);
        positionAttrIndex = vertexBufferIndex[0];
        normalAttrIndex = vertexBufferIndex[1];
        texCoordsAttrIndex = vertexBufferIndex[2];

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBufferIndex[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, positions.length * Float.BYTES, positionBuffer, GLES30.GL_STATIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBufferIndex[1]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, normals.length * Float.BYTES, normalBuffer, GLES30.GL_STATIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBufferIndex[2]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, texCoords.length * Float.BYTES, texCoordsBuffer, GLES30.GL_STATIC_DRAW);

        int[] vertexArrayIndex = new int[1];
        GLES30.glGenVertexArrays(1, vertexArrayIndex, 0);
        this.vertexBufferIndex = vertexArrayIndex[0];
        GLES30.glBindVertexArray(vertexArrayIndex[0]);

        int[] bufferIndex = new int[1];
        GLES30.glGenBuffers(1, bufferIndex, 0);
        indexBufferIndex = bufferIndex[0];
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, bufferIndex[0]);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.limit() * Short.BYTES, indexBuffer, GLES30.GL_STATIC_DRAW);

        GLES30.glBindVertexArray(0);
    }

    protected void bindVerticesAndIndices() {
        GLES30.glBindVertexArray(vertexBufferIndex);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBufferIndex);

        int positionHandle = GLES30.glGetAttribLocation(program, "position");
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, positionAttrIndex);
        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 3 * Float.BYTES, 0);

        int normalHandle = GLES30.glGetAttribLocation(program, "normal");
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, normalAttrIndex);
        GLES30.glEnableVertexAttribArray(normalHandle);
        GLES30.glVertexAttribPointer(normalHandle, 3, GLES30.GL_FLOAT, false, 3 * Float.BYTES, 0);

        int texCoordsHandle = GLES30.glGetAttribLocation(program, "texCoords");
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, texCoordsAttrIndex);
        GLES30.glEnableVertexAttribArray(texCoordsHandle);
        GLES30.glVertexAttribPointer(texCoordsHandle, 2, GLES30.GL_FLOAT, false, 2 * Float.BYTES, 0);
    }

    protected void unBindVerticesAndIndices() {
        int positionHandle = GLES30.glGetAttribLocation(program, "position");
        GLES30.glDisableVertexAttribArray(positionHandle);

        int normalHandle = GLES30.glGetAttribLocation(program, "normal");
        GLES30.glDisableVertexAttribArray(normalHandle);

        int texCoordsHandle = GLES30.glGetAttribLocation(program, "texCoords");
        GLES30.glDisableVertexAttribArray(texCoordsHandle);
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

    protected void draw(int count, int[] startOffset, int[] length) {
        GLES30.glUseProgram(program);
        GLES30.glFrontFace(GLES30.GL_CCW);

        bindVerticesAndIndices();
        bindMainPassCB();
        for (int i = 0; i < count; i++) {
            bindObjectPerCB(i);
            //GLES30.glDrawRangeElements(GLES30.GL_TRIANGLES, 0, 360, 360, GLES30.GL_UNSIGNED_SHORT, 0);
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, length[i], GLES30.GL_UNSIGNED_SHORT, startOffset[i] * Short.BYTES);
        }
        unBindVerticesAndIndices();
    }


    protected void setVertexShader(String shaderName) {
        this.vertexShader = Utility.loadShader(context, GLES30.GL_VERTEX_SHADER, shaderName);
    }

    protected void setFragmentShader(String shaderName) {
        this.fragmentShader = Utility.loadShader(context, GLES30.GL_FRAGMENT_SHADER, shaderName);
    }
}
