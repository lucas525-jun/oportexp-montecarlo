package com.pemex.pep.seemop.Models;

public class Oportunidad {

    private int actualIdVersion;
    private int idOportunidadObjetivo; // ID del objetivo de oportunidad
    private String oportunidad;          // Descripción de la oportunidad
    private String planDesarrollo;
    private double pceP10;
    private double pceP90;  // PCE (Probabilidad de Éxito)
    private double area10;
    private double area90;                 // Área asociada a la oportunidad
    private String hidrocarburo;         // Tipo de hidrocarburo
    private String tipoOportunidad;      // Tipo de oportunidad
    private double pg;                   // PG (Probabilidad Geológica)

    private double gastoMINAceite;       // Gasto asociado al tipo de valor MIN Aceite
    private double gastoMPAceite;        // Gasto asociado al tipo de valor MP Aceite
    private double gastoMAXAceite;       // Gasto asociado al tipo de valor MAX Aceite


    private int idhidrocarburo;




    private double primeraDeclinacionMin;
    private double primeraDeclinacionMP;
    private double primeraDeclinacionMAX;

    private double fcAceite;
    private double fcGas;
    private double fcCondensado;

    private double infraestructuraMin, infraestructuraMP, infraestructuraMax;
    private double perforacionMin, perforacionMP, perforacionMax;
    private double terminacionMin, terminacionMP, terminacionMax;
    private double infraestructuraMinDES, infraestructuraMPDES, infraestructuraMaxDES;
    private double perforacionMinDES, perforacionMPDES, perforacionMaxDES;
    private double terminacionMinDES, terminacionMPDES, terminacionMaxDES;

    InversionOportunidad inversionOportunidad;

    // Constructor actualizado
    public Oportunidad(int actualIdVersion,  int idOportunidadObjetivo, String oportunidad, String planDesarrollo,  double pceP10, double pceP90,
                       double area10, double area90, String hidrocarburo, String tipoOportunidad, double pg,
                       double gastoMINAceite, double gastoMPAceite, double gastoMAXAceite,
                       int idhidrocarburo,
                       double primDeclinacionMIN, double primDeclinacionMP, double primDeclinacionMAX,
                       double fcAceite, double fcGas, double fcCondensado,
                       double infraestructuraMin, double infraestructuraMP, double infraestructuraMax,
                       double perforacionMin, double perforacionMP, double perforacionMax,
                       double terminacionMin, double terminacionMP, double terminacionMax,
                       double infraestructuraMinDES, double infraestructuraMPDES, double infraestructuraMaxDES,
                       double perforacionMinDES, double perforacionMPDES, double perforacionMaxDES,
                       double terminacionMinDES, double terminacionMPDES, double terminacionMaxDES,
                       InversionOportunidad inversionOportunidad) {

        this.actualIdVersion = actualIdVersion;
        this.idOportunidadObjetivo = idOportunidadObjetivo;
        this.oportunidad = oportunidad;
        this.planDesarrollo = planDesarrollo;
        this.pceP10 = pceP10;
        this.pceP90 = pceP90;
        this.area10 = area10;
        this.area90 = area90;
        this.hidrocarburo = hidrocarburo;
        this.tipoOportunidad = tipoOportunidad;
        this.pg = pg;
        this.gastoMINAceite = gastoMINAceite;
        this.gastoMPAceite = gastoMPAceite;
        this.gastoMAXAceite = gastoMAXAceite;
        this.primeraDeclinacionMin = primDeclinacionMIN;
        this.primeraDeclinacionMP = primDeclinacionMP;
        this.primeraDeclinacionMAX = primDeclinacionMAX;
        this.idhidrocarburo = idhidrocarburo;
        this.fcAceite = fcAceite;
        this.fcGas = fcGas;
        this.fcCondensado = fcCondensado;

        this.infraestructuraMin = infraestructuraMin;
        this.infraestructuraMP = infraestructuraMP;
        this.infraestructuraMax = infraestructuraMax;
        this.perforacionMin = perforacionMin;
        this.perforacionMP = perforacionMP;
        this.perforacionMax = perforacionMax;
        this.terminacionMin = terminacionMin;
        this.terminacionMP = terminacionMP;
        this.terminacionMax = terminacionMax;

        this.infraestructuraMinDES = infraestructuraMinDES;
        this.infraestructuraMPDES = infraestructuraMPDES;
        this.infraestructuraMaxDES = infraestructuraMaxDES;
        this.perforacionMinDES = perforacionMinDES;
        this.perforacionMPDES = perforacionMPDES;
        this.perforacionMaxDES = perforacionMaxDES;
        this.terminacionMinDES = terminacionMinDES;
        this.terminacionMPDES = terminacionMPDES;
        this.terminacionMaxDES = terminacionMaxDES;

        this.inversionOportunidad = inversionOportunidad;
    }


    public int getIdOportunidadObjetivo() { return idOportunidadObjetivo; }
    public String getOportunidad() { return oportunidad; }
    public String getPlanDesarrollo() { return planDesarrollo; }
    public double getPceP10() { return pceP10; }
    public double getPceP90() { return pceP90; }
    public double getArea10() { return area10; }
    public double getArea90() { return area90; }
    public String getHidrocarburo() { return hidrocarburo; }
    public String getTipoOportunidad() { return tipoOportunidad; }
    public double getPg() { return pg; }
    public double getPrimeraDeclinacionMin() { return primeraDeclinacionMin; }
    public double getPrimeraDeclinacionMP() { return primeraDeclinacionMP; }
    public double getPrimeraDeclinacionMAX() { return primeraDeclinacionMAX; }
    public double getGastoMINAceite() { return gastoMINAceite; }
    public double getGastoMPAceite() { return gastoMPAceite; }
    public double getGastoMAXAceite() { return gastoMAXAceite; }


    public double getIdHidrocarburo() { return idhidrocarburo; }


    public double getFcAceite() { return fcAceite; }
    public double getFcGas() { return fcGas; }
    public double getFcCondensado() { return fcCondensado; }

    // Métodos getter y setter para las nuevas variables (solo un ejemplo para ahorrar espacio)
    public double getInfraestructuraMin() { return infraestructuraMin; }
    public double getInfraestructuraMP() { return infraestructuraMP; }
    public double getInfraestructuraMax() { return infraestructuraMax; }

    public double getPerforacionMin() { return perforacionMin; }
    public double getPerforacionMP() { return perforacionMP; }
    public double getPerforacionMax() { return perforacionMax; }

    public double getTerminacionMin() { return terminacionMin; }
    public double getTerminacionMP() { return terminacionMP; }
    public double getTerminacionMax() { return terminacionMax; }

    public double getInfraestructuraMinDES() { return infraestructuraMinDES; }
    public double getInfraestructuraMPDES() { return infraestructuraMPDES; }
    public double getInfraestructuraMaxDES() { return infraestructuraMaxDES; }

    public double getPerforacionMinDES() { return perforacionMinDES; }
    public double getPerforacionMPDES() { return perforacionMPDES; }
    public double getPerforacionMaxDES() { return perforacionMaxDES; }

    public double getTerminacionMinDES() { return terminacionMinDES; }
    public double getTerminacionMPDES() { return terminacionMPDES; }
    public double getTerminacionMaxDES() { return terminacionMaxDES; }


    public int getActualIdVersion() {return actualIdVersion;}

    public InversionOportunidad getInversionOportunidad(){ return inversionOportunidad; }





    @Override
    public String toString() {
        return "Oportunidad{" +
                "idOportunidadObjetivo=" + idOportunidadObjetivo +
                ", oportunidad='" + oportunidad + '\'' +
                ", pceP10=" + pceP10 +
                ", pceP90=" + pceP90 +
                ", area10=" + area10 +
                ", area90=" + area90 +
                ", hidrocarburo='" + hidrocarburo + '\'' +
                ", tipoOportunidad='" + tipoOportunidad + '\'' +
                ", pg=" + pg +
                ", infraestructuraMin=" + infraestructuraMin +
                ", infraestructuraMP=" + infraestructuraMP +
                ", infraestructuraMax=" + infraestructuraMax +
                ", perforacionMin=" + perforacionMin +
                ", perforacionMP=" + perforacionMP +
                ", perforacionMax=" + perforacionMax +
                ", terminacionMin=" + terminacionMin +
                ", terminacionMP=" + terminacionMP +
                ", terminacionMax=" + terminacionMax +
                ", infraestructuraMinDES=" + infraestructuraMinDES +
                ", infraestructuraMPDES=" + infraestructuraMPDES +
                ", infraestructuraMaxDES=" + infraestructuraMaxDES +
                ", perforacionMinDES=" + perforacionMinDES +
                ", perforacionMPDES=" + perforacionMPDES +
                ", perforacionMaxDES=" + perforacionMaxDES +
                ", terminacionMinDES=" + terminacionMinDES +
                ", terminacionMPDES=" + terminacionMPDES +
                ", terminacionMaxDES=" + terminacionMaxDES +
                '}';
    }
}
