package com.game.queist.spectrum.shape;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.game.queist.spectrum.utils.Utility;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

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

    /*Cam*/
    public static float[] getCamPosition() {
        return camPosition;
    }

    public static float[] getView() {
        return view;
    }

    public static float[] getProj() {
        return proj;
    }

    private static float[] camPosition = new float[3];
    private static float[] view = new float[16];
    private static float[] proj = new float[16];

    /*Light*/
    private static float[] lightPosition = new float[3 * 3];
    private static float[] lightColor = new float[3 * 3];


    private float shininess;
    private float[] fresnelR0 = new float[3];

    private int indexBufferIndex;
    private int vertexBufferIndex;

    private int positionAttrIndex;
    private int normalAttrIndex;
    private int texCoordsAttrIndex;

    private String[] textures;
    private static HashMap<String, Integer> textureMap = new HashMap<>();
    private float[][] texTransforms;

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
        Shape.camPosition = camPosition;
    }

    public static void setProj(float fovy, float aspect, float near, float far) {
        Matrix.perspectiveM(proj, 0, fovy, aspect, near, far);
    }

    public static void setLight(float[] lightPosition, float[] lightColor) {
        Shape.lightPosition = lightPosition;
        Shape.lightColor = lightColor;
    }

    protected void setMaterial(float shininess, float[] fresnelR0) {
        this.shininess = shininess;
        this.fresnelR0 = fresnelR0;
    }

    public void setWorlds(float[][] worlds) {
        this.worlds = worlds;
    }

    public void setTexTransforms(float[][] texTransforms) { this.texTransforms = texTransforms; }

    public void setTextures(String[] textures) { this.textures = textures; }

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

        int texTransformHandle = GLES30.glGetUniformLocation(program, "texTransform");
        GLES30.glUniformMatrix4fv(texTransformHandle, 1, false, texTransforms[i], 0);

        /*Texture*/
        if (textureMap.get(textures[i]) != null) {
            int textureIndex = textureMap.get(textures[i]);
            int textureHandle = GLES30.glGetUniformLocation(program, "texture1");
            GLES30.glUniform1i(textureHandle, textureIndex);
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + textureIndex);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIndex);
        }
    }

    private void bindMainPassCB() {
        /*Cam*/
        int camPositionHandle = GLES30.glGetUniformLocation(program, "camPosition");
        GLES30.glUniform3fv(camPositionHandle, 1, camPosition, 0);

        int viewHandle = GLES30.glGetUniformLocation(program, "view");
        GLES30.glUniformMatrix4fv(viewHandle, 1, false, view, 0);

        int projHandle = GLES30.glGetUniformLocation(program, "proj");
        GLES30.glUniformMatrix4fv(projHandle, 1, false, proj, 0);

        /*Light*/
        int lightPositionHandle = GLES30.glGetUniformLocation(program, "lightPosition");
        GLES30.glUniform3fv(lightPositionHandle, 3, lightPosition,0);

        int lightColorHandle = GLES30.glGetUniformLocation(program, "lightColor");
        GLES30.glUniform3fv(lightColorHandle, 3, lightColor,0);

        /*Material*/
        int shininessHandle = GLES30.glGetUniformLocation(program, "shininess");
        GLES30.glUniform1f(shininessHandle, shininess);

        int fresnelR0Handle = GLES30.glGetUniformLocation(program, "fresnelR0");
        GLES30.glUniform3fv(fresnelR0Handle, 1, fresnelR0, 0);
    }

    protected void draw(int count, int[] startOffset, int[] length) {
        GLES30.glUseProgram(program);
        GLES30.glFrontFace(GLES30.GL_CCW);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_POLYGON_OFFSET_FILL);
        GLES30.glDepthFunc(GLES30.GL_LEQUAL);
        GLES30.glEnable(GLES30.GL_SAMPLE_ALPHA_TO_COVERAGE);

        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD);

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

    protected void createTexture(String textureName, int resourceID) {
        int[] textureID = new int[1];
        GLES30.glGenTextures(1, textureID, 0);
        textureMap.put(textureName, textureID[0]);

        GLES30.glActiveTexture(textureID[0]);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID[0]);
        Bitmap texture = BitmapFactory.decodeResource(context.getResources(), resourceID);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, texture, 0);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
    }

    protected void createTexture(String textureName, String fileName) {
        int[] textureID = new int[1];
        GLES30.glGenTextures(1, textureID, 0);
        textureMap.put(textureName, textureID[0]);

        GLES30.glActiveTexture(textureID[0]);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID[0]);
        Bitmap texture = BitmapFactory.decodeFile(fileName);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, texture, 0);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
    }
}
