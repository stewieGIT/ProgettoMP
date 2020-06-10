package it.qrntine.chatbluetooth.emoticons;

import java.util.ArrayList;
import java.util.List;

import it.qrntine.chatbluetooth.R;

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

    /*
    Ritorna la risorsa relativa alla keyword, altrimenti 0 se la stringa non e' presente
    */
    public static int selectEmojiByKeyword(String msg) {
        switch (msg) {
            case (emSmile):
                return R.drawable.smile;

            case (emVirus):
                return R.drawable.virus;

            case (emDrago):
                return R.drawable.drago;

            case (emRosa):
                return R.drawable.rosa;

            case (emBomba):
                return R.drawable.bomb;

            case (emBowling):
                return R.drawable.bowling;

            case (emFiore):
                return R.drawable.fiore;

            case (emScimmia):
                return R.drawable.scimmia;

            case (emFoglie):
                return R.drawable.foglie;

            default:
                break;
        }
        return 0;
    }

    public static String selectKeywordByEmoji(Integer res) {
        switch (res) {
            case (R.drawable.smile):
                return emSmile;

            case (R.drawable.virus):
                return emVirus;

            case (R.drawable.drago):
                return emDrago;

            case (R.drawable.rosa):
                return emRosa;

            case (R.drawable.bomb):
                return emBomba;

            case (R.drawable.bowling):
                return emBowling;

            case (R.drawable.fiore):
                return emFiore;

            case (R.drawable.scimmia):
                return emScimmia;

            case (R.drawable.foglie):
                return emFoglie;

            default:
                break;
        }
        return null;
    }

    public static List<Integer> getListEmojiRes() {
        String[] strKeywords = {emSmile, emVirus, emDrago, emRosa, emBomba, emBowling, emFiore, emScimmia, emFoglie};
        List<Integer> listEmojiRes = new ArrayList<>();
        for(int i=0; i<strKeywords.length; i++) {
            listEmojiRes.add(selectEmojiByKeyword(strKeywords[i]));
        }
        return listEmojiRes;
    }

}