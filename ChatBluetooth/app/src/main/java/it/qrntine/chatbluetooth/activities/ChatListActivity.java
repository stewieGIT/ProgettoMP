package it.qrntine.chatbluetooth.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.hardik.clickshrinkeffect.ClickShrinkEffectKt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import it.qrntine.chatbluetooth.Constants;
import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.bluetooth.BluetoothSession;
import it.qrntine.chatbluetooth.database.AppDatabase;
import it.qrntine.chatbluetooth.database.CancellaThreadDB;
import it.qrntine.chatbluetooth.database.CancellaTuttoThreadDB;
import it.qrntine.chatbluetooth.database.Messaggio;
import it.qrntine.chatbluetooth.database.QueryThreadDB;
import it.qrntine.chatbluetooth.decorator.DecoratorRecyclerView;


public class ChatListActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {
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
        setTitle(getString(R.string.chat_list)); //titolo activity

        session.setCurrentActivity(Constants.ACTIVITY_CHATLIST); //activity attiva nella sessione

        BluetoothAdapter adapter = session.getmBluetoothChatService().getmAdapter();
        pairedDevices = adapter.getBondedDevices(); //prendiamo i dispositivi già accoppiati

        creaDB();
        checkDatabaseForExistingChat(); //verifichiamo che abbiamo una chat con questi devices

        holder=new Holder();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkDatabaseForExistingChat(); //verifichiamo se esistono nuove chat
        holder.rvChatList.getAdapter().notifyDataSetChanged();

        if(session.getErrorNum() == Constants.ERROR_USER_DISCONNECTED){ //annunciamo che il destinatario si è disconnesso
            Toast.makeText(ChatListActivity.this, R.string.error_user_disconnected, Toast.LENGTH_LONG).show();
            session.getmBluetoothChatService().stop(); //riavviamo il servizio di chat
            session.getmBluetoothChatService().start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.delete_menu_chatlist){
            if(selectedChats != null){
                if(selectedChats.size() > 0){ //se esistono elementi selezionati cancelliamo le chat selezionate
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
                    selectedChats.clear(); //puliamo l'array
                    Toast.makeText(ChatListActivity.this, getString(R.string.chat_deleted), Toast.LENGTH_SHORT).show();
                    holder.rvChatList.getAdapter().notifyDataSetChanged();
                }
                else{
                    Toast.makeText(ChatListActivity.this, getString(R.string.no_chat_to_delete), Toast.LENGTH_SHORT).show();
                }
            }
        }
        if(item.getItemId()==R.id.delete_all){
                if (chatDevices.size() > 0) {
                    CancellaTuttoThreadDB can = new CancellaTuttoThreadDB(db);
                    Thread cancella = new Thread(can);
                    cancella.start();
                    try {
                        cancella.join();
                        chatDevices.clear();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ChatListActivity.this, getString(R.string.chats_deleted), Toast.LENGTH_SHORT).show();
                    holder.rvChatList.getAdapter().notifyDataSetChanged();
                }
            else{
                Toast.makeText(ChatListActivity.this, getString(R.string.no_chat_to_delete), Toast.LENGTH_SHORT).show();
                }
            }
        holder.deleteItem.setVisible(false);

        return false;
    }

    class Holder implements View.OnClickListener {
        Button btnTabRicerca, btnTabChat;
        TextView tvTueConv;
        RecyclerView rvChatList;
        MenuItem deleteItem;
        MenuItem deleteAll;

        public Holder(){
            tvTueConv=findViewById(R.id.tvTueConversazioni);
            btnTabChat=findViewById(R.id.btnTabChat);
            btnTabRicerca=findViewById(R.id.btnTabRicerca);
            btnTabChat.setEnabled(false);
            btnTabChat.setTypeface(Typeface.DEFAULT_BOLD);
            btnTabRicerca.setOnClickListener(this);
            btnTabChat.setAlpha(0.7f);
            rvChatList=findViewById(R.id.rvChatList);
            rvChatList.setAdapter(new ChatListAdapter(chatDevices));
            rvChatList.setLayoutManager(new LinearLayoutManager(ChatListActivity.this));
            DecoratorRecyclerView decorator = new DecoratorRecyclerView(Constants.RV_HEIGHT);
            rvChatList.addItemDecoration(decorator);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnTabRicerca) {
                onBackPressed();
            }
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
            ClickShrinkEffectKt.applyClickShrink(cl);
            return new ChatHolder(cl);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatListAdapter.ChatHolder holder, int position) {
            holder.tvChat.setText(data.get(position).getName());
            if(checkExistance(data.get(position), selectedChats)){ //se esiste nell'array selezionati evidenzialo
                 holder.cvChatList.setCardBackgroundColor(getColor(R.color.colorSelected));
            }
            else{ //altrimenti niente effetto visivo
                holder.cvChatList.setCardBackgroundColor(getColor(R.color.colorMainCv));
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
            if(selectedChats.size() > 0){
               onLongClick(v);
            } else{
            session.setDevice(data.get(position));
            session.getmBluetoothChatService().connect(data.get(position), true);
            selectedChats.clear(); //pulisci gli array per il passaggio all'activity chat
            chatDevices.clear();
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int position = ((RecyclerView) v.getParent()).getChildAdapterPosition(v);
            if(!checkExistance(data.get(position), selectedChats)){ //se l'elemento non esiste aggiungilo
                holder.deleteItem.setVisible(true);
                selectedChats.add(data.get(position));
                //Toast.makeText(ChatListActivity.this, "Selected Chat", Toast.LENGTH_LONG).show();
                notifyDataSetChanged();
            }else{ //altrimenti no
                holder.deleteItem.setVisible(false);
                selectedChats.remove(data.get(position));
                //Toast.makeText(ChatListActivity.this, "Unselected Chat", Toast.LENGTH_LONG).show();
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
                AppDatabase.class, getString(R.string.messaggi)).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chatlist, menu);
        holder.deleteItem = menu.findItem(R.id.delete_menu_chatlist);
        holder.deleteItem.setVisible(false);
        holder.deleteAll= menu.findItem(R.id.delete_all);
        holder.deleteAll.setOnMenuItemClickListener(this);
        holder.deleteItem.setOnMenuItemClickListener(this);
        return true;
    }

    //verifica se esiste una chat per ogni dispositivo accoppiato
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

    //verifica l'esistenza di un oggetto nell'array
    public boolean checkExistance(BluetoothDevice device, ArrayList<BluetoothDevice> array){
        for(BluetoothDevice d: array){
            if(d.getAddress().equals(device.getAddress())){
                return true;
            }
        }
        return false;
    }

}

