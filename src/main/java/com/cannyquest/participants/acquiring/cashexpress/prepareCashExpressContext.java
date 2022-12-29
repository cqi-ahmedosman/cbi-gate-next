package com.cannyquest.participants.acquiring.cashexpress;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;

public class prepareCashExpressContext extends QBeanSupport implements TransactionParticipant, Configurable {


    /*
    extract parameters from cash express request to do the following:
    1. build balance inquiry request
    2. build cash withdrawal request
    3. build cash express response
     */
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;

    }

    @Override
    public int prepare(long id, Serializable context) {

        Context ctx = (Context) context;
        ISOMsg msg = (ISOMsg) ctx.get(ContextConstants.REQUEST.toString());

        ctx.put("PAN", msg.getString(2));
        ctx.put("CASHEXPRESS_PCODE", msg.getString(3));
        ctx.put("CASHEXPRESS_DATE", msg.getString(7));
        ctx.put("CASHEXPRESS_STAN", msg.getString(11));
        ctx.put("CASHEXPRESS_TIME", msg.getString(12));
        ctx.put("EXP", msg.getString(14));
        ctx.put("MCC", msg.getString(18));
        ctx.put("CASHEXPRESS_CURRENCY", msg.getString(19));
        ctx.put("CASH_EXPRESS_PAN_SEQUENCE_NUMBER", msg.getString(23));
        ctx.put("CASHEXPRESS_POS_COND_CODE", msg.getString(25));
        ctx.put("ACQUIRER_ID", msg.getString(32));
        ctx.put("TRACKII", msg.getString(35));
        ctx.put("CASHEXPRESS_RRN", msg.getString(37));
        ctx.put("TERMINAL_ID", msg.getString(41));
        ctx.put("MCHT_ID", msg.getString(42));
        ctx.put("MCHT_NAME",msg.getString(43));
        ctx.put("CURRENCY", msg.getString(49));
        ctx.put("PINBLOCK", msg.getBytes(52));
        ctx.put("EMV_DATA_REQUEST", msg.getBytes(55));

        return PREPARED | NO_JOIN | READONLY;
    }

    @Override
    public void commit(long id, Serializable context) {
        TransactionParticipant.super.commit(id, context);
    }

    @Override
    public void abort(long id, Serializable context) {
        TransactionParticipant.super.abort(id, context);
    }
}
