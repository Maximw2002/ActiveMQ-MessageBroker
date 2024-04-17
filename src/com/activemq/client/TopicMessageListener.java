package com.activemq.client;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
public class TopicMessageListener implements MessageListener {
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
                System.out.println("Nachricht empfangen: " + text);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

