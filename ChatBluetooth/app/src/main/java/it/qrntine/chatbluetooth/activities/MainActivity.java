package it.qrntine.chatbluetooth.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hardik.clickshrinkeffect.ClickShrinkEffect;
import com.hardik.clickshrinkeffect.ClickShrinkEffectKt;

import java.util.ArrayList;
import java.util.List;
import it.qrntine.chatbluetooth.Constants;
import it.qrntine.chatbluetooth.bluetooth.BluetoothChatService;
import it.qrntine.chatbluetooth.bluetooth.BluetoothSession;
import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.database.AppDatabase;

public class MainActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {
    private Holder holder;
    private Handler mHandler;
    private static boolean RECEIVER_REGISTERED = false;
    private BluetoothSession session = BluetoothSession.getInstance();
    private List<String> devices = new ArrayList<>();   //serve solo per la stampa dei nomi nella recycler (DEBUG)
    private List<BluetoothDevice> objDevices = new ArrayList<>();   //lista di oggetti Bluetooth device

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        session.setCurrentActivity(Constants.ACTIVITY_MAIN);
        setTitle(getString(R.string.main_title));

        reqPermessi();

        //HANDLER SETUP
          mHandler = new Handler(new Handler.Callback() {
              @Override
            public boolean handleMessage(Message msg) {

                switch (msg.what) {

                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case Constants.STATE_CONNECTED:
                                if(session.getCurrentActivity() != Constants.ACTIVITY_CHAT){
                                    Intent intent=new Intent(MainActivity.this, ChatActivity.class);
                                    startActivity(intent);
                                }
                                else{
                                    Intent intent=new Intent(MainActivity.this, ChatActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                                break;
                            case Constants.STATE_CONNECTING:
                                showToast(getString(R.string.connessione));
                                break;
                            case Constants.STATE_LISTEN: break;
                            case Constants.STATE_NONE: break;

                        }
                        break;
                }
                return true;
            }
        } ) ;

        session.setmBluetoothChatService(new BluetoothChatService(MainActivity.this, mHandler));
        // SET DISCOVERABLE
        if(session.getmBluetoothChatService().getmAdapter().getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
        session.getmBluetoothChatService().start();

        holder = new Holder();
    }

    //onResult Permessi
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constants.REQUEST_PERM_ACL:

            case Constants.REQUEST_PERM_AFL:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showToast("Per la funzione di ricerca è necessario consentire l'accesso alla posizione del dispositivo.");
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(session.getErrorNum() == Constants.ERROR_USER_DISCONNECTED){
            showToast(getString( R.string.error_user_disconnected));
            session.getmBluetoothChatService().stop();
            session.getmBluetoothChatService().start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(RECEIVER_REGISTERED) {
            unregisterReceiver(mReceiver);
            RECEIVER_REGISTERED = false;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.menu_about_us){
            Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(intent);
        }
        return true;
    }

    class Holder implements View.OnClickListener{
        Button btnSearch, btnChat, btnAvviaRicerca;
        TextView tvRisultatiRicerca;
        CardView cvDevice;
        RecyclerView rvDevices;
        RecyclerView.Adapter rvAdapter;
        private MenuItem aboutUsItem;

        public Holder(){
            btnSearch=findViewById(R.id.btnSearch);
            btnChat=findViewById(R.id.btnChat);
            btnAvviaRicerca=findViewById(R.id.btnAvviaRicerca);
            tvRisultatiRicerca=findViewById(R.id.tvRisultatiRicerca);
            rvDevices=findViewById(R.id.rvDevices);
            cvDevice = findViewById(R.id.cvDevice);
            btnSearch.setEnabled(false);
            btnSearch.setTypeface(Typeface.DEFAULT_BOLD);
            btnAvviaRicerca.setOnClickListener(this);
            btnChat.setOnClickListener(this);
            ClickShrinkEffectKt.applyClickShrink(btnAvviaRicerca);
            btnSearch.setAlpha(0.7f);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
            rvDevices.setLayoutManager(layoutManager);
            rvAdapter = new RecycleAdapter(devices);
            rvDevices.setAdapter(rvAdapter);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case (R.id.btnAvviaRicerca):
                    if(!checkPermessi()) {
                        reqPermessi();
                        break;
                    }
                    if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        //showToast(getString(R.string.stop_ricerca));
                        holder.btnAvviaRicerca.setText(R.string.avvia_ricerca);
                        holder.tvRisultatiRicerca.setText(R.string.risultati_ricerca);
                    } else {
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(BluetoothDevice.ACTION_FOUND);
                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                        registerReceiver(mReceiver, filter);
                        RECEIVER_REGISTERED = true;
                        //quando clicco su ricerca azzero la lista dei nomi. Vanno gestiti diversamente i dispositivi gia' associati
                        if(devices.size() > 0) {
                            devices.clear();
                        }
                        BluetoothAdapter.getDefaultAdapter().startDiscovery();
                        //showToast("Scansione iniziata");
                    }
                    break;
                case (R.id.btnChat):
                    Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
                    startActivity(intent);
                    break;
            }

        }

        public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> implements View.OnClickListener {
            private List<String> dev;

            RecycleAdapter(List<String> listaDev) {
                dev = listaDev;
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                Context context = parent.getContext();
                LayoutInflater inflater = LayoutInflater.from(context);
                View nameView = inflater.inflate(R.layout.layout_main, parent, false);
                nameView.setOnClickListener(this);
                ClickShrinkEffectKt.applyClickShrink(nameView);
                return new ViewHolder(nameView);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                TextView tv = holder.tvDev;
                tv.setText(dev.get(position));
            }

            @Override
            public int getItemCount() {
                return dev.size();
            }

            @Override
            public void onClick(View v) {
                int position = ((RecyclerView) v.getParent()).getChildAdapterPosition(v);
                // parte la connessione...

                //fermo il discovering
                if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                }

                //prendo l'indirizzo del dispositivo selezionato
                String devAddress = objDevices.get(position).getAddress();
                session.getmBluetoothChatService().connect(objDevices.get(position),true);
                session.setDevice(objDevices.get(position));
            }

            public class ViewHolder extends RecyclerView.ViewHolder {
                private TextView tvDev;
                public ViewHolder(@NonNull View itemView) {
                    super(itemView);
                    tvDev = itemView.findViewById(R.id.tvDevice);
                }
            }   //end Holder

        }
    }
    //END HOLDER


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        holder.aboutUsItem = menu.findItem(R.id.menu_about_us);
        holder.aboutUsItem.setOnMenuItemClickListener(this);
        return true;
    }

    // receiver bluetooth
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //showToast(getString(R.string.inizio_ricerca));
                holder.btnAvviaRicerca.setText(R.string.interrompi_ricerca);
                holder.tvRisultatiRicerca.setText(R.string.inizio_ricerca);
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismiss progress dialog
                //showToast(getString(R.string.fine_ricerca));
                holder.btnAvviaRicerca.setText(R.string.avvia_ricerca);
                holder.tvRisultatiRicerca.setText(R.string.risultati_ricerca);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!devices.contains(device.getName())) {
                    // se il dispositivo non ha un nome prendo l'indirizzo
                    if(device.getName() == null) {
                        //showToast("Dispositivo trovato: " + device.getAddress());
                        devices.add(device.getAddress());
                    } else {
                        //showToast("Dispositivo trovato: " + device.getName());
                        devices.add(device.getName());
                    }
                    objDevices.add(device);
                    holder.rvAdapter.notifyDataSetChanged();
                }
            }
        }
    };  //END BROADCAST RECEIVER

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    //richiesta permessi
    public void reqPermessi() {

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.REQUEST_PERM_ACL);
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_PERM_AFL);
        }

    }

    //controllo permessi
    public boolean checkPermessi() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return false;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return false;

        return true;
    }


}
