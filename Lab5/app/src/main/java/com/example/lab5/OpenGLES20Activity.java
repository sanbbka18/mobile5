package com.example.lab5;

import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class OpenGLES20Activity extends AppCompatActivity {
    static final String TAG = "OpenGLES20Activity";
    GLSurfaceView mGLView;

    private MediaPlayer mMediaPlayer;

    private int currentShape = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Створення і підключення компонент
        mGLView = new MyGLSurfaceView(this);

        mMediaPlayer = MediaPlayer.create(this, R.raw.melody); // ініціалізувати MediaPlayer з аудіофайлом
        mMediaPlayer.setLooping(true); // встановити повторення аудіофайлу
        mMediaPlayer.start(); // запустити відтворення аудіофайлу

        setContentView(mGLView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Зупинка відтворення мелодії та звільнення ресурсів
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
        mMediaPlayer = null;
        // Зупинка потоку рисування. Також тут можна звільнити пам'ять від ресурсів
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Запуск мелодії
        mMediaPlayer = MediaPlayer.create(this, R.raw.melody);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
        // Старт потоку рисування. Тут можна створити ресурси знову
        mGLView.onResume();
    }

    /**
     * Метод для компіляції OpenGL шейдеру.
     *
     * <p>У процесі розробки шейдерів, використовується checkGlError() для відладки
     * method to debug shader coding errors.</p>
     *
     * @param type - тип шейдера (GLES20.GL_VERTEX_SHADER або GL_FRAGMENT_SHADER)
     * @param shaderCode - текст шейдера
     * @return повертає іd для шейдера
     */

     public static int loadShader(int type, String shaderCode) {
         int shader = GLES20.glCreateShader(type); // створити шейдер
         // додати текст шейдера і компілювати його
         GLES20.glShaderSource(shader, shaderCode);
         GLES20.glCompileShader(shader);
         return shader;
     }

    /**
     * Метод відладки OpenGL викликів.
     *
     * @param glOperation - назва OpenGL виклику для перевірки
     */
    public static void checkGlError(String glOperation) {
        int error; // одержання всіх помилок у циклі
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            // створення виключення
            throw new RuntimeException(glOperation + ": glError ");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_color) {
            ((MyGLSurfaceView) mGLView).changeColor();
            return true;
        }
        if (id == R.id.action_figures) {
            // Змінюємо поточну форму при кожному натисканні
            currentShape = (currentShape + 1) % 2;

            ((MyGLSurfaceView) mGLView).setCurrentShape(currentShape);
            mGLView.requestRender();
        }

        return super.onOptionsItemSelected(item);
    }
}
