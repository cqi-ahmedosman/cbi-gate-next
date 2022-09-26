package com.cannyquest.logon;


import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.iso.QMUX;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceUtil;
import org.jpos.util.NameRegistrar;

import java.util.Date;

public class LogonMgr extends QBeanSupport implements Runnable {

    private int LOGON_INTERVAL = 60000;
    private long SVFE_TRACE = 0L;




    ISOMsg LogOn = new ISOMsg("1804");
    ISOMsg LogOff = new ISOMsg("1804");

    @Override
    protected void startService() {
        log.info("starting the thread ");

        new Thread(this).start();
    }

    public void run() {

        Space sp = SpaceFactory.getSpace("je:"+cfg.get("space"));


        sp.out("SVFE_TRACE", SVFE_TRACE);
        log.info("Running ");

        for (int tickCount=0; running (); tickCount++) {
            log.info ("tick " + tickCount);
            try {
                QMUX mux = (QMUX) NameRegistrar.getIfExists("mux."+cfg.get("mux"));

                LogOff.set(11, ISOUtil.zeropad(SpaceUtil.nextLong(sp,"SVFE_TRACE"), 6));
                LogOff.set(12, ISODate.formatDate(new Date(), "yyMMddhhmmss"));
                LogOff.set(24, "802");

                LogOn.set(11, ISOUtil.zeropad(SpaceUtil.nextLong(sp,"SVFE_TRACE"), 6));
                LogOn.set(12, ISODate.formatDate(new Date(), "yyMMddhhmmss"));
                LogOn.set(24, "801");

                boolean logoff = cfg.getBoolean("log-off", false);

                if (logoff){
                    mux.request(LogOff, 10000L);
                }

                mux.request(LogOn, 10000L);
            } catch (ISOException e) {
                e.printStackTrace();
            }
            ISOUtil.sleep (cfg.getLong("logon-interval", LOGON_INTERVAL));
        }

    }
}