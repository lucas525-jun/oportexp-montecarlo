package com.pemex.pep.seemop.impl;

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.lang.reflect.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SimulacionMicrosMulti {
    @JsonIgnore
    private final RestTemplate restTemplate;

    private String economicEvaHost;
    private String economicEvaPort;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulationParameters {
        private Integer idOportunidad;
        private Integer version;
        private double cuota;
        private double declinada;
        private double pce;
        private double area;
        private double plataformaDesarrollo;
        private double lineaDeDescarga;
        private double estacionCompresion;
        private double ducto;
        private double bateria;
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
    }

    public SimulacionMicrosMulti(RestTemplate restTemplate, String economicEvaHost, String economicEvaPort) {
        this.restTemplate = restTemplate;
        this.economicEvaHost = economicEvaHost;
        this.economicEvaPort = economicEvaPort;

    }

    public Object ejecutarSimulacionMulti(
            ConcurrentHashMap<Integer, SimulacionMicrosMulti.SimulationParameters> opportunityParameters, int productionProfileFlag) {
        List<Object> resultados = new ArrayList<>();
        if (opportunityParameters == null || opportunityParameters.isEmpty()) {
            System.err.println("No simulation parameters provided");
            return resultados;
        }

        try {
            List<List<Object>> parameterList = opportunityParameters.values().stream().map(params -> {
                List<Object> opportunityParamsList = new ArrayList<>();
                for (Field field : params.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        opportunityParamsList.add(field.get(params));
                    } catch (IllegalAccessException e) {
                        System.err.println("Failed to access field: " + field.getName());
                        opportunityParamsList.add(null);
                    }
                }
                return opportunityParamsList;
            }).collect(Collectors.toList());

            return executeWithRetry(parameterList, productionProfileFlag, 3);
        } catch (Exception e) {
            System.err.println("Error in simulation execution: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to execute simulation", e);
        }
    }

    private Object executeWithRetry(List<List<Object>> parameterList, int productionProfileFlag, int retries) throws InterruptedException {
        int attempt = 0;
        int alway = 0;
        while (attempt < retries) {
            if(productionProfileFlag != 1) {
                try {
    
                    return restTemplate.postForObject(
                            "http://" + economicEvaHost + ":" + economicEvaPort + "/api/v1/getEvaluacionEconomicaMulti",
                            parameterList, Object.class);
    
                } catch (Exception e) {
                    attempt++;
                    System.err.println("API call failed (Attempt " + attempt + "/" + retries + "): " + e.getMessage());
    
                    if (attempt == retries) {
                        System.err.println("Max retries reached. Failing request.");
                        throw new RuntimeException("Failed to execute simulation after " + retries + " attempts", e);
                    }
    
                    int delay = (int) Math.pow(2, attempt) * 1000;
                    Thread.sleep(delay);
                }
            } else {
                try {
                    return restTemplate.postForObject(
                            "http://" + economicEvaHost + ":" + economicEvaPort + "/api/v1/getEvaluacionPerfilDeProduccion",
                            parameterList, Object.class);
    
                } catch (Exception e) {
                    attempt++;
                    System.err.println("API call failed (Attempt " + attempt + "/" + retries + "): " + e.getMessage());
    
                    if (attempt == retries) {
                        System.err.println("Max retries reached. Failing request.");
                        throw new RuntimeException("Failed to execute simulation after " + retries + " attempts", e);
                    }
    
                    int delay = (int) Math.pow(2, attempt) * 1000;
                    Thread.sleep(delay);
                }
            }
        }
        return null;
    }
}