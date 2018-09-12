package com.example.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.activity.R;
import com.example.data.ChatRoomData;

import org.jivesoftware.smackx.disco.packet.DiscoverItems;

import java.util.ArrayList;
import java.util.List;


public class MultiUserChatRoomListAdapter extends ParentAdapter {
    private List<ChatRoomData> mChatRoomList;

    public MultiUserChatRoomListAdapter(Context mContext, List<DiscoverItems.Item> chatRoomList) {
        super(mContext);
        mChatRoomList = new ArrayList<>();
        for (int i=0; i<chatRoomList.size(); i++)
        {
            ChatRoomData chatRoomData = new ChatRoomData();
            chatRoomData.item = chatRoomList.get(i);
            mChatRoomList.add(chatRoomData);
        }
    }

    @Override
    public int getCount() {
        return mChatRoomList.size();
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public DiscoverItems.Item getChatRoom(int position)
    {
        return position<mChatRoomList.size() ? mChatRoomList.get(position).item:null;
    }

    //刷新聊天室列表
    public void updateChatRoom(List<DiscoverItems.Item> chatRoomList)
    {
        mChatRoomList.clear();
        for (int i=0; i<chatRoomList.size(); i++)
        {
            ChatRoomData chatRoomData = new ChatRoomData();
            chatRoomData.item = chatRoomList.get(i);
            mChatRoomList.add(chatRoomData);
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            convertView = mLayoutInfater.inflate(R.layout.multi_user_chat_room_item,null);
        }
        TextView chatRoom = (TextView) convertView.findViewById(R.id.textview_chat_room_item);
        DiscoverItems.Item item = mChatRoomList.get(position).item;
        chatRoom.setText(item.getName());

        return convertView;
    }
}
