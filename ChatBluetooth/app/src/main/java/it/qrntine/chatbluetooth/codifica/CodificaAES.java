package it.qrntine.chatbluetooth.codifica;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

public class CodificaAES {

    public CodificaAES(){}

    /**
     *
     * @param messaggio testo del messaggio da cifrare
     * @return ritorna un Messaggio con la chiave e il IV di cifratura
     */
    public MetaMessaggio codificaMessaggio(String messaggio){
        MetaMessaggio risultato = new MetaMessaggio();
        byte[] m = messaggio.getBytes();

        try{
            KeyGenerator generatore = KeyGenerator.getInstance("AES"); //generatore chiave con algoritmo "AES"
            generatore.init(256); //di 256bit
            risultato.setChiave(generatore.generateKey()); //salva la chiave generata nel messaggio

            Cipher cifra = Cipher.getInstance("AES/CBC/PKCS5PADDING"); //crea chiave di cifratura
            cifra.init(Cipher.ENCRYPT_MODE, risultato.getChiave());
            risultato.setTesto(cifra.doFinal(m)); //cifra messaggio e aggiungi testo
            risultato.setIvp(cifra.getIV()); //aggiungi il IV

            return risultato;

        }catch(NoSuchAlgorithmException e){
        }catch(NoSuchPaddingException e1){
        }catch(InvalidKeyException e2){
        }catch(BadPaddingException e3){
        }catch(IllegalBlockSizeException e4){
            //fai i catch
        }

        return null;
    }

    /**
     *
     * @param messaggio messaggio da decifrare
     * @return ritorna il testo del messaggio decifrato
     */
    public String decodificaMessaggio(MetaMessaggio messaggio){
        String risultato = "";

        try{
            Cipher decifra = Cipher.getInstance("AES/CBC/PKCS5PADDING"); //crea la chiave di decifratura
            decifra.init(Cipher.DECRYPT_MODE, messaggio.getChiave(), new IvParameterSpec(messaggio.getIvp()));
            byte[] messaggioDecifrato = decifra.doFinal(messaggio.getTesto()); //decifra messaggio
            risultato = new String(messaggioDecifrato).toString();

            return risultato;

        }catch(NoSuchAlgorithmException e){
        }catch(NoSuchPaddingException e1){
        }catch(InvalidAlgorithmParameterException e2){
        }catch(InvalidKeyException e3){
        }catch(BadPaddingException e4){
        }catch(IllegalBlockSizeException e5){
            //fai i catch
        }

        return null;
    }
}
