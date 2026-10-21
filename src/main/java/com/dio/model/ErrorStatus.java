package com.dio.model;

public enum ErrorStatus {
    COM_ERRO("Com erro"),
    SEM_ERRO("Sem erro");

    private final String label;

    ErrorStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
