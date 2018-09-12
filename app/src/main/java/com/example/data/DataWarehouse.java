package com.example.data;

import android.content.Context;

import com.example.MyApplication;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;


public class DataWarehouse {
    public static MyApplication getGlobalData(Context ctx)
    {
        return (MyApplication) ctx.getApplicationContext();
    }

    public static XMPPTCPConnection getXMPPTCPConnection(Context ctx)
    {
        return getGlobalData(ctx).xmpptcpConnection;
    }

    public static void setXMPPTCPConnection(Context ctx,XMPPTCPConnection conn)
    {
        getGlobalData(ctx).xmpptcpConnection = conn;
    }
}
