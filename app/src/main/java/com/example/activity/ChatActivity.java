package com.example.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.CustomXML.VideoInvitation;
import com.example.adapter.ChatListAdapter;
import com.example.common.Const;
import com.example.common.Util;
import com.example.data.ChatData;
import com.example.data.DataWarehouse;
import com.example.data.LoginData;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;

public class ChatActivity extends ParentActivity implements Const{
    protected String mFriendUsername;   //聊天用户名称
    private String mServiceName;    //XMPP服务器名称

    private LoginData mLoginData;  //当前账号登录信息

    private EditText mEditTextChatText;     //聊天文本框
    protected ListView mListViewChatList;     //显示聊天记录

    protected ChatListAdapter mChatListAdapter;

//    private MyHandler myHandler;

//    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mServiceName = connection.getServiceName();
        Log.e("ServiceName","ChatActivity"+mServiceName);

        mLoginData = DataWarehouse.getGlobalData(this).loginData;

        mEditTextChatText = (EditText) findViewById(R.id.edittext_chat_text);
        mListViewChatList = (ListView) findViewById(R.id.listview_ChatList);

        mChatListAdapter = new ChatListAdapter(this);
        mListViewChatList.setAdapter(mChatListAdapter);
        //隐藏分隔条
        mListViewChatList.setDivider(null);

        //处理发送过来的聊天信息等。
        processExtraData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //must store the new intent unless getIntent() will return the old one
        setIntent(intent);
        processExtraData();
    }

    private void processExtraData()
    {
        Intent intent = getIntent();
        if (mFriendUsername == null) {
            mFriendUsername = intent.getStringExtra("friendUsername");
        }


        String body = intent.getStringExtra("body");
        if (body != null)
        {
            ChatData item = new ChatData();
            item.text = body;
            item.user = mFriendUsername;
            mChatListAdapter.addItem(item);
            mListViewChatList.setSelection(mListViewChatList.getAdapter().getCount()-1);
        }

        DataWarehouse.getGlobalData(this).chatUsers.add(mFriendUsername);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case 1:
                int faceId = data.getIntExtra(KEY_FACE_ID, -1);
                if (faceId != -1)
                {
                    String faceResName = FACE_PREFIX + faceId;
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                            Util.getResourceIDFromName(R.drawable.class,faceResName));
                    //将表情缩小到原来的60%
                    Matrix matrix = new Matrix();
                    matrix.postScale(0.6f,0.6f);
                    Bitmap smallBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),
                            bitmap.getHeight(),matrix,true);
                    ImageSpan imageSpan = new ImageSpan(this,smallBitmap);
                    //表情图片代表的表情文本，EditText接收到的表情是表情文本如<:10:>，要想显示
                    //表情图片需要将表情文本转换为表情图片,转换函数为Util.updateFacesForTextView()
                    String faceText = FACE_TEXT_PREFIX + faceId + FACE_TEXT_SUFFIX;
                    SpannableString spannableString = new SpannableString(faceText);
                    spannableString.setSpan(imageSpan, 0, faceText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    //插入表情图片
                    mEditTextChatText.getText().insert(mEditTextChatText.getSelectionStart(), spannableString);

                }
                break;
        }
    }

    public void onClick_Send(View view) {
        String chatText = mEditTextChatText.getText().toString().trim();
        if (!"".equals(chatText))
        {
            String chatJid = mFriendUsername+"@"+mServiceName;
            Message message = new Message(chatJid,Message.Type.chat);
            message.setBody(chatText);
            Chat chat = createChat(chatJid);

            try {
                chat.sendMessage(message);
                mEditTextChatText.setText("");
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
            //显示发送聊天信息
            ChatData item = new ChatData();
            item.user = mLoginData.userName; //没有设置别名的功能。
            item.text = chatText;
            item.isOwner = true;

            mChatListAdapter.addItem(item);
            //滚动到消息的最后一条。
            mListViewChatList.setSelection(mListViewChatList.getAdapter().getCount()-1);

        }
        else
        {
            Toast.makeText(this,"发送消息不能为空",Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //当当前Activity销毁时，关闭监听对象。
        DataWarehouse.getGlobalData(this).chatUsers.remove(mFriendUsername);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id)
        {
            //向对方发送视频通话请求
            case R.id.request_video:
                String chatJid = mFriendUsername+"@"+mServiceName;
                Message message = new Message();

                VideoInvitation videoInvitation = new VideoInvitation();
                videoInvitation.setTypeText("video-invitation");
                message.addExtension(videoInvitation);
                Chat chat = createChat(chatJid);

                try {
                    chat.sendMessage(message);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
