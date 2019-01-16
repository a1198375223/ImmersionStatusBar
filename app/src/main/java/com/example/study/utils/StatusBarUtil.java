package com.example.study.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StatusBarUtil {
    public final static int TYPE_MIUI = 0;
    public final static int TYPE_FLYME = 1;
    public final static int TYPE_M = 3;//6.0
    private static boolean mIsDrawerLayout;
    private static int mContentResourseIdInDrawer;

    public static void setStatusBarColor(Activity activity, int colorId) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             Window window = activity.getWindow(); window.setStatusBarColor(colorId);
         } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
             //使用SystemBarTintManager,需要先将状态栏设置为透明
             setTranslucentStatus(activity);
             SystemBarTintManager systemBarTintManager = new SystemBarTintManager(activity);
             systemBarTintManager.setStatusBarTintEnabled(true);
             //显示状态栏
             systemBarTintManager.setStatusBarTintColor(colorId);//设置状态栏颜色
         }
     }

     public static void setTranslucentStatus(Activity activity) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
             Window window = activity.getWindow();
             View decorView = window.getDecorView();
             //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
             int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
             decorView.setSystemUiVisibility(option);
             window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
             window.setStatusBarColor(Color.TRANSPARENT);
             //导航栏颜色也可以正常设置
             //window.setNavigationBarColor(Color.TRANSPARENT);
         } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
             Window window = activity.getWindow();
             WindowManager.LayoutParams attributes = window.getAttributes();
             int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
             attributes.flags |= flagTranslucentStatus;
             //int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
             // attributes.flags |= flagTranslucentNavigation;
             window.setAttributes(attributes);
         }
     }

      public static void setRootViewFitsSystemWindows(Activity activity, boolean fitSystemWindows) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
             ViewGroup winContent = (ViewGroup) activity.findViewById(android.R.id.content);
             if (winContent.getChildCount() > 0) {
                 ViewGroup rootView = (ViewGroup) winContent.getChildAt(0);
                 if (rootView != null) {
                     rootView.setFitsSystemWindows(fitSystemWindows);
                 }
             }
         }
     }

    public static boolean setStatusBarDarkTheme(Activity activity, boolean dark) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 setStatusBarFontIconDark(activity, TYPE_M, dark);
             } else if (OSUtils.isMiui()) {
                 setStatusBarFontIconDark(activity, TYPE_MIUI, dark);
             } else if (OSUtils.isFlyme()) {
                 setStatusBarFontIconDark(activity, TYPE_FLYME, dark);
             } else {//其他情况
                 return false;
             }
             return true;
         }
         return false;
     }


     public static boolean setStatusBarFontIconDark(Activity activity, int type,boolean dark) {
         switch (type) {
             case TYPE_MIUI:
                 return setMiuiUI(activity, dark);
             case TYPE_FLYME:
                 return setFlymeUI(activity, dark);
             case TYPE_M: default:
                 return setCommonUI(activity,dark);
         }
     }//设置6.0 状态栏深色浅色切换

     public static boolean setCommonUI(Activity activity, boolean dark) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             View decorView = activity.getWindow().getDecorView();
             if (decorView != null) {
                 int vis = decorView.getSystemUiVisibility();
                 if (dark) {
                     vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                 } else {
                     vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                 }
                 if (decorView.getSystemUiVisibility() != vis) {
                     decorView.setSystemUiVisibility(vis);
                 }
                 return true;
             }
         }
         return false;
     }
     //设置Flyme 状态栏深色浅色切换
    public static boolean setFlymeUI(Activity activity, boolean dark) {
         try {
             Window window = activity.getWindow();
             WindowManager.LayoutParams lp = window.getAttributes();
             Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
             Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
             darkFlag.setAccessible(true);
             meizuFlags.setAccessible(true);
             int bit = darkFlag.getInt(null);
             int value = meizuFlags.getInt(lp);
             if (dark) {
                 value |= bit;
             } else {
                 value &= ~bit;
             }
             meizuFlags.setInt(lp, value);
             window.setAttributes(lp); return true;
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }

     //设置MIUI 状态栏深色浅色切换
    public static boolean setMiuiUI(Activity activity, boolean dark) {
         try {
             Window window = activity.getWindow();
             Class<?> clazz = activity.getWindow().getClass();
             @SuppressLint("PrivateApi") Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
             Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
             int darkModeFlag = field.getInt(layoutParams);
             Method extraFlagField = clazz.getDeclaredMethod("setExtraFlags", int.class, int.class);
             extraFlagField.setAccessible(true);
             if (dark) { //状态栏亮色且黑色字体
                  extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
             } else {
                 extraFlagField.invoke(window, 0, darkModeFlag);
             }
             return true;
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }

     //获取状态栏高度
    public static int getStatusBarHeight(Context context) {
         int result = 0;
         int resourceId = context.getResources().getIdentifier( "status_bar_height", "dimen", "android");
         if (resourceId > 0) {
             result = context.getResources().getDimensionPixelSize(resourceId);
         }
         return result;
     }

    /**
     * 是否是最外层布局为 DrawerLayout 的侧滑菜单
     * @param drawerLayout 是否最外层布局为 DrawerLayout
     * @param contentId 内容视图的 id
     * @return
     */
    public static void setIsDrawerLayout(boolean drawerLayout, int contentId) {
        mIsDrawerLayout = drawerLayout;
        mContentResourseIdInDrawer = contentId;
    }

    private static boolean isDrawerLayout() {
        return mIsDrawerLayout;
    }

    /**
     * 添加状态栏占位视图
     *
     * @param activity
     */
    public static void addStatusViewWithColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isDrawerLayout()) {
                //要在内容布局增加状态栏，否则会盖在侧滑菜单上
                ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
                //DrawerLayout 则需要在第一个子视图即内容试图中添加padding
                View parentView = rootView.getChildAt(0);
                LinearLayout linearLayout = new LinearLayout(activity);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                View statusBarView = new View(activity);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        getStatusBarHeight(activity));
                statusBarView.setBackgroundColor(color);
                //添加占位状态栏到线性布局中
                linearLayout.addView(statusBarView, lp);
                //侧滑菜单
                DrawerLayout drawer = (DrawerLayout) parentView;
                //内容视图
                View content = activity.findViewById(mContentResourseIdInDrawer);
                //将内容视图从 DrawerLayout 中移除
                drawer.removeView(content);
                //添加内容视图
                linearLayout.addView(content, content.getLayoutParams());
                //将带有占位状态栏的新的内容视图设置给 DrawerLayout
                drawer.addView(linearLayout, 0);
            } else {
                //设置 paddingTop
                ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
                rootView.setPadding(0, getStatusBarHeight(activity), 0, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //直接设置状态栏颜色
                    activity.getWindow().setStatusBarColor(color);
                } else {
                    //增加占位状态栏
                    ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                    View statusBarView = new View(activity);
                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            getStatusBarHeight(activity));
                    statusBarView.setBackgroundColor(color);
                    decorView.addView(statusBarView, lp);
                }
            }
        }
    }


}

