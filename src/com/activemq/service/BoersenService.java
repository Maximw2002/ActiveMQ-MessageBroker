package com.activemq.service;

import com.activemq.boerse.BorsenOrderBoerse;
import com.activemq.client.BoersenOrderClient;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class BoersenService {

    public static void main(String[] args) {
        thread(new BorsenPreisProducer(), false);
        thread(new BoersenOrderClient("Tim"), false);
        thread(new BorsenOrderBoerse("Frankfurt"), false);
        thread(new BorsenOrderBoerse("Stuttgart"), false);
        thread(new BorsenOrderBoerse("München"), false);
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
                for (int i = 0; i < 1000; i++) {
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
}
