package com.example.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.common.Const;
import com.example.common.Storage;
import com.example.common.XMPPUtil;
import com.example.data.DataWarehouse;
import com.example.data.LoginData;

public class LoginActivity extends AppCompatActivity implements Const{
    private EditText mEditTextUsername;
    private EditText mEditTextPassword;

    private LoginData mLoginDate;

    private Handler handler;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        context = this;
        mEditTextUsername = (EditText) findViewById(R.id.username);
        mEditTextPassword = (EditText) findViewById(R.id.password);

        mLoginDate = DataWarehouse.getGlobalData(this).loginData;

        handler = new Handler();

        mLoginDate.userName = Storage.getString(this,KEY_USERNAME);
        mLoginDate.passWord = Storage.getString(this,KEY_PASSWORD);

        mEditTextUsername.setText(mLoginDate.userName);
        mEditTextPassword.setText(mLoginDate.passWord);

        checkPermissions();

    }

    //登录按钮的单击事件方法
    public void onClickLogin(View view) {
        //将登录信息保存到全局对象中，当然也可以从SharedPreferences中读取，
        // 但是比较麻烦，所以保存到全局变量中。
        mLoginDate.userName = mEditTextUsername.getText().toString();
        mLoginDate.passWord = mEditTextPassword.getText().toString();

        if (mLoginDate.userName.equals(null) || mLoginDate.userName.equals(""))
        {
            Toast.makeText(LoginActivity.this,"请输入用户名",Toast.LENGTH_SHORT).show();
            return;
        }
        if (mLoginDate.passWord.equals(null) || mLoginDate.passWord.equals(""))
        {
            Toast.makeText(LoginActivity.this,"请输入密码",Toast.LENGTH_SHORT).show();
            return;
        }
        Log.e("username",mLoginDate.userName);
        Log.e("password",mLoginDate.passWord);

        //存储登录信息。
        /*为了存储的这些数据可以在别的地方应用，故需要将Key定义为常量，以便其他地方引用。
        *将Key保存在接口中以便其它类引用。
        * 当然，也可以定义在一个类中，但是需要将Key定义为静态变量，比较麻烦，故将其定义在接口中。
        * 将Key保存在接口Const中，所有的Key名称以Key_开头。
        */
        Storage.putString(this,KEY_USERNAME,mLoginDate.userName);
        Storage.putString(this,KEY_PASSWORD,mLoginDate.passWord);

        //新版本中不允许在主线程中直接访问网络。故登录服务器需要另起一个线程。
        new Thread(new Runnable() {
            @Override
            public void run() {
                //登录成功
                if (XMPPUtil.login(context,mLoginDate.userName,mLoginDate.passWord))
                {

                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else //登录失败
                {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this,"登录失败，请检查用户名和密码的正确性",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void onClickRegister(View view) {
        Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }

    private void checkPermissions() {

        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
        };
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
