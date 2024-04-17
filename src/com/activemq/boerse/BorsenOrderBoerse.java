package com.activemq.boerse;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class BorsenOrderBoerse implements Runnable, ExceptionListener {
    private String boerse;

    public BorsenOrderBoerse(String boerse) {
        this.boerse = boerse;
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
            Destination destination = session.createQueue("BOERSENORDER." + boerse);

            // Create a MessageConsumer from the Session to the Queue
            MessageConsumer consumer = session.createConsumer(destination);

            // Listen for orders
            while (true) {
                Message message = consumer.receive(); // Wait for a message

                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    String order = textMessage.getText();
                    System.out.println("Received order from " + boerse + ": " + order);

                    // Process the order (simulated)
                    System.out.println("Processing order...");
                    Thread.sleep(1000); // Simulate order processing time

                    // Send confirmation back to the client
                    Destination replyDestination = message.getJMSReplyTo();
                    MessageProducer producer = session.createProducer(replyDestination);
                    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                    TextMessage confirmation = session.createTextMessage("Order processed successfully");
                    producer.send(confirmation);
                }
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
