package it.qrntine.chatbluetooth.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class Messaggio {
    @PrimaryKey(autoGenerate = true)
    public int mid;

    @ColumnInfo(name="Data")
    public String data;

    @ColumnInfo(name="Ora")
    public String ora;

    @ColumnInfo(name="Testo")
    public String testo;

    @ColumnInfo(name="Mittente")
    public String mittente;

    @ColumnInfo(name="Destinatario")
    public String destinatario;

    public Messaggio(){}

}
