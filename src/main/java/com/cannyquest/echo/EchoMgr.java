package com.cannyquest.echo;


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

public class EchoMgr extends QBeanSupport implements Runnable {

    private int ECHO_INTERVAL = 30000;
    private long SVFE_TRACE = 0L;



    ISOMsg Echo = new ISOMsg("1804");

    @Override
    protected void startService() {
        log.info("starting the thread ");
        new Thread(this).start();
    }

    public void run() {
        Space sp = SpaceFactory.getSpace("je:"+cfg.get("space"));



        for (int tickCount=5; running (); tickCount++) {
            ISOUtil.sleep ( cfg.getLong("echo-interval", ECHO_INTERVAL));

            try {
                QMUX mux = (QMUX) NameRegistrar.getIfExists("mux."+cfg.get("mux"));
                Echo.set(11, ISOUtil.zeropad(SpaceUtil.nextLong(sp,"SVFE_TRACE"), 6));
                Echo.set(12, ISODate.formatDate(new Date(), "yyMMddhhmmss"));
                Echo.set(24, "831");
                mux.request(Echo, 10000L);
            } catch (ISOException e) {
                e.printStackTrace();
            }
        }

    }
}