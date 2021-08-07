package com.assessbyphone.zambia.CallbackUtils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class UiUtil {
    private static final String SHARED_PREF = "AccessByPhone";
    private static final String IS_LOGIN = "is_login";
    private static final String USER_ID = "user_id";
    private static final String PASSWORD = "password";
    private static final String IS_ADMIN = "is_admin";

    public static void setIsLogin(Context ctx, String data) {
        SharedPreferences pref = ctx.getSharedPreferences(SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(IS_LOGIN, data);
        editor.apply();
    }

    public static String getIsLogin(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences(SHARED_PREF, 0);
        String data = pref.getString(IS_LOGIN, "");
        return data;
    }

    public static void setUserID(Context ctx, String data) {
        SharedPreferences pref = ctx.getSharedPreferences(SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(USER_ID, data);
        editor.apply();
    }

    public static String getUserID(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences(SHARED_PREF, 0);
        String data = pref.getString(USER_ID, "");
        return data;
    }

    public static void setPassword(Context ctx, String data) {
        SharedPreferences pref = ctx.getSharedPreferences(SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PASSWORD, data);
        editor.apply();
    }

    public static String getPassword(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences(SHARED_PREF, 0);
        String data = pref.getString(PASSWORD, "");
        return data;
    }

    public static void setIsAdmin(Context ctx, boolean data) {
        SharedPreferences pref = ctx.getSharedPreferences(SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_ADMIN, data);
        editor.apply();
    }

    public static boolean getIsAdmin(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences(SHARED_PREF, 0);
        boolean data = pref.getBoolean(IS_ADMIN, false);
        return data;
    }

    public static void clearSharePreferencesDetails(Context context){
        SharedPreferences mPref = context.getSharedPreferences( SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        editor.clear();
        editor.apply();
    }
}