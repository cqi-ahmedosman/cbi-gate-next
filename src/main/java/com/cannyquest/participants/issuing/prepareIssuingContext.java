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
import java.util.Map;

public class prepareIssuingContext extends QBeanSupport implements TransactionParticipant, Configurable {

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;

    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        log.info("starting preparing issuing ctx");
        ISOMsg msg = (ISOMsg) ctx.get(ContextConstants.REQUEST.toString());
        try {
            GenericPackager genericPackager = new GenericPackager("cfg/iso93ASCII-SVFE.xml") ;
            msg.setPackager(genericPackager);



            /*


            This is how the context should look like

                  SVFE-Card-Data-Input-Capabiltiy: 5
                  SVFE-Cardholder-Authentication-Capability: 1
                  SVFE-Card-Capture-Capability: 0
                  SVFE-Operating-Environment: 1
                  SVFE-Cardholder-Presence-Indicator: 0
                  SVFE-Card-Presence: 1
                  SVFE-Card-Data-Input-Method: 5
                  SVFE-Cardholder-Authentication-Method: 1
                  SVFE-Cardholder-Authentication-Entity: 3
                  SVFE-Card-Data-Output-Capability: 3
                  SVFE-Terminal-Output-Capability: 4
                  SVFE-PIN-Capture-Capability: 6
                  SVFE-TRX-TYPE: 702
                  SVFE-CVC2-CVV2-Present-Indicator: 000
                  SVFE-Terminal-Type: 2
                  SVFE-FINANCIAL-TRX-INDICATOR: 1
                  SVFE-CHIP-COND-CODE: 0

             */

            if (msg.hasField(22)) {
                ISOMsg svfeDE22 = (ISOMsg) msg.getComponent(22);
                Map de22Children = svfeDE22.getChildren();
                int x = de22Children.size();
                de22Children.forEach((k,v)->{
                    ctx.put(svfeDE22.getPackager().getFieldDescription(svfeDE22, (int)k),msg.getString("22."+k));
                });
            }


            if (msg.hasField(48)) {
                ISOMsg svfeDE48 = (ISOMsg) msg.getComponent(48);
                Map de48Children = svfeDE48.getChildren();
                de48Children.forEach((k,v)->{
                    ctx.put(svfeDE48.getPackager().getFieldDescription(svfeDE48, (int)k),msg.getString("48."+k));
                });
            }


            if (msg.hasField(54)) {
                ISOMsg svfeDE54 = (ISOMsg) msg.getComponent(54);
                Map de54Children = svfeDE54.getChildren();
                de54Children.forEach((k,v)->{
                    ctx.put(svfeDE54.getPackager().getFieldDescription(svfeDE54, (int)k),msg.getString("54."+k));
                });
            }


        } catch (ISOException e) {
            throw new RuntimeException(e);
        }

        return PREPARED | NO_JOIN | READONLY;
    }


}

