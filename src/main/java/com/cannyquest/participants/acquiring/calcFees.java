package com.cannyquest.participants.acquiring;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class calcFees extends QBeanSupport implements TransactionParticipant, Configurable {

    private String strCashWithdrawalFeesPercent;
    private BigDecimal bdCashWithdrawalFeesPercent, bdCashFeeConstant;



    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        bdCashWithdrawalFeesPercent = new BigDecimal(cfg.get("CashWithdrawalFees", "0.004"));
        bdCashFeeConstant = bdCashWithdrawalFeesPercent.divide(bdCashWithdrawalFeesPercent.add(new BigDecimal("1")), 12, RoundingMode.HALF_DOWN);
    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg msg = (ISOMsg) ctx.get(ContextConstants.REQUEST.toString());
        BigDecimal amnt = null;
        String s = null;
        if (msg.hasField(4)) {
            amnt = new BigDecimal(msg.getString(4)).movePointLeft(2);
            amnt = amnt.multiply(bdCashFeeConstant).setScale(2,BigDecimal.ROUND_HALF_EVEN);
            try {
                s = ISOUtil.zeropad(amnt.movePointRight(2).toString(),12);
                msg.set("54.1", s);

            } catch (ISOException e) {
                e.printStackTrace();
            }
        }



        ctx.put(ContextConstants.REQUEST.toString(), msg);

        return PREPARED | NO_JOIN | READONLY;
    }
}
