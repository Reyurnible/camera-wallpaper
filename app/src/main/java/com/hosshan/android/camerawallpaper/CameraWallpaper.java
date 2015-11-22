package com.hosshan.android.camerawallpaper;

import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

/**
 * Created by shunhosaka on 2015/11/22.
 */
public class CameraWallpaper extends WallpaperService {
    public static final String TAG = CameraWallpaper.class.getSimpleName();
    private final CameraWallpaper self = this;

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

        return null;
    }

    class CameraEngine extends Engine {
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
        }
    }

}
