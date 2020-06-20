package it.qrntine.chatbluetooth.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.hardik.clickshrinkeffect.ClickShrinkEffectKt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import it.qrntine.chatbluetooth.Constants;
import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.bluetooth.BluetoothSession;
import it.qrntine.chatbluetooth.codifica.CodificaAES;
import it.qrntine.chatbluetooth.codifica.MetaMessaggio;
import it.qrntine.chatbluetooth.database.AppDatabase;
import it.qrntine.chatbluetooth.database.CancellaMessaggiThreadDB;
import it.qrntine.chatbluetooth.database.InserisciThreadDB;
import it.qrntine.chatbluetooth.database.Messaggio;
import it.qrntine.chatbluetooth.database.QueryThreadDB;
import it.qrntine.chatbluetooth.decorator.DecoratorRecyclerView;
import it.qrntine.chatbluetooth.emoticons.EmoticonsManager;
import it.qrntine.chatbluetooth.markdown.ParserMarkdown;

public class ChatActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, MenuItem.OnMenuItemClickListener {

    private static final int REQCODE_POPUP_ACTIVITY = 101;
    private String data;
    private String time;
    private CodificaAES codifica;
    private Handler mHandler;
    private Holder holder;
    private AppDatabase db; //riferimento al db
    private List <Messaggio> messaggi; //lista messaggi relativi alla chat con il destinatario
    private BluetoothSession session = BluetoothSession.getInstance();
    private boolean modCriptata;
    private ArrayList<Messaggio> selectedMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_chat);
        setTitle(session.getDevice().getName());
        session.setCurrentActivity(Constants.ACTIVITY_CHAT); //activity attiva nella sessione
        modCriptata = false; //AES disattivato
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

        if(messaggi.size() > 0) holder.rvChat.smoothScrollToPosition(messaggi.size()-1);

        //gestiscisce comunicazione con il thread connected
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                MetaMessaggio metaR, metaW;

                switch (msg.what) {
                    case Constants.MESSAGE_WRITE: //niente codifica write
                        metaW = (MetaMessaggio) msg.obj;
                        writeMessage(metaW, false);
                        break;
                    case Constants.MESSAGE_READ: //niente codifica read
                        metaR = (MetaMessaggio) msg.obj;
                        readMessage(metaR, false);
                        break;
                    case Constants.MESSAGE_OBJECT_WRITE: //codifica write
                        metaW = (MetaMessaggio) msg.obj;
                        writeMessage(metaW, true);
                        break;
                    case Constants.MESSAGE_OBJECT_READ: //codifica read
                        metaR = (MetaMessaggio) msg.obj;
                        readMessage(metaR, true);
                        break;
                    case Constants.MESSAGE_TOAST:
                        break;
                    case Constants.MESSAGE_OBJECT_ERROR_READ:
                        session.setErrorNum(Constants.ERROR_USER_DISCONNECTED);
                        finish();
                        break;
                }
                return true;
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(session.getmBluetoothChatService().getmConnectedThread() != null)
            session.getmBluetoothChatService().getmConnectedThread().setmHandler(mHandler); //passa il chat handler

        if(session.getmBluetoothChatService().getmState() != 3){ //se non connesso disattiva bottoni
            holder.ivModCriptata.setEnabled(false);
            holder.ivInviaMessaggio.setEnabled(false);
            holder.ivSmileBtn.setEnabled(false);
            holder.ivInviaMessaggio.setAlpha(0.3f);
            holder.ivModCriptata.setAlpha(0.3f);
            holder.ivSmileBtn.setAlpha(0.3f);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.delete_menu){
            if(selectedMessages != null){
                if(selectedMessages.size() > 0){ //se esistono elementi selezionati cancelliamo le chat selezionate
                    for(Messaggio messaggio: selectedMessages){
                        CancellaMessaggiThreadDB can = new CancellaMessaggiThreadDB(db, selectedMessages);
                        Thread cancella = new Thread(can);
                        cancella.start();
                        try {
                            cancella.join();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        messaggi.remove(messaggio);
                    }
                    if(selectedMessages.size()>1) Toast.makeText(ChatActivity.this, getString(R.string.messages_deleted), Toast.LENGTH_SHORT).show();
                    else Toast.makeText(ChatActivity.this, getString(R.string.message_deleted), Toast.LENGTH_SHORT).show();
                    selectedMessages.clear(); //puliamo l'array
                    holder.rvChat.getAdapter().notifyDataSetChanged();
                }
                else{
                    Toast.makeText(ChatActivity.this, getString(R.string.no_message_to_delete), Toast.LENGTH_SHORT).show();
                }
            }
            holder.deleteItem.setVisible(false);
        }

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) { //cerca i messaggi aventi la keyword digitata
        if(!newText.isEmpty()) {
            ArrayList <Messaggio> tmp = new ArrayList <>();
            for (Messaggio msg : messaggi) {
                if (msg.testo.toLowerCase().contains(newText.toLowerCase())) {
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

    //scrivi messaggio sul DB e sulla RV
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

    //Leggi messaggio e scrivi sul DB
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

    /**
     * crea istanza di DB con Room
     */
    private void creaDB() {
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "messaggi").build();
    }

    class Holder implements View.OnClickListener {

        private RelativeLayout rlInsertMessage; //  relative layout corrispondente alla bar inserimento msg
        private RecyclerView rvChat;
        private EditText etInserisciMessaggio;
        private ImageView ivInviaMessaggio, ivModCriptata, ivSmileBtn;
        private MenuItem deleteItem, searchItem;
        public Holder() {
            rlInsertMessage = findViewById(R.id.rlInsertMessage);
            etInserisciMessaggio = findViewById(R.id.etInserisciMessaggio);
            ivInviaMessaggio = findViewById(R.id.ivInviaMessaggio);
            ivModCriptata = findViewById(R.id.ivModCriptata);
            ivSmileBtn = findViewById(R.id.ivSmileBtn);
            ivInviaMessaggio.setOnClickListener(this);
            ivModCriptata.setOnClickListener(this);
            ivSmileBtn.setOnClickListener(this);
            ClickShrinkEffectKt.applyClickShrink(ivSmileBtn);
            ClickShrinkEffectKt.applyClickShrink(ivModCriptata);
            ClickShrinkEffectKt.applyClickShrink(ivInviaMessaggio);
            // RV messaggi chat
            rvChat = findViewById(R.id.rvChat);
            rvChat.setAdapter(new ChatBluetoothAdapter(messaggi));
            LinearLayoutManager lm = new LinearLayoutManager(ChatActivity.this);
            rvChat.setLayoutManager(lm);

            // DECORATOR RV messaggi chat
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
            if (v.getId() == R.id.ivInviaMessaggio) { //manda messaggio non codificato
                if (!etInserisciMessaggio.getText().toString().equals("")) {
                    String messaggioInserito = etInserisciMessaggio.getText().toString();

                    if (modCriptata) {
                        codifica = new CodificaAES();
                        MetaMessaggio metaMessaggio = codifica.codificaMessaggio(messaggioInserito);
                        session.getmBluetoothChatService().getmConnectedThread().writeObject(metaMessaggio);
                    } else {
                        MetaMessaggio metaMessaggio = new MetaMessaggio();
                        metaMessaggio.setTesto(messaggioInserito.getBytes());
                        session.getmBluetoothChatService().getmConnectedThread().writeObject(metaMessaggio);
                    }
                    etInserisciMessaggio.setText("");
                }
            }

            if (v.getId() == R.id.ivModCriptata) { //manda messaggio codificato
                modCriptata = !modCriptata;
                String msg;
                if (modCriptata) {
                    holder.ivModCriptata.setImageResource(R.drawable.ic_aes);
                    msg="Crittografia AES attivata.";
                    Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();
                } else {
                    holder.ivModCriptata.setImageResource(R.drawable.ic_no_aes);
                    msg="Crittografia AES disattivata.";
                    Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            if (v.getId() == R.id.ivSmileBtn) { // apri popup emoji
                // Rendo invisibile relative layout di inserimento messaggi
                hideKeyboard(ChatActivity.this);
                holder.rlInsertMessage.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(ChatActivity.this, EmoticonsActivity.class);
                //passare context per prelevare immagini da assets
                startActivityForResult(intent, REQCODE_POPUP_ACTIVITY);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case (REQCODE_POPUP_ACTIVITY):
                // Rendo visibile relative layout di inserimento messaggi
                holder.rlInsertMessage.setVisibility(View.VISIBLE);
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    String kwEmoji = data.getStringExtra("kwEmoji");
                    MetaMessaggio metaMessaggio = new MetaMessaggio();
                    metaMessaggio.setTesto(kwEmoji.getBytes());
                    session.getmBluetoothChatService().getmConnectedThread().writeObject(metaMessaggio);
                }
                break;
        }
    }


        /*adapter per il recyclerview*/
    class ChatBluetoothAdapter extends RecyclerView.Adapter <ChatBluetoothAdapter.ChatHolder> implements View.OnLongClickListener,View.OnClickListener {

        private List <Messaggio> dati;

        public ChatBluetoothAdapter(List <Messaggio> dati) {
            this.dati = dati;
        }

        @NonNull
        @Override
        public ChatBluetoothAdapter.ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RelativeLayout rl = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat,
                    parent, false);
            rl.setOnClickListener(this);
            rl.setOnLongClickListener(this);
            ClickShrinkEffectKt.applyClickShrink(rl);
            return new ChatHolder(rl);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatBluetoothAdapter.ChatHolder holder, int position) {

            String msg = (dati.get(position).testo);

            // se emoji
            if(EmoticonsManager.isEmojiKeyword(msg)) {
                holder.tvMessaggio.setText(null);   //campo testo nullo. Se si desidera e' possibile inserire del testo e gestire la posizione della emoji nella textview cambiando i parametri della setCompoundDrawablesRelativeWithIntrinsicBounds().
                try {
                    holder.tvMessaggio.setCompoundDrawablesRelativeWithIntrinsicBounds(EmoticonsManager.selectEmojiByKeyword(msg, ChatActivity.this), null, null, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // messaggio testuale
            } else {
                holder.tvMessaggio.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null); //campo drawable nullo

                //se il messaggio presenta gia' tag html non effettuo il processamento Markdown
                if (!msg.matches("(.)*(<(.)+>)+(.)*")) {
                    // lista dei casi di Markdown trovati
                    List<Integer> matchesParser = ParserMarkdown.selectParser(msg);
                    // se ci sono casi effettuo i relativi parsing
                    if(matchesParser.size() > 0) {
                        for(int i = 0; i < matchesParser.size(); i++) {
                            msg = ParserMarkdown.parserStartEndMarker(msg, matchesParser.get(i));   //faccio il parsing da markdown al corrispondente html, leggibile dalla textview
                        }
                    }
                }

                    // setText del messaggio con formattazione html
                    holder.tvMessaggio.setText(Html.fromHtml(msg, Html.FROM_HTML_MODE_LEGACY));

            }

            //scrivo l'ora del messaggio nella textview
            holder.tvData.setText(dati.get(position).ora);
            //serve per fare il display dei messaggi
            if (dati.get(position).mittente != null) {
                if (dati.get(position).mittente.equals(BluetoothAdapter.getDefaultAdapter().getAddress())) {
                    holder.cvChat.setCardBackgroundColor(getColor(R.color.colorMittente));
                    holder.rlChat.setGravity(Gravity.RIGHT); //se sei il mittente i messaggi sono visualizzati a destra
                } else {
                    holder.cvChat.setCardBackgroundColor(getColor(R.color.colorDestinatario));
                    holder.rlChat.setGravity(Gravity.LEFT); //altrimenti a sinistra
                }
            }
            if(checkExistance(dati.get(position), selectedMessages)){ //se esiste nell'array selezionati evidenzialo
                holder.itemView.setBackgroundColor(getColor(R.color.colorBGSelected));
                holder.cvChat.setCardBackgroundColor(getColor(R.color.colorSelected));
            }
            else{ //altrimenti niente effetto visivo
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        @Override
        public int getItemCount() {
            if (dati != null)
                return dati.size();
            return 0;
        }

        @Override
        public void onClick(View v) {
            if(selectedMessages.size() > 0) onLongClick(v);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = ((RecyclerView) v.getParent()).getChildAdapterPosition(v);
            if(!checkExistance(dati.get(position), selectedMessages)){ //se l'elemento non esiste aggiungilo
                selectedMessages.add(dati.get(position));
                holder.deleteItem.setVisible(true);
                //Toast.makeText(ChatActivity.this, "Selected Chat", Toast.LENGTH_LONG).show();
                notifyDataSetChanged();
            }else{ //altrimenti no
                holder.deleteItem.setVisible(false);
                selectedMessages.remove(dati.get(position));
                //Toast.makeText(ChatActivity.this, "Unselected Chat", Toast.LENGTH_LONG).show();
                notifyDataSetChanged();
            }
            return true;
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
        holder.searchItem = menu.findItem(R.id.search_menu);
        holder.deleteItem = menu.findItem(R.id.delete_menu);
        holder.deleteItem.setOnMenuItemClickListener(this);
        holder.deleteItem.setVisible(false);
        SearchView searchView = (SearchView) holder.searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.Cerca_msg));
        searchView.setOnQueryTextListener(this);
        return true;
    }

    public boolean checkExistance(Messaggio message, ArrayList<Messaggio> array){
        for(Messaggio d: array){
            if(d.equals(message)){
                return true;
            }
        }
        return false;
    }

    void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}


