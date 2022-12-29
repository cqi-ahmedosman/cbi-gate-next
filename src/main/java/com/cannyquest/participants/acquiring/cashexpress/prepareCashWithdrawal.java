package com.cannyquest.participants.acquiring.cashexpress;

import org.jpos.core.Configurable;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;
import com.cannyquest.participants.acquiring.common.transformDHItoSVFE;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cannyquest.participants.acquiring.common.transformDHItoSVFE.*;

public class prepareCashWithdrawal extends QBeanSupport implements TransactionParticipant, Configurable {

    /*
    get information from context and build ISO Msg for cash withdrawal
    get response and put it relevant information into context


Mag Stripe cash withdrawal:
--------------------------
<isomsg direction="outgoing">
      <!-- org.jpos.iso.packager.GenericPackager[cfg/iso93ASCII-SVFE.xml] -->
      <field id="0" value="1100"/>
      <field id="2" value="5193601186360427"/>
      <field id="3" value="010000"/>
      <field id="4" value="002000000000"/>
      <field id="11" value="120787"/>
      <field id="12" value="220925100514"/>
      <field id="14" value="2309"/>
      <field id="15" value="220925"/>
      <field id="18" value="4829"/>
      <field id="22" value="511401511344"/>
      <field id="32" value="1432"/>
      <field id="35" value="5193601186360427=23092061796564800000"/>
      <field id="37" value="226807610048"/>
      <field id="41" value="557640  "/>
      <field id="42" value="36814539       "/>
      <field id="43" value="Nahir Dyala              &gt; &gt;Diyala       &gt;&gt;IRQ&gt;"/>
      <isomsg id="48">
        <field id="2" value="700"/>
        <field id="4" value="000"/>
        <field id="12" value="2"/>
        <field id="40" value="1"/>
      </isomsg>
      <field id="49" value="368"/>
      <field id="52" value="99275A0C25920EDE"/>
    </isomsg>
  </send>


    <isomsg direction="incoming">
      <!-- org.jpos.iso.packager.GenericPackager[cfg/iso93ASCII-SVFE.xml] -->
      <field id="0" value="1110"/>
      <field id="2" value="5193601186360427"/>
      <field id="3" value="010000"/>
      <field id="4" value="002000000000"/>
      <field id="11" value="120787"/>
      <field id="12" value="220925100514"/>
      <field id="15" value="220925"/>
      <field id="32" value="1432"/>
      <field id="37" value="226807610048"/>
      <field id="38" value="433262"/>
      <field id="39" value="000"/>
      <field id="41" value="557640  "/>
      <isomsg id="48">
        <!-- org.jpos.iso.packager.GenericTaggedFieldsPackager -->
        <field id="2" value="700"/>
        <field id="4" value="000"/>
        <field id="12" value="2"/>
        <field id="16" value="226807610048"/>
        <field id="40" value="1"/>
      </isomsg>
      <field id="49" value="368"/>
      <isomsg id="54">
        <!-- org.jpos.iso.packager.GenericTaggedFieldsPackager -->
        <field id="5" value="033457354315"/>
        <field id="6" value="368"/>
      </isomsg>
    </isomsg>

     */

    @Override
    public int prepare(long id, Serializable context) {

        Context ctx = (Context) context;

        ISOMsg msg = ctx.get("CASH_EXPRESS_BALANCE_INQUIRY_RESPONSE");
        ctx.put("EMV_DATA_RESPONSE", msg.getString(55));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyMMdd");
        LocalDateTime now = LocalDateTime.now();
        ISOMsg cash_withdrawal_request = new ISOMsg("1100");
        cash_withdrawal_request.set(2, msg.getString(2));
        cash_withdrawal_request.set(3,"010000");
        cash_withdrawal_request.set(4, ctx.getString("AVAILABLE_BALANCE"));
        cash_withdrawal_request.set(11, ctx.getString("CASHEXPRESS_STAN"));
        cash_withdrawal_request.set(12,dtf.format(now)+ctx.getString("CASHEXPRESS_TIME"));
        cash_withdrawal_request.set(14, ctx.getString("EXP"));
        cash_withdrawal_request.set(15,dtf.format(now));
        cash_withdrawal_request.set(18, ctx.getString("MCC"));
        cash_withdrawal_request.set(22,"511401511344");
        cash_withdrawal_request.set(23, ctx.getString("CASH_EXPRESS_PAN_SEQUENCE_NUMBER").substring(1));
        cash_withdrawal_request.set(32, ctx.getString("ACQUIRER_ID"));
        cash_withdrawal_request.set(35, ctx.getString("TRACKII"));
        cash_withdrawal_request.set(37,ctx.getString("CASHEXPRESS_RRN"));
        cash_withdrawal_request.set(41, ctx.getString("TERMINAL_ID"));
        cash_withdrawal_request.set(42, ctx.getString("MCHT_ID"));
        cash_withdrawal_request.set(43, ctx.getString("MCHT_NAME"));
        cash_withdrawal_request.set("48.2", "700");
        cash_withdrawal_request.set("48.4", "000");
        cash_withdrawal_request.set("48.12", "2");
        cash_withdrawal_request.set("48.40", "1");
        cash_withdrawal_request.set(49, ctx.getString("AVAILABLE_BALANCE_CURRENCY"));
        cash_withdrawal_request.set(52, transformPINBLOCK(ctx.get("PINBLOCK")));
        //msg.dump(System.out, "balance-inquiry-response");
        ctx.put("CASH_EXPRESS_CASH_WITHDRAWAL_REQUEST",cash_withdrawal_request);
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
