package com.example.lab5;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView {
    private final MyGLRenderer mRenderer;
    private GestureDetector mGestureDetector;

    private boolean mDragging = false;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Створення контексту OpenGL ES 2.0
        setEGLContextClientVersion(2);
        // Підключення Renderer для рисування на поверхні GLSurfaceView
        mRenderer = new MyGLRenderer();

        // налаштування обробника жестів
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // обробка подвійного кліку
                queueEvent(() -> {
                    float scaleFactor = 1.5f;
                    mRenderer.scaleFigures(scaleFactor);
                });
                return true;
            }
        });

        setRenderer(mRenderer);
    }

    float mPrevX; float mPrevY; // координати останньої точки

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(); float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN: // якщо вказівник торкнувся екрану
                mDragging = true;
                break;

            case MotionEvent.ACTION_MOVE: // якщо вказівник рухається
                // отримаємо дистанцію
                float dx = x - mPrevX; float dy = y - mPrevY;
                if (e.getPointerCount() == 1) {
                    if (mDragging) {
                        float dx_ = x - mPrevX;
                        float dy_ = y - mPrevY;

                        float translateX = dx_ / getWidth();
                        float translateY = -dy_ / getHeight();

                        // Обмеження для руху трикутника
                        float maxTranslationX = 0.9f; // Максимальне зміщення по осі X
                        float maxTranslationY = 0.9f; // Максимальне зміщення по осі Y

                        translateX = clamp(translateX, -maxTranslationX, maxTranslationX);
                        translateY = clamp(translateY, -maxTranslationY, maxTranslationY);

                        mRenderer.translateFigures(translateX, translateY);
                    }
                } else if (e.getPointerCount() == 2){
                    // обчислення кута
                    mRenderer.setAngle(mRenderer.getAngle() + ((dx + dy) * 180.0f / 320));
                }
                requestRender(); // запит на рисування
                break;

            case MotionEvent.ACTION_UP: // якщо вказівник піднято з екрану
                mDragging = false;
                break;
        }
        mPrevX = x; mPrevY = y;
        // Передача подій в об'єкт GestureDetector
        mGestureDetector.onTouchEvent(e);
        return true;
    }

    public void changeColor() {
        mRenderer.changeColor();
    }

    public void setCurrentShape(int currentShape) {
        mRenderer.setCurrentShape(currentShape);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
}
