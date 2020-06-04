package it.qrntine.chatbluetooth.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.bluetooth.BluetoothSession;
import it.qrntine.chatbluetooth.database.AppDatabase;
import it.qrntine.chatbluetooth.database.Messaggio;
import it.qrntine.chatbluetooth.database.QueryThreadDB;
import it.qrntine.chatbluetooth.decorator.DecoratorRecyclerView;

public class ChatListActivity extends AppCompatActivity {
    private BluetoothSession session= BluetoothSession.getInstance();
    private Holder holder;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<BluetoothDevice> chatDevices = new ArrayList<>();
    private AppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        setTitle("Chat list"); //il titolo

        BluetoothAdapter adapter = session.getmBluetoothChatService().getmAdapter();

        creaDB();
        pairedDevices = adapter.getBondedDevices();

        Iterator it = pairedDevices.iterator();
        BluetoothDevice device;
        List<Messaggio> messaggi;
        while(it.hasNext()){
            device = (BluetoothDevice) it.next();
            QueryThreadDB sc = new QueryThreadDB(db, session.getmBluetoothChatService().getmAdapter().getAddress()
                    , device.getAddress());
            Thread scarica = new Thread(sc);
            scarica.start();
            try {
                scarica.join();
                messaggi = sc.scarica();
                if(messaggi.size() > 0){
                    chatDevices.add(device);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        holder=new Holder();
    }

    class Holder {
        RecyclerView rvChatList;
        public Holder(){
            rvChatList=findViewById(R.id.rvChatList);

            rvChatList.setAdapter(new ChatListAdapter(chatDevices));
            rvChatList.setLayoutManager(new LinearLayoutManager(ChatListActivity.this));

            DecoratorRecyclerView decorator = new DecoratorRecyclerView(10);
            rvChatList.addItemDecoration(decorator);
        }
    }

    class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatHolder> implements View.OnClickListener {

        private ArrayList<BluetoothDevice> data;

        public ChatListAdapter(ArrayList<BluetoothDevice> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ChatListAdapter.ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_chat_list, parent, false);
            cl.setOnClickListener(this);
            return new ChatHolder(cl);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatListAdapter.ChatHolder holder, int position) {
            holder.tvChat.setText(data.get(position).getName());
        }

        @Override
        public int getItemCount() {
            if(data != null){
                return data.size();
            }
            return 0;
        }

        @Override
        public void onClick(View v) {
            int position = ((RecyclerView) v.getParent()).getChildAdapterPosition(v);

            session.setDevice(data.get(position));
            session.getmBluetoothChatService().connect(data.get(position), true);
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            startActivity(intent);
        }

        class ChatHolder extends RecyclerView.ViewHolder {

            private TextView tvChat;
            public ChatHolder(@NonNull View itemView) {
                super(itemView);
                tvChat = itemView.findViewById(R.id.tvChat);
            }
        }
    }

    /**
     * crea istanza di DB con Room
     */
    private void creaDB(){
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "messaggi").build();
    }
}
