package com.dio.model;

public enum GameStatus {
    NAO_INICIADO("Não iniciado"),
    INCOMPLETO("Incompleto"),
    COMPLETO("Completo");

    private final String label;

    GameStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
