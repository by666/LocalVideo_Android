package com.example;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.example.data.LoginData;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.Set;
import java.util.TreeSet;

public class MyApplication extends Application{
    public XMPPTCPConnection xmpptcpConnection;
    //记录登录信息
    public LoginData loginData = new LoginData();
    //记录当前正在聊天的用户
    public Set<String> chatUsers = new TreeSet<>();



}
