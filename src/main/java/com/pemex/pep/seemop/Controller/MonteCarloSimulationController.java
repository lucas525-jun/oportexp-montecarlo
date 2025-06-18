package com.pemex.pep.seemop.Controller;

import com.pemex.pep.seemop.Services.MonteCarloService;
import com.pemex.pep.seemop.impl.MonteCarloSimulation;
import com.pemex.pep.seemop.impl.MonteCarloSimulationMultiObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.pemex.pep.seemop.impl.MonteCarloDAO;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/simulation")
public class MonteCarloSimulationController {

    @Autowired
    private MonteCarloService monteCarloService;

    @Autowired
    private MonteCarloDAO monteCarloDAO;

    @Autowired
    RestTemplate restTemplate;

    @Value("${OPORTEXT_GENEXCEL_HOST:localhost}")
    private String genExcelHost;

    @Value("${OPORTEXT_GENEXCEL_POST:3000}")
    private String genExcelPort;

    public long showMemory () {
        Runtime rt = Runtime.getRuntime();
        long usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        return usedMB;
    }
    @GetMapping("/run")
    public ResponseEntity<?> runSimulation(
            @RequestParam("Version") String version,
            @RequestParam("IdOportunidadObjetivo") int idOportunidadObjetivo,
            @RequestParam("IdOportunidad") int idOportunidad,
            @RequestParam("evaluationType") String evaluationType,
            @RequestParam("iterationsNumber") int iterations,
            @RequestParam("iterationCheck") Boolean iterationCheck,
            @RequestParam("profileCheck") Boolean profileCheck,
            @RequestParam("evaluationId") String evaluationId) {
        Boolean multiType = false;
        List<Map<String, Object>> multiObjetivos;
        multiObjetivos = monteCarloDAO.getMultiOjbectivo(idOportunidad);
        String jsonFilePath;
        if (evaluationType.equals("opportunity") && multiObjetivos.size() > 1) {
            System.err.println("Multi Object started.");
            int[] idOportunidadObjetivoArray = new int[multiObjetivos.size()];
            for (int i = 0; i < multiObjetivos.size(); i++) {
                idOportunidadObjetivoArray[i] = (int) multiObjetivos.get(i).get("idoportunidadobjetivo");
            }
            MonteCarloSimulationMultiObject monteCarloSimulationMultiObject = monteCarloService
                    .createSimulationForMulti(
                            version,
                            idOportunidadObjetivoArray, 
                            iterations, 
                            iterationCheck,
                            profileCheck,
                            evaluationId);
            jsonFilePath = monteCarloSimulationMultiObject.runSimulation();
            multiType = true;
        } else {
            MonteCarloSimulation monteCarloSimulation = monteCarloService.createSimulation(version, idOportunidadObjetivo, iterations, iterationCheck, profileCheck, evaluationId);
            jsonFilePath = monteCarloSimulation.runSimulation();

        }
        if(profileCheck == true) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            return new ResponseEntity<>("Profile check completed successfully", headers, HttpStatus.OK);

        } 
        try {
            String encodedJsonPath = UriUtils.encode(jsonFilePath, "UTF-8");
           
            String nodeUrl = "http://" + genExcelHost + ":" + genExcelPort +
                 "/generate-excel?filePath=" + encodedJsonPath + 
                 "&evaluationType=" + multiType;

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM)); 
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                nodeUrl,
                HttpMethod.GET,
                entity,
                byte[].class
            );

            // Verifica si la respuesta es válida
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                HttpHeaders fileHeaders = new HttpHeaders();
                fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                fileHeaders.setContentDisposition(ContentDisposition
                        .attachment()
                        .filename("Resultados.xlsx")
                        .build());

                return new ResponseEntity<>(response.getBody(), fileHeaders, HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al generar el archivo Excel.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en la simulación: " + e.getMessage());
        }
    }
    
    /**
     * Collects files with names starting with evaluationId, compresses them into a ZIP file,
     * deletes the source files, and returns the ZIP to the frontend.
     * When iterationCheck is true, only Monte Carlo simulation files are compressed.
     * When profileCheck is true, only profile files are compressed.
     * When neither is true, no files are compressed.
     *
     * @param evaluationId Unique identifier for the evaluation session
     * @param versionName Version name from the simulation
     * @param iterationCheck Flag to indicate if Monte Carlo simulation files should be included
     * @param profileCheck Flag to indicate if profile files should be included
     * @return ZIP file containing the selected files for the given evaluation
     */
    @GetMapping("/downloadexcel")
    public ResponseEntity<Resource> downloadMonteCarloZip(
        @RequestParam("evaluationId") String evaluationId,
        @RequestParam("versionName") String versionName,
        @RequestParam("iterationCheck") Boolean iterationCheck,
        @RequestParam("profileCheck") Boolean profileCheck) {

        if (!iterationCheck && !profileCheck) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        
        try {
            // Use the fixed directory path
            Path workingDir = Paths.get("").toAbsolutePath();
            Path directoryPath = workingDir;

            List<Path> filesToZip = Files.list(directoryPath)
                .filter(path -> {
                    String filename = path.getFileName().toString();
                    boolean containsEvaluationId = filename.contains(evaluationId);
                    
                    if (!containsEvaluationId) {
                        return false;
                    }
                    
                    if (iterationCheck) {
                        return filename.contains("SimulacionMonteCarlo");
                    } else if (profileCheck) {
                        return filename.contains("Perfiles de producción");
                    }
                    
                    return false;
                })
                .collect(Collectors.toList());

            if (filesToZip.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            
            for (Path filePath : filesToZip) {
                File file = filePath.toFile();
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);
                
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
            
            zos.close();
            byte[] zipBytes = baos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(zipBytes);
            
            for (Path filePath : filesToZip) {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    System.err.println("Could not delete file: " + filePath.getFileName() + ": " + e.getMessage());
                }
            }
            
            // Prepare and return the ZIP file
            String zipFileName = "MonteCarlo_" + evaluationId + ".zip";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);
                    
        } catch (IOException e) {
            System.err.println("Error creating Monte Carlo ZIP file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}