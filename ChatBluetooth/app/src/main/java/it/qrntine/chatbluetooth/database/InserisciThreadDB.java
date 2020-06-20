package it.qrntine.chatbluetooth.database;

public class InserisciThreadDB implements Runnable {

    private AppDatabase db; //riferimento al db
    private Messaggio messaggio; //messaggio da inserire
    private boolean running; //variabile di stato running

    /**
     * costruttore
     * @param db riferimento al db
     * @param messaggio da inserire
     */
    public InserisciThreadDB(AppDatabase db, Messaggio messaggio){
        this.db = db;
        this.messaggio = messaggio;
        running = true;
    }

    @Override
    public void run() {
        while(running){
            db.messaggioDAO().inserisciMessaggio(messaggio); //inserisci messaggio
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
