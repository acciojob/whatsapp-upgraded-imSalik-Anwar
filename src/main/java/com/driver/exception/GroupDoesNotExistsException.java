package com.driver.exception;

public class GroupDoesNotExistsException extends RuntimeException{
    public GroupDoesNotExistsException(String message){
        super(message);
    }
    public GroupDoesNotExistsException(){}
}
