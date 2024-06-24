package com.example.mylibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.genymobile.scrcpy.ConfigurationException;
import com.genymobile.scrcpy.Controller;
import com.genymobile.scrcpy.Device;
import com.genymobile.scrcpy.FakeContext;
import com.genymobile.scrcpy.Options;
import com.genymobile.scrcpy.Point;
import com.genymobile.scrcpy.Position;
import com.genymobile.scrcpy.Workarounds;
import com.genymobile.scrcpy.wrappers.ClipboardManager;
import com.genymobile.scrcpy.wrappers.ServiceManager;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class Main extends ContextWrapper implements Callable<Object[]> {

    public static final int PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY = 1 << 20;

    public static final int PRIVATE_FLAG_TRUSTED_OVERLAY = 0x20000000;
    public static final int SHELL_UID = 2000;
    public static final String TAG = "日志";
    public static Context context = null;

    public static WindowManager windowManager = null;

    /*    public static View view = null;*/

    public static Handler handler;

    public static Context getSystemContext() {
        try {
            Class<?> atClazz = Class.forName("android.app.ActivityThread");
            Method systemMain = atClazz.getMethod("systemMain");
            Object activityThread = systemMain.invoke(null);
            Method getSystemContext = atClazz.getMethod("getSystemContext");
            return (Context) getSystemContext.invoke(activityThread);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("PrivateApi")
    public static Context createContext() {
        Resources systemRes = Resources.getSystem();
        Field systemResField = null;
        try {
            // This class only exists on LG ROMs with broken implementations
            Class.forName("com.lge.systemservice.core.integrity.IntegrityManager");
            // If control flow goes here, we need the resource hack
            Resources wrapper = new ResourcesWrapper(systemRes);
            systemResField = Resources.class.getDeclaredField("mSystem");
            systemResField.setAccessible(true);
            systemResField.set(null, wrapper);
        } catch (ReflectiveOperationException ignored) {
        }

        Context systemContext = getSystemContext();
        Context context = null;
        try {
            context = systemContext.createPackageContext(FakeContext.PACKAGE_NAME, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            context = systemContext;
        }

        // Restore the system resources object after classloader is available
        if (systemResField != null) {
            try {
                systemResField.set(null, systemRes);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        return context;
    }

    public Main() {
        super(null);
        context = createContext();
        attachBaseContext(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        handler = new Handler(Looper.getMainLooper());
    }

    public static void main(String[] args) {
        Looper.prepareMainLooper();
        try {
            new Main();
        } catch (Exception e) {
            Log.e("IPC", "Error in IPCMain", e);
        }
        // Main thread event loop
        // Looper.loop();
        Log.d(TAG, "main:  end");
    }

    /* @SuppressLint({"ClickableViewAccessibility", "WrongConstant"})
     void createSurface() {
         var windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
         var screenWidth = 2712;
         var screenHeight = 1220;

         int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                 Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? WindowManager.LayoutParams.TYPE_PHONE : WindowManager.LayoutParams.TYPE_TOAST;

         int flagNoTouch = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |//不接受触控
                 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                 WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                 WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |//硬件加速
                 WindowManager.LayoutParams.FLAG_FULLSCREEN |//隐藏状态栏导航栏以全屏(貌似没什么用)
                 WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |//忽略屏幕边界
                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |//布局充满整个屏幕 忽略应用窗口限制
                 WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                 WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | 4136;

         int flagTouch = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                 WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |//硬件加速
                 WindowManager.LayoutParams.FLAG_FULLSCREEN |//隐藏状态栏导航栏以全屏(貌似没什么用)
                 WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |//忽略屏幕边界
                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |//布局充满整个屏幕 忽略应用窗口限制
                 WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                 WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | 4136;

         var surfacelp = new WindowManager.LayoutParams();
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
             surfacelp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;//覆盖刘海
         }
         surfacelp.type = type;
         surfacelp.gravity = Gravity.TOP | Gravity.START;
         surfacelp.format = PixelFormat.TRANSLUCENT;
         surfacelp.x = 0;
         surfacelp.y = 0;
         surfacelp.width = screenWidth;
         surfacelp.height = WindowManager.LayoutParams.MATCH_PARENT;
         surfacelp.flags = flagNoTouch;
         surfacelp.setTitle("com.miui.screenrecorder");

         var view = new TextureView(this);
         windowManager.addView(view, surfacelp);

         WindowManager.LayoutParams iconlp = new WindowManager.LayoutParams();
         iconlp.type = type;
         iconlp.gravity = Gravity.TOP | Gravity.START;
         iconlp.format = PixelFormat.RGBA_8888;
         iconlp.x = 200;
         iconlp.y = 200;
         iconlp.width = 130;
         iconlp.height = 130;
         iconlp.flags = flagTouch;
         iconlp.setTitle("com.miui.screenrecorder");

         var icon = new ImageView(this);
         byte[] decode = Base64.decode("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", 0);
         icon.setImageBitmap(BitmapFactory.decodeByteArray(decode, 0, decode.length));

         icon.setOnTouchListener(new View.OnTouchListener() {
             private float initialTouchX, initialTouchY;
             private int initialX, initialY;

             public boolean onTouch(View v, MotionEvent motionEvent) {
                 switch (motionEvent.getAction()) {
                     case MotionEvent.ACTION_DOWN:
                         initialX = iconlp.x;
                         initialY = iconlp.y;
                         initialTouchX = motionEvent.getRawX();
                         initialTouchY = motionEvent.getRawY();
                         return true;
                     case MotionEvent.ACTION_UP:
                         int rawX = (int) (motionEvent.getRawX() - initialTouchX);
                         int rawY = (int) (motionEvent.getRawY() - initialTouchY);
                         icon.setAlpha(1f);
                         if (rawX < 10 && rawY < 10) {
                             if (surfacelp.flags == flagNoTouch) {
                                 surfacelp.flags = flagTouch;
                                 // native_menu(true);
                             } else {
                                 surfacelp.flags = flagNoTouch;
                                 //  native_menu(false);
                             }
                             windowManager.updateViewLayout(view, surfacelp);
                         }
                         return true;
                     case MotionEvent.ACTION_MOVE:
                         icon.setAlpha(0.5f);
                         iconlp.x = initialX + ((int) (motionEvent.getRawX() - initialTouchX));
                         iconlp.y = initialY + ((int) (motionEvent.getRawY() - initialTouchY));
                         //Update the layout with new X & Y coordinate
                         windowManager.updateViewLayout(icon, iconlp);
                         return true;
                     default:
                         return false;
                 }
             }
         });

         windowManager.addView(icon, iconlp);
     }*/
    public static Controller controller = null;
    public static Device device = null;

    public static void loop() {

        Looper.loop();

    }

    public static void injectTouchEvent(int action, long pointerId, int x, int y) {
        if (controller == null) {
            try {
                Workarounds.apply(false, false);
                Options options = new Options();

                device = new Device(options);
                controller = new Controller(device, null, false, false);
            } catch (ConfigurationException e) {
                return;
            }
        }
        var size = device.getScreenInfo().getUnlockedVideoSize();
        controller.injectTouch(action, pointerId, new Position(x, y, size.getWidth(), size.getHeight()), 1.f, 0, 0);
    }

    public static String getClipboardText() {
        ClipboardManager manager = ServiceManager.getClipboardManager();
        if (manager == null) {
            return "";
        }
        var text = manager.getText();
        return text == null ? "" : text.toString();
    }

    public static boolean setClipboardText(String text) {
        ClipboardManager manager = ServiceManager.getClipboardManager();
        if (manager == null) {
            return false;
        }
        return manager.setText(text);
    }

 /*   public static View getView(int width, int height, boolean hide, boolean secure) {
        handler.post(() -> view = createView(width, height, hide, secure));
        return null;
    }*/

    public static View getView(int width, int height, boolean hide, boolean secure) {
        View v;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S ||
                Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2) {
            v = new TextureView(context);
        } else {
            try {
                var surfaceView = new SurfaceView(context);
                surfaceView.setZOrderOnTop(true);
                surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
                v = surfaceView;
            } catch (Exception e) {
                v = new TextureView(context);
            }
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.format = PixelFormat.RGBA_8888;
        if (width == -1 || height == -1) {
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
        } else {
            params.width = width;
            params.height = height;
        }
        params.flags =
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | //布局充满整个屏幕 忽略应用窗口限制
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |//不接受触控
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | //不接受焦点
                        //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | //允许有触摸属性
                        //WindowManager.LayoutParams.FLAG_SPLIT_TOUCH | //接受多点触控
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | //硬件加速
                        WindowManager.LayoutParams.FLAG_FULLSCREEN | //全屏
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; //忽略屏幕边界

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            try {
                Field privateFlags = WindowManager.LayoutParams.class.getDeclaredField("privateFlags");
                privateFlags.setAccessible(true);
                privateFlags.setInt(params, privateFlags.getInt(params) |
                        PRIVATE_FLAG_TRUSTED_OVERLAY);
            } catch (Exception ignored) {
            }
        }
        if (hide) {
            try {
                Field privateFlags = WindowManager.LayoutParams.class.getDeclaredField("privateFlags");
                privateFlags.setAccessible(true);
                privateFlags.setInt(params, privateFlags.getInt(params) |
                        PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY);
            } catch (Exception ignored) {
            }
        }
        if (secure && !hide) {
            params.flags |= WindowManager.LayoutParams.FLAG_SECURE;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;//覆盖刘海
        }

        windowManager.addView(v, params);
        return v;
    }

    public static Surface getSurface(View view) {
        if (view == null) {
            return null;
        }
        if (view instanceof SurfaceView surfaceView) {
            if (surfaceView.getHolder().getSurface().isValid()) {
                return surfaceView.getHolder().getSurface();
            }
            return null;
        }
        TextureView textureView = (TextureView) view;
        if (textureView.isAvailable()) {
            return new Surface(textureView.getSurfaceTexture());
        }
        return null;
    }

    public static void removeView(View view) {
        if (view == null) {
            return;
        }
        handler.post(() -> windowManager.removeViewImmediate(view));

        Looper.getMainLooper().quit();
    }

    public static int[] getDisplayInfo() {
        android.view.Display display = windowManager.getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getRealSize(size);
        return new int[]{size.x, size.y, display.getRotation()};
    }

    @Override
    public Object[] call() throws Exception {
        return new Object[0];
    }

    static class ResourcesWrapper extends Resources {

        @SuppressLint("PrivateApi")
        @SuppressWarnings("JavaReflectionMemberAccess")
        public ResourcesWrapper(Resources res) throws ReflectiveOperationException {
            super(res.getAssets(), res.getDisplayMetrics(), res.getConfiguration());
            Method getImpl = Resources.class.getDeclaredMethod("getImpl");
            getImpl.setAccessible(true);
            Method setImpl = Resources.class.getDeclaredMethod("setImpl", getImpl.getReturnType());
            setImpl.setAccessible(true);
            Object impl = getImpl.invoke(res);
            setImpl.invoke(this, impl);
        }

        @Override
        public boolean getBoolean(int id) {
            try {
                return super.getBoolean(id);
            } catch (NotFoundException e) {
                return false;
            }
        }
    }

}
