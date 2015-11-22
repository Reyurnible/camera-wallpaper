package com.hosshan.android.camerawallpaper;

import android.Manifest;
import android.content.Context;
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
            mCameraCallback = new CameraCallback(getApplicationContext());
            holder.addCallback(mCameraCallback);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
        }

        private class CameraCallback implements SurfaceHolder.Callback {
            private Context mContext;
            private Semaphore mCameraOpenCloseLock = new Semaphore(1);
            private CameraDevice mCameraDevice;

            public CameraCallback(Context context) {
                mContext = context;
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                openCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                closeCamera();
            }

            private void openCamera() {
                CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                try {
                    Log.d(TAG, "tryAcquire");
                    if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                        throw new RuntimeException("Time out waiting to lock camera opening.");
                    }
                    String cameraId = manager.getCameraIdList()[0];
                    manager.openCamera(cameraId, mStateCallback, null);
                } catch (CameraAccessException | SecurityException e) {
                    // Nothing action
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    // Currently an NPE is thrown when the Camera2API is used but not supported on the device this code runs.
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted while trying to lock camera opening.");
                }
            }

            private void closeCamera() {
                try {
                    mCameraOpenCloseLock.acquire();
                    if (null != mCameraDevice) {
                        mCameraDevice.close();
                        mCameraDevice = null;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted while trying to lock camera closing.");
                } finally {
                    mCameraOpenCloseLock.release();
                }
            }

            private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

                @Override
                public void onOpened(CameraDevice cameraDevice) {
                    Log.d(TAG, "deviceCallback.onOpened() start");
                    mCameraDevice = cameraDevice;
                    Surface surface = getSurfaceHolder().getSurface();
                    List<Surface> surfaceList = Collections.singletonList(surface);
                    try {
                        mCameraDevice.createCaptureSession(surfaceList, mSessionCallback, null);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "couldn't create capture session for camera: " + mCameraDevice.getId(), e);
                        return;
                    }
                    mCameraOpenCloseLock.release();
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    Log.d(TAG, "deviceCallback.onDisconnected() start");
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    Log.d(TAG, "deviceCallback.onError() start");
                }
            };

            CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.i(TAG, "capture session configured: " + session);
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "capture session configure failed: " + session);
                }
            };
        }
    }
}
