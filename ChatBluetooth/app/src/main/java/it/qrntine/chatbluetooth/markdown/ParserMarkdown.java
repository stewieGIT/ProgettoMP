package it.qrntine.chatbluetooth.markdown;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/***
 * Classe che consente il riconoscimento di semplici formattazioni markdown, traducendole in tag HTML leggibili
 * in fase di setText della TextView.
 *
 *   Mark supportati:       Markdown:                 HTML:
 *
 *   GRASSETTO              ...**string**...          <b>string</b>
 *   CORSIVO                ...*string*...            <em>string</em>
 *   CANCELLATO             ...~string~...            <s>string</s>
 *   HEADER                 #string                   <h1>string</h1>
 *   HEADER+PARAGRAPH       #string.Other             <h1>string</h1><br><p>Other</p>
 *
 */

public class ParserMarkdown {

    /*
     * REGEX CASI
     */
    public static final String regex_bold = "(.+)*((\\*\\*)(.+)(\\*\\*)(.+))*"; // ..**...**..
    //public static final String splitterCursive = "((\\*?)(.+?)(\\*?))*";  // ..*...*..
    public static final String regex_cursive = "(.+)*((\\*{1})(.+)(\\*{1}))*";
    public static final String regex_header = "(.+)*((#)(.+)(#))*";
    public static final String regex_cancellato = "(.+)*((~)(.+)(~))*";

    /*
     * REGEX MARKERS
     */
    public static final String markerBold = "\\*\\*";
    public static final String markerEmphasis = "\\*";
    public static final String markerBold_2 = "__";
    public static final String markerEmphasis_2 = "_";
    public static final String markerHeader = "#";
    public static final String markerCancellato = "~";
    public static final String markerCode = "`";

    /*
     * TAG HTML
     */
    public static final String startTagBold = "<b>";
    public static final String endTagBold = "</b>";

    public static final String startTagEmphasis = "<em>";
    public static final String endTagEmphasis = "</em>";

    public static final String startTagHeader = "<h1>";
    public static final String endTagHeader = "</h1>";

    public static final String startTagCancellato = "<s>";
    public static final String endTagCancellato = "</s>";

    public static final String startTagCode = "<code>";
    public static final String endTagCode = "</code>";

    public static final String newLineHtml = "<br>";


    /***
     * Effettua il parsing di casi markdown in cui i marker sono all'inizio e alla fine della
     * stringa che si vuole parsare. (e.g. msg = "**si**", msgParsato = "<b>si</b>").
     * @param msg
     * @return msgParsato
     */
    public static String parserStartEndMarker(String msg, int caso) {

        //System.out.println("Messaggio inserito: \n" + msg);

        String msgParsato = "";
        String[] lista;

        String startTag;
        String endTag;
        String strRegex;

        switch (caso) {

            case 0:
                //caso header
                startTag = startTagHeader;
                endTag = endTagHeader + newLineHtml;  // titolo e a capo
                strRegex = markerHeader;
                break;

            case 1:
                //caso bold
                startTag = startTagBold;
                endTag = endTagBold;
                strRegex = markerBold;
                break;

            case 2:
                //caso corsivo
                startTag = startTagEmphasis;
                endTag = endTagEmphasis;
                strRegex = markerEmphasis;
                break;

            case 3:
                //caso cancellato
                startTag = startTagCancellato;
                endTag = endTagCancellato;
                strRegex = markerCancellato;
                break;

            case 4:
                //caso bold versione 2
                startTag = startTagBold;
                endTag = endTagBold;
                strRegex = markerBold_2;
                break;

            case 5:
                //caso corsivo versione 2
                startTag = startTagEmphasis;
                endTag = endTagEmphasis;
                strRegex = markerEmphasis_2;
                break;

            case 6:
                //caso code
                startTag = startTagCode;
                endTag = endTagCode;
                strRegex = markerCode;
                break;

                /*
                E' possibile inserire nuovi casi relativi ad altri tag
                 */

            default:
                //nessun caso da esaminare
                return msg;

        }

        lista = (msg+" ").split(strRegex);	//ho aggiunto lo spazio " " altrimenti non riconosce l'ultimo nei casi col ** alla fine (e.g. "prova ancora **si** o **no**")

        // CASO SOTTOSTRINGHE DISPARI
        if((lista.length % 2) != 0) {	//se ottengo un numero dispari di sottostringhe, metto il tag bold prima e dopo ogni sottostringa pari

            for(int i=0; i<lista.length; i++) {
                if((i%2) == 0) {
                    msgParsato += lista[i];
                } else {
                    msgParsato += startTag + lista[i] + endTag;
                }
            }

        } // END CASO SOTTOSTRINGHE DISPARI

        // CASO SOTTOSTRINGHE PARI
        if((lista.length % 2) == 0) {	//se ottengo un numero pari di sottostringhe, metto il tag bold prima e dopo ogni sottostringa eccetto l'ultima pari

            for(int i=0; i<lista.length; i++) {
                if(i == lista.length-1) {	//e.g. "**si** **no"
                    msgParsato += lista[i];
                    break;
                }
                if((i%2) == 0) {
                    msgParsato += lista[i];
                } else {
                    msgParsato += startTag + lista[i] + endTag;
                }
            }

        } // END CASO SOTTOSTRINGHE PARI

        //System.out.println("Messaggio parsato:  \n" + msgParsato);

        return msgParsato;

    }


    public static List<String> listMarkers() {
        return Arrays.asList(markerHeader, markerBold, markerEmphasis, markerCancellato, markerBold_2, markerEmphasis_2, markerCode);
    }


    /***
     * (PREDISPOSIZIONE)
     * Indica quale regex viene riscontrata, quindi quale parser applicare
     * @param msg
     * @return int
     */
    public static int selectParser(String msg) {

        String[] splitters = {regex_bold, regex_cursive, regex_header};
        for (int i=0; i<splitters.length; i++) {
            Pattern pattern = Pattern.compile(splitters[i]);
            if (pattern.matcher(msg).matches()) return i;
        }
        return 0;

    }
}
