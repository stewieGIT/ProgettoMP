package it.qrntine.chatbluetooth.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.bluetooth.BluetoothChatService;
import it.qrntine.chatbluetooth.bluetooth.BluetoothSession;
import it.qrntine.chatbluetooth.bluetooth.MessageConstants;
import it.qrntine.chatbluetooth.database.AppDatabase;
import it.qrntine.chatbluetooth.database.InserisciThreadDB;
import it.qrntine.chatbluetooth.database.Messaggio;
import it.qrntine.chatbluetooth.database.QueryThreadDB;
import it.qrntine.chatbluetooth.decorator.DecoratorRecyclerView;

public class ChatActivity extends AppCompatActivity {
    private Handler mHandler;
    private Holder holder;
    private AppDatabase db; //riferimento al db
    private List<Messaggio> messaggi; //lista messaggi relativi alla chat con il destinatario
    private BluetoothSession session=BluetoothSession.getInstance();
    private String mittente = "";

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_chat);

        creaDB(); //ritorna il riferimento al db

        messaggi = new ArrayList<>();

        holder = new Holder();

        // DA GESTIRE LA CALLBACK FARE UN NUOVO HANDLER DEDICATO ALLA CHAT NEL BLUETOOTH CHAT SERVICE DA PASSARE AL CONNECTED THREAD
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MessageConstants.MESSAGE_WRITE:
                        byte[] buf = (byte[]) msg.obj;
                        String m = new String(buf);
                        Messaggio messaggio = new Messaggio();
                        messaggio.testo = m;
                        messaggio.mittente = BluetoothAdapter.getDefaultAdapter().getAddress();
                        messaggi.add(messaggio);
                        holder.rvChat.getAdapter().notifyDataSetChanged();
                        break;
                    case MessageConstants.MESSAGE_READ:
                        buf = (byte[]) msg.obj;
                        m = new String(buf, 0 , msg.arg1);
                        messaggio = new Messaggio();
                        messaggio.testo = m;
                        messaggi.add(messaggio);
                        holder.rvChat.getAdapter().notifyDataSetChanged();
                        break;
                    case MessageConstants.MESSAGE_TOAST: break;
                }
                return true;
            }
        } ) ;

        //session.getmBluetoothChatService().setChatHandler(mHandler);
        //session.getmBluetoothChatService().connect(session.getDevice(), true);
        session.getmBluetoothChatService().getmConnectedThread().setmHandler(mHandler);
    }

    /**
     * per il menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
       /* if(item.getItemId() == R.id.itRicerca){
            //ricerca keyword
        }else{
            return super.onContextItemSelected(item);
        }*/
        return true;
    }

    /**
     * crea istanza di DB con Room
     */
    private void creaDB(){
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "messaggi").build();
    }

    class Holder implements View.OnClickListener{

        private RecyclerView rvChat;
        private EditText etInserisciMessaggio;
        private Button btnInviaMessaggio;

        public Holder(){
            etInserisciMessaggio = findViewById(R.id.etInserisciMessaggio);
            btnInviaMessaggio = findViewById(R.id.btnInviaMessaggio);
            btnInviaMessaggio.setOnClickListener(this);

            rvChat = findViewById(R.id.rvChat);
            rvChat.setAdapter(new ChatBluetoothAdapter(messaggi));
            LinearLayoutManager lm = new LinearLayoutManager(ChatActivity.this);
            rvChat.setLayoutManager(lm);

            DecoratorRecyclerView decorator = new DecoratorRecyclerView(15); //decorator per spaziare gli oggetti
            rvChat.addItemDecoration(decorator);
        }

        /**
         * nel caso del bottone inviaMessaggio provvederà a creare il messaggio, visualizzarlo nella RV e salvarlo nel DB
         * @param v la view che ha ricevuto l'evento
         */
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnInviaMessaggio){
                if(!etInserisciMessaggio.getText().toString().equals("")){
                    session.getmBluetoothChatService().write(etInserisciMessaggio.getText().toString().getBytes());
                    etInserisciMessaggio.setText("");
                }
            }
        }
    }

    /*
    /*adapter per il recyclerview*/
    class ChatBluetoothAdapter extends RecyclerView.Adapter<ChatBluetoothAdapter.ChatHolder>{

        private List<Messaggio> dati;

        public ChatBluetoothAdapter(List<Messaggio> dati){
            this.dati = dati;
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + dati);
        }

        @NonNull
        @Override
        public ChatBluetoothAdapter.ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RelativeLayout rl = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat,
                    parent, false);
            return new ChatHolder(rl);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatBluetoothAdapter.ChatHolder holder, int position) {
            holder.tvMessaggio.setText(dati.get(position).testo);
            holder.tvData.setText(dati.get(position).ora);
            //serve per fare il display dei messaggi
            if(dati.get(position).mittente != null){
                if(dati.get(position).mittente.equals(BluetoothAdapter.getDefaultAdapter().getAddress())){
                    holder.rlChat.setGravity(Gravity.RIGHT); //se sei il mittente i messaggi sono visualizzati a destra
                    System.out.println("DESTRA");
                }
                else{
                    holder.rlChat.setGravity(Gravity.LEFT); //altrimenti a sinistra
                    System.out.println("SINISTRA");
                }
            }
        }

        @Override
        public int getItemCount() {
            if(dati != null)
                return dati.size();
            return 0;
        }

        class ChatHolder extends RecyclerView.ViewHolder{

            private RelativeLayout rlChat;
            private CardView cvChat;
            private TextView tvMessaggio;
            private TextView tvData;

            public ChatHolder(@NonNull View itemView) {
                super(itemView);

                rlChat = itemView.findViewById(R.id.rlChat);
                cvChat = itemView.findViewById(R.id.cvChat);
                tvMessaggio = cvChat.findViewById(R.id.tvMessaggio);
                tvData = cvChat.findViewById(R.id.tvData);
            }
        }
    }
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
