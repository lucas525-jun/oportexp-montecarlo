package com.pemex.pep.seemop.Services;

import com.pemex.pep.seemop.impl.MonteCarloDAO;
import com.pemex.pep.seemop.impl.MonteCarloSimulation;
import com.pemex.pep.seemop.impl.MonteCarloSimulationMultiObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MonteCarloService {
    @Autowired
    private MonteCarloDAO monteCarloDAO;

    @Value("${OPORTEXT_ECONOMIC_EVA_HOST:localhost}")
    private String economicEvaHost;

    @Value("${OPORTEXT_ECONOMIC_EVA_PORT:8082}")
    private String economicEvaPort;

    public MonteCarloSimulation createSimulation(String version, int idOportunidadObjetivo, int iterations, boolean iterationCheck, boolean profileCheck, String evaluationId) {
        MonteCarloSimulation monteCarloSimulation = new MonteCarloSimulation(version, idOportunidadObjetivo, iterations, iterationCheck, profileCheck, evaluationId);
        monteCarloSimulation.setMonteCarloDAO(monteCarloDAO);
        monteCarloSimulation.setEconomicEvaHostAndPort(economicEvaHost, economicEvaPort);
        return monteCarloSimulation;
    }

    public MonteCarloSimulationMultiObject createSimulationForMulti(String version, int[] idOportunidadObjetivo, int iterations, boolean iterationCheck, boolean profileCheck, String evaluationId) {
        MonteCarloSimulationMultiObject monteCarloSimulationMultiObject = new MonteCarloSimulationMultiObject(version,
                idOportunidadObjetivo, economicEvaHost, economicEvaPort, iterations, iterationCheck, profileCheck, evaluationId);
        monteCarloSimulationMultiObject.setMonteCarloDAO(monteCarloDAO);
        monteCarloSimulationMultiObject.initializeSimulation();

        return monteCarloSimulationMultiObject;
    }
}
