package com.activemq.client;

import com.activemq.service.BoersenService;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class BoersenOrderClient implements Runnable, ExceptionListener {
    private String clientName;

    public BoersenOrderClient(String boerse) {
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

            // Create the destination (Queue)
            Destination topic = session.createTopic("BOERSENSPREISE");
            Destination destination = session.createQueue("BOERSENORDER." + clientName);

            // Create a MessageConsumer from the Session to the Queue
            MessageConsumer consumer = session.createConsumer(topic);
            MessageProducer producer = session.createProducer(destination);

            consumer.setMessageListener(new TopicMessageListener());

            System.out.println("Subscriber gestartet. Warte auf Nachrichten...");

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
