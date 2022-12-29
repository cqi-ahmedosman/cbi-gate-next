package com.cannyquest.participants.acquiring.billpayment;

import org.jpos.core.*;
import org.jpos.iso.ISODate;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.q2.QBeanSupport;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceUtil;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class transformPOStoSVFE extends QBeanSupport implements TransactionParticipant, Configurable {
    Configuration cfg;
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;

    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg msg = (ISOMsg) ctx.get(ContextConstants.REQUEST.toString());
        ISOMsg svfeReq = null;
        try {
            svfeReq = this.transformer(msg);
        } catch (ISOException e) {
            e.printStackTrace();
        } catch (InvalidCardException e) {
            e.printStackTrace();

        }
        ctx.put(ContextConstants.REQUEST.toString(), svfeReq);





        try {


            GenericPackager svfe = new GenericPackager("cfg/iso93ASCII-SVFE.xml");
            svfeReq.setPackager(svfe);
        } catch (ISOException e) {
            e.printStackTrace();
        }
        return PREPARED | NO_JOIN | READONLY;
    }

    private ISOMsg transformer(ISOMsg msg) throws ISOException, InvalidCardException {

        ISOMsg svfeMsg = new ISOMsg();
        Space sp = SpaceFactory.getSpace("je:svfeSpace");



        /**
         * transformation logic goes here
         */

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyMMdd");
        LocalDateTime now = LocalDateTime.now();
        //Card card = Card.builder().isomsg(msg).build();

        /**
         * hardcoded value for De22 for testing purpose
         * to be deleted
         */

        /////////////////////////////////////////



        StringBuilder dhiDE22 = new StringBuilder();


        if (msg.hasField(12)) {
            svfeMsg.set(12, dtf.format(now)+msg.getString(12));
            svfeMsg.set(15, dtf.format(now));
        }
        svfeMsg.set(14, msg.getString(14));




        switch (msg.getMTI()){


            case "0400":
            case "0420":
                svfeMsg.setMTI("1420");

                break;
            default:
                svfeMsg.setMTI("1100");
                break;
        }


        if (msg.hasField(3)) {
            switch (msg.getString(3)) {
                case "300000":
                    svfeMsg.set(3, "310000");
                    svfeMsg.set(4, "000000000000");
                    svfeMsg.set("48.2", "702");
                    svfeMsg.set("48.40", "0");
                    break;
                case "010000":
                    svfeMsg.set(3, "010000");
                    svfeMsg.set(4, msg.getString(4));
                    svfeMsg.set("48.2", "700");
                    svfeMsg.set("48.40", "1");
                    break;
                case "000000":
                    svfeMsg.set(3, "000000");
                    svfeMsg.set(4, msg.getString(4));
                    svfeMsg.set("48.2", "774");
                    svfeMsg.set("48.40", "1");
                    break;
                case "500000":
                    svfeMsg.set(2, msg.getString(2));
                    svfeMsg.set(3, msg.getString(3));
                    svfeMsg.set(4, msg.getString(4));

                    if(msg.hasField(14))
                        svfeMsg.set(14, msg.getString(14));
                    svfeMsg.set(22, msg.getString(22));

                    break;

            }
        }



        if(msg.hasField(11)){

            svfeMsg.set(11, ISOUtil.zeropad(SpaceUtil.nextLong(sp,"SVFE_TRACE"), 6));

            // svfeMsg.set(11, msg.getString(11));
        }


        String yymmdDhhmmss = ISODate.formatDate(new Date(), "YYMMDDhhmmss");


        if(msg.hasField(18)){
            svfeMsg.set(18, msg.getString(18));
        }






        if(msg.hasField(23)){
            svfeMsg.unset(23);
        }

        if(msg.hasField(32)){
            svfeMsg.set(32, msg.getString(32));
        }


        if(msg.hasField(37)){
            svfeMsg.set(37, msg.getString(37));
        }


        if(msg.hasField(41)){
            svfeMsg.set(41, msg.getString(41));
        }


        if(msg.hasField(42)){
            svfeMsg.set(42, msg.getString(42));
        }

        if(msg.hasField(43)){
            String name = msg.getString(43).substring(0,25);
            String street = " ";
            String city = msg.getString(43).substring(25,38);
            String state = " ";
            String country = "IRQ";
            String postal_code = " ";
            String separator = "\u003E";
            svfeMsg.set(43, name+separator+street+separator+city+separator+separator+country+separator);
        }

        if (msg.hasField(48)){
            log.info("DE48 found");
            msg.getComponent(48).dump(System.out, "DE48");

            svfeMsg.set("48", msg.getComponent(48));
        } else
            log.info("DE48 not found");

        if(msg.hasField(49)){
            svfeMsg.set(49, msg.getString(49));
        }

        if(msg.hasField(52)){
           svfeMsg.unset(52);


        }


        if (msg.hasField(55)){

            svfeMsg.unset(55);
        }
        svfeMsg.set(100,"1431");
        return svfeMsg;
    }
}
