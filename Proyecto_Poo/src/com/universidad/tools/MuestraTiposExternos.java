package com.universidad.tools;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Clase de prueba para verificar tipos externos del JDK en el diagrama
 * (UUID, BigDecimal, OffsetDateTime). No forma parte del dominio;
 * existe solo para que el generador tenga qué detectar.
 */
public class MuestraTiposExternos {

    private UUID id;
    private BigDecimal costo;
    private OffsetDateTime momento;
    private String nombre;

    public MuestraTiposExternos() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public OffsetDateTime getMomento() {
        return momento;
    }

    public void setMomento(OffsetDateTime momento) {
        this.momento = momento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
