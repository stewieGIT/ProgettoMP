package it.qrntine.chatbluetooth.markdown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 * Classe che consente il riconoscimento di semplici formattazioni markdown, ed il parsing inserendo
 * i tag HTML corrispondenti.
 *
 *   Casi supportati:       Markdown:                 HTML:
 *
 *   GRASSETTO              ...**string**...          <b>string</b>
 *   GRASSETTO              ...__string__...          <b>string</b>
 *   CORSIVO                ...*string*...            <em>string</em>
 *   CORSIVO                ..._string_...            <em>string</em>
 *   CANCELLATO             ...~string~...            <s>string</s>
 *   HEADER                 # string#other...         <h1>string</h1>other...
 *
 */
public class ParserMarkdown {

    /*
     * REGEX CASI DI NOTAZIONI MARKDOWN
     */
    private static final String regexCancellato = "((.)*(~){1}(.)+(~){1}(.)*)+";
    private static final String regexHeader = "(.)*(#){1}(\\ ){1}[^#]+(#){1}(.)*";
    private static final String regexEmphasis = "((.)*(\\*){1}(.)+(\\*){1}(.)*)+";
    private static final String regexEmphasis_ = "((.)*(_){1}(.)+(_){1}(.)*)+";
    private static final String regexBold = "((.)*(\\*){2}(.)+(\\*){2}(.)*)+";
    private static final String regexBold_ = "((.)*(_){2}(.)+(_){2}(.)*)+";
    //public static final String regexList = "((.)*(-){1}(.)+(\\n))+"; Riconoscimento liste per sviluppi futuri

    /*
     * MARKERS MARKDOWN
     */
    private static final String markerBold = "\\*\\*";
    private static final String markerEmphasis = "\\*";
    private static final String markerBold_ = "__";
    private static final String markerEmphasis_ = "_";
    private static final String markerHeader = "#";
    private static final String markerCancellato = "~";

    /*
     * TAG HTML
     */
    private static final String startTagBold = "<b>";
    private static final String endTagBold = "</b>";

    private static final String startTagEmphasis = "<em>";
    private static final String endTagEmphasis = "</em>";

    private static final String startTagHeader = "<h1>";
    private static final String endTagHeader = "</h1>";

    private static final String startTagCancellato = "<s>";
    private static final String endTagCancellato = "</s>";


    /***
     * Effettua il parsing di casi markdown in cui i marker sono all'inizio e alla fine della
     * stringa che si vuole parsare. (e.g. msg = "**si**", msgParsato = "<b>si</b>").
     * @param msg
     * @return msgParsato
     */
    public static String parserStartEndMarker(String msg, int caso) {

        String msgParsato = ""; // inizializzo msg di output
        String[] lista;         // usata per il processamento

        String startTag;    // tag html di apertura
        String endTag;      // tag html di chiusura
        String strMarker;    // marker

        // settaggio variabili in base alla tipologia
        switch (caso) {

            case 0:
                //caso header
                startTag = startTagHeader;
                endTag = endTagHeader;
                strMarker = markerHeader;
                break;

            case 1:
                //caso bold
                startTag = startTagBold;
                endTag = endTagBold;
                strMarker = markerBold;
                break;

            case 2:
                //caso corsivo
                startTag = startTagEmphasis;
                endTag = endTagEmphasis;
                strMarker = markerEmphasis;
                break;

            case 3:
                //caso cancellato
                startTag = startTagCancellato;
                endTag = endTagCancellato;
                strMarker = markerCancellato;
                break;

            case 4:
                //caso bold versione __
                startTag = startTagBold;
                endTag = endTagBold;
                strMarker = markerBold_;
                break;

            case 5:
                //caso corsivo versione _
                startTag = startTagEmphasis;
                endTag = endTagEmphasis;
                strMarker = markerEmphasis_;
                break;

                /*
                E' possibile inserire nuovi casi relativi ad altri casi di wrap
                 */

            default:
                //nessun caso da esaminare
                return msg;

        }

        // inizio del parsing
        lista = (msg+" ").split(strMarker);	//aggiunto lo spazio per riconoscere casi di marker a fine stringa, che lo split invaliderebbe (e.g. "prova ancora **si** o **no**")

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
        return msgParsato;

    }


    /***
     * Ritorna una lista con gli indici di quali casi di notazione Markdown sono stati individuati,
     * da utilizzare nel metodo parser.
     * @param msg
     * @return matchCasesList
     */
    public static List<Integer> selectParser(String msg) {
        List<String> regexList = Arrays.asList(regexHeader, regexBold, regexEmphasis, regexCancellato, regexBold_, regexEmphasis_);
        List<Integer> matchCasesList = new ArrayList<>();
        for (int i=0; i<regexList.size(); i++) {
            if (msg.matches(regexList.get(i))) matchCasesList.add(i);
        }
        return matchCasesList;
    }

}
