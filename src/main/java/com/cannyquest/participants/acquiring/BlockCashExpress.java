package com.cannyquest.participants.acquiring;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.IOException;
import java.io.Serializable;

public class BlockCashExpress extends QBeanSupport implements TransactionParticipant, Configurable {

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;

    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg msg = (ISOMsg) ctx.get(ContextConstants.REQUEST.toString());
        ISOSource src = (ISOSource) ctx.get(ContextConstants.SOURCE.toString());

        if(!msg.hasField(4)  ){

            if(msg.getString(3).equals("010000")){

                try {
                    msg.setResponseMTI();
                    msg.set(39,"12");
                    src.send(msg);

                } catch (ISOException | IOException e) {
                    e.printStackTrace();
                }
                return ABORTED | READONLY | NO_JOIN;
            }


        }

        ctx.put("DHI-ORIGINAL-REQUEST", msg);



        return PREPARED | NO_JOIN | READONLY;
    }
}
