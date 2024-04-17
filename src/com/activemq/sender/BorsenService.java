package com.activemq.sender;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class BorsenService {

    public static void main(String[] args) {
        thread(new BorsenPreisProducer(), false);
        thread(new BorsenOrderConsumer("Frankfurt"), false);
        thread(new BorsenOrderConsumer("Stuttgart"), false);
        thread(new BorsenOrderConsumer("München"), false);
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(daemon);
        thread.start();
    }

    public static class BorsenPreisProducer implements Runnable {

        public void run() {
            try {
                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

                // Create a Connection
                Connection connection = connectionFactory.createConnection("artemis", "artemis");
                connection.start();

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic)
                Destination destination = session.createTopic("BOERSENPREISE");

                // Create a MessageProducer from the Session to the Topic
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                // Simulate sending stock price updates
                for (int i = 0; i < 10; i++) {
                    // Create a message
                    String text = "AAPL: $150.50 | GOOGL: $2500.20 | MSFT: $300.75";
                    TextMessage message = session.createTextMessage(text);

                    // Tell the producer to send the message
                    System.out.println("Sent price update: " + text);
                    producer.send(message);

                    Thread.sleep(2000); // Simulate periodic updates
                }

                // Clean up
                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
    }

    public static class BorsenOrderConsumer implements Runnable, ExceptionListener {
        private String boerse;

        public BorsenOrderConsumer(String boerse) {
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
}
