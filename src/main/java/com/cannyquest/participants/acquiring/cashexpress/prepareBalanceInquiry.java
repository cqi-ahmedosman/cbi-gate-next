package com.cannyquest.participants.acquiring.cashexpress;

import org.jpos.core.Configurable;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;
import com.cannyquest.participants.acquiring.common.transformDHItoSVFE;

import java.io.Serializable;
import java.util.Random;

import static com.cannyquest.participants.acquiring.common.transformDHItoSVFE.*;

public class prepareBalanceInquiry extends QBeanSupport implements TransactionParticipant, Configurable {

    /*
    get information from context,
    build ISO msg for balance inquiry and put it in the context as REQUEST
    get response
    put in context information required such as balance

    REQUEST
        chip balance inquiry
    --------------------
    <isomsg direction="outgoing">
      <!-- org.jpos.iso.packager.GenericPackager[cfg/iso93ASCII-SVFE.xml] -->
      <field id="0" value="1100"/>
      <field id="2" value="519360______2645"/>
      <field id="3" value="310000"/>
      <field id="4" value="000000000000"/>
      <field id="11" value="120866"/>
      <field id="12" value="220925121604"/>
      <field id="14" value="____"/>
      <field id="15" value="220925"/>
      <field id="18" value="4829"/>
      <field id="22" value="511401511344"/>
      <field id="23" value="00"/>
      <field id="32" value="1432"/>
      <field id="35" value="519360______2645=____________________"/>
      <field id="37" value="226809630767"/>
      <field id="41" value="407682  "/>
      <field id="42" value="36858239       "/>
      <field id="43" value="suham diyala             &gt; &gt;Diyala       &gt;&gt;IRQ&gt;"/>
      <isomsg id="48">
        <field id="2" value="702"/>
        <field id="4" value="000"/>
        <field id="12" value="2"/>
        <field id="40" value="0"/>
      </isomsg>
      <field id="49" value="368"/>
      <field id="52" value="[WIPED]"/>
      <field id="55" value="AA55AA55" type="binary"/>
    </isomsg>
  </send>
</log>
<log realm="channel/172.29.238.10:6432" at="2022-09-25T12:16:04.787" lifespan="17172ms">
  <receive>
    <isomsg direction="incoming">
      <!-- org.jpos.iso.packager.GenericPackager[cfg/iso93ASCII-SVFE.xml] -->
      <field id="0" value="1110"/>
      <field id="2" value="519360______2645"/>
      <field id="3" value="310000"/>
      <field id="4" value="000000000000"/>
      <field id="11" value="120866"/>
      <field id="12" value="220925121604"/>
      <field id="15" value="220925"/>
      <field id="23" value="00"/>
      <field id="32" value="1432"/>
      <field id="37" value="226809630767"/>
      <field id="38" value="494362"/>
      <field id="39" value="000"/>
      <field id="41" value="407682  "/>
      <isomsg id="48">
        <!-- org.jpos.iso.packager.GenericTaggedFieldsPackager -->
        <field id="2" value="702"/>
        <field id="4" value="000"/>
        <field id="12" value="2"/>
        <field id="16" value="226809630767"/>
        <field id="40" value="0"/>
      </isomsg>
      <field id="49" value="368"/>
      <isomsg id="54">
        <!-- org.jpos.iso.packager.GenericTaggedFieldsPackager -->
        <field id="5" value="000000474648"/>
        <field id="6" value="368"/>
      </isomsg>
      <field id="55" value="AA55AA55" type="binary"/>
    </isomsg>

    <log realm="com.cannyquest.participants.acquiring.transfromSVFEtoDHI" at="2022-09-25T12:16:04.789">
  <info>
    Balance as request: 000000474648
  </info>
</log>
<log realm="com.cannyquest.participants.acquiring.transfromSVFEtoDHI" at="2022-09-25T12:16:04.789">
  <info>
    Balance BigDecimal#1
    000000474648
  </info>
</log>

     */
    @Override
    public int prepare(long id, Serializable context) {

        Context ctx = (Context) context;

        log.info("inside prep balance inquiry");


        ISOMsg balance_inquiry = new ISOMsg("1100");
        balance_inquiry.set(2, ctx.getString("PAN"));
        balance_inquiry.set(3, "310000");
        balance_inquiry.set(4, "000000000000");
        balance_inquiry.set(11, ISOUtil.getRandomDigits(new Random(), 6, 10));
        balance_inquiry.set(12, "");
        balance_inquiry.set(14, ctx.getString("EXP"));
        balance_inquiry.set(15, "");
        balance_inquiry.set(18, ctx.getString("MCC"));
        balance_inquiry.set(22, "511401511344");
        //log.info("CASH_EXPRESS_PAN_SEQUENCE_NUMBER", ctx.getString("CASH_EXPRESS_PAN_SEQUENCE_NUMBER").substring(1));
        balance_inquiry.set(23, ctx.getString("CASH_EXPRESS_PAN_SEQUENCE_NUMBER").substring(1));
        //log.info(balance_inquiry.getString(23));
        balance_inquiry.set(32,ctx.getString("ACQUIRER_ID"));
        balance_inquiry.set(35, ctx.getString("TRACKII"));
        balance_inquiry.set(37, ISOUtil.getRandomDigits(new Random(), 12, 10));
        balance_inquiry.set(41, ctx.getString("TERMINAL_ID"));
        balance_inquiry.set(42, ctx.getString("MCHT_ID"));
        balance_inquiry.set(43, transformMERCHANT_NAME(ctx.getString("MCHT_NAME")));
        balance_inquiry.set("48.2", "702");
        balance_inquiry.set("48.4", "0");
        balance_inquiry.set("48.12","2");
        balance_inquiry.set("48.40", "0");
        balance_inquiry.set(49, ctx.getString("CURRENCY"));
        balance_inquiry.set(52, transformPINBLOCK(ctx.get("PINBLOCK")));
        balance_inquiry.set(55, transformCHIP_DATA(ctx.get("EMV_DATA_REQUEST")));
        balance_inquiry.set(100, "1431");

        ctx.put("CASH_EXPRESS_BALANCE_INQUIRY_REQUEST",balance_inquiry);
        //balance_inquiry.dump(System.out,"");

        return PREPARED | NO_JOIN | READONLY;
    }

    @Override
    public void commit(long id, Serializable context) {
        TransactionParticipant.super.commit(id, context);
    }

    @Override
    public void abort(long id, Serializable context) {
        TransactionParticipant.super.abort(id, context);
    }
}
