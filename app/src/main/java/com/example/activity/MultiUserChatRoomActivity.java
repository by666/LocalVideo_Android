package com.example.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.adapter.ChatListAdapter;
import com.example.common.Const;
import com.example.common.Util;
import com.example.data.ChatData;
import com.example.data.DataWarehouse;
import com.example.data.LoginData;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

public class MultiUserChatRoomActivity extends ParentActivity implements Const{
    private ChatListAdapter mChatListAdapter;

    private ChatManager mChatManager;
    private LoginData mLoginData;

    private EditText mEditTextChatText;
    private ListView mListViewChatList;
    private String mChatServiceJID;
    private String mChatRoomName;
    private MultiUserChat mMultiUserChat;

//    private PacketFilter mFilter = new MessageTypeFilter(Message.Type.groupchat);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_user_chat_room);

        mLoginData = DataWarehouse.getGlobalData(this).loginData;

        mEditTextChatText = (EditText) findViewById(R.id.edittext_chat_text);
        mListViewChatList = (ListView) findViewById(R.id.listview_ChatList);

//        mXMPPConnection.addPacketListener(this, mFilter);
        mChatServiceJID = getIntent().getStringExtra(JID);
        mChatRoomName = getIntent().getStringExtra(CHAT_ROOM_NAME);

        mChatListAdapter = new ChatListAdapter(this);

        mListViewChatList.setAdapter(mChatListAdapter);
        mListViewChatList.setDivider(null);

        //加入聊天室
        try {
            mMultiUserChat = MultiUserChatManager.getInstanceFor(connection).
                    getMultiUserChat(mChatRoomName+"@"+mChatServiceJID);
            mMultiUserChat.join(mLoginData.userName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, "聊天室加入失败", Toast.LENGTH_LONG).show();
            finish();
        }

//        myHandler = new MyHandler(this);

        //接收群聊信息
        mMultiUserChat.addMessageListener(new MessageListener() {
            @Override
            public void processMessage(Message message) {
                Log.e("ChatGroup",message.getFrom());
                Log.e("MsgBody",message.getBody());
                android.os.Message msg = android.os.Message.obtain();
                msg.obj = message;
//                myHandler.sendMessage(msg);
                handler.sendMessage(msg);
            }
        });
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            Message message = (Message) msg.obj;
            String body = message.getBody();
            if (body != null)
            {
                String from = Util.extractUserFromChatGroup(message.getFrom());
                if(mLoginData.userName.equals(from))
                {
                    return;
                }

                ChatData item = new ChatData();

                item.text = body;

                mChatListAdapter.addItem(item);
                mListViewChatList.setSelection(mListViewChatList.getAdapter().getCount() - 1);
            }
            return;
        }
    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode)
        {
            case 1:
                int faceId = data.getIntExtra(KEY_FACE_ID, -1);
                if (faceId != -1)
                {
                    String faceResName = FACE_PREFIX + faceId;

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                            Util.getResourceIDFromName(R.drawable.class, faceResName));

                    Matrix matrix = new Matrix();
                    matrix.postScale(0.6f, 0.6f);
                    Bitmap smallBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                            matrix, true);
                    ImageSpan imageSpan = new ImageSpan(this, smallBitmap);
                    String faceText = FACE_TEXT_PREFIX + faceId + FACE_TEXT_SUFFIX;
                    SpannableString spannableString = new SpannableString(faceText);

                    spannableString.setSpan(imageSpan, 0, faceText.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    mEditTextChatText.getText().insert(mEditTextChatText.getSelectionStart(),
                            spannableString);
                }
                break;
        }
    }

    //发送群聊信息
    public void onClick_Send(View view) {
        try
        {
            String text = mEditTextChatText.getText().toString().trim();
            if (!"".equals(text))
            {
                mMultiUserChat.sendMessage(text);
                mEditTextChatText.setText("");

                ChatData item = new ChatData();
                item.text = text;
                item.user = mLoginData.userName;
                item.isOwner = true;
                mChatListAdapter.addItem(item);
                mListViewChatList.setSelection(mListViewChatList.getAdapter().getCount() - 1);
            }
            else
            {
                Toast.makeText(this, "请输入要发送的文本.", Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
