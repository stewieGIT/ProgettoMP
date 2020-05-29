package it.qrntine.chatbluetooth.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.database.AppDatabase;
import it.qrntine.chatbluetooth.database.InserisciThreadDB;
import it.qrntine.chatbluetooth.database.Messaggio;
import it.qrntine.chatbluetooth.database.QueryThreadDB;
import it.qrntine.chatbluetooth.decorator.DecoratorRecyclerView;

public class ChatActivity extends AppCompatActivity {

    private Holder holder;
    private String mittente = "Matteo"; //proprietario del telefono
    private String destinatario = "Damiano"; //destinatario
    private AppDatabase db; //riferimento al db
    private List<Messaggio> messaggi; //lista messaggi relativi alla chat con il destinatario

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_chat);
        setTitle(destinatario); //il titolo

        creaDB(); //ritorna il riferimento al db

        QueryThreadDB sc = new QueryThreadDB(db);
        try {
            Thread scarica = new Thread(sc);
            scarica.start(); //avvia thread
            scarica.join(); //aspetta i risultati
            messaggi = sc.scarica(); //scarica i messaggi: per ora scarica tutti i messaggi del db!!!
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        holder = new Holder();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.itRicerca){
            //ricerca keyword
        }else{
            return super.onContextItemSelected(item);
        }
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
                    Messaggio messaggio = new Messaggio();
                    messaggio.mittente = mittente;
                    messaggio.destinatario = destinatario;
                    messaggio.data = LocalDate.now().toString();
                    messaggio.ora = LocalTime.now().toString();
                    messaggio.testo = etInserisciMessaggio.getText().toString();
                    messaggi.add(messaggio);

                    InserisciThreadDB in = new InserisciThreadDB(db, messaggio);
                    Thread inserisci = new Thread(in);
                    inserisci.start();
                }
            }
        }
    }

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
            if(dati.get(position).mittente.equals(mittente)){
                holder.rlChat.setGravity(Gravity.RIGHT); //se sei il mittente i messaggi sono visualizzati a destra
                System.out.println("DESTRA");
            }
            else{
                holder.rlChat.setGravity(Gravity.LEFT); //altrimenti a sinistra
                System.out.println("SINISTRA");
            }
        }

        @Override
        public int getItemCount() {
            return dati.size();
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
}
