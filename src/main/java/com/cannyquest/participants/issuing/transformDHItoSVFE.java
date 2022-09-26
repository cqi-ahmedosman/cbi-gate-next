package com.cannyquest.participants.issuing;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.tlv.TLVList;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;

public class transformDHItoSVFE implements TransactionParticipant, Configurable {

    Configuration cfg;

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {

        this.cfg = cfg;

    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg dhiResponse = (ISOMsg) ctx.get(ContextConstants.RESPONSE.toString());
        ISOMsg svfeResponse = (ISOMsg) ctx.get("SVFE-ORIGINAL-REQUEST");


        if (dhiResponse != null) {
            try {

                svfeResponse.setResponseMTI();


                svfeResponse.unset(new int[] {14,18,22,35,42,43,100});

                if (dhiResponse.hasField(38)) {
                    svfeResponse.set(38, dhiResponse.getString(38));
                }
                if (dhiResponse.hasField(39)) {
                    switch (dhiResponse.getString(39)) {
                        case "00":
                            svfeResponse.set(39, "000");
                            break;
                        case "55":
                            svfeResponse.set(39, "901");
                            break;
                        case "54":
                            svfeResponse.set(39, "906");
                            break;
                        case "57":
                            svfeResponse.set(39, "886");
                            break;
                        case "51":
                            svfeResponse.set(39, "915");
                            break;
                        case "77":
                            svfeResponse.set(39, "940");
                            break;
                        case "12":
                            svfeResponse.set(39,"880");
                        default:
                            svfeResponse.set(39, "902");

                    }
                }

                if (dhiResponse.hasField(54)) {

                    String de54 = dhiResponse.getString(54);
                    String rec1 = de54.substring(0,20);
                    String acctType = rec1.substring(0,2);
                    String amntType = rec1.substring(2, 4);
                    String crn = rec1.substring(4, 7);
                    String sign = rec1.substring(7,8);
                    String amnt = rec1.substring(8, rec1.length());

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder = stringBuilder.append("005").append("012").append(amnt).append("006").append("003").append(crn);
                    svfeResponse.set(54, stringBuilder.toString());
                    if (!sign.equals("C")){
                        svfeResponse.set(4, "000000000000");
                    }

                }

                if(dhiResponse.hasField(55)){
                    TLVList DE55res = new TLVList();
                    TLVList dhiDE55 = new TLVList();
                    dhiDE55.unpack(dhiResponse.getBytes(55));

                    if (dhiDE55.hasTag(0x91)){
                        DE55res.append(dhiDE55.find(0x91));
                        svfeResponse.set(55, DE55res.pack());
                    } else {
                        svfeResponse.unset(55);
                    }



                } else
                    svfeResponse.unset(55);

                ctx.put(ContextConstants.RESPONSE.toString(),svfeResponse);
            } catch (ISOException e) {
                e.printStackTrace();
            }
        }

        return PREPARED | NO_JOIN | READONLY;
    }
}
