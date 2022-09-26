package com.cannyquest.participants.issuing;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Log;

public class DHIField60 {

    public DHIField60() {
        for (int i = 0; i < positions.length; i++) {
            positions[i] = '0';
        }


    }

    public DHIField60 (String field60) throws ISOException {
        if(field60.length()>14)
            throw new ISOException("DE 60 length is more than 14");

        field60 = ISOUtil.strpad(field60,14);



        for (int i = 0; i < positions.length; i++) {
            positions[i] = field60.charAt(i);
        }


 
    }

    private char[] positions = new char[14];

    public char getPos1(){ return positions[0];}
    public char getPos2(){ return positions[1];}
    public char getPos3(){ return positions[2];}
    public char getPos4(){ return positions[3];}
    public String getPos5_6(){ return String.copyValueOf(positions,4,2);}
    public char getPos7() {return positions[6];}
    public char getPos8() {return positions[7];}
    public String getPos9_10(){ return String.copyValueOf(positions,8,2);}
    public char getPos11() {return positions[10];}
    public char getPos12() {return positions[11];}
    public char getPos13() {return positions[12];}
    public char getPos14() {return positions[13];}

    public void setPos1(char pos1) {
        this.positions[0] = pos1;
    }

    public void setPos2(char pos2) {
        this.positions[1] = pos2;
    }

    public void setPos3(char pos3) {
        this.positions[2] = pos3;
    }

    public void setPos4(char pos4) {
        this.positions[3] = pos4;
    }


    public void setPos5_6(char pos5, char pos6) {
        this.positions[4] = pos5;
        this.positions[5] = pos6;
    }

    public void setPos5_6(String pos5_6) {
        this.positions[4] = pos5_6.charAt(1);
        this.positions[5] = pos5_6.charAt(2);
    }

    public void setPos7(char pos7) {
        this.positions[6] = pos7;
    }


    public void setPos8(char pos8) {
        this.positions[7] = pos8;
    }

    public void setPos9_10(char pos9, char pos10) {
        this.positions[8] = pos9;
        this.positions[9] = pos10;
    }

    public void setPos9_10(String pos9_10) {
        this.positions[8] = pos9_10.charAt(1);
        this.positions[9] = pos9_10.charAt(2);
    }

    public void setPos11(char pos11) {
        this.positions[10] = pos11;
    }


    public void setPos12(char pos12) {
        this.positions[11] = pos12;
    }


    public void setPos13(char pos13) {
        this.positions[12] = pos13;
    }


    public void setPos14(char pos14) {
        this.positions[13] = pos14;
    }

    public String get (){

        return new String(positions);

    }
}
