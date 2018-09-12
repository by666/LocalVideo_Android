package com.example.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.activity.R;
import com.example.data.ChatServiceData;

import org.jivesoftware.smackx.muc.HostedRoom;

import java.util.ArrayList;
import java.util.List;


public class ChatServiceListAdapter extends ParentAdapter {
    private List<ChatServiceData> mChatServiceList;

    public ChatServiceListAdapter(Context mContext, List<HostedRoom> chatServiceList) {
        super(mContext);
        mChatServiceList = new ArrayList<>();
        for (int i=0; i<chatServiceList.size(); i++)
        {
            String jid = chatServiceList.get(i).getJid();
            //因为chatServiceList获得的不光是群聊服务(会议服务),还有许多其他的服务，
            //例如：conference.songxitang-pc、search.songxitang-pc、proxy.songxitang-pc
            //rayo.songxitang-pc等等， 故需要过滤，将conference服务过滤出来。
            if (!jid.startsWith("conference"))
            {
                continue;
            }
            ChatServiceData data = new ChatServiceData();
            data.hostedRoom = chatServiceList.get(i);
            mChatServiceList.add(data);
        }
    }

    @Override
    public int getCount() {
        return mChatServiceList.size();
    }

    @Override
    public Object getItem(int position) {
        return position < mChatServiceList.size() ? mChatServiceList.get(position):null;
    }

    public String getJID(int position)
    {
        return position<mChatServiceList.size() ? mChatServiceList.get(position).hostedRoom.getJid():null;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            convertView = mLayoutInfater.inflate(R.layout.chat_service_item,null);
        }
        TextView chatService = (TextView) convertView.findViewById(R.id.textview_chat_service_item);
        HostedRoom hostedRoom = mChatServiceList.get(position).hostedRoom;
        chatService.setText(hostedRoom.getJid());
        return convertView;
    }
}
