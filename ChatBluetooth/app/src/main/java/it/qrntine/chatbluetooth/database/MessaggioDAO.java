package it.qrntine.chatbluetooth.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessaggioDAO {

    @Insert
    void inserisciMessaggio(Messaggio messaggio);


    @Delete
    void cancellaMessaggio(Messaggio messaggio);


    @Query("SELECT DISTINCT * FROM messaggio WHERE (Destinatario = :destinatario AND Mittente = :mittente) OR " +
            "(Destinatario = :mittente AND Mittente = :destinatario)")
    List<Messaggio> scaricaMessaggiDestinatario(String mittente, String destinatario);

    @Query("DELETE FROM messaggio WHERE Destinatario = :destinatario OR Mittente = :destinatario")
    void cancellaMessaggiDestinatario(String destinatario);

    @Query("DELETE FROM messaggio")
    void cancellatutto();
}
