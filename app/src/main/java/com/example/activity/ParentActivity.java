package com.example.activity;
/*创建ParentActivity的原因：是因为好多Activity都需要用到下面的connetiton
*对象，故将需要connection的Activity继承ParentActivity对象即可。
 */
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.data.DataWarehouse;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.IOException;

public class ParentActivity extends AppCompatActivity {
    protected XMPPTCPConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connection = DataWarehouse.getXMPPTCPConnection(this);
    }

    protected boolean isConnected()
    {
        if (connection == null)
        {
            return false;
        }
        if (!connection.isConnected())
        {
            try {
                connection.connect();
                return true;
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    //获取聊天对象管理器
    public ChatManager getChatManager()
    {
        if (isConnected())
        {
            ChatManager chatManager = ChatManager.getInstanceFor(connection);
            return chatManager;
        }
        throw new NullPointerException("服务器连接失败，请先连接服务器!");
    }

    //chatJid: friendUsername@serviceName
    protected Chat createChat(String chatJid)
    {
        if (isConnected())
        {
            ChatManager chatManager = ChatManager.getInstanceFor(connection);
            return chatManager.createChat(chatJid);
        }
        throw new NullPointerException("连接服务器失败，请先连接服务器!");
    }
}
