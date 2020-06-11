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
    public static final String emSmile = ":smile:";
    public static final String emVirus = ":virus:";
    public static final String emDrago = ":drago:";
    public static final String emRosa = ":rosa:";
    public static final String emBomba = ":bomba:";
    public static final String emBowling = ":bowling:";
    public static final String emFiore = ":fiore:";
    public static final String emScimmia = ":scimmia:";
    public static final String emFoglie = ":foglia:";
    public static final String emLeone = ":leone:";
    public static final String emPappagallo = ":pappagallo:";
    public static final String emRana = ":rana:";
    public static final String emTigre = ":tigre:";
    public static final String emRugby = ":rugby:";
    public static final String emMaple = ":maple:";
    public static final String emAlbero = ":albero:";
    public static final String emCactus = ":cactus:";

    /*
    Ritorna la risorsa drawable relativa alla keyword, altrimenti NULL se la keyword non esiste
    */
    public static Drawable selectEmojiByKeyword(String msg, Context context) throws IOException {

        Drawable drawableEmoji = null;

        // opzioni per visualizzare la risorsa asset a grandezza piena, altrimenti viene rimpicciolita
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inDensity = DisplayMetrics.DENSITY_HIGH;

        switch (msg) {
            case (emSmile):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "smile.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "smile.png"), null, opts);
                break;

            case (emVirus):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "virus.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "virus.png"), null, opts);
                break;

            case (emDrago):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "drago.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "drago.png"), null, opts);
                break;

            case (emRosa):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "rosa.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "rosa.png"), null, opts);
                break;

            case (emBomba):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "bomb.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "bomb.png"), null, opts);
                break;

            case (emBowling):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "bowling.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "bowling.png"), null, opts);
                break;

            case (emFiore):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "fiore.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "fiore.png"), null, opts);
                break;

            case (emScimmia):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "scimmia.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "scimmia.png"), null, opts);
                break;

            case (emFoglie):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "foglie.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "foglie.png"), null, opts);
                break;

            case (emLeone):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "leone.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "leone.png"), null, opts);
                break;

            case (emPappagallo):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "pappagallo.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "pappagallo.png"), null, opts);
                break;

            case (emRana):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "rana.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "rana.png"), null, opts);
                break;

            case (emTigre):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "tigre.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "tigre.png"), null, opts);
                break;

            case (emRugby):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "rugby.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "rugby.png"), null, opts);
                break;

            case (emMaple):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "maple.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "maple.png"), null, opts);
                break;

            case (emAlbero):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "albero.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "albero.png"), null, opts);
                break;

            case (emCactus):
                //drawableEmoji = Drawable.createFromStream(context.getResources().getAssets().open("emoticons/" + "cactus.png"), null);
                drawableEmoji = Drawable.createFromResourceStream(context.getResources(), null, context.getResources().getAssets().open("emoticons/" + "cactus.png"), null, opts);
                break;

            default:
                break;
        }
        return drawableEmoji;
    }


    /*
    Ritorna una lista delle keywords
     */
    public static List<String> listKeywords() {
        return Arrays.asList(emSmile, emVirus, emDrago, emRosa, emBomba, emBowling, emFiore, emScimmia, emFoglie, emLeone, emPappagallo, emRana, emTigre, emRugby, emMaple, emAlbero, emCactus);
    }


    /*
    Ritorna un booleano che indica se la keyword in input è valida
     */
    public static boolean isEmojiKeyword(String msg) {
        List<String> strKeywords = listKeywords();
        return strKeywords.contains(msg);
    }


}