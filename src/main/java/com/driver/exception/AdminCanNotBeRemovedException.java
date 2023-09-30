package com.driver.exception;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

public class AdminCanNotBeRemovedException extends RuntimeException{
    public AdminCanNotBeRemovedException(String message){
        super(message);
    }
    public AdminCanNotBeRemovedException(){}
}
