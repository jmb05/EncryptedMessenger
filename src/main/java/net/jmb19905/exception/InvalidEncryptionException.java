package net.jmb19905.exception;

/**
 * Is thrown when there are issues during Encryption
 */
public class InvalidEncryptionException extends Exception {

    public InvalidEncryptionException(String message) {
        super(message);
    }

}
