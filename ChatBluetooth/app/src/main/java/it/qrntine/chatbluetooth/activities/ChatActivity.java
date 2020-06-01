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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.bluetooth.BluetoothChatService;
import it.qrntine.chatbluetooth.bluetooth.BluetoothSession;
import it.qrntine.chatbluetooth.bluetooth.MessageConstants;
import it.qrntine.chatbluetooth.codifica.CodificaAES;
import it.qrntine.chatbluetooth.codifica.MetaMessaggio;
import it.qrntine.chatbluetooth.database.AppDatabase;
import it.qrntine.chatbluetooth.database.InserisciThreadDB;
import it.qrntine.chatbluetooth.database.Messaggio;
import it.qrntine.chatbluetooth.database.QueryThreadDB;
import it.qrntine.chatbluetooth.decorator.DecoratorRecyclerView;

public class ChatActivity extends AppCompatActivity {
    private CodificaAES codifica;
    private Handler mHandler;
    private Holder holder;
    private AppDatabase db; //riferimento al db
    private List<Messaggio> messaggi; //lista messaggi relativi alla chat con il destinatario
    private BluetoothSession session=BluetoothSession.getInstance();

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_chat);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
        final String data = calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" +
                calendar.get(Calendar.YEAR);
        final String time = calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE);

        creaDB(); //ritorna il riferimento al db

        messaggi = new ArrayList<>();
        QueryThreadDB sc = new QueryThreadDB(db, session.getmBluetoothChatService().getmAdapter().getAddress(),
                session.getDevice().getAddress());
        Thread scarica = new Thread(sc);
        scarica.start();
        try {
            scarica.join();
            messaggi = sc.scarica(); //scarica la cronologia della chat
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        holder = new Holder();

        // DA GESTIRE LA CALLBACK FARE UN NUOVO HANDLER DEDICATO ALLA CHAT NEL BLUETOOTH CHAT SERVICE DA PASSARE AL CONNECTED THREAD
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                InserisciThreadDB in;
                Thread inserisci;
                Messaggio messaggio;
                MetaMessaggio meta;
                String m;
                byte[] buf;

                switch (msg.what) {
                    case MessageConstants.MESSAGE_WRITE:
                        buf = (byte[]) msg.obj;
                        m = new String(buf);
                        messaggio = new Messaggio();
                        messaggio.testo = m;
                        messaggio.mittente = session.getmBluetoothChatService().getmAdapter().getAddress();
                        messaggio.destinatario = session.getDevice().getAddress();
                        messaggio.data = data;
                        messaggio.ora = time;
                        messaggi.add(messaggio);
                        in = new InserisciThreadDB(db, messaggio);
                        inserisci = new Thread(in);
                        inserisci.start();
                        holder.rvChat.getAdapter().notifyDataSetChanged();
                        break;
                    case MessageConstants.MESSAGE_READ:
                        buf = (byte[]) msg.obj;
                        m = new String(buf, 0 , msg.arg1);
                        messaggio = new Messaggio();
                        messaggio.testo = m;
                        messaggio.mittente = session.getDevice().getAddress();
                        messaggio.destinatario = session.getmBluetoothChatService().getmAdapter().getAddress();
                        messaggio.data = data;
                        messaggio.ora = time;
                        messaggi.add(messaggio);
                        in = new InserisciThreadDB(db, messaggio);
                        inserisci = new Thread(in);
                        inserisci.start();
                        holder.rvChat.getAdapter().notifyDataSetChanged();
                        break;
                    case MessageConstants.MESSAGE_TOAST: break;
                    case MessageConstants.MESSAGE_OBJECT_WRITE:
                        meta = (MetaMessaggio) msg.obj;
                        messaggio = new Messaggio();
                        messaggio.testo = codifica.decodificaMessaggio(meta);
                        messaggio.mittente = session.getmBluetoothChatService().getmAdapter().getAddress();
                        messaggio.destinatario = session.getDevice().getAddress();
                        messaggio.data = data;
                        messaggio.ora = time;
                        messaggi.add(messaggio);
                        in = new InserisciThreadDB(db, messaggio);
                        inserisci = new Thread(in);
                        inserisci.start();
                        holder.rvChat.getAdapter().notifyDataSetChanged();
                        break;
                    case MessageConstants.MESSAGE_OBJECT_READ:
                        meta = (MetaMessaggio) msg.obj;
                        messaggio = new Messaggio();
                        messaggio.testo = codifica.decodificaMessaggio(meta);
                        messaggio.mittente = session.getDevice().getAddress();
                        messaggio.destinatario = session.getmBluetoothChatService().getmAdapter().getAddress();
                        messaggio.data = data;
                        messaggio.ora = time;
                        messaggi.add(messaggio);
                        in = new InserisciThreadDB(db, messaggio);
                        inserisci = new Thread(in);
                        inserisci.start();
                        holder.rvChat.getAdapter().notifyDataSetChanged();
                        break;
                }
                return true;
            }
        } ) ;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(session.getmBluetoothChatService().getmConnectedThread() != null)
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
        private Button btnInviaMessaggio, btnCSend;

        public Holder(){
            etInserisciMessaggio = findViewById(R.id.etInserisciMessaggio);
            btnInviaMessaggio = findViewById(R.id.btnInviaMessaggio);
            btnCSend = findViewById(R.id.btnCSend);
            btnInviaMessaggio.setOnClickListener(this);
            btnCSend.setOnClickListener(this);

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
            if(v.getId() == R.id.btnCSend){
                if(!etInserisciMessaggio.getText().toString().equals("")){
                    codifica = new CodificaAES();
                    String messaggio = etInserisciMessaggio.getText().toString();

                    MetaMessaggio metaMessaggio = codifica.codificaMessaggio(messaggio);
                    session.getmBluetoothChatService().getmConnectedThread().writeObject(metaMessaggio);
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
