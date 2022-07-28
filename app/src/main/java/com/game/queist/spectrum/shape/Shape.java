package com.game.queist.spectrum.shape;

import android.opengl.GLES30;

import com.game.queist.spectrum.utils.Utility;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public abstract class Shape {

    private final int program;

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

    public Shape() {
        initBufferResources();
        initShader();

        ByteBuffer vbb = ByteBuffer.allocateDirect(positions.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        positionBuffer = vbb.asFloatBuffer();
        positionBuffer.put(positions);
        positionBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        colorBuffer = vbb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        ByteBuffer nbb = ByteBuffer.allocateDirect(normals.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        normalBuffer = vbb.asFloatBuffer();
        normalBuffer.put(normals);
        normalBuffer.position(0);

        ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        texCoordsBuffer = vbb.asFloatBuffer();
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
    }

    /**
     * initialize buffer-array
     */
    protected abstract void initBufferResources();

    /**
     * initialize shader
     */
    protected abstract void initShader();

    public void enableBuffer() {

    }
    public void disableBuffer() {

    }
    public void draw(int startOffset, int length) {
        GLES30.glUseProgram(program);
        GLES30.glFrontFace(GLES30.GL_CCW);
        GLES30.glEnable(GL10.GL_CULL_FACE);

        int positionHandle = GLES30.glGetAttribLocation(program, "position");
        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 3 * 4, positionBuffer);

        int colorHandle = GLES30.glGetAttribLocation(program, "color");
        GLES30.glEnableVertexAttribArray(colorHandle);
        GLES30.glVertexAttribPointer(colorHandle, 3, GLES30.GL_FLOAT, false, 3 * 4, colorBuffer);

        int normalHandle = GLES30.glGetAttribLocation(program, "normal");
        GLES30.glEnableVertexAttribArray(normalHandle);
        GLES30.glVertexAttribPointer(normalHandle, 3, GLES30.GL_FLOAT, false, 3 * 4, normalBuffer);

        int texCoordsHandle = GLES30.glGetAttribLocation(program, "texCoords");
        GLES30.glEnableVertexAttribArray(texCoordsHandle);
        GLES30.glVertexAttribPointer(texCoordsHandle, 2, GLES30.GL_FLOAT, false, 2 * 4, texCoordsBuffer);

        /**
         * TODO : bind constant buffer
         */

        GLES30.glDrawRangeElements(GLES30.GL_TRIANGLES, startOffset, startOffset + length, length / 3, GLES30.GL_SHORT, indexBuffer);
        /*gl.glFrontFace(GL10.GL_CCW);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);


        gl.glCullFace(GL10.GL_BACK);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glColorPointer(3, GL10.GL_FLOAT, 0, colorBuffer);
        gl.glNormalPointer(3, GL10.GL_FLOAT, normalBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texCoordsBuffer);

        indexBuffer.position(startOffset);
        indexBuffer.limit(startOffset + length);
        ShortBuffer buffer = indexBuffer.slice();
        gl.glDrawElements(GL10.GL_TRIANGLES, length,
                GL10.GL_UNSIGNED_SHORT, buffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisable(GL10.GL_CULL_FACE);*/
    }


    protected void setVertexShader(String vertexShader) {
        this.vertexShader = vertexShader;
    }

    protected void setFragmentShader(String fragmentShader) {
        this.fragmentShader = fragmentShader;
    }
}
