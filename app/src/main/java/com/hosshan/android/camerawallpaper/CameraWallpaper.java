package com.hosshan.android.camerawallpaper;

import android.content.Context;
import android.content.ContextWrapper;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by shunhosaka on 2015/11/22.
 * http://techbooster.jpn.org/andriod/application/6738/
 * http://devdroid123.blog.fc2.com/blog-entry-22.html
 * https://github.com/googlesamples/android-Camera2Video/blob/master/Application/src/main/java/com/example/android/camera2video/Camera2VideoFragment.java
 */
public class CameraWallpaper extends WallpaperService {
    public static final String TAG = CameraWallpaper.class.getSimpleName();

    private CameraEngine.CameraCallback mCameraCallback;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new CameraEngine();
    }

    // Live Wallpaperの描画などを個なうクラス
    class CameraEngine extends Engine {

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            mCameraCallback = new CameraCallback(CameraWallpaper.this);
            holder.addCallback(mCameraCallback);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
        }

        private class CameraCallback implements SurfaceHolder.Callback {
            private ContextWrapper mContext;
            private Camera mCamera;

            public CameraCallback(ContextWrapper context) {
                mContext = context;
            }

            // 画面が開いた時に呼ばれるメソッド
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                openCamera(holder);
            }

            // 画面が破棄されるときに呼ばれるメソッド
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                closeCamera();
            }

            // 画面が回転した時などに呼ばれるメソッド
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                changeCameraState(width, height);
            }

            private void openCamera(SurfaceHolder holder) {
                // カメラインスタンスを取得
                mCamera = Camera.open();
                try {
                    mCamera.setPreviewDisplay(holder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void closeCamera() {
                // カメラインスタンス開放
                mCamera.release();
                mCamera = null;
            }

            private void changeCameraState(int width, int height) {
                Log.d(TAG, "surfaceChanged width:" + width + " height:" + height);

                Camera.Parameters parameters = mCamera.getParameters();

                // デバッグ用表示
                Camera.Size size = parameters.getPictureSize();
                Log.d(TAG, "getPictureSize width:" + size.width + " size.height:" + size.height);
                size = parameters.getPreviewSize();
                Log.d(TAG, "getPreviewSize width:" + size.width + " size.height:" + size.height);

                // プレビューのサイズを変更
                 parameters.setPreviewSize(size.width, size.height);    // 画面サイズに合わせて変更しようとしたが失敗する
                // 使用できるサイズはカメラごとに決まっているみたいなので、うまくいかなければこちらを使う
                // parameters.setPreviewSize(640, 480);

                // 縦画面の場合回転させる
                if (width < height) {
                    // 縦画面
                    mCamera.setDisplayOrientation(90);
                }else{
                    // 横画面
                    mCamera.setDisplayOrientation(0);
                }

                // パラメーターセット
                mCamera.setParameters(parameters);
                // プレビュー開始
                mCamera.startPreview();
            }

        }
    }
}
