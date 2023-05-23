package com.example.lab5;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Містить інструкції для рисування для GLSurfaceView
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Triangle mTriangle;
    private Square mSquare;

    // mMVPMatrix абревіатура для "Модель Вид Проекція Матриця"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    private float mAngle;

    private float mScaleFactor = 1.0f; // оголошення змінної

    private Cube mCube;
    private Cylinder mCylinder;

    private int currentShape;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // фоновий колір для кадра
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        // створення фігур
        mTriangle = new Triangle();
        mSquare = new Square();
        mCube = new Cube();
        mCylinder = new Cylinder();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];

        GLES20.glClearDepthf(1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // Очищення
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Анімація обертання навколо вертикальної вісі
        long time = SystemClock.uptimeMillis();
        float angle = 0.00050f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, 1.0f);
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        // Розташування камери
        Matrix.setLookAtM(mViewMatrix, 0, (float) (4 * Math.sin(angle)), 0,
                (float) (-4 * Math.cos(angle)), 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        // Обчислення трансформації проекції і виду
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        if (currentShape == 0) {
            // Рисування квадрата
            mSquare.draw(mMVPMatrix);
        } else if (currentShape == 1) {
            mCube.draw(scratch);
        }
        if (currentShape == 0) {
            // Обертання трикутника
            Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);
            // Об'єднання матриці обертання, проекції та виду у одну матрицю
            Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
            // Рисування трикутника
            mTriangle.draw(scratch);
        } if (currentShape == 1) {
            mCylinder.draw(scratch);
        }
    }

    @Override
    public void onSurfaceChanged (GL10 unused, int width, int height) {
        // Якщо поверхня змінилася, тоді оновлюємо viewport
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        // Оновлення матриці проекції, яка використовується в onDrawFrame()
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public void scaleFigures(float scaleFactor) {
        // збереження поточного масштабування
        mScaleFactor *= scaleFactor;
        // масштабування фігур
        if (currentShape == 0) {
            mTriangle.scale(mScaleFactor);
            mSquare.scale(mScaleFactor);
        } else {
            mCube.scale(mScaleFactor);
            mCylinder.scale(mScaleFactor);
        }
    }

    public void changeColor() {
        if (currentShape == 0) {
            mTriangle.changeColor();
            mSquare.changeColor();
        } else {
            mCube.changeColor();
            mCylinder.changeColor();
        }
    }

    public void translateFigures(float dx, float dy) {
        if (currentShape == 0) {
            mSquare.translateSquare(dx, dy);
            mTriangle.translateTriangle(dx, dy);
        } else {
            mCube.translateCube(dx, dy);
            mCylinder.translateCylinder(dx, dy);
        }
    }

    // кут обертання
    public float getAngle() {
        return mAngle;
    }
    public void setAngle(float angle) {
        mAngle = angle;
    }

    public void setCurrentShape(int shape) {
        currentShape = shape;
    }
}
