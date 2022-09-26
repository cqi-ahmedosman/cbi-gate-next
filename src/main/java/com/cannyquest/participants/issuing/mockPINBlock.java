package com.cannyquest.participants.issuing;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class mockPINBlock implements TransactionParticipant, Configurable {
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {

    }

    @Override
    public int prepare(long id, Serializable context) {

        Context ctx = (Context) context;
        ISOMsg msg = (ISOMsg) ctx.get(ContextConstants.REQUEST.toString());
        try {
            byte[] bytes;
            if (msg.hasField(2)) {
                bytes = this.computePINBlockFormat0(msg.getString(2), msg.getString(52));
            } else {
                bytes = this.computePINBlockFormat0(msg.getString(35).substring(0,16), msg.getString(52));

            }
            msg.set(52,bytes);
            ctx.put(ContextConstants.REQUEST.toString(), msg);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return PREPARED | NO_JOIN | READONLY;
    }

    private byte[] computePINBlockFormat0(String panNumber, String pin ) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {

        byte padByteISO0 	= 0x0F ;
        int BLK16 = getBlk16();
        int BLK08 = getBlk08();
        byte[] blkA 	= new byte[BLK16];
        byte[] blkB 	= new byte[BLK16];
        byte[] blkAc 	= new byte[BLK08];
        byte[] blkBc 	= new byte[BLK08];
        byte[] blkXOR	= new byte[BLK08];

        //fill blkA
        int pinLength = pin.length() ;
        blkA[0] = 0x00 ;					//ISOFORMAT0
        blkA[1] = (byte) pinLength;

        for (int i=0; i < pinLength ; i++){
            blkA[2+i] = (byte) Character.getNumericValue( pin.charAt(i) ) ;
        }

        for (int i=2 + pinLength ; i < BLK16 ; i++){ //Padding Values
            blkA[i] = (byte) padByteISO0 ;
        }

        blkAc = compressBlock(blkA) ;
        //
        //blkB = Block.buildBlkB( panNumber, blkB );
        //fill blkB
        byte ZERO = getZero();
        blkB[0] = ZERO ;
        blkB[1] = ZERO;
        blkB[2] = ZERO;
        blkB[3] = ZERO;

        String pan12RigthPosition = get12RigthPosition( panNumber );
        for (int i = 4 ; i < BLK16 ; i++ ){
            blkB[i] = (byte) Character.getNumericValue ( pan12RigthPosition.charAt(i-4) ) ;
        }
        //
        blkBc = compressBlock(blkB) ;

        //  XOR(blkAc,blkBc)
        for (int i=0 ; i < BLK08 ; i++){
            blkXOR[i] =  (byte) ( blkAc[i] ^ blkBc[i] ) ;
        }


        byte[] TPK = ISOUtil.hex2byte("1FB3F48A6D51832CE91C1C734554086D1FB3F48A6D51832C");

        System.out.println("Encryption Key:");
        for (int i = 0; i < TPK.length; i++) {
            System.out.printf("%02X", TPK[i]);
        }

        byte[] encryptedPINBlock;
        SecretKeySpec zpk = new SecretKeySpec(TPK, "DESede");
        Cipher TDES;
        TDES = Cipher.getInstance("DESede/ECB/NoPadding");
        TDES.init(Cipher.ENCRYPT_MODE, zpk);
        try {
            encryptedPINBlock = TDES.doFinal(blkXOR);
            System.out.println("\nEncrypted PIN Block:");
            for (int i = 0; i < encryptedPINBlock.length; i++) {
                System.out.printf("%02X", encryptedPINBlock[i]);
            }
            return encryptedPINBlock;

        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }





        //System.out.println( "isoFormat0 clearpinblock is : "+ org.inds.utl.translate.Translate.toHex( blkXOR));





        return  null ;
    }
    private int getBlk16() {
        return BLK16;
    }

    private int getBlk08() {
        return BLK08;
    }

    private byte getZero() {
        return ZERO;
    }

    /**
     * helper methods to create clear PIN Block
     */

    private final int  BLK16  = 16  ;
    private final int  BLK08  = 8   ;
    private final byte ZERO 	 = 0x00 ;

    /**
     * Compress an Binary byte array.
     * <p>
     * Example :
     * <pre>
     *  Input  :
     *  Output :
     * </pre>
     * @param blkIN input Ascii numeric byte array
     * @return compressed byte array
     *
     */
    public static byte[] compressBlock(byte[] blkIN)  {

        byte[] blkOUT = new byte[blkIN.length / 2];
        for (int i = 0; i < blkIN.length; i++) {
            int nibble = 0, index = 0;
            index = (i / 2);
            if ((i % 2) == 0) { // i is even
                nibble = (byte) (blkIN[i] << 4);
                blkOUT[index] = (byte) (blkOUT[index] | nibble);
            } else { // i is odd
                blkOUT[index] = (byte) (blkOUT[index] | blkIN[i]);
            }
        }
        return blkOUT;
    }

    private String get12RigthPosition(String panNumber){
        int LENGHT = panNumber.length() ;
        return panNumber.substring( LENGHT - 12 - 1  , LENGHT - 1 ) ;
    }
}
