package com.dio.model;

/** Thrown when a move or action violates the game rules; the message is user-facing (pt-BR). */
public class InvalidMoveException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidMoveException(String message) {
        super(message);
    }
}
