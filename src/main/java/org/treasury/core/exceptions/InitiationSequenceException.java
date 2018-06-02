package org.treasury.core.exceptions;

public class InitiationSequenceException extends RuntimeException {
    public InitiationSequenceException() {
        super("Initiating in wrong sequence");
    }
}
