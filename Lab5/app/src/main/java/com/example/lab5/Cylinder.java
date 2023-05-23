package com.example.lab5;

import static com.example.lab5.OpenGLES20Activity.checkGlError;
import static com.example.lab5.OpenGLES20Activity.loadShader;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Cylinder {
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

    private final int mProgram;
    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    private static int numPoints = 250;  // Кількість точок на колі
    private static float radius = 0.25f; // Радіус циліндра
    private static float height = 0.5f; // Висота циліндра

    float color[] = {0.863671875f, 0.876953125f, 0.22265625f, 0.0f};

    // Кількість координат на вершину
    private static final int COORDS_PER_VERTEX = 3;

    private static float[] cylinderCoords;
    private static short[] drawOrder;

    private static void generateCylinderGeometry() {
        // Генеруємо координати вершин бічної поверхні циліндра
        cylinderCoords = new float[numPoints * 12];
        drawOrder = new short[numPoints * 36];

        float angleStep = 360.0f / numPoints;
        float angle = 150.0f;
        int index = 0;

        for (int i = 0; i < numPoints; i++) {
            float x = radius * (float) Math.cos(Math.toRadians(angle));
            float z = radius * (float) Math.sin(Math.toRadians(angle));

            // Вершина на верхній основі
            cylinderCoords[index++] = x;
            cylinderCoords[index++] = height / 2 + 0.25f;
            cylinderCoords[index++] = z;

            // Вершина на нижній основі
            cylinderCoords[index++] = x;
            cylinderCoords[index++] = -height / 2 + 0.25f;
            cylinderCoords[index++] = z;

            // Вершина на верхній поверхні
            cylinderCoords[index++] = x;
            cylinderCoords[index++] = height / 2 + 0.25f;
            cylinderCoords[index++] = z;

            // Вершина на нижній поверхні
            cylinderCoords[index++] = x;
            cylinderCoords[index++] = -height / 2 + 0.25f;
            cylinderCoords[index++] = z;

            angle += angleStep;
        }

        index = 0;

        // Малюємо бічну поверхню циліндра
        for (short i = 0; i < numPoints; i++) {
            // Верхня сторона поверхні
            drawOrder[index++] = (short) (i * 4);
            drawOrder[index++] = (short) ((i + 1) % numPoints * 4);
            drawOrder[index++] = (short) (i * 4 + 1);
            drawOrder[index++] = (short) (i * 4 + 1);
            drawOrder[index++] = (short) ((i + 1) % numPoints * 4);
            drawOrder[index++] = (short) (((i + 1) % numPoints * 4) + 1);

            // Нижня сторона поверхні
            drawOrder[index++] = (short) (i * 4 + 2);
            drawOrder[index++] = (short) ((i + 1) % numPoints * 4 + 2);
            drawOrder[index++] = (short) (i * 4 + 3);
            drawOrder[index++] = (short) (i * 4 + 3);
            drawOrder[index++] = (short) ((i % numPoints * 4 + 2));
            drawOrder[index++] = (short) (((i + 1) % numPoints * 4 + 3));

            // Верхня основа
            drawOrder[index++] = (short) (i * 4);
            drawOrder[index++] = (short) (i * 4 + 1);
            drawOrder[index++] = (short) ((i + 1) % numPoints * 4 + 1);
            drawOrder[index++] = (short) (i * 4);
            drawOrder[index++] = (short) ((i + 1) % numPoints * 4 + 1);
            drawOrder[index++] = (short) ((i + 1) % numPoints * 4);

            // Нижня основа
            drawOrder[index++] = (short) (i * 4 + 2);
            drawOrder[index++] = (short) ((i + 1) % numPoints * 4 + 2);
            drawOrder[index++] = (short) (i * 4 + 3);
            drawOrder[index++] = (short) (i * 4 + 3);
            drawOrder[index++] = (short) ((i + 1) % numPoints * 4 + 2);
            drawOrder[index++] = (short) (((i + 1) % numPoints * 4 + 3));
        }
    }


    private final int vertexStride = COORDS_PER_VERTEX * 4; // Bytes per vertex

    public Cylinder() {
        generateCylinderGeometry();

        // Ініціалізуємо буфери вершин і порядку малювання
        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(cylinderCoords.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = vertexByteBuffer.asFloatBuffer();
        vertexBuffer.put(cylinderCoords);
        vertexBuffer.position(0);

        ByteBuffer drawListByteBuffer = ByteBuffer.allocateDirect(drawOrder.length * 2);
        drawListByteBuffer.order(ByteOrder.nativeOrder());
        drawListBuffer = drawListByteBuffer.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // Компілюємо шейдери
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // Створюємо OpenGL ES програму і прикріплюємо шейдери
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix) {
        // Вказуємо OpenGL використовувати програму
        GLES20.glUseProgram(mProgram);

        // Отримуємо позиції атрибутів з програми
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Ввімкнення атрибуту вершин
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Передача координат вершин в шейдер
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer
        );

        // Отримуємо розмір матриці проекції
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Передача матриці проекції в шейдер
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        checkGlError("glUniformMatrix4fv");

        // Малюємо циліндр
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer
        );

        // Вимкнення атрибуту вершин
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void scale(float scaleFactor) {
        // масштабування вершин циліндра
        for (int i = 0; i < cylinderCoords.length; i++) {
            cylinderCoords[i] *= scaleFactor;
        }
        // оновлення буфера вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(cylinderCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer.clear();
        vertexBuffer.put(cylinderCoords);
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

    public void translateCylinder(float dx, float dy) {
        for (int i = 0; i < cylinderCoords.length; i += COORDS_PER_VERTEX) {
            cylinderCoords[i] += dx;
            cylinderCoords[i + 1] += dy;
        }
        // Оновлення буфера вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(cylinderCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer.clear();
        vertexBuffer.put(cylinderCoords);
        vertexBuffer.position(0);
    }
}
