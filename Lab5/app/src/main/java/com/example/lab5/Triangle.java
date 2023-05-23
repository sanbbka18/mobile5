package com.example.lab5;

import static com.example.lab5.OpenGLES20Activity.checkGlError;
import static com.example.lab5.OpenGLES20Activity.loadShader;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
public class Triangle {
    // текст вершинного шейдеру
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main(){" +
                    "gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    // текст піксельного шейдеру
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main(){" +
                    "gl_FragColor = vColor;" +
                    "}";

    private FloatBuffer vertexBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // число координат для кожної вершини
    static final int COORDS_PER_VERTEX = 3;
    float triangleCoords[] = {
            // у порядку проти часової стрілки
            0.0f, 0.622008459f, 0.1f, // верx
            -0.5f, -0.311004243f, 0.1f, // зліва унизу
            0.5f, -0.311004243f, 0.1f // справа унизу
    };

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 байта для вершини
    float color[] = {0.863671875f, 0.876953125f, 0.22265625f, 0.0f};

    public Triangle() {
        // масив байтів для координат вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // 4 байта для float
                triangleCoords.length * 4);
        // порядок байтів пристрою
        bb.order(ByteOrder.nativeOrder());
        // вершинний масив
        vertexBuffer = bb.asFloatBuffer();
        // додавання координат
        vertexBuffer.put(triangleCoords);
        // на початок масиву
        vertexBuffer.position(0);
        // компіляція шейдерів
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        // створення програми і підключення шейдерів
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    /**
     * Містить інструкції OpenGL ES для рисування фігури.
     *
     * @param mvpMatrix - Матриця для рисування
     */
    public void draw(float[] mvpMatrix) {
        // використовуємо програму з шейдерами
        GLES20.glUseProgram(mProgram);
        // одержати вказівник до змінної vPosition у вершинному шейдері
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Включення
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Підготовка координат фігури
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        // одержати вказівник до змінної vColor у піксельному шейдері
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor"); // колір фігури
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        // вказівник на матрицю перетворення
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation");
        // Застосувати трансформації проектування і виду
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        checkGlError("glUniformMatrix4fv"); // Рисування по вершинам
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // Відключення вершинного масиву
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void scale(float scaleFactor) {
        // масштабування вершин трикутника
        for (int i = 0; i < triangleCoords.length; i++) {
            triangleCoords[i] *= scaleFactor;
        }
        // оновлення буфера вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
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

    public void translateTriangle(float dx, float dy) {
        for (int i = 0; i < triangleCoords.length; i += COORDS_PER_VERTEX) {
            triangleCoords[i] += dx;
            triangleCoords[i + 1] += dy;
        }
        // Оновлення буфера вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);
    }
}

