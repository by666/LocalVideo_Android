package com.example.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.adapter.ChatServiceListAdapter;
import com.example.common.Const;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatServiceActivity extends ParentActivity implements Const{
    private ListView listView;
    private ChatServiceListAdapter chatServiceListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_service);

        listView = (ListView) findViewById(R.id.listview_chat_services);
        chatServiceListAdapter = new ChatServiceListAdapter(this,getHostedRooms());
        listView.setAdapter(chatServiceListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String jid = chatServiceListAdapter.getJID(position);
                Intent intent = new Intent(ChatServiceActivity.this,MultiUserChatRoomsActivity.class);
                intent.putExtra(JID,jid);
                startActivity(intent);
            }
        });
    }

    private List<HostedRoom> getHostedRooms()
    {

        List<HostedRoom> chatServices = new ArrayList<>();

        try {
            chatServices = MultiUserChatManager.getInstanceFor(connection).
                    getHostedRooms(connection.getServiceName());
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return chatServices;
    }
}
