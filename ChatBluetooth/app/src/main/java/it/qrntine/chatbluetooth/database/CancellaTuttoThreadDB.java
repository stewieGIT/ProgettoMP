package it.qrntine.chatbluetooth.database;

public class CancellaTuttoThreadDB implements Runnable{

    private AppDatabase db; //riferimento al db
    private String destinatario; //messaggio da cancellare: da implementare varie funzioni
    private boolean running; //variabile di stato running

    /**
     * costruttore senza messaggio
     * @param db riferimento db
     */
    public  CancellaTuttoThreadDB(AppDatabase db){
        this.db = db;
        running = true;
    }

    @Override
    public void run() {
        while(running){
            db.messaggioDAO().cancellatutto(); //cancella messaggio/messaggi con quel destinatario
            running = false;
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
