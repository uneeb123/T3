package org.treasury.core;

public class InitiationSequenceException extends RuntimeException {
    InitiationSequenceException() {
        super("Initiating in wrong sequence");
    }
}
