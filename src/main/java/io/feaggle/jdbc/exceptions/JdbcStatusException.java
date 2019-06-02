package io.feaggle.jdbc.exceptions;

public class JdbcStatusException extends RuntimeException {
    public JdbcStatusException(String message) {
        super(message);
    }

    public JdbcStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
