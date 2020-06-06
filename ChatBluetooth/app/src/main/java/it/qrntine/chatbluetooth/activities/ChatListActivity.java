package it.qrntine.chatbluetooth.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import it.qrntine.chatbluetooth.database.CancellaThreadDB;
import it.qrntine.chatbluetooth.database.Messaggio;
import it.qrntine.chatbluetooth.database.QueryThreadDB;
import it.qrntine.chatbluetooth.decorator.DecoratorRecyclerView;

public class ChatListActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener{
    private BluetoothSession session= BluetoothSession.getInstance();
    private Holder holder;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<BluetoothDevice> chatDevices = new ArrayList<>();
    private ArrayList<BluetoothDevice> selectedChats = new ArrayList<>();
    private AppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        setTitle("Chat List"); //il titolo

        session.setCurrentActivity(ActivityConstants.ACTIVITY_CHATLIST);

        BluetoothAdapter adapter = session.getmBluetoothChatService().getmAdapter();

        creaDB();
        pairedDevices = adapter.getBondedDevices();

        checkDatabaseForExistingChat();

        holder=new Holder();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkDatabaseForExistingChat();
        holder.rvChatList.getAdapter().notifyDataSetChanged();

        if(session.getErrorNum() == ErrorConstants.ERROR_USER_DISCONNECTED){
            Toast.makeText(ChatListActivity.this, R.string.error_user_disconnected, Toast.LENGTH_LONG).show();
        }
        session.getmBluetoothChatService().stop();
        session.getmBluetoothChatService().start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.delete_menu_chatlist){
            if(selectedChats.size() > 0){
                for(BluetoothDevice device: selectedChats){
                    CancellaThreadDB can = new CancellaThreadDB(db, device.getAddress());
                    Thread cancella = new Thread(can);
                    cancella.start();
                    try {
                        cancella.join();
                        chatDevices.remove(device);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                clearArray(selectedChats);
                Toast.makeText(ChatListActivity.this, "Chat deleted", Toast.LENGTH_LONG).show();
                holder.rvChatList.getAdapter().notifyDataSetChanged();
            }
            else{
                Toast.makeText(ChatListActivity.this, "No chat to delete", Toast.LENGTH_LONG).show();
            }
        }

        return false;
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

    class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatHolder> implements View.OnClickListener, View.OnLongClickListener {

        private ConstraintLayout cl;
        private ArrayList<BluetoothDevice> data;

        public ChatListAdapter(ArrayList<BluetoothDevice> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ChatListAdapter.ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            cl = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_chat_list, parent, false);
            cl.setOnClickListener(this);
            cl.setOnLongClickListener(this);
            return new ChatHolder(cl);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatListAdapter.ChatHolder holder, int position) {
            holder.tvChat.setText(data.get(position).getName());
            if(checkExistance(data.get(position), selectedChats)){
                cl.setBackgroundColor(getColor(R.color.colorBGSelected));
                holder.cvChatList.setCardBackgroundColor(getColor(R.color.colorSelected));
            }
            else{
                cl.setBackgroundColor(Color.TRANSPARENT);
                holder.cvChatList.setCardBackgroundColor(getColor(R.color.colorDestinatario));
            }
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
            clearArray(selectedChats);
            clearArray(chatDevices);
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = ((RecyclerView) v.getParent()).getChildAdapterPosition(v);
            if(!checkExistance(data.get(position), selectedChats)){
                selectedChats.add(data.get(position));
                Toast.makeText(ChatListActivity.this, "Selected Chat", Toast.LENGTH_LONG).show();
                notifyDataSetChanged();
            }else{
                selectedChats.remove(data.get(position));
                Toast.makeText(ChatListActivity.this, "Unselected Chat", Toast.LENGTH_LONG).show();
                notifyDataSetChanged();
            }
            return true;
        }

        class ChatHolder extends RecyclerView.ViewHolder {

            private TextView tvChat;
            private CardView cvChatList;
            private LinearLayout llChatlist;
            public ChatHolder(@NonNull View itemView) {
                super(itemView);
                cvChatList = itemView.findViewById(R.id.cvChatList);
                llChatlist = cvChatList.findViewById(R.id.llChatList);
                tvChat = llChatlist.findViewById(R.id.tvChat);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_chatlist, menu);

        MenuItem deleteItem = menu.findItem(R.id.delete_menu_chatlist);
        deleteItem.setOnMenuItemClickListener(this);

        return true;
    }

    public void checkDatabaseForExistingChat(){
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
                    if(!checkExistance(device, chatDevices)){
                        chatDevices.add(device);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkExistance(BluetoothDevice device, ArrayList<BluetoothDevice> array){
        for(BluetoothDevice d: array){
            if(d.getAddress().equals(device.getAddress())){
                return true;
            }
        }
        return false;
    }

    public void clearArray(ArrayList<BluetoothDevice> array){
        for(BluetoothDevice d: array){
            array.remove(d);
        }
    }
}
