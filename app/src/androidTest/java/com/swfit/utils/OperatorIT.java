package com.swfit.utils;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.swift.R;
import com.swift.io.net.Operator;
import com.swift.io.net.OperatorFactory;
import com.swift.model.Account;
import com.swift.model.Network;
import com.swift.tasks.Status;
import com.swift.tasks.results.OperationResult;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class OperatorIT {

    private static final Status SUCCESS = Status.SUCCESS;

    private static final Random rand = new Random();
    private static final Properties props = new Properties();
    private static final List<String> recipients = new ArrayList<>();

    @BeforeClass
    public static void setupClass() throws IOException {
        final InputStream stream = ClassLoader.getSystemResourceAsStream("credentials.properties");
        props.load(stream);

        recipients.add(props.getProperty("recipients"));
    }

    @Test
    public void testOldMeteor() {
        final Account account = new Account(props.getProperty("oldmeteor.username"), "Meteor", props.getProperty("oldmeteor.password"), Network.METEOR);
        final Operator operator = OperatorFactory.getOperator(account);

        operator.login();
        operator.getRemainingSMS();
        operator.getCharacterLimit();
        final OperationResult result = operator.send(recipients, rand.nextInt(99999) + ": Hello World from Old Meteor!");

        assertEquals(R.string.message_sent, result.getStringResource());
        assertEquals(SUCCESS, result.getStatus());
    }

    @Test
    public void testMeteor() {
        final Account account = new Account(props.getProperty("meteor.username"), "Meteor", props.getProperty("meteor.password"), Network.METEOR);
        final Operator operator = OperatorFactory.getOperator(account);

        operator.login();
        operator.getRemainingSMS();
        final OperationResult result = operator.send(recipients, rand.nextInt(99999) + ": Hello World from Meteor!");

        assertEquals(R.string.message_sent, result.getStringResource());
        assertEquals(SUCCESS, result.getStatus());
    }

    @Test
    public void testThree() {
        final Account account = new Account(props.getProperty("three.username"), "Three", props.getProperty("three.password"), Network.THREE);
        final Operator operator = OperatorFactory.getOperator(account);

        operator.login();
        operator.getRemainingSMS();
        final OperationResult result = operator.send(recipients, rand.nextInt(99999) + ": Hello World from Three!");

        assertEquals(R.string.message_sent, result.getStringResource());
        assertEquals(SUCCESS, result.getStatus());
    }

    @Test
    public void testTesco() {
        final Account account = new Account(props.getProperty("tesco.username"), "Tesco", props.getProperty("tesco.password"), Network.TESCO);
        final Operator operator = OperatorFactory.getOperator(account);

        operator.login();
        operator.getRemainingSMS();
        final OperationResult result = operator.send(recipients, rand.nextInt(99999) + ": Hello World from Tesco!");

        assertEquals(R.string.message_sent, result.getStringResource());
        assertEquals(SUCCESS, result.getStatus());
    }

    @Test
    public void testO2() {
        final Account account = new Account(props.getProperty("o2.username"), "O2", props.getProperty("o2.password"), Network.O2);
        final Operator operator = OperatorFactory.getOperator(account);

        operator.login();
        operator.getRemainingSMS();
        final OperationResult result = operator.send(recipients, rand.nextInt(99999) + ": Hello World from O2!");

        assertEquals(R.string.message_sent, result.getStringResource());
        assertEquals(SUCCESS, result.getStatus());
    }

}
