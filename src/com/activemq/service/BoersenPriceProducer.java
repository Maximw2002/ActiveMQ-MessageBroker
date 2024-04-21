package com.activemq.service;

import com.activemq.client.BoersenOrderConsumer;
import com.activemq.client.BoersenPreisConsumer;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

public class BoersenPriceProducer {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String TOPIC_NAME = "StockPrices";
    private static final double MAX_ABWEICHUNG = 0.3;
    static String nachricht;

    public static void main(String[] args) {
        thread(new BoersenPreisProducer(), false);
        BoersenPreisConsumer consumer = new BoersenPreisConsumer();
        Thread consumerThread = new Thread(consumer);
        consumerThread.setDaemon(false);
        consumerThread.start();
        thread(new BoersenOrderConsumer(), false);
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(daemon);
        thread.start();
    }

    public static class BoersenPreisProducer implements Runnable {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        public BoersenPreisProducer(){
            try {
                // Verbindung zur ActiveMQ-Broker-Instanz herstellen
                ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("artemis", "artemis", BROKER_URL);
                connection = connectionFactory.createConnection();
                connection.start();

                // Eine Sitzung erstellen
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Das Ziel (Topic) für den Nachrichtenaustausch erstellen
                Destination destination = session.createTopic(TOPIC_NAME);

                // Einen Nachrichtenerzeuger für das Ziel erstellen
                producer = session.createProducer(destination);
            }catch (Exception e) {
                System.out.println(e);
            }
        }
        @Override
        public void run() {
            try {
                // Aktienkurse erzeugen und als Textnachrichten senden
                while (true) {
                    double stockPriceOld = Math.random() * 100;
                    double stockPriceNew = stockPriceOld;
                    while (stockPriceOld * (1 + MAX_ABWEICHUNG) < stockPriceNew || stockPriceOld * (1 - MAX_ABWEICHUNG) > stockPriceNew)
                        stockPriceNew = (Math.random() * 100);
                    nachricht = "A:"+ stockPriceNew + "/B:"+ stockPriceNew;
                    TextMessage message = session.createTextMessage(nachricht);
                    producer.send(message);
                    System.out.println("Sent: " + stockPriceNew);
                    Thread.sleep(1000);
                }

            } catch (JMSException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        private void close(){

                // Ressourcen schließen
                if (producer != null) {
                    try {
                        producer.close();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
                if (session != null) {
                    try {
                        session.close();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
}
