package com.cannyquest.participants.acquiring;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;

public class MockPINBlock extends QBeanSupport implements TransactionParticipant, Configurable {

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg msg = (ISOMsg) ctx.get(ContextConstants.REQUEST.toString());
        if(msg.hasField(52)){
            byte[] pblock = msg.getBytes(52);
            StringBuilder sb = new StringBuilder(pblock.length * 2);
            for (byte b : pblock) {
                sb.append(String.format("%02x", b));
            }
            msg.set(52, sb.toString());
        }
        msg.unset(52);
        ctx.put(ContextConstants.REQUEST.toString(), msg);
        return PREPARED | NO_JOIN | READONLY;
    }
}
