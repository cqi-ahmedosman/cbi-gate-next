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

public class prepareCashExpressResponse extends QBeanSupport implements TransactionParticipant, Configurable {

    /*
    build cash express response ISO
    put it in context as RESPONSE
     */

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;

    }
    @Override
    public int prepare(long id, Serializable context) {

        Context ctx = (Context) context;
        ISOMsg t = ctx.get("CASH_EXPRESS_CASH_WITHDRAWAL_RESPONSE");
        ISOMsg msg = new ISOMsg("0110");

        msg.set(2, ctx.getString("PAN"));
        msg.set(3, ctx.getString("PCODE"));
        msg.set(7,ctx.getString("CASHEXPRESS_DATE"));
        msg.set(11,ctx.getString("CASHEXPRESS_STAN"));
        msg.set(19, ctx.getString("CASHEXPRESS_CURRENCY"));
        msg.set(25, ctx.getString("CASHEXPRESS_POS_COND_CODE"));
        msg.set(32, ctx.getString("ACQUIRER_ID"));
        msg.set(37, ctx.getString("CASHEXPRESS_RRN"));
        msg.set(38, t.getString(38));
        msg.set(39, t.getString(39));
        msg.set(41, ctx.getString("TERMINAL_ID"));
        msg.set(42, ctx.getString("MCHT_ID"));
        msg.set(44, "    2   1");
        msg.set(49, ctx.getString("CURRENCY"));
        msg.set(55, ctx.getString("EMV_DATA_RESPONSE"));
        msg.set(56, "000000408792631");
        ctx.put("CASH_EXPRESS_RESPONSE",msg);

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
