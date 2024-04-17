package com.activemq.client;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.jline.terminal.TerminalBuilder;

import javax.jms.*;

    public class BoersenPreisConsumer implements Runnable {

        private static final String BROKER_URL = "tcp://localhost:61616";
        private static final String TOPIC_NAME = "StockPrices";
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        Destination destination = null;

        Boerse b = Boerse.QUOTRIX;


        public BoersenPreisConsumer() {
            try {
                // Verbindung zur ActiveMQ-Broker-Instanz herstellen
                ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("artemis", "artemis", BROKER_URL);
                connection = connectionFactory.createConnection();
                connection.start();

                // Eine Sitzung erstellen
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Das Ziel (Topic) für den Nachrichtenaustausch erstellen
                destination = session.createTopic(TOPIC_NAME);

                // Einen Nachrichtenempfänger für das Ziel erstellen
                consumer = session.createConsumer(destination);

            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run(){
            while (true) {
                Message message = null;
                try {
                    message = consumer.receive();

                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        double stockPrice = 0;
                        stockPrice = Double.parseDouble(textMessage.getText());

                        System.out.println("Received price: " + stockPrice);
                        response();
                    } else {
                        System.out.println("Received message of unexpected type: " + message.getClass().getSimpleName());
                    }
                } catch(Exception e){
                    System.out.println(e);
                }
            }
        }
        private void response() throws JMSException {
            try {
                MessageProducer producer = null;
                // Eine Sitzung erstellen
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Das Ziel (Topic) für den Nachrichtenaustausch erstellen
                Destination destination = session.createTopic(String.valueOf(Boerse.QUOTRIX));

                // Einen Nachrichtenerzeuger für das Ziel erstellen
                producer = session.createProducer(destination);

                producer.send(session.createTextMessage("buy"));
                producer.close();
                session.close();
            }catch (Exception e) {
                System.out.println(e);
            }

        }

        private void close(){

                // Ressourcen schließen
                if (consumer != null) {
                    try {
                        consumer.close();
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


