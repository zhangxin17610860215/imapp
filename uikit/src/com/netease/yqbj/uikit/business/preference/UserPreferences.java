package com.netease.yqbj.uikit.business.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.netease.yqbj.uikit.api.NimUIKit;

/**
 * Created by hzxuwen on 2015/10/21.
 */
public class UserPreferences {

    private final static String KEY_EARPHONE_MODE = "KEY_EARPHONE_MODE";

    private final static String SOFT_KEYBOARD_HEIGHT = "SOFT_KEYBOARD_HEIGHT";

    public static void setEarPhoneModeEnable(boolean on) {
        saveBoolean(KEY_EARPHONE_MODE, on);
    }

    public static boolean isEarPhoneModeEnable() {
        return getBoolean(KEY_EARPHONE_MODE, true);
    }

    private static boolean getBoolean(String key, boolean value) {
        return getSharedPreferences().getBoolean(key, value);
    }

    private static void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private static int getInter(String key, int value) {
        return getSharedPreferences().getInt(key, value);
    }

    private static void saveInt(String key,int value){
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putInt(key, value);
        editor.apply();

    }

    public static void saveKeyboardHeight(int height){
        saveInt(SOFT_KEYBOARD_HEIGHT,height);
    }

    public static int getKeyboardHeight(){
        return getInter(SOFT_KEYBOARD_HEIGHT,-1);
    }



    private static SharedPreferences getSharedPreferences() {
        return NimUIKit.getContext().getSharedPreferences("UIKit." + NimUIKit.getAccount(), Context.MODE_PRIVATE);
    }
}
