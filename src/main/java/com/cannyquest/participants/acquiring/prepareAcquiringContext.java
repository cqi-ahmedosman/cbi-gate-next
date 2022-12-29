package com.cannyquest.participants.acquiring;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;
import java.util.Map;

public class prepareAcquiringContext extends QBeanSupport implements TransactionParticipant, Configurable {

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;

    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        log.info("starting preparing acquiring ctx");
        ISOMsg msg = (ISOMsg) ctx.get(ContextConstants.REQUEST.toString());
        try {
            GenericPackager genericPackager = new GenericPackager("cfg/iso87BCD-DHI.xml") ;
            msg.setPackager(genericPackager);

            /*
                This is how the context should look like
                      DHI_PAN_DATA_ENTRY_MODE_1: 30
                      DHI_PAN_DATA_ENTRY_MODE_2: 35
                      DHI_PIN_ENTRY_CAPABILITY: 31
                      DHI_DE22_UNUSED: 30
                      DHI-Terminal-Type: 30
                      DHI-Terminal-Entry-Capability: 35
                      DHI-Chip-Condition-Code: 30
                      DHI-New-Service-Development: 30
                      DHI-Merchant-Group-Indicator: 3030
                      DHI-Transaction-Indicator: 30
                      DHI-Card-Authentication-Capability: 30
                      DHI-E-Commerce-Indicator: 3030
                      DHI-RFU: 30
                      DHI-Partial-Authorization-Indicator: 30
                      DHI-Terminal-Indicator-TLE: 30
                      DHI-Terminal-Indicator-DUKPT: 30

             */

            if (msg.hasField(22)) {
                ISOMsg dhiDE22 = (ISOMsg) msg.getComponent(22);
                Map de22Children = dhiDE22.getChildren();
                int x = de22Children.size();
                de22Children.forEach((k,v)->{
                    ctx.put(dhiDE22.getPackager().getFieldDescription(dhiDE22, (int)k),msg.getString("22."+k));
                });
            }


            /*
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

             */

            if (msg.hasField(60)) {
                ISOMsg dhiDE60 = (ISOMsg) msg.getComponent(60);
                Map de60Children = dhiDE60.getChildren();
                de60Children.forEach((k,v)->{
                    ctx.put(dhiDE60.getPackager().getFieldDescription(dhiDE60, (int)k),msg.getString("60."+k));
                });
            }




        } catch (ISOException e) {
            throw new RuntimeException(e);
        }

        return PREPARED | NO_JOIN | READONLY;
    }


}

