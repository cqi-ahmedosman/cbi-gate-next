package com.cannyquest.participants.acquiring.cashexpress;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;

public class prepareCashWithdrawalContext extends QBeanSupport implements TransactionParticipant, Configurable {



    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;

    }


    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg msg = (ISOMsg) ctx.get("CASH_EXPRESS_BALANCE_INQUIRY_RESPONSE");

        /*
        take the balance from the balance inquiry response and put in the context
         */

        String balance = msg.getString("54.5");
        String currency = msg.getString("54.6");

        ctx.put("AVAILABLE_BALANCE", balance);

        ctx.put("AVAILABLE_BALANCE_CURRENCY", currency);


        return PREPARED | NO_JOIN | READONLY;
    }
}
