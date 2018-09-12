package com.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.activity.R;
import com.example.common.Util;
import com.example.data.ChatData;

import java.util.ArrayList;
import java.util.List;


public class ChatListAdapter extends BaseAdapter {
    private Context mContext;
    private List<ChatData> mChatDataList;
    private LayoutInflater mLayoutInflater;

    public ChatListAdapter(Context mContext) {
        this.mContext = mContext;
        mChatDataList = new ArrayList<>();
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mChatDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return position<mChatDataList.size() ? mChatDataList.get(position):null;
    }

    public void addItem(ChatData chatData)
    {
        mChatDataList.add(chatData);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            convertView = mLayoutInflater.inflate(R.layout.chat_list_item,null);
        }

        //获取左右聊天信息框
        View left = null;
        View right = null;
        if (convertView.getTag(R.id.linearlayout_chat_left) == null)
        {
            left = convertView.findViewById(R.id.linearlayout_chat_left);
            right = convertView.findViewById(R.id.linearlayout_chat_right);
            convertView.setTag(R.id.linearlayout_chat_left,left);
            convertView.setTag(R.id.linearlayout_chat_right,right);
        }
        else
        {
            left = (View) convertView.getTag(R.id.linearlayout_chat_left);
            right = (View) convertView.getTag(R.id.linearlayout_chat_right);
        }

        TextView leftChatText = (TextView) convertView.findViewById(R.id.textview_chat_text_left);
        TextView rightChatText = (TextView) convertView.findViewById(R.id.textview_chat_text_right);

        TextView leftUsername = (TextView) convertView.findViewById(R.id.textview_left_user_name);
        TextView rightUsername = (TextView) convertView.findViewById(R.id.textview_right_user_name);


        //先将左右视图隐藏，根据实际情况显示左右视图中的哪一个。
        left.setVisibility(View.GONE);
        right.setVisibility(View.GONE);

        //获取要显示的数据
        ChatData chatData = mChatDataList.get(position);

        //此处使用固定头像，如果要使用变化的头像，可以自己增加currentChatHeadPortrait对象，去实现。
        //也没有添加显示昵称的功能，以后慢慢在添加。
        TextView currentChatText;
        TextView currentChatUsername;
        //如果是自己发送的数据
        if (chatData.isOwner)
        {
            right.setVisibility(View.VISIBLE);
            currentChatText = rightChatText;
            currentChatUsername = rightUsername;
        }
        else
        {
            left.setVisibility(View.VISIBLE);
            currentChatText = leftChatText;
            currentChatUsername = leftUsername;
        }
        currentChatText.setText(chatData.text);
        currentChatUsername.setText(chatData.user);
        //将接收到的表情文本转化为表情。
        Util.updateFacesForTextView(mContext, currentChatText);
        return convertView;
    }
}
