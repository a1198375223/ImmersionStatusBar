package com.example.study.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class StateBarUtil {


    //step 1 (让toolbar fitSystemWindows)
    public static void fitSystemWindows(Activity activity, boolean fitSystemWindows) {
        ViewGroup group = (ViewGroup) activity.findViewById(android.R.id.content);
        if (group.getChildCount() > 0) {
            ViewGroup view = (ViewGroup)group.getChildAt(0);
            if (view != null)
                view.setFitsSystemWindows(fitSystemWindows);
        }
    }


    //step 2 设置状态栏为半透明
    public static void setTranslucentStatus(Activity activity) {
        //api > 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            View decorView = window.getDecorView();
            //这两个属性表示让主体内容沿用状态栏
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //去除半透明黑色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //设置状态栏为半透明
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){ //api >= 19 && api < 21
            Window window = activity.getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            attributes.flags |= flagTranslucentStatus;
            window.setAttributes(attributes);
        }
    }


    //step 3 适应状态栏颜色和文字颜色 (匹配大部分机型的 miui 和 flyme 需要自己定义
    //dark 表示文字的颜色
    public static boolean setCommonUI(Activity activity, boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = activity.getWindow().getDecorView();
            int vis = decorView.getSystemUiVisibility();
            if (dark) {
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }

            if (decorView.getSystemUiVisibility() != vis) {
                decorView.setSystemUiVisibility(vis);
            }
            return dark;
        }
        return false;
    }



    public static void setStatusBarColor(Activity activity, int colorId) {
        //if api > 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.setStatusBarColor(colorId);
        } else { //api < 21
            //使用SystemBarTintManager来改变状态栏的颜色 改变之前需要先将状态栏设置成透明
            setTranslucentStatus(activity);
            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(activity);
            systemBarTintManager.setStatusBarTintEnabled(true);
            systemBarTintManager.setStatusBarTintColor(colorId);
        }
    }



}
