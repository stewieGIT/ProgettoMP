package it.qrntine.chatbluetooth.database;

public class CancellaThreadDB implements Runnable{

    private AppDatabase db; //riferimento al db
    private Messaggio messaggio; //messaggio da cancellare: da implementare varie funzioni
    private boolean running; //variabile di stato running

    /**
     * costruttore senza messaggio
     * @param db riferimento db
     */
    public CancellaThreadDB(AppDatabase db){
        this.db = db;
        running = true;
    }

    /**
     * costruttore con messaggio
     * @param db riferimento db
     * @param messaggio da eliminare
     */
    public CancellaThreadDB(AppDatabase db, Messaggio messaggio){
        this.db = db;
        this.messaggio = messaggio;
        running = true;
    }

    @Override
    public void run() {
        while(running){
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Delete Avviato");
            db.messaggioDAO().cancellaMessaggiDestinatario(messaggio.destinatario); //cancella messaggio/messaggi con quel destinatario
            running = false;
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Delete Terminato");
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
