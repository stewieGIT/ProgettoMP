package it.qrntine.chatbluetooth.database;

import java.util.List;

public class QueryThreadDB implements Runnable{

    private AppDatabase db; //riferimento al database
    private boolean running; //variabile stato running
    private List<Messaggio> messaggi; //lista messaggi scaricati dal db
    private String mittente, destinatario;

    /**
     * costruttore
     * @param db riferimento al db
     */
    public QueryThreadDB(AppDatabase db, String mittente, String destinatario){
        this.db = db;
        this.mittente = mittente;
        this.destinatario = destinatario;
        running = true;
    }

    @Override
    public void run() {
        while(running){
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Query Avviato");
            messaggi = db.messaggioDAO().scaricaMessaggiDestinatario(mittente, destinatario); //scarica i messaggi nel db
            for(Messaggio messaggio: messaggi){
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>Messaggio " + messaggio.testo); //semplice stampa
            }
            running = false;
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Query terminato");
    }

    /**
     * server per terminare il thread in caso di errore
     */
    public void terminate(){
        running = false;
    }

    public boolean isRunning(){
        return running;
    }

    public List<Messaggio> scarica(){
        return messaggi;
    }
}
