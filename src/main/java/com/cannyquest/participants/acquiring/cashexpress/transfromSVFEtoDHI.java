package com.cannyquest.participants.acquiring.cashexpress;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;

public class transfromSVFEtoDHI extends QBeanSupport implements TransactionParticipant, Configurable {
    Configuration cfg;
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;

    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg svfeResponse = (ISOMsg) ctx.get(ContextConstants.RESPONSE.toString());
        ISOMsg dhiResponse = (ISOMsg) ctx.get("DHI-ORIGINAL-REQUEST");

        if (svfeResponse != null) {
            try {

                dhiResponse.setResponseMTI();

                if (svfeResponse.hasField(38)) {
                    dhiResponse.set(38, svfeResponse.getString(38));
                }


                if (svfeResponse.hasField(39)) {
                    switch (svfeResponse.getString(39)) {
                        case "000":
                            dhiResponse.set(39, "00");
                            break;
                        case "901":
                            dhiResponse.set(39, "55");
                            break;
                        case "906":
                            dhiResponse.set(39, "54");
                            break;
                        case "886":
                            dhiResponse.set(39, "78");
                            break;
                        case "915":
                            dhiResponse.set(39, "51");
                            break;
                        case "940":
                            dhiResponse.set(39, "77");
                            break;
                        case "802":
                            dhiResponse.set(39, "91");
                            break;
                        case "805":
                            dhiResponse.set(39, "06");
                            break;
                        case "821":
                            dhiResponse.set(39, "38");
                            break;
                        case "827":
                            dhiResponse.set(39, "05");
                            break;
                        case "959":
                            dhiResponse.set(39, "96");
                            break;
                        default:
                            dhiResponse.set(39, "05");
                            break;



                    }
                }
                if(svfeResponse.hasField("48.2")){

                    switch (svfeResponse.getString("48.2")){
                        case "702":
                            if (svfeResponse.hasField(39)  ) {
                                if (svfeResponse.getString(39).equals("000")) {
                                    log.info("Balance as request: " + svfeResponse.getString("54.5"));
                                    String balance = (svfeResponse.hasField("54.5") ? svfeResponse.getString("54.5") : null);
                                    log.info("Balance BigDecimal#1", balance);
                                    if (balance != null) {
                                        if (svfeResponse.hasField("54.5")) {
                                            dhiResponse.set(54, "00" + "00" + svfeResponse.getString("54.6") + "C" + balance);

                                        }
                                    }
                                }
                            }
                            break;
                        default:
                            dhiResponse.unset(54);
                            break;

                    }
                }
                if(svfeResponse.hasField(55)){
                    dhiResponse.set(55, svfeResponse.getBytes(55));
                }

                dhiResponse.unset(100);
                ctx.put(ContextConstants.RESPONSE.toString(),dhiResponse);
            } catch (ISOException e) {
                e.printStackTrace();
            }
        }

        return PREPARED | NO_JOIN | READONLY;
    }
}
