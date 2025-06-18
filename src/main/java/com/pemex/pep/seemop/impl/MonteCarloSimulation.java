package com.pemex.pep.seemop.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.pemex.pep.seemop.Models.Oportunidad;
import com.pemex.pep.seemop.Models.ResultadoSimulacion;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import com.fasterxml.jackson.databind.ObjectMapper;


@Slf4j
public class MonteCarloSimulation {
    @Setter
    private MonteCarloDAO monteCarloDAO;
    private String economicEvaHost;
    private String economicEvaPort;
    private final String version;
    private final int iterations;
    private final int pgValue;
    private final int idOportunidadObjetivo;
    private final String evaluationId;
    private Integer iterationCheck;

    private Oportunidad oportunidad;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    private Sheet pceSheet;
    private Sheet aceiteSheet;
    private Sheet gasSheet;
    private Sheet condensadoSheet;  

    private List<Map<String, Object>> pceSheetData = Collections.synchronizedList(new ArrayList<>());
    private List<Map<String, Object>> aceiteSheetData = Collections.synchronizedList(new ArrayList<>());
    private List<Map<String, Object>> gasSheetData = Collections.synchronizedList(new ArrayList<>());
    private List<Map<String, Object>> condensadoSheetData = Collections.synchronizedList(new ArrayList<>());


    private final AtomicReference<Double> grandTotalPCE = new AtomicReference<>(0.0);
    private final AtomicReference<Double> grandTotalAceite = new AtomicReference<>(0.0);
    private final AtomicReference<Double> grandTotalGas = new AtomicReference<>(0.0);
    private final AtomicReference<Double> grandTotalCondensado = new AtomicReference<>(0.0);

    public void setOportunidad(Oportunidad oportunidad) {
        this.oportunidad = oportunidad;
    }

    public MonteCarloSimulation(String version, int idOportunidadObjetivo, int iterations, boolean iterationCheck, boolean profileCheck, String evaluationId) {
        this.version = version;
        this.idOportunidadObjetivo = idOportunidadObjetivo;
        this.iterations = iterations;
        this.iterationCheck = iterationCheck ? 1 : 0;
        this.pgValue = profileCheck == true ? 1 : 0;
        this.evaluationId = evaluationId;
    }

    public void setEconomicEvaHostAndPort(String host, String port) {
        this.economicEvaHost = host;
        this.economicEvaPort = port;
    }

    double triangularInversionBat, triangularInversionPlataforma, triangularInversionLineaDescarga;
    double triangularInversionEstacionCompresion, triangularInversionDucto, triangularInversionArbolesSubmarinos;
    double triangularInversionManifolds, triangularInversionRisers, triangularInversionSistemasDeControl;
    double triangularInversionCubiertaDeProces, triangularInversionBuqueTanqueCompra,
            triangularInversionBuqueTanqueRenta;

    public String runSimulation() {
        Oportunidad oportunidad = monteCarloDAO.executeQuery(version, idOportunidadObjetivo);
        setOportunidad(oportunidad);

        int cantidadIteraciones = iterations;

        int indexAleatorioRecurso = 0;
        int indexAleatorioDeclinacion = 2;
        int indexAleatorioDesInfra = 4;
        int indexAleatorioDesPer = 5;
        int indexAleatorioDesTer = 6;
        int indexBateriaMin = 7;
        int indexAleatorioInverision = 8;
        int indexAleatorioLineadedescarga = 9;
        int indexAleatorioEstacioncompresion = 10;
        int indexAleatorioDucto = 11;
        int indexAleatorioArbolessubmarinos = 12;
        int indexAleatorioManifolds = 13;
        int indexAleatorioRisers = 14;
        int indexAleatorioSistemasdecontrol = 15;
        int indexAleatorioCubiertadeproces = 16;
        int indexAleatorioBuquetanquecompra = 17;
        int indexAleatorioBuquetanquerenta = 18;

        int numberOfVariables = 23; 
        List<Double> randomNumbers = IntStream.range(0, cantidadIteraciones * numberOfVariables)
                .mapToDouble(i -> ThreadLocalRandom.current().nextDouble())
                .boxed()
                .collect(Collectors.toList());

        double mediaTruncada = monteCarloDAO.getMediaTruncada(version, idOportunidadObjetivo);
        double kilometraje = monteCarloDAO.getKilometraje(version, idOportunidadObjetivo);

        List<Object> resultados = Collections.synchronizedList(new ArrayList<>());
        Map<Double, Integer> limitesEconomicosRepetidos = new ConcurrentHashMap<>();

        Workbook productionWorkbook = new XSSFWorkbook();
        
        pceSheet = productionWorkbook.createSheet("PCE");
        aceiteSheet = productionWorkbook.createSheet("Aceite");
        gasSheet = productionWorkbook.createSheet("Gas");
        condensadoSheet = productionWorkbook.createSheet("Condensado");

        Font font = productionWorkbook.createFont();
        font.setFontHeightInPoints((short) 16); 
        font.setBold(true); 
        CellStyle titleStyle = productionWorkbook.createCellStyle();
        titleStyle.setFont(font);


        Row titleRowOfPce = pceSheet.createRow(0);
        titleRowOfPce.setHeight((short) 600);
        Cell titleCellOfPce = titleRowOfPce.createCell(2);
        titleCellOfPce.setCellValue("Producción diaria promedio de Petróleo Crudo Equivalente (mbpced)");
        titleCellOfPce.setCellStyle(titleStyle);

        Row titleRowOfAceite = aceiteSheet.createRow(0);
        titleRowOfAceite.setHeight((short) 600);
        Cell titleCellOfAceite = titleRowOfAceite.createCell(2);
        titleCellOfAceite.setCellValue("Producción diaria promedio de Aceite (mbd)");
        titleCellOfAceite.setCellStyle(titleStyle);

        Row titleRowOfGas = gasSheet.createRow(0);
        titleRowOfGas.setHeight((short) 600);
        Cell titleCellOfGas = titleRowOfGas.createCell(2);
        titleCellOfGas.setCellValue("Producción diaria promedio de Gas (mmpcd)");
        titleCellOfGas.setCellStyle(titleStyle);

        Row titleRowOfCondensado = condensadoSheet.createRow(0);
        titleRowOfCondensado.setHeight((short) 600);
        Cell titleCellOfCondensado = titleRowOfCondensado.createCell(2);
        titleCellOfCondensado.setCellValue("Producción diaria promedio de Condensado (mbd)");
        titleCellOfCondensado.setCellStyle(titleStyle);


        // Crear libro y hoja en Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Simulación Monte Carlo");

        // Crear encabezado con columnas adicionales para "Años" y "CtoAnuales"
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Iteración", "Resultado", "Aleatorio PCE", "PCE",
                "Aleatorio Gasto", "Gasto Inicial (mbpce)",
                "Aleatorio Declinación", "Declinación",
                "Aleatorio Área", "Área", "E.I.", "E.P", "E.T", "D.I", "D.P", "D.T", "Bateria", "Plataforma Desarrollo",
                "Linea Descarga", "E.comprension ", "ducto", "Arboles submarinos", "manifols", "risers",
                "Sistemas de control", "Cubierta Proces", "Buquetaquecompra", "buquetaQuerenta" // Añadido aquí
        };

        IndexedColors[] colors = {
                IndexedColors.LIGHT_BLUE, IndexedColors.GREY_25_PERCENT,
                IndexedColors.LIGHT_GREEN, IndexedColors.LIGHT_YELLOW,
                IndexedColors.LIGHT_ORANGE, IndexedColors.LIGHT_CORNFLOWER_BLUE,
                IndexedColors.LIGHT_TURQUOISE, IndexedColors.BROWN, IndexedColors.GOLD
        };

        for (int i = 0; i < headers.length; i++) {
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(colors[i % colors.length].getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000); 
        }
        
        // Calculate exploratory values once, outside the loop
        double aleatorioExploratorioInfra = random.nextDouble();
        double triangularExploratorioMin = triangularDistributionSinPCE(
                oportunidad.getInfraestructuraMin(),
                oportunidad.getInfraestructuraMax(),
                oportunidad.getInfraestructuraMP(),
                aleatorioExploratorioInfra);

        double aleatorioExploratorioPerforacion = random.nextDouble();
        double triangularExploratorioPer = triangularDistributionSinPCE(
                oportunidad.getPerforacionMin(),
                oportunidad.getPerforacionMax(),
                oportunidad.getPerforacionMP(),
                aleatorioExploratorioPerforacion);

        double aleatorioExploratorioTerminacion = random.nextDouble();
        double triangularExploratorioTer = triangularDistributionSinPCE(
                oportunidad.getTerminacionMin(),
                oportunidad.getTerminacionMax(),
                oportunidad.getTerminacionMP(),
                aleatorioExploratorioTerminacion);

        // ForkJoinPool customThreadPool = new ForkJoinPool(24);
        int optimalThreads = Runtime.getRuntime().availableProcessors() * 2;

        ForkJoinPool customThreadPool = new ForkJoinPool(optimalThreads);

        Queue<Object> resultadosQueue = new ConcurrentLinkedQueue<>();

        List<Map<Integer, Object>> excelRowBuffer = Collections.synchronizedList(new ArrayList<>());

        try {
            customThreadPool.submit(() -> IntStream.range(0, cantidadIteraciones).parallel().forEach(i -> {
                int baseIndex = i * numberOfVariables;

                Map<Integer, Object> excelRowData = new HashMap<>();

                ResultadoSimulacion ResultadoSimulacion = new ResultadoSimulacion();
                excelRowData.put(0, i + 1); 
                Object resultado;
                if (pruebaGeologica()) {
                    excelRowData.put(1, "Éxito");
                    double aleatorioRecurso = 0.01 + (0.99 - 0.01) * randomNumbers.get(baseIndex + indexAleatorioRecurso);

                    double recurso = calcularRecursoProspectivo(aleatorioRecurso, oportunidad.getPceP10(),
                            oportunidad.getPceP90());
                    excelRowData.put(2, aleatorioRecurso);
                    excelRowData.put(3, recurso);
                    ResultadoSimulacion.setPce(recurso);

                    double limiteEconomico = calcularLimiteEconomico(recurso) / 12;
                    limitesEconomicosRepetidos.merge(limiteEconomico, 1, Integer::sum);

                    double gastoTriangular = calcularGastoInicial(recurso, aleatorioRecurso);
                    excelRowData.put(4, aleatorioRecurso);
                    excelRowData.put(5, gastoTriangular);
                    ResultadoSimulacion.setGastoInicial(gastoTriangular);

                    double aleatorioDeclinacion = randomNumbers.get(baseIndex + indexAleatorioDeclinacion);

                    double declinacion = triangularDistribution(recurso, oportunidad.getPrimeraDeclinacionMin(),
                            oportunidad.getPrimeraDeclinacionMAX(),
                            oportunidad.getPrimeraDeclinacionMP(),
                            aleatorioDeclinacion);
                    excelRowData.put(6, aleatorioDeclinacion);
                    excelRowData.put(7, declinacion);

                    ResultadoSimulacion.setDeclinacion(declinacion);

                    double area = calcularRecursoProspectivo(aleatorioRecurso, oportunidad.getArea10(),
                            oportunidad.getArea90());
                    excelRowData.put(8, aleatorioRecurso);
                    excelRowData.put(9, area);

                    ResultadoSimulacion.setArea(area);

                    excelRowData.put(10, triangularExploratorioMin);
                    ResultadoSimulacion.setExploratoriaInfra(triangularExploratorioMin);

                    excelRowData.put(11, triangularExploratorioPer);
                    ResultadoSimulacion.setExploratoriaPerf(triangularExploratorioPer);

                    excelRowData.put(12, triangularExploratorioTer);
                    ResultadoSimulacion.setExploratoriaTer(triangularExploratorioTer);

                    double aleatorioDesInfra = randomNumbers.get(baseIndex + indexAleatorioDesInfra);

                    double triangularDESInfra = triangularDistribution(recurso, oportunidad.getInfraestructuraMinDES(),
                            oportunidad.getInfraestructuraMaxDES(),
                            oportunidad.getInfraestructuraMPDES(),
                            aleatorioDesInfra);
                    excelRowData.put(13, triangularDESInfra);
                    ResultadoSimulacion.setDesarrolloInfra(triangularDESInfra);

                    double aleatorioDesPer = randomNumbers.get(baseIndex + indexAleatorioDesPer);
                    double triangularDESPer = triangularDistribution(recurso, oportunidad.getPerforacionMinDES(),
                            oportunidad.getPerforacionMaxDES(),
                            oportunidad.getPerforacionMPDES(),
                            aleatorioDesPer);
                    excelRowData.put(14, triangularDESPer);
                    ResultadoSimulacion.setDesarrolloPerf(triangularDESPer);

                    double aleatorioDesTer = randomNumbers.get(baseIndex + indexAleatorioDesTer);

                    double triangularDESTer = triangularDistribution(recurso, oportunidad.getTerminacionMinDES(),
                            oportunidad.getTerminacionMaxDES(),
                            oportunidad.getTerminacionMPDES(),
                            aleatorioDesTer);
                    excelRowData.put(15, triangularDESTer);
                    ResultadoSimulacion.setDesarrolloTer(triangularDESTer);

                    if (oportunidad.getInversionOportunidad().getBateriaMin() != 0) {
                        double AleatorioInverision = randomNumbers.get(baseIndex + indexBateriaMin);

                        triangularInversionBat = triangularDistribution(recurso,
                                oportunidad.getInversionOportunidad().getBateriaMin(),
                                oportunidad.getInversionOportunidad().getBateriaMax(),
                                oportunidad.getInversionOportunidad().getBateriaMp(),
                                AleatorioInverision);
                        excelRowData.put(16, triangularInversionBat);
                        ResultadoSimulacion.setBateria(triangularInversionBat);
                    }

                    if (oportunidad.getInversionOportunidad().getPlataformadesarrolloMin() != 0) {
                        double AleatorioInverision = randomNumbers.get(baseIndex + indexAleatorioInverision);

                        triangularInversionPlataforma = triangularDistribution(recurso,
                                oportunidad.getInversionOportunidad().getPlataformadesarrolloMin(),
                                oportunidad.getInversionOportunidad().getPlataformadesarrolloMax(),
                                oportunidad.getInversionOportunidad().getPlataformadesarrolloMp(),
                                AleatorioInverision);
                        excelRowData.put(17, triangularInversionPlataforma);
                        ResultadoSimulacion.setPlataformaDesarrollo(triangularInversionPlataforma);
                    }

                    if (oportunidad.getInversionOportunidad().getLineadedescargaMin() != 0) {
                        double AleatorioInverision = randomNumbers.get(baseIndex + indexAleatorioLineadedescarga);

                        triangularInversionLineaDescarga = triangularDistribution(recurso,
                                oportunidad.getInversionOportunidad().getLineadedescargaMin(),
                                oportunidad.getInversionOportunidad().getLineadedescargaMax(),
                                oportunidad.getInversionOportunidad().getLineadedescargaMp(),
                                AleatorioInverision);
                        excelRowData.put(18, triangularInversionLineaDescarga);
                        ResultadoSimulacion.setLineaDescarga(triangularInversionLineaDescarga);
                    }

                    if (oportunidad.getInversionOportunidad().getEstacioncompresionMin() != 0) {
                        double AleatorioInverision = randomNumbers.get(baseIndex + indexAleatorioEstacioncompresion);

                        triangularInversionEstacionCompresion = triangularDistribution(recurso,
                                oportunidad.getInversionOportunidad().getEstacioncompresionMin(),
                                oportunidad.getInversionOportunidad().getEstacioncompresionMax(),
                                oportunidad.getInversionOportunidad().getEstacioncompresionMp(),
                                AleatorioInverision);
                        excelRowData.put(19, triangularInversionEstacionCompresion);
                        ResultadoSimulacion.setEComprension(triangularInversionEstacionCompresion);
                    }

                    if (oportunidad.getInversionOportunidad().getDuctoMin() != 0) {
                        double AleatorioInverision = randomNumbers.get(baseIndex + indexAleatorioDucto);

                        triangularInversionDucto = triangularDistribution(recurso,
                                oportunidad.getInversionOportunidad().getDuctoMin(),
                                oportunidad.getInversionOportunidad().getDuctoMax(),
                                oportunidad.getInversionOportunidad().getDuctoMp(),
                                AleatorioInverision);
                        excelRowData.put(20, triangularInversionDucto);
                        ResultadoSimulacion.setDucto(triangularInversionDucto);
                    }

                    if (oportunidad.getInversionOportunidad().getArbolessubmarinosMin() != 0) {
                        double AleatorioInversion = randomNumbers.get(baseIndex + indexAleatorioArbolessubmarinos);

                        triangularInversionArbolesSubmarinos = triangularDistribution(recurso,
                                oportunidad.getInversionOportunidad().getArbolessubmarinosMin(),
                                oportunidad.getInversionOportunidad().getArbolessubmarinosMax(),
                                oportunidad.getInversionOportunidad().getArbolessubmarinosMp(),
                                AleatorioInversion);
                        excelRowData.put(21, triangularInversionArbolesSubmarinos);
                        ResultadoSimulacion.setArbolesSubmarinos(triangularInversionArbolesSubmarinos);
                    }

                    if (oportunidad.getInversionOportunidad().getManifoldsMin() != 0) {
                        double AleatorioInversion = randomNumbers.get(baseIndex + indexAleatorioManifolds);

                        triangularInversionManifolds = triangularDistribution(recurso,
                                oportunidad.getInversionOportunidad().getManifoldsMin(),
                                oportunidad.getInversionOportunidad().getManifoldsMax(),
                                oportunidad.getInversionOportunidad().getManifoldsMp(),
                                AleatorioInversion);
                        excelRowData.put(22, triangularInversionManifolds);
                        ResultadoSimulacion.setManifolds(triangularInversionManifolds);
                    }

                    if (oportunidad.getInversionOportunidad().getRisersMin() != 0) {
                        double AleatorioInversion = randomNumbers.get(baseIndex + indexAleatorioRisers);

                        triangularInversionRisers = triangularDistribution(recurso,
                                oportunidad.getInversionOportunidad().getRisersMin(),
                                oportunidad.getInversionOportunidad().getRisersMax(),
                                oportunidad.getInversionOportunidad().getRisersMp(),
                                AleatorioInversion);
                        excelRowData.put(23, triangularInversionRisers);
                        ResultadoSimulacion.setRisers(triangularInversionRisers);
                    }

                    if (oportunidad.getInversionOportunidad().getSistemasdecontrolMin() != 0) {
                        double AleatorioInversion = randomNumbers.get(baseIndex + indexAleatorioSistemasdecontrol);

                        triangularInversionSistemasDeControl = triangularDistribution(recurso,
                                oportunidad.getInversionOportunidad().getSistemasdecontrolMin(),
                                oportunidad.getInversionOportunidad().getSistemasdecontrolMax(),
                                oportunidad.getInversionOportunidad().getSistemasdecontrolMp(),
                                AleatorioInversion);
                        excelRowData.put(24, triangularInversionSistemasDeControl);
                        ResultadoSimulacion.setSistemasDeControl(triangularInversionSistemasDeControl);
                    }

                    if (oportunidad.getInversionOportunidad().getCubiertadeprocesMin() != 0) {
                        double AleatorioInversion = randomNumbers.get(baseIndex + indexAleatorioCubiertadeproces);

                        triangularInversionCubiertaDeProces = triangularDistribution(recurso,
                                oportunidad.getInversionOportunidad().getCubiertadeprocesMin(),
                                oportunidad.getInversionOportunidad().getCubiertadeprocesMax(),
                                oportunidad.getInversionOportunidad().getCubiertadeprocesMp(),
                                AleatorioInversion);
                        excelRowData.put(25, triangularInversionCubiertaDeProces);
                        ResultadoSimulacion.setCubiertaDeProces(triangularInversionCubiertaDeProces);
                    }

                    if (oportunidad.getInversionOportunidad().getBuquetanquecompraMin() != 0) {
                        double AleatorioInversion = randomNumbers.get(baseIndex + indexAleatorioBuquetanquecompra);

                        triangularInversionBuqueTanqueCompra = triangularDistribution(recurso,
                                oportunidad.getInversionOportunidad().getBuquetanquecompraMin(),
                                oportunidad.getInversionOportunidad().getBuquetanquecompraMax(),
                                oportunidad.getInversionOportunidad().getBuquetanquecompraMp(),
                                AleatorioInversion);
                        excelRowData.put(26, triangularInversionBuqueTanqueCompra);
                        ResultadoSimulacion.setBuqueTanqueCompra(triangularInversionBuqueTanqueCompra);
                    }

                    if (oportunidad.getInversionOportunidad().getBuquetanquerentaMin() != 0) {
                        double AleatorioInversion = randomNumbers.get(baseIndex + indexAleatorioBuquetanquerenta);

                        triangularInversionBuqueTanqueRenta = triangularDistribution(recurso,
                                oportunidad.getInversionOportunidad().getBuquetanquerentaMin(),
                                oportunidad.getInversionOportunidad().getBuquetanquerentaMax(),
                                oportunidad.getInversionOportunidad().getBuquetanquerentaMp(),
                                AleatorioInversion);
                        excelRowData.put(27, triangularInversionBuqueTanqueRenta);
                        ResultadoSimulacion.setBuqueTanqueRenta(triangularInversionBuqueTanqueRenta);
                    }

                    RestTemplate restTemplate = new RestTemplate();

                    SimulacionMicros simulacionMicros = new SimulacionMicros(idOportunidadObjetivo,
                            oportunidad.getActualIdVersion(), gastoTriangular, declinacion, recurso, area,

                            triangularInversionPlataforma, triangularInversionLineaDescarga,
                            triangularInversionEstacionCompresion, triangularInversionDucto,
                            triangularInversionBat,

                            triangularExploratorioMin, triangularExploratorioPer, triangularExploratorioTer,
                            triangularDESInfra, triangularDESPer, triangularDESTer,
                            triangularInversionArbolesSubmarinos,
                            triangularInversionManifolds, triangularInversionRisers,
                            triangularInversionSistemasDeControl, triangularInversionCubiertaDeProces,
                            triangularInversionBuqueTanqueCompra, triangularInversionBuqueTanqueRenta, restTemplate);

                    // Set host and port for Economic Eva service
                    simulacionMicros.setEconomicEvaHost(economicEvaHost);
                    simulacionMicros.setEconomicEvaPort(economicEvaPort);

                    resultado = simulacionMicros.ejecutarSimulacion(this.pgValue);

                    resultadosQueue.add(resultado);
                } else {

                    excelRowData.put(1, "Fracaso");
                    for (int j = 2; j < headers.length; j++) {
                        excelRowData.put(j, 0);

                    }

                    excelRowData.put(10, triangularExploratorioMin);
                    ResultadoSimulacion.setExploratoriaInfra(triangularExploratorioMin);

                    excelRowData.put(11, triangularExploratorioPer);
                    ResultadoSimulacion.setExploratoriaPerf(triangularExploratorioPer);

                    excelRowData.put(12, triangularExploratorioTer);
                    ResultadoSimulacion.setExploratoriaTer(triangularExploratorioTer);

                    limitesEconomicosRepetidos.merge(0.0, 1, Integer::sum);

                    RestTemplate restTemplate = new RestTemplate();

                    SimulacionMicros simulacionMicros = new SimulacionMicros(idOportunidadObjetivo,
                            oportunidad.getActualIdVersion(), 0, 0, 0, 0,

                            0, 0, 0, 0,
                            0,
                            triangularExploratorioMin, triangularExploratorioPer, triangularExploratorioTer,
                            0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, restTemplate);

                    simulacionMicros.setEconomicEvaHost(economicEvaHost);
                    simulacionMicros.setEconomicEvaPort(economicEvaPort);
                    resultado = simulacionMicros.ejecutarSimulacion(this.pgValue);

                    resultadosQueue.add(resultado);
                }
                final int iteration = i;

                if(this.iterationCheck == 1) {
                    excelRowBuffer.add(excelRowData);
                }

                List<Map<String, Object>> resultsList;
                if (resultado instanceof Map) {
                    resultsList = Collections.singletonList((Map<String, Object>) resultado);
                } else if (resultado instanceof List) {
                    resultsList = (List<Map<String, Object>>) resultado;
                } else {
                    System.err.println("Unexpected result type: " + 
                        (resultado != null ? resultado.getClass().getName() : "null"));
                    resultsList = Collections.emptyList();
                }

                if (!resultsList.isEmpty()) {
                    Map<String, Object> eachEvaluationResult = resultsList.get(0);
                    
                    // Extract start and end year from the evaluation result
                    int[] yearRange = extractYearRange(eachEvaluationResult);
                    int startYear = yearRange[0]; 
                    int endYear = yearRange[1];   
                    
                    synchronized (pceSheet) {
                        createSheetHeaders(pceSheet, startYear, endYear, productionWorkbook);
                    }
                    synchronized (aceiteSheet) {
                        createSheetHeaders(aceiteSheet, startYear, endYear, productionWorkbook);
                    }
                    synchronized (gasSheet) {
                        createSheetHeaders(gasSheet, startYear, endYear, productionWorkbook);
                    }
                    synchronized (condensadoSheet) {
                        createSheetHeaders(condensadoSheet, startYear, endYear, productionWorkbook);
                    }
                
                    synchronized (this) {
                        writeRowDataPerSheet(resultado, iteration, iteration+2, productionWorkbook);
                    }
                } else {
                    System.err.println("No evaluation results found for iteration " + iteration);
                }



            })).join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            customThreadPool.shutdown();
            try {
                if (!customThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    customThreadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                customThreadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        synchronized (pceSheet) {
            Row row = pceSheet.getRow(0);
            if (row == null) row = pceSheet.createRow(0);
            Cell cell = row.createCell(1);
            cell.setCellValue(grandTotalPCE.get() / cantidadIteraciones);
        }
        
        synchronized (aceiteSheet) {
            Row row = aceiteSheet.getRow(0);
            if (row == null) row = aceiteSheet.createRow(0);
            Cell cell = row.createCell(1);
            cell.setCellValue(grandTotalAceite.get() / cantidadIteraciones);
        }
        
        synchronized (gasSheet) {
            Row row = gasSheet.getRow(0);
            if (row == null) row = gasSheet.createRow(0);
            Cell cell = row.createCell(1);
            cell.setCellValue(grandTotalGas.get() / cantidadIteraciones);
        }
        
        synchronized (condensadoSheet) {
            Row row = condensadoSheet.getRow(0);
            if (row == null) row = condensadoSheet.createRow(0);
            Cell cell = row.createCell(1);
            cell.setCellValue(grandTotalCondensado.get() / cantidadIteraciones);
        }

        List<Double> reporte804 = new ArrayList<>();
        reporte804.add(kilometraje);
        List<Double> reporte805 = escaleraLimEconomicos(limitesEconomicosRepetidos, mediaTruncada, cantidadIteraciones);


        resultados = new ArrayList<>(resultadosQueue);
        if (!resultados.isEmpty() && resultados.get(0) instanceof Map) {
            Map<String, Object> primerElemento = (Map<String, Object>) resultados.get(0);
            primerElemento.put("reporte804", reporte804);
            primerElemento.put("reporte805", reporte805);
        }
        
        
        if(this.pgValue == 1) {
            try (FileOutputStream fileOut = new FileOutputStream(
                evaluationId + "_Perfiles de producción_" + oportunidad.getOportunidad().replaceAll("[^a-zA-Z0-9.-]", "") + "_" + oportunidad.getIdOportunidadObjetivo() + ".xlsx")) {
                        productionWorkbook.write(fileOut);
    
            } catch (IOException e) {
                System.err.println("Error writing Excel file: " + e.getMessage());
            } finally {
                try {
                    productionWorkbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            
            if(this.iterationCheck == 1) {

                Collections.sort(excelRowBuffer, Comparator.comparingInt(row -> ((Number) row.get(0)).intValue()));
                excelRowBuffer.forEach(row -> writeResultsToExcel(sheet, row));
                try (
                    FileOutputStream fileOut = new FileOutputStream(
                            evaluationId + "_SimulacionMonteCarlo_" + oportunidad.getOportunidad().replaceAll("[^a-zA-Z0-9.-]", "") + "_" + oportunidad.getIdOportunidadObjetivo() + ".xlsx")) {
                    workbook.write(fileOut);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        workbook.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        List<Object> filteredResultados = resultados.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        String sharedFolder = "/evaluation-results/";
        String jsonFileName = "result_json_" + evaluationId + "_" + 
                      oportunidad.getOportunidad().replaceAll("[^a-zA-Z0-9.-]", "") + "_" + idOportunidadObjetivo + "_" + 
                      Thread.currentThread().getName() + ".json";
        String jsonFilePath = sharedFolder + jsonFileName;
        
        saveJsonFile(filteredResultados, jsonFilePath);
        resultadosQueue.clear();
        excelRowBuffer.clear();
        resultados.clear();
        resultados = null;
       
        return jsonFileName;

    }
    
    private void saveJsonFile(List<Object> data, String fileName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            objectMapper.writeValue(fos, data);
        } catch (IOException e) {
            System.err.println("Error saving JSON file: " + e.getMessage());
        }
    }

    private int[] extractYearRange(Map<String, Object> evaluationResult) {
        int[] yearRange = new int[2];
        
        // Default values in case extraction fails
        
        try {
            Map<String, Object> infoOportunidad = (Map<String, Object>) evaluationResult.get("infoOportunidad");
            if (infoOportunidad != null && infoOportunidad.get("fechainicioperfexploratorio") != null) {
                String startYearStr = (String) infoOportunidad.get("fechainicioperfexploratorio");
                yearRange[0] = Integer.parseInt(startYearStr);
            }
            
            // Find the maximum year in evaluacionEconomica
            List<Map<String, Object>> evaluaciones = (List<Map<String, Object>>) evaluationResult.get("evaluacionEconomica");
            if (evaluaciones != null && !evaluaciones.isEmpty()) {
                int maxYear = yearRange[0]; // Initialize with start year
                
                for (Map<String, Object> evaluacion : evaluaciones) {
                    String yearStr = (String) evaluacion.get("anio");
                    if (yearStr != null) {
                        try {
                            int year = Integer.parseInt(yearStr);
                            if (year > maxYear) {
                                maxYear = year;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing year: " + yearStr);
                        }
                    }
                }
                
                yearRange[1] = maxYear;
            }
        } catch (Exception e) {
            System.err.println("Error extracting year range: " + e.getMessage());
        }
        
        return yearRange;
    }
 
    private void writeRowDataPerSheet(Object eachResultado, int finalI, int iterationNumber, Workbook productionWorkbook) {
        if (!(eachResultado instanceof Map)) {
            return;
        }
        
        Map<String, Object> resultMap = (Map<String, Object>) eachResultado;
        List<Map<String, Object>> evaluacionEconomica = (List<Map<String, Object>>) resultMap.get("evaluacionEconomica");
        
        if (evaluacionEconomica == null) {
            return;
        }
        
        // Use these for accumulating totals for this iteration
        double pceTotal = 0.0;
        double aceiteTotal = 0.0;
        double gasTotal = 0.0;
        double condensadoTotal = 0.0;
    
        // Create map to collect all year data
        Map<String, Double> pceYearValues = new HashMap<>();
        Map<String, Double> aceiteYearValues = new HashMap<>();
        Map<String, Double> gasYearValues = new HashMap<>();
        Map<String, Double> condensadoYearValues = new HashMap<>();
        
        // Extract start and end year from the evaluation result for consistent column alignment
        int[] yearRange = extractYearRange(resultMap);
        int startYear = yearRange[0]; 
        int endYear = yearRange[1];
        
        // Process each year's data
        for (Map<String, Object> evaluacion : evaluacionEconomica) {
            String anio = (String) evaluacion.get("anio");
            Map<String, Object> produccionDiariaPromedio = (Map<String, Object>) evaluacion.get("produccionDiariaPromedio");
            
            if (produccionDiariaPromedio != null) {
                double mbpce = produccionDiariaPromedio.get("mbpce") != null ? 
                    ((Number) produccionDiariaPromedio.get("mbpce")).doubleValue() : 0.0;
                double aceiteTotalValue = produccionDiariaPromedio.get("aceiteTotal") != null ? 
                    ((Number) produccionDiariaPromedio.get("aceiteTotal")).doubleValue() : 0.0;
                double gasTotalValue = produccionDiariaPromedio.get("gasTotal") != null ? 
                    ((Number) produccionDiariaPromedio.get("gasTotal")).doubleValue() : 0.0;
                double condensado = produccionDiariaPromedio.get("condensado") != null ? 
                    ((Number) produccionDiariaPromedio.get("condensado")).doubleValue() : 0.0;
                
                // Keep running sum of all values
                pceTotal += mbpce;
                aceiteTotal += aceiteTotalValue;
                gasTotal += gasTotalValue;
                condensadoTotal += condensado;
    
                // Store year values
                pceYearValues.put(anio, mbpce);
                aceiteYearValues.put(anio, aceiteTotalValue);
                gasYearValues.put(anio, gasTotalValue);
                condensadoYearValues.put(anio, condensado);
            }
        }
    
        final double pceTotalFinal = pceTotal;
        final double aceiteTotalFinal = aceiteTotal;
        final double gasTotalFinal = gasTotal;
        final double condensadoTotalFinal = condensadoTotal;
        
        grandTotalPCE.updateAndGet(currentTotal -> currentTotal + pceTotalFinal);
        grandTotalAceite.updateAndGet(currentTotal -> currentTotal + aceiteTotalFinal);
        grandTotalGas.updateAndGet(currentTotal -> currentTotal + gasTotalFinal);
        grandTotalCondensado.updateAndGet(currentTotal -> currentTotal + condensadoTotalFinal);
        
        // Make sure headers are created with the correct year range
        synchronized (pceSheet) {
            createSheetHeaders(pceSheet, startYear, endYear, productionWorkbook);
            writeRowToSheet(pceSheet, iterationNumber, pceTotal, pceYearValues, startYear, endYear);
        }
        
        synchronized (aceiteSheet) {
            createSheetHeaders(aceiteSheet, startYear, endYear, productionWorkbook);
            writeRowToSheet(aceiteSheet, iterationNumber, aceiteTotal, aceiteYearValues, startYear, endYear);
        }
        
        synchronized (gasSheet) {
            createSheetHeaders(gasSheet, startYear, endYear, productionWorkbook);
            writeRowToSheet(gasSheet, iterationNumber, gasTotal, gasYearValues, startYear, endYear);
        }
        
        synchronized (condensadoSheet) {
            createSheetHeaders(condensadoSheet, startYear, endYear, productionWorkbook);
            writeRowToSheet(condensadoSheet, iterationNumber, condensadoTotal, condensadoYearValues, startYear, endYear);
        }
    }
        

    private void writeRowToSheet(Sheet sheet, int rowNum, double total, Map<String, Double> yearValues, 
                                 int startYear, int endYear) {
        Row row = sheet.createRow(rowNum);
        
        // First column: iteration number
        Cell iterationCell = row.createCell(0);
        iterationCell.setCellValue(rowNum - 1);
        
        // Second column: total
        Cell totalCell = row.createCell(1);
        totalCell.setCellValue(total);
        
        // For each year in the range, write to the correct column
        for (int year = startYear; year <= endYear; year++) {
            // Calculate the correct column index for this year
            int columnIndex = year - startYear + 2;
            
            // Get the value for this year, default to 0.0 if not found
            String yearStr = String.valueOf(year);
            double value = yearValues.getOrDefault(yearStr, 0.0);
            
            // Create the cell and set its value
            Cell cell = row.createCell(columnIndex);
            cell.setCellValue(value);
        }
    }


    private void createSheetHeaders(Sheet sheet, int startYear, int endYear, Workbook workbook) {
        synchronized (sheet) {
            Row headerRow = sheet.getRow(1);
            if (headerRow == null) {
                headerRow = sheet.createRow(1);
                
                // Create style
                CellStyle style = workbook.createCellStyle();
                style.setAlignment(HorizontalAlignment.CENTER);
                style.setVerticalAlignment(VerticalAlignment.CENTER);
                
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                
                // Create standard header cells
                Cell cellIteration = headerRow.createCell(0);
                cellIteration.setCellStyle(style);
                cellIteration.setCellValue("Iteración");
                
                Cell cellTotal = headerRow.createCell(1);
                cellTotal.setCellStyle(style);
                cellTotal.setCellValue("Total");
            }
            
            // Create year headers with consistent column indexing
            for (int year = startYear; year <= endYear; year++) {
                // The column index is based on the position relative to the start year
                int columnIndex = year - startYear + 2;
                
                Cell cell = headerRow.getCell(columnIndex);
                if (cell == null) {
                    cell = headerRow.createCell(columnIndex);
                    cell.setCellValue(year);
                    
                    // Apply style if needed
                    if (headerRow.getCell(0) != null && headerRow.getCell(0).getCellStyle() != null) {
                        cell.setCellStyle(headerRow.getCell(0).getCellStyle());
                    }
                }
            }
        }
    }
    
    private boolean pruebaGeologica() {
        if(pgValue == 1) {
            return true;
        } else {
            return random.nextDouble() <= oportunidad.getPg();
        }
    }

    private double calcularRecursoProspectivo(double aleatorio, double percentil10, double percentil90) {

        if(percentil10 == percentil90) return percentil10;

        NormalDistribution normalStandard = new NormalDistribution(0, 1);
        double z90 = normalStandard.inverseCumulativeProbability(0.9);
        double mediaLog = (Math.log(percentil10) + Math.log(percentil90)) / 2;
        double desviacionLog = (Math.log(percentil10) - mediaLog) / z90;

        NormalDistribution normal = new NormalDistribution(mediaLog, desviacionLog);

        double logValue = normal.inverseCumulativeProbability(aleatorio);
        return Math.exp(logValue);
    }

    private double calcularLimiteEconomico(double pce) {
        double resultado;
        if (pce == 0) {
            resultado = 0.0;
        } else {
            resultado = Math.round((-0.00003 * Math.pow(pce, 2)) + (pce * 0.068) + 4.3824) * 12;
        }

        return resultado;
    }

    public List<Double> escaleraLimEconomicos(Map<Double, Integer> limitesEconomicosRepetidos, double mediaTruncada,
            int cantidadIteraciones) {
        List<Double> reporte805 = new ArrayList<>();

        // Filtrar las claves cuyo valor sea != 0 y encontrar la cantidad máxima de
        // repeticiones
        double maxLimiteEconomico = limitesEconomicosRepetidos.entrySet().stream()
                .filter(entry -> entry.getKey() != 0) // Filtrar límites donde la clave sea != 0
                .map(Map.Entry::getKey) // Obtener las claves (límites económicos)
                .max(Double::compare) // Encontrar el máximo
                .orElse(0.0); // Valor por defecto si no hay límites válidos // Manejo de caso vacío
        // Calcular la última fila
        for (int iteracion = 1; iteracion <= maxLimiteEconomico; iteracion++) {
            double sumaIteracion = 0.0;

            for (Map.Entry<Double, Integer> entry : limitesEconomicosRepetidos.entrySet()) {
                Integer repeticiones = entry.getValue();
                Double limite = entry.getKey();

                // Sumar solo si la iteración está dentro del rango de repeticiones
                if (iteracion <= limite && limite != 0) {
                    sumaIteracion += (repeticiones * mediaTruncada);
                }
            }
            sumaIteracion /= cantidadIteraciones;

            // Agregar el resultado de la suma al final de la lista
            reporte805.add(sumaIteracion);
        }

        return reporte805;
    }

    public double calcularGastoInicial(double PCE, double aleatorioGasto) {

        double gastoInicialMin = 0.0;
        double gastoInicialMP = 0.0;
        double gastoInicialMax = 0.0;

        if (oportunidad.getIdHidrocarburo() == 1 || oportunidad.getIdHidrocarburo() == 2
                || oportunidad.getIdHidrocarburo() == 4 || oportunidad.getIdHidrocarburo() == 6) {

            gastoInicialMin = oportunidad.getGastoMINAceite() / oportunidad.getFcAceite();
            gastoInicialMP = oportunidad.getGastoMPAceite() / oportunidad.getFcAceite();
            gastoInicialMax = oportunidad.getGastoMAXAceite() / oportunidad.getFcAceite();

        } else if (oportunidad.getIdHidrocarburo() == 3 || oportunidad.getIdHidrocarburo() == 5
                || oportunidad.getIdHidrocarburo() == 7 || oportunidad.getIdHidrocarburo() == 9) {

            gastoInicialMin = oportunidad.getGastoMINAceite() / oportunidad.getFcGas();
            gastoInicialMP = oportunidad.getGastoMPAceite() / oportunidad.getFcGas();
            gastoInicialMax = oportunidad.getGastoMAXAceite() / oportunidad.getFcGas();

        } else {

            gastoInicialMin = oportunidad.getGastoMINAceite() / oportunidad.getFcCondensado();
            gastoInicialMP = oportunidad.getGastoMPAceite() / oportunidad.getFcCondensado();
            gastoInicialMax = oportunidad.getGastoMAXAceite() / oportunidad.getFcCondensado();

        }

        return triangularDistribution(PCE, gastoInicialMin, gastoInicialMax, gastoInicialMP, aleatorioGasto);
    }

    private double triangularDistribution(double PCE, double gmin, double gmax, double gmp, double aleatorio) {
        if (PCE == 0) {
            return 0.0;
        }

        if (gmax - gmin != 0) {
            double fraction = (gmp - gmin) / (gmax - gmin);
            if (aleatorio < fraction) {
                return gmin + Math.sqrt(aleatorio * (gmax - gmin) * (gmp - gmin));
            } else {
                return gmax - Math.sqrt((1 - aleatorio) * (gmax - gmin) * (gmax - gmp));
            }
        }
        return gmin;
    }

    private double triangularDistributionSinPCE(double gmin, double gmax, double gmp, double aleatorio) {
        if (gmax - gmin != 0) {
            double fraction = (gmp - gmin) / (gmax - gmin);

            if (aleatorio < fraction) {
                return gmin + Math.sqrt(aleatorio * (gmp - gmin) * (gmax - gmin));
            } else {
                return gmax - Math.sqrt((1 - aleatorio) * (gmp - gmin) * (gmax - gmin));
            }
        }
        return gmp;

    }

    private void writeResultsToExcel(Sheet sheet, Map<Integer, Object> rowData) {
        Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
        rowData.forEach((colIndex, value) -> {
            Cell cell = row.createCell(colIndex);
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                cell.setCellValue(value.toString());
            }
        });
    }
    public void cleanup() {
        pceSheetData.clear();
        aceiteSheetData.clear();
        gasSheetData.clear();
        condensadoSheetData.clear();
    }

}
