package com.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.CustomXML.VideoInvitation;
import com.example.activity.R;
import com.example.common.XMPPUtil;
import com.example.data.DataWarehouse;
import com.example.data.UserData;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class FriendListAdapter extends BaseAdapter {
    private List<UserData> mUsers;
    private Map<String,String> mUserMap;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private XMPPTCPConnection connection;
    private String mServiceName;

    public FriendListAdapter(Context mContext,Collection<RosterEntry> entries) {
        this.mContext = mContext;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUsers = new ArrayList<>();
        mUserMap = new HashMap<>()  ;
        connection = DataWarehouse.getXMPPTCPConnection(mContext);
        mServiceName = connection.getServiceName();
        if (entries != null)
        {
            Iterator<RosterEntry> iterator = entries.iterator();
            while (iterator.hasNext())
            {
                RosterEntry entry = iterator.next();
                //有的返回的用户JID后面包含@
                if (entry.getUser().indexOf("@") == -1)
                {
                    UserData userData = new UserData(entry.getUser());
                    mUserMap.put(entry.getUser(),entry.getName());
                    mUsers.add(userData);
                }
            }
        }
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //根据位置获取用户名
    public String getUsername(int position)
    {
        return mUsers.get(position).username;
    }


    //实现ListView的实时刷新。
    public void addUserData(UserData userData)
    {
        mUsers.add(userData);
        //如果adapter有数据更细，调用此函数可以更细adapter
        notifyDataSetChanged();
    }

    public void removeUserData(int position)
    {
        XMPPTCPConnection connection = DataWarehouse.getXMPPTCPConnection(mContext);
        RosterEntry entry = Roster.getInstanceFor(connection).getEntry(getUsername(position));
        if (entry != null)
        {
            try {
                Roster.getInstanceFor(connection).removeEntry(entry);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            mUsers.remove(position);
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            convertView = mLayoutInflater.inflate(R.layout.friend_list_item,null);
        }
        TextView user = (TextView) convertView.findViewById(R.id.textview_friend_list_item_name);
        user.setText(getUsername(position));



        return convertView;
    }

    //chatJid: friendUsername@serviceName
    private Chat createChat(String chatJid)
    {
        if (isConnected())
        {
            ChatManager chatManager = ChatManager.getInstanceFor(connection);
            return chatManager.createChat(chatJid);
        }
        throw new NullPointerException("连接服务器失败，请先连接服务器!");
    }

    private boolean isConnected()
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
}
