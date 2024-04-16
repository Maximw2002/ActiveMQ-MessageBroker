package com.activemq.sender;

public class Test {
    public static void main(String[] args){
        while(true){
            System.out.println(Math.random() * 100);
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
