package it.qrntine.chatbluetooth.database;

import java.util.List;

public class CancellaMessaggiThreadDB implements Runnable {

    private AppDatabase db; //riferimento al db
    private List<Messaggio> messaggi; //messaggio da cancellare: da implementare varie funzioni
    private boolean running; //variabile di stato running

    public CancellaMessaggiThreadDB(AppDatabase db, List<Messaggio> messaggi){
        this.db=db;
        this.messaggi=messaggi;
        running=true;
    }

    @Override
    public void run() {
        while(running){
            for(Messaggio msg : messaggi) {
                db.messaggioDAO().cancellaMessaggio(msg);
            }
            running=false;
        }
    }


    /**
     * serve per terminare thread in caso di errore
     */
    public void terminate(){
        running = false;
    }

    public boolean isRunning(){
        return running;
    }
}
