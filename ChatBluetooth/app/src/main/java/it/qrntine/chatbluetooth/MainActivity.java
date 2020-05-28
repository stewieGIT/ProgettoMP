package it.qrntine.chatbluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import it.qrntine.chatbluetooth.database.AppDatabase;
import it.qrntine.chatbluetooth.database.CancellaThreadDB;
import it.qrntine.chatbluetooth.database.InserisciThreadDB;
import it.qrntine.chatbluetooth.database.QueryThreadDB;
import it.qrntine.chatbluetooth.database.Messaggio;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //inizializzazione del BTAdapter

        //esempio di inserimento-cancellazione-query messaggio
        creaDB(); //crea il DB locale
        Messaggio messaggio = new Messaggio(); //creo messaggio
        messaggio.testo = "prova1";
        messaggio.data = LocalDate.now().toString();
        messaggio.ora = LocalTime.now().toString();
        messaggio.mittente = "Matteo";
        messaggio.destinatario = "Franchino";

        //creazione thread
        InserisciThreadDB in = new InserisciThreadDB(db, messaggio);
        Thread inserisci = new Thread(in);
        QueryThreadDB sc = new QueryThreadDB(db);
        Thread scarica = new Thread(sc);
        CancellaThreadDB ca = new CancellaThreadDB(db, messaggio);
        Thread cancella = new Thread(ca);
        //inserisci.start(); //inserisci
        //cancella.start(); //cancella con destinatario
        //scarica.start(); //scarica tutti i messaggi

        //esempio codifica messaggio(da eliminare in seguito)
        MetaMessaggio nuovoMex = codificaMessaggio("matteo"); //codifica messaggio
        System.out.println(new String(nuovoMex.getTesto()).toString()); //stampa messaggio codificato
        System.out.println(decodificaMessaggio(nuovoMex)); //decodifica il messaggio
    }


    /**
     *
     * @param messaggio testo del messaggio da cifrare
     * @return ritorna un Messaggio con la chiave e il IV di cifratura
     */
    private MetaMessaggio codificaMessaggio(String messaggio){
        MetaMessaggio risultato = new MetaMessaggio();
        byte[] m = messaggio.getBytes();

        try{
            KeyGenerator generatore = KeyGenerator.getInstance("AES"); //generatore chiave con algoritmo "AES"
            generatore.init(256); //di 256bit
            risultato.setChiave(generatore.generateKey()); //salva la chiave generata nel messaggio

            Cipher cifra = Cipher.getInstance("AES/CBC/PKCS5PADDING"); //crea chiave di cifratura
            cifra.init(Cipher.ENCRYPT_MODE, risultato.getChiave());
            risultato.setTesto(cifra.doFinal(m)); //cifra messaggio e aggiungi testo
            risultato.setIvp(new IvParameterSpec(cifra.getIV())); //aggiungi il IV

            return risultato;

        }catch(NoSuchAlgorithmException e){
        }catch(NoSuchPaddingException e1){
        }catch(InvalidKeyException e2){
        }catch(BadPaddingException e3){
        }catch(IllegalBlockSizeException e4){
            //fai i catch
        }

        return null;
    }

    /**
     *
     * @param messaggio messaggio da decifrare
     * @return ritorna il testo del messaggio decifrato
     */
    private String decodificaMessaggio(MetaMessaggio messaggio){
        String risultato = "";

        try{
            Cipher decifra = Cipher.getInstance("AES/CBC/PKCS5PADDING"); //crea la chiave di decifratura
            decifra.init(Cipher.DECRYPT_MODE, messaggio.getChiave(), messaggio.getIvp());
            byte[] messaggioDecifrato = decifra.doFinal(messaggio.getTesto()); //decifra messaggio
            risultato = new String(messaggioDecifrato).toString();

            return risultato;

        }catch(NoSuchAlgorithmException e){
        }catch(NoSuchPaddingException e1){
        }catch(InvalidAlgorithmParameterException e2){
        }catch(InvalidKeyException e3){
        }catch(BadPaddingException e4){
        }catch(IllegalBlockSizeException e5){
            //fai i catch
        }

        return null;
    }

    /**
     * crea istanza di DB con Room
     */
    private void creaDB(){
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "messaggi").build();
    }

    /*La classe MetaMessaggio contiene le informazioni necessarie per la codifica e decodifica del messaggio*/
    public class MetaMessaggio{
        private IvParameterSpec ivp; //Vettore di inizializzazione necessario per la decifratura
        private SecretKey chiave; //Chiave segreta
        private byte[] testo; //testo cifrato

        public MetaMessaggio(){}

        public void setChiave(SecretKey chiave) {
            this.chiave = chiave;
        }

        public void setIvp(IvParameterSpec ivp) {
            this.ivp = ivp;
        }

        public void setTesto(byte[] testo) {
            this.testo = testo;
        }

        public SecretKey getChiave() {
            return chiave;
        }

        public IvParameterSpec getIvp() {
            return ivp;
        }

        public byte[] getTesto() {
            return testo;
        }
    }
}
