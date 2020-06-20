package it.qrntine.chatbluetooth.emoticons;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/***
 * Classe che si occupa di verificare se il messaggio ottenuto in ingresso matcha con una keyword
 * corrispondente ad una emoji, ed in caso positivo ne ritorna la risorsa, altrimenti 0.
 */
public class EmoticonsManager {

    /*
    Keywords per emoticons
     */
    private static final String emSmile = ":smile:";
    private static final String emVirus = ":virus:";
    private static final String emDrago = ":drago:";
    private static final String emRosa = ":rosa:";
    private static final String emBowling = ":bowling:";
    private static final String emFiore = ":fiore:";
    private static final String emScimmia = ":scimmia:";
    private static final String emFoglie = ":foglia:";
    private static final String emLeone = ":leone:";
    private static final String emPappagallo = ":pappagallo:";
    private static final String emRana = ":rana:";
    private static final String emTigre = ":tigre:";
    private static final String emRugby = ":rugby:";
    private static final String emMaple = ":maple:";
    private static final String emAlbero = ":albero:";
    private static final String emCactus = ":cactus:";

    /*
    Ritorna la risorsa drawable relativa alla keyword, altrimenti NULL se la keyword non esiste
    */
    public static Drawable selectEmojiByKeyword(String msg, Context context) throws IOException {

        Drawable drawableEmoji = null;

        String subDir = "emoticons/";   //sub directory che contiene i file delle emoticons, in assets folder

        String nomeFile = msg.replaceAll(":", "");  // prendo il nome del file, che corrisponde al nome della keyword senza i ":"

        if(isEmojiKeyword(msg)) {
            // opzioni per visualizzare la risorsa asset a grandezza piena, altrimenti viene rimpicciolita
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inDensity = DisplayMetrics.DENSITY_HIGH;
            // settaggio del drawable
            drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open(subDir + nomeFile + ".png"), null, opts);
        }

        return drawableEmoji;
    }


    /*
    Ritorna una lista delle keywords
     */
    public static List<String> listKeywords() {
        return Arrays.asList(emSmile, emVirus, emDrago, emRosa, emBowling, emFiore, emScimmia, emFoglie, emLeone, emPappagallo, emRana, emTigre, emRugby, emMaple, emAlbero, emCactus);
    }


    /*
    Ritorna un booleano che indica se la keyword in input è valida
     */
    public static boolean isEmojiKeyword(String msg) {
        List<String> strKeywords = listKeywords();
        return strKeywords.contains(msg);
    }


}