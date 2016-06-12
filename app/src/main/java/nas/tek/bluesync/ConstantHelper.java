package nas.tek.bluesync;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

//Helper class for global constants
public class ConstantHelper {

    //HC-05 MAC Address
    public static final String address  =  "98:D3:32:70:91:3B";

    //Hex Codes for each RFID card
    public static final String ONE      =  "665defbfbd6626efbfbdefbfbdefbfbdd9b9efbfbd";
    public static final String TWO      =  "665defbfbd6626efbfbdefbfbd22efbfbdefbfbd22";
    public static final String THREE    =  "665defbfbd66efbfbdefbfbd6666efbfbd1e26";
    public static final String FOUR     =  "665defbfbd66efbfbd4cefbfbdefbfbdd999efbfbd";
    public static final String FIVE     =  "665defbfbd66efbfbdefbfbdefbfbd26266666";
    public static final String FIVE_ALT =  "665defbfbd66efbfbdcc9926266666";
    public static final String SIX      =  "665defbfbd66efbfbd4c66692eefbfbdefbfbd";

    //Converts an UTF-8 encoded string to Hex
    public static String toHex(String arg){
        try {
            return String.format("%x", new BigInteger(1, arg.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

}
