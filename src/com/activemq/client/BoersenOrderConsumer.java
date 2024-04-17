package com.activemq.client;

import com.activemq.service.BoersenPriceProducer;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class BoersenOrderConsumer implements Runnable, ExceptionListener {
    private String clientName;
    MessageConsumer consumerQ = null;
    MessageConsumer consumerS = null;
    MessageConsumer consumerF = null;

    public BoersenOrderConsumer(String boerse) {
        this.clientName = boerse;
    }

    public void run() {
        try {
            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

            // Create a Connection
            Connection connection = connectionFactory.createConnection("artemis", "artemis");
            connection.start();

            connection.setExceptionListener(this);

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destinations (Queue)
            Destination destinationQ = session.createTopic(String.valueOf(Boerse.QUOTRIX));
            Destination destinationS = session.createTopic(String.valueOf(Boerse.STUTTGART));
            Destination destinationF = session.createTopic(String.valueOf(Boerse.FRANKFURT));

            // Create the consumers
            consumerQ = session.createConsumer(destinationQ);
            consumerS = session.createConsumer(destinationS);
            consumerF = session.createConsumer(destinationF);

            System.out.println("Order Consumer gestartet");

            // Listen for orders
            while (true) {

            }
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

    public synchronized void onException(JMSException ex) {
        System.out.println("JMS Exception occurred. Shutting down client.");
    }
}
