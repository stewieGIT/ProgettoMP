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
import androidx.room.Room;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import it.qrntine.chatbluetooth.R;
import it.qrntine.chatbluetooth.bluetooth.BluetoothSession;
import it.qrntine.chatbluetooth.database.AppDatabase;
import it.qrntine.chatbluetooth.database.InserisciThreadDB;
import it.qrntine.chatbluetooth.database.Messaggio;
import it.qrntine.chatbluetooth.database.QueryThreadDB;
import it.qrntine.chatbluetooth.decorator.DecoratorRecyclerView;

public class ChatActivity extends AppCompatActivity {

    private String mittente = "Matteo"; //proprietario del telefono
    private String destinatario = "Damiano"; //destinatario
    private AppDatabase db; //riferimento al db
    private List<Messaggio> messaggi; //lista messaggi relativi alla chat con il destinatario

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_chat);
        setTitle(destinatario); //il titolo
        System.out.println("***********RIFERIMENTO CHAT ACTIVITY**********"+BluetoothSession.getInstance().getmBluetoothChatService());
        creaDB(); //ritorna il riferimento al db
    }



    /**
     * crea istanza di DB con Room
     */
    private void creaDB(){
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "messaggi").build();
    }


}
