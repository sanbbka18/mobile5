package com.example.lab5;

import static com.example.lab5.OpenGLES20Activity.checkGlError;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Square {
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main(){"+ "" +
                    "gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "gl_FragColor= vColor;" +
                    "}";

    private FloatBuffer vertexBuffer; private final ShortBuffer drawListBuffer;
    private final int mProgram; private int mPositionHandle; private int mColorHandle;
    private int mMVPMatrixHandle; static final int COORDS_PER_VERTEX = 3;
    float squareCoords[] = {
            -0.5f, 0.5f, 0.0f, // вверху зліва
            -0.5f, -0.5f, 0.0f, // внизу зліва
            0.5f, -0.5f, 0.0f, // внизу праворуч
            0.5f, 0.5f, 0.0f }; // вверху праворуч
    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // порядок вершин
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    public Square () {
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        int vertexShader = OpenGLES20Activity.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = OpenGLES20Activity.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram (mProgram);
        mPositionHandle = GLES20.glGetAttribLocation (mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray (mPositionHandle);
        GLES20.glVertexAttribPointer ( mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        mColorHandle = GLES20.glGetUniformLocation (mProgram, "vColor");
        GLES20.glUniform4fv (mColorHandle, 1, color, 0);
        mMVPMatrixHandle = GLES20.glGetUniformLocation (mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv (mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        checkGlError("glUniformMatrix4fv");
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        checkGlError("glDrawElements");
        GLES20.glDisableVertexAttribArray (mPositionHandle);
        checkGlError("glDisableVertexAttribArray");
    }

    public void scale(float scaleFactor) {
        // масштабування вершин квадрата
        for (int i = 0; i < squareCoords.length; i++) {
            squareCoords[i] *= scaleFactor;
        }
        // оновлення буфера вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
    }

    public void changeColor() {
        float red = (float) Math.random();
        float green = (float) Math.random();
        float blue = (float) Math.random();
        color[0] = red;
        color[1] = green;
        color[2] = blue;
    }

    public void translateSquare(float dx, float dy) {
        for (int i = 0; i < squareCoords.length; i += COORDS_PER_VERTEX) {
            squareCoords[i] += dx;
            squareCoords[i + 1] += dy;
        }
        // Оновлення буфера вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
    }
}