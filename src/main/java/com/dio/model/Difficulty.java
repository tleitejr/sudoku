package com.dio.model;

public enum Difficulty {
    FACIL("Fácil"),
    MEDIO("Médio"),
    DIFICIL("Difícil");

    private final String label;

    Difficulty(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
