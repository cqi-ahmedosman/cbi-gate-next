package com.cannyquest.participants.acquiring;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.GroupSelector;

import java.io.Serializable;

import static org.jline.utils.Log.warn;

public class Switch extends QBeanSupport implements GroupSelector, Configurable {
    private Configuration cfg;

    public String select(long id, Serializable context) {
        Context ctx = (Context) context;
        String groups = null;
        ISOMsg m = (ISOMsg) (ctx.get(ContextConstants.REQUEST.toString()));
        try {
            if (m.hasField(3)){
                groups  = cfg.get (m.getString(3), null);
                if (groups.equals("CASH")){
                    if (!m.hasField(4)) {
                        groups = "CASHEXPRESS";
                        log.info("GROUP SELECTED:"+ groups);
                        return groups;

                    }
                }
            }

            log.info("Group Selected is:\t" + groups);
            ctx.checkPoint("SELECTED GROUP");
            return groups;
        } catch (Exception e) {
            warn (e);
            return null;
        }
    }
    @Override
    public int prepare(long id, Serializable context) {
        return PREPARED | READONLY | NO_JOIN;
    }
    public void commit   (long id, Serializable context) { }
    public void abort    (long id, Serializable context) { }
    @Override
    public void setConfiguration (Configuration cfg) {
        this.cfg = cfg;
    }
}
