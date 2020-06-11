package it.qrntine.chatbluetooth.markdown;

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
 *   HEADER                 #string#                   <h1>string</h1>
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
    public static final String markerBold = "(\\*\\*)";
    public static final String markerEmphasis = "(\\*)";
    public static final String markerHeader = "(#)";
    public static final String markerCancellato = "(~)";

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


    /***
     * (PREDISPOSIZIONE)
     * Indica quale case va effettuato nel metodo parserString
     * @param msg
     * @return int
     */
    public static int selectParser(String msg) {

        String[] splitters = {regex_bold, regex_cursive, regex_header};
        for (int i=0; i<splitters.length; i++) {
            Pattern pattern = Pattern.compile(splitters[i]);
            if (pattern.matcher(msg).matches()) return i+1;
        }
        return 0;

    }

    /***
     * Effettua il parsing di markdown relativi al grassetto, in tag html (e.g. msg = "**si**", msgParsato = "<b>si</b>").
     * @param msg
     * @return msgParsato
     */
    public static String parserString(String msg, int caso) {

        //System.out.println("Messaggio inserito: \n" + msg);

        String msgParsato = "";
        String[] lista;

        String startTag;
        String endTag;
        String strRegex;

        switch (caso) {
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
                //caso header
                startTag = startTagHeader;
                endTag = endTagHeader;
                strRegex = markerHeader;
                break;

            case 4:
                //caso cancellato
                startTag = startTagCancellato;
                endTag = endTagCancellato;
                strRegex = markerCancellato;
                break;

            default:
                //nessun caso da parsare
                return msg;

        }

        lista = (msg+" ").split(strRegex);	//ho aggiunto lo spazio " " altrimenti non riconosce l'ultimo nei casi col ** alla fine (e.g. "prova ancora **si** o **no**")
        //System.out.println("Lungh: " + lista.length);
        for(int i=0; i<lista.length; i++) {
            //System.out.println("- " + i + ": " + lista[i]);
        }

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

			/*	NON VA BENE
			if(lista.length == 2) {		//caso di singola parola: **si**   ->   non entro proprio nel for
				msgParsato += startTag + lista[1] + endTag;
				System.out.println("Messaggio parsato:  \n" + msgParsato);
				return msgParsato;
			}
			*/

            for(int i=0; i<lista.length; i++) {
                if(i == lista.length-1) {	//caso: **si** **no   ->   devo escludere gli ultimi **
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
    } // END parser
}
