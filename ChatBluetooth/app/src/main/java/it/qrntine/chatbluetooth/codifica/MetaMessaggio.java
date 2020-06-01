package it.qrntine.chatbluetooth.codifica;

import java.io.Serializable;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/*La classe MetaMessaggio contiene le informazioni necessarie per la codifica e decodifica del messaggio*/
public class MetaMessaggio implements Serializable {

    static final long serialVersionUID = 1234L;

    private byte[] ivp; //Vettore di inizializzazione necessario per la decifratura
    private SecretKey chiave; //Chiave segreta
    private byte[] testo; //testo cifrato

    public MetaMessaggio(){}

    public void setChiave(SecretKey chiave) {
        this.chiave = chiave;
    }

    public void setIvp(byte[] ivp) {
        this.ivp = ivp;
    }

    public void setTesto(byte[] testo) {
        this.testo = testo;
    }

    public SecretKey getChiave() {
        return chiave;
    }

    public byte[] getIvp() {
        return ivp;
    }

    public byte[] getTesto() {
        return testo;
    }

}
