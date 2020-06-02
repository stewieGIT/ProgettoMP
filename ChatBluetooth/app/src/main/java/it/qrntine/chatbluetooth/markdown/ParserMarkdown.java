package it.qrntine.chatbluetooth.markdown;

import java.util.regex.Pattern;

public class ParserMarkdown {

    private static final String regexBold = "(.+?)*(\\*\\*)(.+?)(\\*\\*)(.+?)*"; // ..**...**..
    private static final String regexCursive = "(.+?)*(\\*)(.+?)(\\*)(.+?)*";  // ..*...*..
    private static final String regexList = "(^[(.+?)(\\:)]((\\*){1}(.+?))*)"; // ..:*...*...
    private static final String regexHeader = "^(\\#{1})(.+?)*";    // #...

    //funzione che ritorna l'int da usare nella switch del metodo parsing
    public static int numParser(String stringa) {

        String[] regExpressions = {regexBold, regexCursive, regexList, regexHeader};

        for (int i=0; i<regExpressions.length; i++) {
            Pattern pattern = Pattern.compile(regExpressions[i]);
            if (pattern.matcher(stringa).matches()) {
                return i; //deve controllare quale parser (case) chiamare e ritorna il numero, che poi uso per chiamare parsing
            }
        }

        return 10;	//caso default
    }


    public static String parsing(String stringa, int n) {
        String subMsg = "", markMsg = "", finalString = "";
        String[] listaMsg;
        Pattern pat;

        switch(n) {

            case(0):
                //PARSER BOLD	(**stringa**)
                pat = Pattern.compile(regexBold);
                if (pat.matcher(stringa).matches()) {
                    System.out.println("**************MATCH BOLD OK**********");
                    if(stringa.startsWith("**")) {
                        listaMsg = stringa.split("\\*\\*");
                        for(int i=0; i<listaMsg.length; i++) {
                            if(i%2 == 0) {
                                subMsg += listaMsg[i];
                            } else {
                                subMsg += "<b>"+listaMsg[i]+"</b>";
                            }
                        }
                    } else {
                        listaMsg = stringa.split("\\*\\*");
                        for(int j=0; j<listaMsg.length; j++) {
                            if(j==listaMsg.length-1) {
                                subMsg += "<b>"+listaMsg[j]+"</b>";
                                continue;
                            }
                            if(j%2 == 0) {
                                subMsg += listaMsg[j];
                            } else {
                                subMsg += "<b>"+listaMsg[j]+"</b>";
                            }
                        }
                    }
                    finalString = subMsg;
                }
                break;

            case(1):
                //PARSER CURSIVE	(*stringa*)
                //if(stringa.contains("*")) {
                pat = Pattern.compile(regexCursive);
                if(pat.matcher(stringa).matches()){
                    listaMsg = stringa.split("\\*");
                    if(listaMsg[0] != "*") {
                        for(int i=0; i<listaMsg.length; i++) {
                            if(i%2 == 0) {
                                subMsg += listaMsg[i];
                            } else {
                                subMsg += "<em>"+listaMsg[i]+"</em>";
                            }
                        }
                    } else if (listaMsg[0] == "*") {
                        for(int i=0; i<listaMsg.length; i++) {
                            if(i==listaMsg.length-1) {
                                subMsg += "<em>"+listaMsg[i]+"</em>";
                                continue;
                            }
                            if(i%2 != 0) {
                                subMsg += listaMsg[i];
                            } else {
                                subMsg += "<em>"+listaMsg[i]+"</em>";
                            }
                        }
                    }
                    finalString = subMsg;
                }
                break;

            case(2):
                //PARSER LISTA	(Nomelista:*item1*item2...)
                pat = Pattern.compile(regexList);
                if (pat.matcher(stringa).matches()) {
                    System.out.println("**************MATCH LIST OK**********");
                    listaMsg = stringa.split("(\\*)");
                    subMsg = "";
                    for(int i=0; i<(listaMsg.length); i++) {
                        //if(i==0) continue;	//se la lista inizia per * il primo elemento che ottengo e' '' quindi lo scarto
                        if(i==0) {
                            subMsg += "<ul>"+listaMsg[i];
                        } else {
                            System.out.println("**************MATCH LIST OK********** "+listaMsg[i]);
                            subMsg += "<li>"+listaMsg[i]+"</li>";
                        }
                    }
                    System.out.println("**************MATCH LIST res********** "+subMsg);
                    finalString = subMsg+"</ul>";
                    System.out.println("**************MATCH LIST res********** "+finalString);
                }
                break;

            case(3):
                //PARSER HEADER	(#stringa)
                pat = Pattern.compile(regexHeader);
                if (pat.matcher(stringa).matches()) {
                    System.out.println("**************MATCH Header1 OK**********");
                    subMsg = stringa.substring(stringa.indexOf("#") + 1);
                    finalString = "<h1>" + subMsg + "</h1>";
                }
                break;

            case(10):
                finalString = stringa;
                break;

            default:
                finalString = stringa;
                break;

        }//END SWITCH
        return finalString;
    }//END PARSING
}//END CLASS
