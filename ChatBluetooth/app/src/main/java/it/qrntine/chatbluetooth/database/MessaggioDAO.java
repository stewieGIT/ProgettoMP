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

    @Insert
    void inserisciMessaggi(List<Messaggio> messaggi);

    @Delete
    void cancellaMessaggio(Messaggio messaggio);

    @Delete
    void cancellaMessaggi(List<Messaggio> messaggi);

    @Query("SELECT * FROM messaggio")
    List<Messaggio> scaricaMessaggi();

    @Query("SELECT DISTINCT * FROM messaggio WHERE (Destinatario = :destinatario AND Mittente = :mittente) OR " +
            "(Destinatario = :mittente AND Mittente = :destinatario)")
    List<Messaggio> scaricaMessaggiDestinatario(String mittente, String destinatario);

    @Query("DELETE FROM messaggio WHERE Destinatario = :destinatario")
    void cancellaMessaggiDestinatario(String destinatario);
}
