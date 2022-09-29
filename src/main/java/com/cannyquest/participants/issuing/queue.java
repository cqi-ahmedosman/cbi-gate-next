package com.cannyquest.participants.issuing;

import com.cannyquest.packager.dhi.JSONPackager;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class queue extends QBeanSupport implements TransactionParticipant, Configurable {

    private static final String QUEUE_NAME = "issuing_auth";
    String[] blockedAcqID = null;
    String mcc = null;

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg msg_req = (ISOMsg) ctx.get(ContextConstants.REQUEST.toString());
        ISOMsg msg_res = (ISOMsg) ctx.get(ContextConstants.RESPONSE.toString());

        try {
            msg_req.setPackager(new JSONPackager());
            msg_res.setPackager(new JSONPackager());


        } catch (ISOException e) {
            throw new RuntimeException(e);
        }

        ISOMsg msg = msg_req;
        msg.merge(msg_res);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("172.17.0.2");
        try (
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, msg.pack());

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } catch (ISOException e) {
            throw new RuntimeException(e);
        }


        return PREPARED | NO_JOIN | READONLY;
    }





}

