package com.activemq.sender;

import java.text.DecimalFormat;
import javax.jms.*;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
public class MessageSender {

    //URL of the JMS server. DEFAULT_BROKER_URL will just mean that JMS server is on localhost
    private static final String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private static final double stockPriceOld = 22.5;
    private static final double maxAbweichung = 0.15;
    private static double stockPriceNew;

    // default broker URL is : tcp://localhost:61616"
    private static final String brokerF = "Börse Frankfurt"; // Queue Name.You can create any/many queue names as per your requirement.
    private static final String brokerAnswerF = "Börse Frankfurt Antwortkanal"; // Queue Name.You can create any/many queue names as per your requirement.

    private static final String brokerM = "Börse München";
    private static final String brokerS = "Börse Stuttgart";

    private static final DecimalFormat df = new DecimalFormat("#.##");

    public static void main(String[] args) throws JMSException {
        // Getting JMS connection from the server and starting it
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection("artemis", "artemis");
        connection.start();

        //Creating a non-transactional session to send/receive JMS message.
        Session session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);

        //Destination represents here our queue 'JCG_QUEUE' on the JMS server.
        //The queue will be created automatically on the server.
        Destination frankfurt = session.createQueue(brokerF);
        Queue frankfurtAntwort = session.createQueue(brokerAnswerF);
        Destination muenchen = session.createQueue(brokerM);

        // MessageProducer is used for sending messages to the queue.
        MessageProducer producer1 = session.createProducer(frankfurt);
        MessageProducer producer2 = session.createProducer(muenchen);


        stockPriceNew = (Math.random() * 100);
            while(stockPriceOld * (1 + maxAbweichung) < stockPriceNew || stockPriceOld * (1 - maxAbweichung) > stockPriceNew)
                stockPriceNew = (Math.random() * 100);
            TextMessage message = session.createTextMessage((Double.toString(stockPriceNew)));
            producer1.send(message);
            System.out.println("Börse Frankfurt schickt Angebot: " + df.format(stockPriceNew));


        stockPriceNew = (Math.random() * 100);
        while(stockPriceOld * (1 + maxAbweichung) < stockPriceNew || stockPriceOld * (1 - maxAbweichung) > stockPriceNew)
            stockPriceNew = (Math.random() * 100);
        TextMessage message1 = session.createTextMessage(Double.toString(stockPriceNew));
        producer2.send(message1);
        System.out.println("Börse Frankfurt schickt Angebot: " + df.format(stockPriceNew));


        QueueBrowser browser = session.createBrowser(frankfurtAntwort);

        Boolean isEmpty = !browser.getEnumeration().hasMoreElements();
        System.out.println(isEmpty);
        if (isEmpty == false) {
            MessageConsumer consumer1 = session.createConsumer(frankfurtAntwort);
            Message answer = consumer1.receive();
                System.out.println("Bestätige den Kauf der Aktie von Client 1");
            }
        connection.close();

    }
}



