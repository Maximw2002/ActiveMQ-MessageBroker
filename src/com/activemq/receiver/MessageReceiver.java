package com.activemq.receiver;

import java.text.DecimalFormat;
import javax.jms.Connection;
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

public class MessageReceiver {

    // URL of the JMS server
    private static final String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    // default broker URL is : tcp://localhost:61616"

    // Name of the queue we will receive messages from
    private static final String brokerF = "Börse Frankfurt";
    private static final String brokerAnswerF = "Börse Frankfurt Antwortkanal";

    private static final String brokerM = "Börse München";
    private static final String brokerS = "Börse Stuttgart";


    private static final double minAbweichung = 0.01;
    private static final double stockPriceOld = 22.5;

    private static final DecimalFormat df = new DecimalFormat("#.##");

    public static void main(String[] args) throws JMSException {

        // Getting JMS connection from the server
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection("artemis", "artemis");
        connection.start();

        // Creating session for sending messages
        Session session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);

        // Getting the queue 'JCG_QUEUE'
        Destination frankfurt = session.createQueue(brokerF);
        Destination frankfurtAntwort = session.createQueue(brokerAnswerF);
        Destination muenchen = session.createQueue(brokerM);

        // MessageConsumer is used for receiving (consuming) messages
        MessageConsumer consumer1 = session.createConsumer(frankfurt);
        MessageConsumer consumer2 = session.createConsumer(muenchen);

        // Here we receive the message.
        Message message = consumer1.receive();

        // We will be using TestMessage in our example. MessageProducer sent us a TextMessage
        // ,so we must cast to it to get access to its .getText() method.
        if(message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage)message;

            if(Double.parseDouble(textMessage.getText()) > stockPriceOld * (1 + minAbweichung)) {
                System.out.println("Client 1 möchte für " + df.format(Double.parseDouble(textMessage.getText())) + " an der Börse Frankfurt verkaufen");
                MessageProducer producer1 = session.createProducer(frankfurtAntwort);
                TextMessage answer = session.createTextMessage("Nachricht von Client 1 an Börse Frankfurt: ich möchte diese Aktie verkaufen");
                producer1.send(answer);
            }
            else if(Double.parseDouble(textMessage.getText()) < stockPriceOld * (1 - minAbweichung)) {
                System.out.println("Client 1 möchte für " + df.format(Double.parseDouble(textMessage.getText())) + " an der Börse Frankfurt kaufen");
                MessageProducer producer1 = session.createProducer(frankfurtAntwort);
                TextMessage answer = session.createTextMessage("Nachricht von Client 1 an Börse Frankfurt: ich möchte diese Aktie kaufen");
                producer1.send(answer);
            }
            else {
                System.out.println("Client 1 hält seine Aktie " + df.format(Double.parseDouble(textMessage.getText())));
            }
            }
        // Here we receive the message.
        Message message2 = consumer2.receive();

        // We will be using TestMessage in our example. MessageProducer sent us a TextMessage
        // ,so we must cast to it to get access to its .getText() method.
        if(message2 instanceof TextMessage) {
            TextMessage textMessage1 = (TextMessage)message2;

            if(Double.parseDouble(textMessage1.getText()) > stockPriceOld * (1 + minAbweichung))
                System.out.println("Client 2 möchte für " + df.format(Double.parseDouble(textMessage1.getText())) + " an der Börse München verkaufen");
            else if(Double.parseDouble(textMessage1.getText()) < stockPriceOld * (1 - minAbweichung))
                System.out.println("Client 2 möchte für " + df.format(Double.parseDouble(textMessage1.getText())) + " an der Börse München kaufen");
            else {
                System.out.println("Client 2 hält seine Aktie " + df.format(Double.parseDouble(textMessage1.getText())));
            }
        }
            connection.close();
        }
    }

