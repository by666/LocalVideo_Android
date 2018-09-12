package com.example.common;

import android.content.Context;
import android.content.SharedPreferences;


public class Storage {
    private final static String STORAGE_FINAL_NAME = "chatclent.config";

    //获取SharedPreferences
    private static SharedPreferences getSharedPreference(Context ctx)
    {
        return ctx.getSharedPreferences(STORAGE_FINAL_NAME,Context.MODE_PRIVATE);
    }

    //往SharedPreferences中存放String类型的值。
    public static void putString(Context ctx,String key,String value)
    {
        SharedPreferences sharedPreferences = getSharedPreference(ctx);
        sharedPreferences.edit().putString(key,value).commit();
    }

    //获取键值时，可以设置默认值，也可以不设置默认值。故将默认值设置为String...可变参数。
    public static String getString(Context ctx,String key,String... defaultValue)
    {
        SharedPreferences sharedPreferences = getSharedPreference(ctx);
        //如果getString()不设置默认值，则将默认值设置为""。
        String dv = "";
        //如果getString()设置了默认值，则读取defaultValue中的第一个值。
        for(String v : defaultValue)
        {
            dv = v;
            break;
        }
        return sharedPreferences.getString(key,dv);
    }

    //往SharedPreferences中存放Boolean类型的数据。
    public static void putBollean(Context ctx,String key,Boolean value)
    {
        SharedPreferences sharedPreferences = getSharedPreference(ctx);
        sharedPreferences.edit().putBoolean(key,value).commit();
    }

    public static Boolean getBoolean(Context ctx,String key,Boolean... defaultValue)
    {
        SharedPreferences sharedPreferences = getSharedPreference(ctx);
        boolean dv = false;
        for (boolean v : defaultValue)
        {
            dv = v;
            break;
        }
        return sharedPreferences.getBoolean(key,dv);
    }

}
