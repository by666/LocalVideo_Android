package com.example.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.common.XMPPUtil;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.iqregister.packet.Registration;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEditTextUsername;
    private EditText mEditTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEditTextUsername = (EditText) findViewById(R.id.userRegister);
        mEditTextPassword = (EditText) findViewById(R.id.passwordInput);
    }


    public void onClickRegister(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                registerUser(mEditTextUsername.getText().toString(),mEditTextPassword.getText().toString());
            }
        }).start();
    }


    //注册用户
    public Boolean registerUser(String username,String password)
    {
        try {
            XMPPTCPConnection connection = XMPPUtil.getXMPPConnection(this);
            if (connection == null)
            {
                try {
                    connection.connect();
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
                Log.e("connect","连接服务器失败!");
            }

            AccountManager.getInstance(connection).createAccount(username,password);
            finish();
            return true;
        }
        catch (SmackException.NoResponseException | XMPPException.XMPPErrorException |
                SmackException.NotConnectedException e)
        {
            Log.e("register", "注册失败！");
            return false;
        }
    }
}
