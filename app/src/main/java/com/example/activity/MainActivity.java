package com.example.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.CustomXML.IceCandidateExtensionElement;
import com.example.CustomXML.SDPExtensionElement;
import com.example.CustomXML.VideoInvitation;
import com.example.adapter.FriendListAdapter;
import com.example.common.Const;
import com.example.common.Storage;
import com.example.common.Util;
import com.example.data.DataWarehouse;
import com.example.data.LoginData;
import com.example.data.UserData;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.Roster;

import java.util.Set;

public class MainActivity extends ParentActivity {
    private ListView mListViewFriend;
    private Set<String> mChatUsers;

    public static FriendListAdapter mFriendListAdapter;

    private MyHandler myHandler;
    private int mCurrentPosition = -1;
    private String mServiceName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mServiceName = connection.getServiceName();

        mListViewFriend = (ListView) findViewById(R.id.listview_friends);

        mFriendListAdapter = new FriendListAdapter(this, Roster.getInstanceFor(connection).getEntries());

        mChatUsers = DataWarehouse.getGlobalData(this).chatUsers;

        mListViewFriend.setAdapter(mFriendListAdapter);

        //点击进入聊天窗口
        mListViewFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                String friendUserame = mFriendListAdapter.getUsername(position);
                intent.putExtra("friendUsername", friendUserame);
                startActivity(intent);
            }
        });

        //设置聊天对象管理处理监听。
        getChatManager().addChatListener(chatManagerListenerMain);

        myHandler = new MyHandler(this);

        registerForContextMenu(mListViewFriend);
        mListViewFriend.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrentPosition = position;
                return false;
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        switch (id)
        {
            case R.id.menu_item_remove_friend:
                if(mCurrentPosition > -1)
                {
                    mFriendListAdapter.removeUserData(mCurrentPosition);
                }
                break;
        }
        return super.onContextItemSelected(item);
    }



    //创建上下文菜单时触发该方法。
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.friend_list_context_menu,menu);
    }

    //创建聊天对象管理监听器,监听从远端发送过来的message。
    private ChatManagerListener chatManagerListenerMain = new ChatManagerListener() {
        @Override
        public void chatCreated(Chat chat, boolean b) {
            chat.addMessageListener(new ChatMessageListener() {
                @Override
                public void processMessage(Chat chat, Message message) {
                    android.os.Message msg = android.os.Message.obtain();
                    msg.obj = message;
                    myHandler.sendMessage(msg);

                }
            });
        }
    };

    //创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    //相应菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        switch (id)
        {
            //聊天界面
            case R.id.add_friend:
                intent = new Intent(this,AddFriendActivity.class);
                startActivityForResult(intent,1);
                break;
            //聊天室服务界面，显示聊天室服务。
            case R.id.chat_services:
                intent = new Intent(this,ChatServiceActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_item_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("注销")
                        .setMessage("确定要注销当前用户吗?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Storage.putBollean(MainActivity.this, Const.KEY_AUTO_LOGIN, false);
                                try {
                                    connection.disconnect();
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("取消",null);
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case 1:
                if (resultCode == 1)
                {
                    String username = data.getStringExtra("friendName");

                    UserData userData = new UserData(username);
                    //实时刷新ListView
                    mFriendListAdapter.addUserData(userData);
                }
        }
    }

    private static class MyHandler extends Handler {
        private Context mContext;
        private MainActivity mMainActivity;

        public MyHandler(Context mContext) {
            this.mContext = mContext;
            this.mMainActivity = (MainActivity) mContext;
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            Message message = (Message) msg.obj;
            final String friendUsername = Util.extractUserFromChat(message.getFrom());
            //如果包含视频通话信息
            if (message.hasExtension(VideoInvitation.ELEMENT_NAME,VideoInvitation.NAME_SPACE))
            {
                // <video-chat xmlns='com.webrtc.video'><type>video-invitation</type></video-chat>
//                Log.e("XML","XML: "+message.getExtension(VideoInvitation.NAME_SPACE).toXML());

                DefaultExtensionElement defaultExtensionElement = message.getExtension(VideoInvitation.ELEMENT_NAME,
                        VideoInvitation.NAME_SPACE);
                String type = defaultExtensionElement.getValue("type");

                switch (type)
                {
                    case "video-invitation":
                        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity)
                                .setTitle("视频聊天")
                                .setMessage(friendUsername + "请求与你视频聊天");

                        builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //点击拒绝按钮需要做的事情。
                                VideoInvitation videoInvitation = new VideoInvitation();
                                videoInvitation.setTypeText("video-deny");
                                String chatJid = friendUsername+"@"+mMainActivity.mServiceName;
                                Message message = new Message();
                                message.addExtension(videoInvitation);

                                Chat chat = mMainActivity.createChat(chatJid);
                                try {
                                    chat.sendMessage(message);
                                } catch (SmackException.NotConnectedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        //被叫方跳转到VideoCallActivity
                        builder.setPositiveButton("接受", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //单击接受视频通话按钮后，处理的动作
                                VideoInvitation videoInvitation = new VideoInvitation();
                                videoInvitation.setTypeText("video-agree");
                                String chatJid = friendUsername+"@"+mMainActivity.mServiceName;
                                Message message = new Message();
                                message.addExtension(videoInvitation);

                                Chat chat = mMainActivity.createChat(chatJid);
                                try {
                                    chat.sendMessage(message);
                                } catch (SmackException.NotConnectedException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(mMainActivity,VideoCallActivity.class);
                                intent.putExtra("remoteName",friendUsername);
                                mMainActivity.startActivity(intent);
                            }
                        });

                        builder.create().show();
                        break;

                    case "video-deny":
                        Toast.makeText(mMainActivity,"对方拒绝视频邀请",Toast.LENGTH_SHORT).show();
                        break;
                    //主叫方跳转到VideoCallActivity
                    case "video-agree":
                        Intent intent = new Intent(mMainActivity,VideoCallActivity.class);
                        intent.putExtra("createOffer",true);
                        Log.e("getFrom",friendUsername);
                        intent.putExtra("remoteName",friendUsername);
                        mMainActivity.startActivity(intent);
                        break;
                    //视频通话结束，则结束VideoCallActivity。
                    case "video-ended":
                        Intent intentEndVideo = new Intent(mMainActivity,VideoCallActivity.class);
                        intentEndVideo.putExtra("videoEnded",true);
                        mMainActivity.startActivity(intentEndVideo);
                        break;
                }
            }
            //如果是SDP信息
            else if (message.hasExtension(SDPExtensionElement.ELEMENT_NAME,SDPExtensionElement.NAME_SPACE))
            {
                DefaultExtensionElement defaultExtensionElement =
                        message.getExtension(SDPExtensionElement.ELEMENT_NAME, SDPExtensionElement.NAME_SPACE);
                String type = defaultExtensionElement.getValue("type");
                String description = defaultExtensionElement.getValue("description");

                //发送SDP数据到VideoCallActivity
                Intent intent = new Intent(mMainActivity,VideoCallActivity.class);
                intent.putExtra("type",type);
                intent.putExtra("description",description);

                mMainActivity.startActivity(intent);
            }
            //如果是ICE Candidate
            else if (message.hasExtension(IceCandidateExtensionElement.ELEMENT_NAME,
                    IceCandidateExtensionElement.NAME_SPACE))
            {
                DefaultExtensionElement defaultExtensionElement =
                        message.getExtension(IceCandidateExtensionElement.ELEMENT_NAME, IceCandidateExtensionElement.NAME_SPACE);
                String sdpMid = defaultExtensionElement.getValue("sdpMid");
                int sdpMLineIndex = Integer.parseInt(defaultExtensionElement.getValue("sdpMLineIndex"));
                String sdp = defaultExtensionElement.getValue("sdp");
                //发送ICE Candidate数据到VideoCallActivity
                Intent intent = new Intent(mMainActivity,VideoCallActivity.class);
                intent.putExtra("sdpMid",sdpMid);
                intent.putExtra("sdpMLineIndex",sdpMLineIndex);
                intent.putExtra("sdp",sdp);

                mMainActivity.startActivity(intent);
            }
            //如果是聊天文本信息
            else if (!mMainActivity.mChatUsers.contains(friendUsername))
            {
                Log.e("Handler","handler message");
                String body = message.getBody();
                if (body == null)
                {
                    return;
                }

                mMainActivity.mChatUsers.add(friendUsername);

                Intent intent = new Intent(mMainActivity,ChatActivity.class);
                intent.putExtra("friendUsername", friendUsername);
                intent.putExtra("body",body);
                mMainActivity.startActivity(intent);
            }
            else
            {
                String body = message.getBody();
                if (body == null) {
                    return;
                }
                Intent intent = new Intent(mMainActivity,ChatActivity.class);
                intent.putExtra("body",body);
                mMainActivity.startActivity(intent);
            }
        }
    }
}
