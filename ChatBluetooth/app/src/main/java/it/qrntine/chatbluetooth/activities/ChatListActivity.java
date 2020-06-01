package it.qrntine.chatbluetooth.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.bluetooth.BluetoothSession;

public class ChatListActivity extends AppCompatActivity {
    private BluetoothSession session= BluetoothSession.getInstance();
    private Holder holder;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setTitle("Chat list"); //il titolo
        holder=new Holder();
    }

    class Holder {
        RecyclerView rvChatList;
        public Holder(){
            rvChatList=findViewById(R.id.rvChat);

        }
    }
}
