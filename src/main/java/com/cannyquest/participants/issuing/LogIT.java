package com.cannyquest.participants.issuing;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

public class LogIT extends QBeanSupport implements TransactionParticipant, Configurable {

    String[] blockedAcqID = null;
    String mcc = null;
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;

    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;

        return PREPARED | NO_JOIN | READONLY;
    }




}

