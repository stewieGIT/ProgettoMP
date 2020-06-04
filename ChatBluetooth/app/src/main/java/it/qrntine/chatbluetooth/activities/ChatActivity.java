package it.qrntine.chatbluetooth.activities;

import android.bluetooth.BluetoothAdapter;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.bluetooth.BluetoothSession;
import it.qrntine.chatbluetooth.bluetooth.MessageConstants;
import it.qrntine.chatbluetooth.codifica.CodificaAES;
import it.qrntine.chatbluetooth.codifica.MetaMessaggio;
import it.qrntine.chatbluetooth.database.AppDatabase;
import it.qrntine.chatbluetooth.database.InserisciThreadDB;
import it.qrntine.chatbluetooth.database.Messaggio;
import it.qrntine.chatbluetooth.database.QueryThreadDB;
import it.qrntine.chatbluetooth.decorator.DecoratorRecyclerView;
import it.qrntine.chatbluetooth.markdown.ParserMarkdown;

public class ChatActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private SearchView searchView;
    private String data;
    private String time;
    private CodificaAES codifica;
    private Handler mHandler;
    private Holder holder;
    private AppDatabase db; //riferimento al db
    private List <Messaggio> messaggi; //lista messaggi relativi alla chat con il destinatario
    private BluetoothSession session = BluetoothSession.getInstance();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_chat);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
        data = calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" +
                calendar.get(Calendar.YEAR);
        int minuto = calendar.get(Calendar.MINUTE);
        if(minuto < 10) {
            time = calendar.get(Calendar.HOUR_OF_DAY) + ":0" + minuto;
        } else {
            time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + minuto;
        }

        creaDB(); //ritorna il riferimento al db

        messaggi = new ArrayList <>();
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
                MetaMessaggio metaR, metaW;

                switch (msg.what) {
                    case MessageConstants.MESSAGE_WRITE: //niente codifica write
                        metaW = (MetaMessaggio) msg.obj;
                        writeMessage(metaW, false);
                        break;
                    case MessageConstants.MESSAGE_READ: //niente codifica read
                        metaR = (MetaMessaggio) msg.obj;
                        readMessage(metaR, false);
                        break;
                    case MessageConstants.MESSAGE_OBJECT_WRITE: //codifica write
                        metaW = (MetaMessaggio) msg.obj;
                        writeMessage(metaW, true);
                        break;
                    case MessageConstants.MESSAGE_OBJECT_READ: //codifica read
                        metaR = (MetaMessaggio) msg.obj;
                        readMessage(metaR, true);
                        break;
                    case MessageConstants.MESSAGE_TOAST:
                        break;
                }
                return true;
            }
        });
    }

    public void writeMessage(MetaMessaggio msg, boolean codifica) {
        Messaggio messaggio = new Messaggio();

        if (codifica) {
            CodificaAES cod = new CodificaAES();
            messaggio.testo = cod.decodificaMessaggio(msg);
        } else {
            messaggio.testo = new String(msg.getTesto());
        }
        messaggio.mittente = session.getmBluetoothChatService().getmAdapter().getAddress();
        messaggio.destinatario = session.getDevice().getAddress();
        messaggio.data = data;
        messaggio.ora = time;
        messaggi.add(messaggio);
        InserisciThreadDB in = new InserisciThreadDB(db, messaggio);
        Thread inserisci = new Thread(in);
        inserisci.start();
        holder.rvChat.getAdapter().notifyDataSetChanged();
        holder.rvChat.smoothScrollToPosition(messaggi.size()-1);

    }

    public void readMessage(MetaMessaggio msg, boolean codifica) {
        Messaggio messaggio = new Messaggio();

        if (codifica) {
            CodificaAES cod = new CodificaAES();
            messaggio.testo = cod.decodificaMessaggio(msg);
        } else {
            messaggio.testo = new String(msg.getTesto());
        }
        messaggio.mittente = session.getDevice().getAddress();
        messaggio.destinatario = session.getmBluetoothChatService().getmAdapter().getAddress();
        messaggio.data = data;
        messaggio.ora = time;
        messaggi.add(messaggio);
        InserisciThreadDB in = new InserisciThreadDB(db, messaggio);
        Thread inserisci = new Thread(in);
        inserisci.start();
        holder.rvChat.getAdapter().notifyDataSetChanged();
        holder.rvChat.smoothScrollToPosition(messaggi.size()-1);

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(session.getmBluetoothChatService().getmConnectedThread() != null)
            session.getmBluetoothChatService().getmConnectedThread().setmHandler(mHandler);

        if(session.getmBluetoothChatService().getmState() != 3){
            holder.btnCSend.setEnabled(false);
            holder.btnInviaMessaggio.setEnabled(false);
        }
    }

    /**
     * per il menu
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    private void creaDB() {
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "messaggi").build();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(!newText.isEmpty()) {
            ArrayList <Messaggio> tmp = new ArrayList <>();
            for (Messaggio msg : messaggi) {
                if (msg.testo.contains(newText)) {
                    tmp.add(msg);
                }
            }
            holder.rvChat.setAdapter(new ChatBluetoothAdapter(tmp));
            holder.rvChat.getAdapter().notifyDataSetChanged();
        }
        else {
            holder.rvChat.setAdapter(new ChatBluetoothAdapter(messaggi));
            holder.rvChat.getAdapter().notifyDataSetChanged();
            holder.rvChat.smoothScrollToPosition(messaggi.size()-1);
        }
        return false;
    }



    class Holder implements View.OnClickListener {

        private RecyclerView rvChat;
        private EditText etInserisciMessaggio;
        private Button btnInviaMessaggio, btnCSend;

        public Holder() {
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
         *
         * @param v la view che ha ricevuto l'evento
         */
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btnInviaMessaggio) { //manda messaggio non codificato
                if (!etInserisciMessaggio.getText().toString().equals("")) {
                    String messaggioInserito = etInserisciMessaggio.getText().toString();

                    if (!messaggioInserito.matches("^(<(.+?)*>)")) {  //se il messaggio non e' in formato html, verifico se ha notazioni markdown
                        int n;
                        n = ParserMarkdown.numParser(messaggioInserito);
                        messaggioInserito = ParserMarkdown.parsing(messaggioInserito, n);   //faccio il parsing da markdown al corrispondente html, leggibile dalla textview
                    }

                    MetaMessaggio metaMessaggio = new MetaMessaggio();
                    metaMessaggio.setTesto(messaggioInserito.getBytes());
                    session.getmBluetoothChatService().getmConnectedThread().writeObject(metaMessaggio);
                    etInserisciMessaggio.setText("");
                }
            }
            if (v.getId() == R.id.btnCSend) { //manda messaggio codificato
                if (!etInserisciMessaggio.getText().toString().equals("")) {
                    codifica = new CodificaAES();

                    String messaggioInserito = etInserisciMessaggio.getText().toString();

                    if (!messaggioInserito.matches("^(<(.+?)*>)")) {  //se il messaggio non e' in formato html, verifico se ha notazioni markdown
                        int n;
                        n = ParserMarkdown.numParser(messaggioInserito);
                        messaggioInserito = ParserMarkdown.parsing(messaggioInserito, n);   //faccio il parsing da markdown al corrispondente html, leggibile dalla textview
                    }

                    MetaMessaggio metaMessaggio = codifica.codificaMessaggio(messaggioInserito);

                    System.out.println(">>>>>>>>>>>>>>>>>>>>MSG CODED: " + metaMessaggio);

                    session.getmBluetoothChatService().getmConnectedThread().writeObject(metaMessaggio);
                    etInserisciMessaggio.setText("");
                }
            }
        }
    }

    /*
    /*adapter per il recyclerview*/
    class ChatBluetoothAdapter extends RecyclerView.Adapter <ChatBluetoothAdapter.ChatHolder> {

        private List <Messaggio> dati;

        public ChatBluetoothAdapter(List <Messaggio> dati) {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.tvMessaggio.setText(Html.fromHtml(dati.get(position).testo, Html.FROM_HTML_MODE_LEGACY));
            } else {
                holder.tvMessaggio.setText(dati.get(position).testo);
            }
            holder.tvData.setText(dati.get(position).ora);
            //serve per fare il display dei messaggi
            if (dati.get(position).mittente != null) {
                if (dati.get(position).mittente.equals(BluetoothAdapter.getDefaultAdapter().getAddress())) {
                    holder.cvChat.setCardBackgroundColor(getColor(R.color.colorMittente));
                    holder.rlChat.setGravity(Gravity.RIGHT); //se sei il mittente i messaggi sono visualizzati a destra
                    System.out.println("DESTRA");
                } else {
                    holder.cvChat.setCardBackgroundColor(getColor(R.color.colorDestinatario));
                    holder.rlChat.setGravity(Gravity.LEFT); //altrimenti a sinistra
                    System.out.println("SINISTRA");
                }
            }
        }

        @Override
        public int getItemCount() {
            if (dati != null)
                return dati.size();
            return 0;
        }


        class ChatHolder extends RecyclerView.ViewHolder {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        MenuItem searchItem = menu.findItem(R.id.search_menu);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search Message");
        searchView.setOnQueryTextListener(this);

        return true;
    }
    }


