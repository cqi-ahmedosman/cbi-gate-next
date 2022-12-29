package com.cannyquest.participants.acquiring.billpayment;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;

public class transfromSVFEtoPOS implements TransactionParticipant, Configurable {
    Configuration cfg;
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;

    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg svfeResponse = (ISOMsg) ctx.get(ContextConstants.RESPONSE.toString());
        //ISOMsg dhiResponse = (ISOMsg) ctx.get("DHI-ORIGINAL-REQUEST");


        if (svfeResponse != null) {

                ctx.put(ContextConstants.RESPONSE.toString(),svfeResponse);
            }


        return PREPARED | NO_JOIN | READONLY;
    }
}
