package com.pemex.pep.seemop.Models;

public class CtoAnualResultado {

    private int anio;
    private double ctoAnual;


    public CtoAnualResultado(int anio, double ctoAnual) {
        this.anio = anio;
        this.ctoAnual = ctoAnual;
    }


    public double getCtoAnual() {
        return ctoAnual;
    }

    public void setCtoAnual(double ctoAnual) {
        this.ctoAnual = ctoAnual;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }



}
