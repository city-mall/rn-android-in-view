package io.autodidact.visibilitytracker;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.graphics.Rect;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.NativeViewHierarchyManager;
import com.facebook.react.uimanager.UIBlock;
import com.facebook.react.uimanager.UIManagerModule;

public class VisibilityTrackerModule extends ReactContextBaseJavaModule {

  public static final String NAME = "VisibilityTracker";

  private final ReactApplicationContext reactContext;

  public VisibilityTrackerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void isViewVisible(final int tag, final Callback success, final Callback failure) {
    try {
      final ReactApplicationContext context = getReactApplicationContext();
      UIManagerModule uiManager = context.getNativeModule(UIManagerModule.class);
      uiManager.addUIBlock(new UIBlock() {
        public void execute(NativeViewHierarchyManager nvhm) {
          try {
            View view = nvhm.resolveView(tag);
            if (view.isShown()) {
              Rect rect = new Rect();
              view.getGlobalVisibleRect(rect);

              int[] location = new int[2];
              view.getLocationOnScreen(location); // x and y in screen coordinates

              int x = location[0];
              int y = location[1];
              int width = view.getWidth();
              int height = view.getHeight();
              int pageX = rect.left;
              int pageY = rect.top;

              // Get screen width and height
              DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
              int screenWidth = displayMetrics.widthPixels;
              int screenHeight = displayMetrics.heightPixels;

              // Calculate rectTop, rectBottom, rectWidth, and isVisible
              int rectBottom = pageY + height;
              int rectWidth = pageX + width;

              boolean isVisible =
                      rectBottom >= 0 &&
                              pageX >= 0 &&
                              rectBottom <= screenHeight &&
                              rectWidth > 0 &&
                              rectWidth <= screenWidth;

              double totalArea = width * height;
              double visibleArea = rect.width() * rect.height();
              boolean isSeventyPercentVisible = (visibleArea / totalArea) >= 0.7;

              success.invoke(isSeventyPercentVisible && isVisible);
            } else {
              success.invoke(false);
            }
          } catch (Exception e) {
            success.invoke(false);
          }
        }
      });
    } catch (Throwable throwable) {
      failure.invoke(throwable);
    }
  }
}
