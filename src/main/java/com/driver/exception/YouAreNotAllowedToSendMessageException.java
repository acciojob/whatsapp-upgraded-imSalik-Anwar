package com.driver.exception;

public class YouAreNotAllowedToSendMessageException extends RuntimeException{
    public YouAreNotAllowedToSendMessageException(String message){
        super(message);
    }
    YouAreNotAllowedToSendMessageException(){}
}
