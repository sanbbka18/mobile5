package com.example.lab5;

import static com.example.lab5.OpenGLES20Activity.checkGlError;
import static com.example.lab5.OpenGLES20Activity.loadShader;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Cube {

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main(){" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main(){" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    static final int COORDS_PER_VERTEX = 3;

    float cubeCoords[] = {
            // Передня грань
            -0.25f, -0.25f, 0.25f,  // вгорі ліворуч
            -0.25f, -0.75f, 0.25f,  // внизу ліворуч
            0.25f, -0.75f, 0.25f,  // праворуч внизу
            0.25f, -0.25f, 0.25f,  // вгорі справа

            // Задня грань
            -0.25f, -0.25f, -0.25f,  // вгорі ліворуч
            -0.25f, -0.75f, -0.25f,  // внизу ліворуч
            0.25f, -0.75f, -0.25f,  // праворуч внизу
            0.25f, -0.25f, -0.25f,  // вгорі справа

            // Верхня грань
            -0.25f, -0.25f, -0.25f,  // передній лівий
            -0.25f, -0.25f, 0.25f,  // передній правий
            0.25f, -0.25f, 0.25f,  // назад праворуч
            0.25f, -0.25f, -0.25f,  // назад ліворуч


            // Нижня грань
            -0.25f, -0.75f, -0.25f,  // передній лівий
            -0.25f, -0.75f, 0.25f,  // передній правий
            0.25f, -0.75f, 0.25f,  // назад праворуч
            0.25f, -0.75f, -0.25f,  // назад ліворуч

            // Права грань
            0.25f, -0.25f, -0.25f,  // верхній перед
            0.25f, -0.25f, 0.25f,  // зверху назад
            0.25f, -0.75f, 0.25f,  // знизу назад
            0.25f, -0.75f, -0.25f,  // нижній передній

            // Ліва грань
            -0.25f, -0.25f, -0.25f,  // верхній перед
            -0.25f, -0.25f, 0.25f,  // зверху назад
            -0.25f, -0.75f, 0.25f,  // знизу назад
            -0.25f, -0.75f, -0.25f  // нижній передній
    };

    // Порядок малювання вершин
    private final short drawOrder[] = {
            0, 1, 2, 0, 2, 3,  // Передня грань
            4, 5, 6, 4, 6, 7,  // Задня грань
            8, 9, 10, 8, 10, 11,  // Верхня грань
            12, 13, 14, 12, 14, 15,  // Нижня грань
            16, 17, 18, 16, 18, 19,  // Права грань
            20, 21, 22, 20, 22, 23  // Ліва грань
    };

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 байта для вершини

    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    public Cube() {
        // Ініціалізація байтового буфера вершини для координат форми
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
        vertexBuffer.position(0);

        // Ініціалізація байтового буфера для списку малювання
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // Підготовка шейдерів і програми OpenGL
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation");

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        checkGlError("glUniformMatrix4fv");

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void scale(float scaleFactor) {
        // масштабування вершин куба
        for (int i = 0; i < cubeCoords.length; i++) {
            cubeCoords[i] *= scaleFactor;
        }
        // оновлення буфера вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
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

    public void translateCube(float dx, float dy) {
        for (int i = 0; i < cubeCoords.length; i += COORDS_PER_VERTEX) {
            cubeCoords[i] += dx;
            cubeCoords[i + 1] += dy;
        }
        // Оновлення буфера вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
        vertexBuffer.position(0);
    }
}
