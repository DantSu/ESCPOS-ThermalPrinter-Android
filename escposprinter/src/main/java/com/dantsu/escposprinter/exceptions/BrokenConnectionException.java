package com.dantsu.escposprinter.exceptions;

public class BrokenConnectionException extends Exception {
    public BrokenConnectionException(String errorMessage) {
        super(errorMessage);
    }
}
