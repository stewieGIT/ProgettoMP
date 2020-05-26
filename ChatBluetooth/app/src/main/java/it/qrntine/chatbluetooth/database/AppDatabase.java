package it.qrntine.chatbluetooth.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Messaggio.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MessaggioDAO messaggioDAO();
}
