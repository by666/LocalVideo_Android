package com.example.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.adapter.MultiUserChatRoomListAdapter;
import com.example.common.Const;
import com.example.data.DataWarehouse;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.util.List;

public class MultiUserChatRoomsActivity extends ParentActivity implements Const {
    private ListView listView;
    private MultiUserChatRoomListAdapter multiUserChatRoomListAdapter;
    private ServiceDiscoveryManager serviceDiscoveryManager;
    private String chatServiceJID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_user_chat_rooms);

        listView = (ListView) findViewById(R.id.listview_chat_rooms);
        chatServiceJID = getIntent().getStringExtra(JID);
        setTitle(chatServiceJID);
        serviceDiscoveryManager = ServiceDiscoveryManager.getInstanceFor(connection);

        try {
            multiUserChatRoomListAdapter = new MultiUserChatRoomListAdapter(this,
                    serviceDiscoveryManager.discoverItems(chatServiceJID).getItems());
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        listView.setAdapter(multiUserChatRoomListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DiscoverItems.Item item = multiUserChatRoomListAdapter.getChatRoom(position);
                Intent intent = new Intent(MultiUserChatRoomsActivity.this, MultiUserChatRoomActivity.class);
                intent.putExtra(JID, chatServiceJID);
                intent.putExtra(CHAT_ROOM_NAME, item.getName());
                startActivity(intent);

            }
        });
    }

    //新建聊天室
    public void onClick_New_Room(View view) {
        final EditText editText = new EditText(MultiUserChatRoomsActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(MultiUserChatRoomsActivity.this)
                .setTitle("请输入聊天室名称")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String chatRoomName = editText.getText().toString().trim();
                        if ("".equals(chatRoomName))
                        {
                            Toast.makeText(MultiUserChatRoomsActivity.this,
                                    "聊天时名称不能为空",Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (!isConnected())
                        {
                            throw new NullPointerException("服务器连接失败，请先连接服务器");
                        }

                        // 创建一个MultiUserChat
                        MultiUserChat muc = null;
                        muc = MultiUserChatManager.getInstanceFor(connection).getMultiUserChat(
                                chatRoomName+"@"+chatServiceJID);
                        // 创建聊天室
                        try {
                            boolean isCreate = muc.createOrJoin(DataWarehouse.getGlobalData(
                                    MultiUserChatRoomsActivity.this).loginData.userName);
                        } catch (XMPPException.XMPPErrorException e) {
                            e.printStackTrace();
                        } catch (SmackException e) {
                            e.printStackTrace();
                        }
                        Form form = null;
                        try {
                            form = muc.getConfigurationForm();
                        } catch (SmackException.NoResponseException e) {
                            e.printStackTrace();
                        } catch (XMPPException.XMPPErrorException e) {
                            e.printStackTrace();
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                        Form submitForm = form.createAnswerForm();
                        List<FormField> fields = form.getFields();
                        for (FormField field : fields) {
                            if (!FormField.Type.hidden.equals(field.getType()) &&
                                    field.getVariable() != null) {
                                submitForm.setDefaultAnswer(field.getVariable());
                            }
                        }

                        //  设置聊天室为公共聊天室
                        submitForm.setAnswer("muc#roomconfig_publicroom", true);
                        //  设置聊天室是永久存在的
                        submitForm.setAnswer("muc#roomconfig_persistentroom", true);

                        try {
                            muc.sendConfigurationForm(submitForm);
                        } catch (SmackException.NoResponseException e) {
                            e.printStackTrace();
                        } catch (XMPPException.XMPPErrorException e) {
                            e.printStackTrace();
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                        try {
                            multiUserChatRoomListAdapter.updateChatRoom(serviceDiscoveryManager.
                                    discoverItems(chatServiceJID).getItems());
                        } catch (SmackException.NoResponseException e) {
                            e.printStackTrace();
                        } catch (XMPPException.XMPPErrorException e) {
                            e.printStackTrace();
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(MultiUserChatRoomsActivity.this, "成功创建聊天室", Toast.LENGTH_LONG).show();

                    }
                })
                .setNegativeButton("取消",null);
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id)
        {
            case R.id.menu_item_chat_room_refresh:
                try
                {
                    multiUserChatRoomListAdapter.updateChatRoom(serviceDiscoveryManager.
                            discoverItems(chatServiceJID).getItems());
                    Toast.makeText(this, "成功刷新", Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    Toast.makeText(this, "刷新失败", Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
