package com.cannyquest.participants.issuing;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

public class prepareIssuingContext extends QBeanSupport implements TransactionParticipant, Configurable {

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;

    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg msg = (ISOMsg) ctx.get(ContextConstants.REQUEST.toString());
        try {
            msg.setPackager(new GenericPackager("cfg/iso93ASCII-SVFE.xml"));
        } catch (ISOException e) {
            throw new RuntimeException(e);
        }
        log.info("starting preparing issuing ctx");
        ISOMsg SVFEDE48 = (ISOMsg) msg.getComposite();
        log.info("DE48 max filed:\t" + SVFEDE48.getMaxField());


        return PREPARED | NO_JOIN | READONLY;
    }


}

