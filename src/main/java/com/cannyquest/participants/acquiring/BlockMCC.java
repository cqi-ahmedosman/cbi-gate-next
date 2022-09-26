package com.cannyquest.participants.acquiring;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

public class BlockMCC extends QBeanSupport implements TransactionParticipant, Configurable {

    String[] blockedMCC = null;
    String mcc = null;
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        blockedMCC = cfg.getAll("blocked-mcc");

    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg msg = (ISOMsg) ctx.get(ContextConstants.REQUEST.toString());
        ISOSource src = (ISOSource) ctx.get(ContextConstants.SOURCE.toString());

        if(msg.hasField(18) ){
            mcc = msg.getString(18);
            if (hasBlockedMCC(msg.getString(18))){
                try {
                    msg.setResponseMTI();
                    msg.set(39,"12");
                    src.send(msg);
                    return ABORTED | READONLY | NO_JOIN;

                } catch (ISOException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return PREPARED | NO_JOIN | READONLY;
    }



    private boolean hasBlockedMCC (String mcc) {
        return Arrays.stream(blockedMCC).anyMatch (m -> mcc != null && mcc.equals(m));
    }
}
