package com.cannyquest.participants.issuing;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Track2;
import org.jpos.iso.*;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Log;
import org.jpos.util.Logger;

import java.io.Serializable;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class transformSVFEtoDHI extends QBeanSupport implements TransactionParticipant, Configurable {
    Configuration cfg;
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;

    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg msg = (ISOMsg) ctx.get(ContextConstants.REQUEST.toString());
        ISOMsg dhiReq;

        ctx.dump(System.out, "");


        ctx.put("SVFE-ORIGINAL-REQUEST", msg);
        try {
            GenericPackager svfe = new GenericPackager("cfg/iso93ASCII-SVFE.xml");
            msg.setPackager(svfe);
        } catch (ISOException e) {
            e.printStackTrace();
        }
        try {
            dhiReq = this.transformer(msg);
            ctx.put(ContextConstants.REQUEST.toString(), dhiReq);
        } catch (ISOException e) {
            e.printStackTrace();
        }

        return PREPARED | NO_JOIN | READONLY;
    }

    public ISOMsg transformer(ISOMsg svfeMsg) throws ISOException {
        ISOMsg dhiMsg = new ISOMsg();
        DHIField60 dhiField60 = new DHIField60();
        String MTI = svfeMsg.getMTI();
        SvfeTrxType svfeTrxType = null;
        boolean reversal = false;


        if(svfeMsg.hasField("48.2")){
            String type = svfeMsg.getString("48.2");
            switch (type){
                case ("700"):
                    if((svfeMsg.getString(3).substring(0,2).equals("01"))){
                        svfeTrxType = SvfeTrxType.CASHWITHDRAW;
                        dhiMsg.set("60.1", "2");
                        //dhiField60.setPos1('2');

                    }
                    if((svfeMsg.getString(3).substring(0,2).equals("17"))) {
                        svfeTrxType = SvfeTrxType.CASHADVANCE;
                        dhiMsg.set("60.1", "0");
                        //dhiField60.setPos1('0');


                    }
                    break;
                case "774":
                    svfeTrxType = SvfeTrxType.PURCHASE;
                    break;
                case "775":
                    svfeTrxType = SvfeTrxType.REFUND;
                    break;
                case "736":
                    svfeTrxType = SvfeTrxType.PREAUTH;
                    dhiMsg.set("60.8", "4");
                    //dhiField60.setPos8('4');
                    dhiMsg.set("63.2", "0000");

                    break;
                case "702":
                    svfeTrxType = SvfeTrxType.BALANCEINQUIRY;
                    break;
                case "704":
                    svfeTrxType = SvfeTrxType.MINISTATEMENT;
                    break;
                case "737":
                    svfeTrxType = SvfeTrxType.COMPLETION;
                    //String bytes = ISOUtil.padleft(svfeMsg.getString(4),24,'0');
                    dhiMsg.set(30, ISOUtil.padleft(svfeMsg.getString(4),24,'0'));
                    break;
                case "508":
                    svfeTrxType = SvfeTrxType.BILLPAYMENT;
                    break;
                default:
                    svfeTrxType = SvfeTrxType.UNKNOWN;
                    break;
            }
        } else
            svfeTrxType = SvfeTrxType.CASHWITHDRAW;

        //dhiMsg.set(60, dhiField60.get());


        switch (MTI){

            case "1100":
                if(svfeTrxType.equals(SvfeTrxType.CASHWITHDRAW) || svfeTrxType.equals(SvfeTrxType.PURCHASE)||  svfeTrxType.equals(SvfeTrxType.PREAUTH)|| svfeTrxType.equals(SvfeTrxType.BALANCEINQUIRY)|| svfeTrxType.equals(SvfeTrxType.MINISTATEMENT)|| svfeTrxType.equals(SvfeTrxType.CASHADVANCE) || svfeTrxType.equals(SvfeTrxType.BILLPAYMENT)){
                    dhiMsg.setMTI("0100");
                } else if (svfeTrxType.equals(SvfeTrxType.REFUND) || svfeTrxType.equals(SvfeTrxType.COMPLETION)){
                    dhiMsg.setMTI("0220");

                } else

                {
                    throw new ISOException("Unidentified Trx Type in message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");
                }

                break;
            case "1804":
                dhiMsg.setMTI("0800");
                break;
            case "1420":
                reversal = true;
                dhiMsg.setMTI("0420");
                dhiMsg.set("63.3", "0200");
                break;

            case "1421":
                reversal = true;
                dhiMsg.setMTI("0421");
                dhiMsg.set("63.3", "0200");
                break;

            default:
                throw new ISOException("unidentified MTI"+svfeMsg.getMTI()+"in message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");

        }

        if(reversal){
            StringBuilder stringBuilder = new StringBuilder();
            dhiMsg.set(90, stringBuilder.append("0100").append(svfeMsg.getString(11)).append(svfeMsg.getString(12).substring(6,12)).append(svfeMsg.getString(32)).toString());
        }

        if(svfeMsg.hasField(2)){

            String strPAN = svfeMsg.getString(2);
            if(strPAN.length()<=19){
                dhiMsg.set(2,strPAN);
            }
            else
                throw new ISOException ("PAN in SVFE exceeds 19 digits");
        }

        if(svfeMsg.hasField(3)){

            if (svfeTrxType.equals(SvfeTrxType.CASHWITHDRAW) || svfeTrxType.equals(SvfeTrxType.CASHADVANCE))
                dhiMsg.set(3,"010000");
            else if (svfeTrxType.equals(SvfeTrxType.BALANCEINQUIRY))
                dhiMsg.set(3, "300000");
            else if (svfeTrxType.equals(SvfeTrxType.PURCHASE) || svfeTrxType.equals(SvfeTrxType.PREAUTH) || svfeTrxType.equals(SvfeTrxType.COMPLETION) || svfeTrxType.equals(SvfeTrxType.BILLPAYMENT))
                dhiMsg.set(3, "000000");
            else if(svfeTrxType.equals(SvfeTrxType.MINISTATEMENT))
                dhiMsg.set(3,"380000");
            else if (svfeTrxType.equals(SvfeTrxType.REFUND))
                dhiMsg.set(3,"200000");
            else if(svfeTrxType.equals(SvfeTrxType.UNKNOWN))
                throw new ISOException("Uknown Transaction Type in message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(4)){

            dhiMsg.set(4,svfeMsg.getString(4));

        }

        if(svfeMsg.hasField(5)){
            throw new ISOException("Not Supported Field ["+5+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");

        }

        if(svfeMsg.hasField(6)){

            /**
             * multi-currency field present if the billing currency is different than the transaction currency.
             * requires field 10 and 51 to be present.
             */
            dhiMsg.set(6,svfeMsg.getString(6));

            if(svfeMsg.hasField(9)){
                dhiMsg.set(10, svfeMsg.getString(9));
            }
            if(svfeMsg.hasField(49)){
                dhiMsg.set(49, svfeMsg.getString(49));
            }
            if(svfeMsg.hasField(51)){
                dhiMsg.set(51, svfeMsg.getString(51));
            }


        }

        if(svfeMsg.hasField(7)){
            throw new ISOException("Not Supported Field ["+7+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");

        } else {
            dhiMsg.set(7, ISODate.formatDate(ISODate.parseISODate(svfeMsg.getString(12).substring(2,12)), "MMddhhmmss", TimeZone.getTimeZone("GMT")));


            //System.out.println("DHI field 7");

        }

        if(svfeMsg.hasField(8)){

            throw new ISOException("Not Supported Field ["+8+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");

        }

        if(svfeMsg.hasField(9)){
            //dhiMsg.set(10,svfeMsg.getString(9));

        }

        if(svfeMsg.hasField(10)){
            throw new ISOException("Not Supported Field ["+10+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(11)){
            dhiMsg.set(11,  svfeMsg.getString(11));

        }

        if(svfeMsg.hasField(12)){
            dhiMsg.set(12, svfeMsg.getString(12).substring(6,12));

        }

        if(svfeMsg.hasField(13)){
            throw new ISOException("Not Supported Field ["+13+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(14)){
            dhiMsg.set(14,  svfeMsg.getString(14));


        }

        if(svfeMsg.hasField(15)){

            /**
             * field is not applicable for 0100. put the condition just in case in future 1200 message type is used
             */
            if (svfeMsg.getMTI().equals("1100")){
                dhiMsg.unset(15);

            }




        }

        if(svfeMsg.hasField(16)){
            throw new ISOException("Not Supported Field ["+16+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(17)){
            throw new ISOException("Not Supported Field ["+17+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(18)){
            dhiMsg.set(18,  svfeMsg.getString(18));



        }

        if(svfeMsg.hasField(19)){

            dhiMsg.set(19,  svfeMsg.getString(19));


        }
        /**
         * if ACQ country code is missing, always default to 368 which is Iraq ISO country code.
         * all transactions coming from CBI are supposed to be originated from Iraq.
         */
        else
            dhiMsg.set(19,  "368");


        if(svfeMsg.hasField(20)){
            throw new ISOException("Not Supported Field ["+20+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(21)){
            throw new ISOException("Not Supported Field ["+21+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(22)){

            String DE22 = (svfeMsg.getString(22));
            StringBuilder stringBuilder = new StringBuilder();



            switch (svfeMsg.getString("22.1")){
                case "0":
                    stringBuilder.append("00");
                    break;
                case "1":
                case "6":
                    stringBuilder.append("01");
                    break;
                case "2":
                case "7":
                    stringBuilder.append("90");
                    break;
                case "3":
                    stringBuilder.append("03");
                    break;
                case "5":
                    stringBuilder.append("05");
                    break;
                case "8":
                    if (svfeMsg.hasField(55))
                        stringBuilder.append("05");
                    else
                        stringBuilder.append("00");
                    break;
                case "9":
                    stringBuilder.append("07");
                    break;
                default:
                    stringBuilder.append("00");
            }

            switch (svfeMsg.getString("22.2")){
                case "0":
                    stringBuilder.append("0");
                    break;
                case "1":
                    stringBuilder.append("1");
                    break;
                default:
                    stringBuilder.append("0");
                    break;
            }

            switch (svfeMsg.getString("22.3")){
                case "0":
                    dhiMsg.set(25,"00");
                    break;
                case "1":
                    dhiMsg.set(25,"01");
                    break;
                default:
                    dhiMsg.set(25, "00");
                    break;
            }
            stringBuilder.append("0");
            dhiMsg.set(22,stringBuilder.toString());
        }

        if(svfeMsg.hasField(23)){
            dhiMsg.set(23, "0"+svfeMsg.getString(23));

            /**
             *
             */



        }




        if(svfeMsg.hasField(25)){

            throw new ISOException("Not Supported Field ["+25+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");

        }

        if(svfeMsg.hasField(26)){

            throw new ISOException("Not Supported Field ["+26+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(27)){

            throw new ISOException("Not Supported Field ["+27+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(28)){

            throw new ISOException("Not Supported Field ["+28+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(29)){

            throw new ISOException("Not Supported Field ["+29+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(30)){

            

            throw new ISOException("Not Supported Field ["+30+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(31)){

            throw new ISOException("Not Supported Field ["+31+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");


        }

        if(svfeMsg.hasField(32)){
            String DE32 = svfeMsg.getString(32);
            if (DE32.length()%2 !=0){
                DE32 = DE32+"0";
            }
            dhiMsg.set(32, DE32);

        }


        if(svfeMsg.hasField(33)){
            throw new ISOException("Not Supported Field ["+33+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");
        }

        if(svfeMsg.hasField(34)){
            throw new ISOException("Not Supported Field ["+34+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");
        }

        if(svfeMsg.hasField(35)){
            String T2 = svfeMsg.getString(35);
            if (T2.indexOf("D")>0){
                T2 = T2.replace('D', '=');
                dhiMsg.set(35, T2 );
            } else {
                dhiMsg.set(35, T2 );
            }
            //old code to map DE39 as per the card data not simulator
           //dhiMsg.set(35, svfeMsg.getString(35) );
        }


        if(svfeMsg.hasField(36)){
            throw new ISOException("Not Supported Field ["+36+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");
        }


        if(svfeMsg.hasField(37)){
            dhiMsg.set(37, svfeMsg.getString(37));
        }


        if(svfeMsg.hasField(38)){
            dhiMsg.set(38, svfeMsg.getString(38));
        }


        if(svfeMsg.hasField(39)){
            switch (svfeMsg.getString(39)){
                case "000":
                    dhiMsg.set(39, "00");
                    break;
                case "901":
                    dhiMsg.set(39, "55");
                    break;
                case "906":
                    dhiMsg.set(39, "54");
                    break;
                case "886":
                    dhiMsg.set(39, "78");
                    break;
                case "915":
                    dhiMsg.set(39, "51");
                    break;
                case "940":
                    dhiMsg.set(39,"77");
                    break;
                default:
                    dhiMsg.set(39, "05");
                    break;
            }
        }


        if(svfeMsg.hasField(40)){
            throw new ISOException("Not Supported Field ["+40+"] in SVFE message: STAN["+svfeMsg.getString(11)+"]TimeStamp["+svfeMsg.getString(12)+"]");
        }


        if(svfeMsg.hasField(41)){
            dhiMsg.set(41, svfeMsg.getString(41));
        }


        if(svfeMsg.hasField(42)){
            dhiMsg.set(42, svfeMsg.getString(42));
        }


        if(svfeMsg.hasField(43)){
            StringTokenizer tokenizer = new StringTokenizer(svfeMsg.getString(43).replace('|', ' '), ">");
            String merchantName =tokenizer.nextToken();
            if(merchantName.length() == 0){
                merchantName = "EMPTY-MERCHANT-NAME";
            } else if (merchantName.length() > 25) {
                merchantName = merchantName.substring(0,25);
            }
            String streetName = "";
            String cityName = "";
            if(tokenizer.hasMoreTokens())
                streetName = tokenizer.nextToken();
            String concat = merchantName.concat("-").concat(streetName);
            if (concat.length()>25) {
                concat = concat.substring(0,25);
            } else {
                concat = ISOUtil.padright(concat, 25, ' ');
            }
            if(tokenizer.hasMoreTokens())
                cityName = tokenizer.nextToken();
            if(cityName.length()<13){
                cityName = ISOUtil.padright(cityName, 13, ' ');
            }
            StringBuilder builder = new StringBuilder();
            builder = builder.append(concat).append(cityName).append("IQ");
            dhiMsg.set(43, builder.toString());

        }

        if(svfeMsg.hasField("48.14")){
            dhiMsg.set("126.10", "11 "+svfeMsg.getString("48.14"));
        }


        if(svfeMsg.hasField(49)){
            dhiMsg.set(49, svfeMsg.getString(49));
        }


        if(svfeMsg.hasField(52)){
            log.info(ISOUtil.hex2byte(svfeMsg.getString(52)));
            dhiMsg.set(52, (ISOUtil.hex2byte(svfeMsg.getString(52))));
            dhiMsg.set(53, "2001010100000000");

        }


        if(svfeMsg.hasField(55)){
            dhiMsg.set(55, svfeMsg.getBytes(55));
        }

        if (reversal)
            dhiMsg.unset(new int[]{18, 19, 22, 25, 35, 43, 60});

        if(svfeTrxType.equals(SvfeTrxType.BILLPAYMENT)){

            dhiMsg.set(22,"0120");
            dhiMsg.set(60, "00020800800000");
        }

        return dhiMsg;
    }
}
