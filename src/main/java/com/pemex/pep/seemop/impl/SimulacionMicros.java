package com.pemex.pep.seemop.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.web.client.RestTemplate;


@Data
public class SimulacionMicros {
    private String economicEvaHost;
    private String economicEvaPort;

    private Integer idOportunidad;
    private Integer version;
    private double cuota;
    private double declinada;
    private double pce;
    private double area;
    private double arbolesSubmarinos;
    private double manifolds;
    private double risers;
    private double sistemasDeControl;
    private double cubiertaDeProceso;
    private double buquetaqueCompra;
    private double buquetaqueRenta;
    private double gastoTriangular;
    private double declinacion;
    private double recurso;
    private double triangularExploratorioMin;
    private double triangularExploratorioPer;
    private double triangularExploratorioTer;
    private double triangularDESInfra;
    private double triangularDESPer;
    private double triangularDESTer;
    private double triangularInversionArbolesSubmarinos;
    private double triangularInversionManifolds;
    private double triangularInversionRisers;
    private double triangularInversionSistemasDeControl;
    private double triangularInversionCubiertaDeProces;
    private double triangularInversionBuqueTanqueCompra;
    private double triangularInversionBuqueTanqueRenta;
    private double plataformaDesarrollo;
    private double lineaDeDescarga;
    private double estacionCompresion;
    private double ducto;
    private double bateria;

    @JsonIgnore
    private final RestTemplate restTemplate;

    public SimulacionMicros(Integer idOportunidad, Integer version, double cuota, double declinada, double pce,
                            double area,

                            double plataformaDesarrollo, double lineaDeDescarga, double estacionCompresion, double ducto,
                            double bateria,
                            double triangularExploratorioMin, double triangularExploratorioPer, double triangularExploratorioTer,
                            double triangularDESInfra, double triangularDESPer, double triangularDESTer,
                            double triangularInversionArbolesSubmarinos, double triangularInversionManifolds,
                            double triangularInversionRisers,
                            double triangularInversionSistemasDeControl, double triangularInversionCubiertaDeProces,
                            double triangularInversionBuqueTanqueCompra, double triangularInversionBuqueTanqueRenta,
                            RestTemplate restTemplate) {

        this.idOportunidad = idOportunidad;
        this.version = version;
        this.cuota = cuota;
        this.declinada = declinada;
        this.pce = pce;
        this.area = area;

        this.plataformaDesarrollo = plataformaDesarrollo;
        this.lineaDeDescarga = lineaDeDescarga;
        this.estacionCompresion = estacionCompresion;
        this.ducto = ducto;
        this.bateria = bateria;

        this.gastoTriangular = gastoTriangular;
        this.declinacion = declinacion;
        this.recurso = recurso;
        this.triangularExploratorioMin = triangularExploratorioMin;
        this.triangularExploratorioPer = triangularExploratorioPer;
        this.triangularExploratorioTer = triangularExploratorioTer;
        this.triangularDESInfra = triangularDESInfra;
        this.triangularDESPer = triangularDESPer;
        this.triangularDESTer = triangularDESTer;
        this.triangularInversionArbolesSubmarinos = triangularInversionArbolesSubmarinos;
        this.triangularInversionManifolds = triangularInversionManifolds;
        this.triangularInversionRisers = triangularInversionRisers;
        this.triangularInversionSistemasDeControl = triangularInversionSistemasDeControl;
        this.triangularInversionCubiertaDeProces = triangularInversionCubiertaDeProces;
        this.triangularInversionBuqueTanqueCompra = triangularInversionBuqueTanqueCompra;
        this.triangularInversionBuqueTanqueRenta = triangularInversionBuqueTanqueRenta;
        this.restTemplate = restTemplate;
    }

    public Object ejecutarSimulacion(int productionProfileFlag) {
        try {

            String url = String.format(
                    "http://" + economicEvaHost + ":" + economicEvaPort +
                    "/api/v1/getEvaluacionEconomica/%s/version/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s/%s",
                    idOportunidad, version, cuota, declinada, pce, area, plataformaDesarrollo, lineaDeDescarga,
                    estacionCompresion,
                    ducto, bateria, triangularExploratorioMin, triangularExploratorioPer, triangularExploratorioTer,
                    triangularDESInfra, triangularDESPer,
                    triangularDESTer, triangularInversionArbolesSubmarinos, triangularInversionManifolds,
                    triangularInversionRisers, triangularInversionSistemasDeControl,
                    triangularInversionCubiertaDeProces,
                    triangularInversionBuqueTanqueCompra, triangularInversionBuqueTanqueRenta, productionProfileFlag);

            
            Object response = restTemplate.getForObject(url, Object.class);

            return response;
        } catch (Exception e) {
            System.out.println("Error al ejecutar la simulación: " + e.getMessage());
            return null; // O maneja el error según sea necesario
        }
    }

}
