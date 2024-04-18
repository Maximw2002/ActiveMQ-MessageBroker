package com.activemq.client;

import com.activemq.service.BoersenPriceProducer;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BoersenOrderConsumer implements Runnable, ExceptionListener {


    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String TOPIC_NAME = "StockPrices";
    Connection connection = null;
    Session session = null;
    MessageConsumer consumer = null;
    Destination destination = null;
    Message message = null;
    Boerse b = Boerse.QUOTRIX;



    public BoersenOrderConsumer() {
        try {
            // Verbindung zur ActiveMQ-Broker-Instanz herstellen
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("artemis", "artemis", BROKER_URL);
            connection = connectionFactory.createConnection();
            connection.start();

            // Eine Sitzung erstellen
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Das Ziel (Topic) für den Nachrichtenaustausch erstellen
            destination = session.createQueue("QUOTRIX");

            // Einen Nachrichtenempfänger für das Ziel erstellen
            consumer = session.createConsumer(destination);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        while (true) {
            try {
                message = consumer.receive();

                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    String txtmessage = textMessage.getText();

                    System.out.println("Received order: " + txtmessage);
                    response();

                } else {
                    System.out.println("Received message of unexpected type Order : " + message.getClass().getSimpleName());
                }
            } catch(Exception e){
                System.out.println(e);
            }
        }
    }


    private void response() throws JMSException {
        MessageProducer producer = null;
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            // Bestimmen des Antwortziels
            Destination replyDestination = session.createQueue("RESPONSE");
            if (replyDestination != null) {
                // Festlegen des Zielortes (hier: Queue oder Topic des Produzenten)
                producer = session.createProducer(replyDestination);

                // Erstellen einer Antwortnachricht
                TextMessage responseMessage = session.createTextMessage("Order bestätigt");
                System.out.println("Order bestätigt.");

                // Senden der Antwortnachricht
                producer.send(responseMessage);

                message = null;
                // Schließen von Ressourcen
                producer.close();
                session.close();
            } else {
                System.out.println("Kein Antwortziel angegeben.");
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    public synchronized void onException(JMSException ex) {
        System.out.println("JMS Exception occurred. Shutting down client.");
    }
}
