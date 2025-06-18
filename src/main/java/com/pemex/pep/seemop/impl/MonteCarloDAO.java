package com.pemex.pep.seemop.impl;

import com.pemex.pep.seemop.Models.InversionOportunidad;
import com.pemex.pep.seemop.Models.Oportunidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

@Component
public class MonteCarloDAO {
    @Autowired
    JdbcTemplate jdbcTemplate;

    private Connection getConnection() throws SQLException {
        Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
        return connection;
    }

    public Oportunidad executeQuery(String version, int idOportunidadObjetivo) {

        double pce10 = 0;
        double area10 = 0;
        double pce90 = 0;
        double area90 = 0;
        double fcAceite = 0;
        double fcGas = 0;
        double fcCondensado = 0;

        String tipoValorMIN = null, tipoValorMP = null, tipoValorMAX = null;
        double gastoMIN = 0, gastoMP = 0, gastoMAX = 0;

        double primDeclinacionMIN = 0, primDeclinacionMP = 0, primDeclinacionMAX = 0;

        String idVersionQuery = """

                SELECT idversion FROM catalogo.versiontbl WHERE nombreversion = ?

                """;

        String oportunidadObjetivoQuery = """
                            SELECT idoportunidadobjetivo, oportunidad, hidrocarburo, tipooportunidad, pg, idhidrocarburo, regimenfiscal, plandesarrollo
                            FROM catalogo.claveobjetivovw\s
                            WHERE idversion = ? AND idoportunidadobjetivo = ?
                    """;

        String VolumetriaQuery = """
                SELECT idoportunidadobjetivo, pce, area, percentil
                FROM catalogo.volumetriaoportunidadvw
                WHERE idversion = ? AND idoportunidadobjetivo = ? AND percentil IN (10, 90)
                """;

        String GastoInicialQuery = """
                SELECT DISTINCT idoportunidadobjetivo, idtipovalor, tipovalor, gasto, idversion, idhidrocarburo
                FROM catalogo.gastoinicialoportunidadvw
                WHERE idtipovalor IN (1, 2, 3) AND idoportunidadobjetivo = ? AND idversion = ?
                """;

        String DeclinacionQuery = """
                SELECT DISTINCT idoportunidadobjetivo, idtipovalor, tipovalor, primdeclinacionoportunidad, idversion
                FROM catalogo.declinacionoportunidadvw
                WHERE idtipovalor IN (1, 2, 3) AND idoportunidadobjetivo = ? AND idversion = ?
                """;

        String fcQuery = """
                SELECT mediaaceite/mediapce AS fc_aceite, mediagas/mediapce AS fc_gas, mediacondensado/mediapce AS fc_condensado
                FROM catalogo.mediavolumetriaoportunidadtbl
                WHERE idoportunidadobjetivo = ? AND idversion = ?
                """;

        String queryExploratorio = """
                SELECT *
                FROM inversion.exploratoriooportunidadvw
                WHERE idversion = ?
                AND idoportunidadobjetivo = ?
                """;

        String queryDesarrollo = """
                SELECT * FROM
                inversion.desarrollooportunidadvw
                WHERE idversion= ?
                AND idoportunidadobjetivo= ?
                """;

        String queryInversion = """

                SELECT * FROM inversion.otrosdatostbl WHERE idversion= ? AND idoportunidadobjetivo= ?


                """;

        Oportunidad oportunidadObj = null;
        int idhidrocarburo = 0;

        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(idVersionQuery);
                PreparedStatement statement1 = connection.prepareStatement(oportunidadObjetivoQuery);
                PreparedStatement statement2 = connection.prepareStatement(VolumetriaQuery);
                PreparedStatement statement3 = connection.prepareStatement(GastoInicialQuery);
                PreparedStatement statement4 = connection.prepareStatement(DeclinacionQuery);
                PreparedStatement statement5 = connection.prepareStatement(fcQuery);
                PreparedStatement statement6 = connection.prepareStatement(queryExploratorio);
                PreparedStatement statement7 = connection.prepareStatement(queryDesarrollo);
                PreparedStatement statement8 = connection.prepareStatement(queryInversion);

        ) {

            preparedStatement.setString(1, version);

            ResultSet preparedStatementQuery = preparedStatement.executeQuery();
            int actualIdVersion = 0; // Valor por defecto
            if (preparedStatementQuery.next()) {
                actualIdVersion = preparedStatementQuery.getInt("idversion");

            } else {
                System.err.println("No se encontró idVersion para la versión 'VersionMontecarlos1'.");
                return null;
            }

            statement1.setInt(1, actualIdVersion);
            statement1.setInt(2, idOportunidadObjetivo);

            ResultSet resultSet1 = statement1.executeQuery();

            if (resultSet1.next()) {
                int idOportunidadObjetivoResult = resultSet1.getInt("idoportunidadobjetivo");
                String oportunidad = resultSet1.getString("oportunidad");
                String hidrocarburo = resultSet1.getString("hidrocarburo");
                String tipoOportunidad = resultSet1.getString("tipooportunidad");
                String planDesarrollo = resultSet1.getString("plandesarrollo");
                int idhidrocarburoo = resultSet1.getInt("idhidrocarburo");
                double pg = resultSet1.getDouble("pg");

                // Volumetria Query
                statement2.setInt(1, actualIdVersion);
                statement2.setInt(2, idOportunidadObjetivo);
                ResultSet resultSet2 = statement2.executeQuery();

                while (resultSet2.next()) {
                    int percentil = resultSet2.getInt("percentil");
                    double pce = resultSet2.getDouble("pce");
                    double area = resultSet2.getDouble("area");

                    if (percentil == 10) {
                        pce10 = pce;
                        area10 = area;
                    } else if (percentil == 90) {
                        pce90 = pce;
                        area90 = area;
                    }
                }

                // GastoInicial Query
                statement3.setInt(1, idOportunidadObjetivo);
                statement3.setInt(2, actualIdVersion);

                ResultSet resultSet3 = statement3.executeQuery();

                while (resultSet3.next()) {
                    String tipoValor = resultSet3.getString("tipovalor");
                    double gasto = resultSet3.getDouble("gasto");
                    idhidrocarburo = resultSet1.getInt("idhidrocarburo");

                    switch (tipoValor) {
                        case "MIN":
                            tipoValorMIN = tipoValor;
                            gastoMIN = gasto;
                            break;
                        case "MP":
                            tipoValorMP = tipoValor;
                            gastoMP = gasto;
                            break;
                        case "MAX":
                            tipoValorMAX = tipoValor;
                            gastoMAX = gasto;
                            break;
                    }
                }

                statement4.setInt(1, idOportunidadObjetivo);
                statement4.setInt(2, actualIdVersion);

                ResultSet resultSet4 = statement4.executeQuery();

                while (resultSet4.next()) {
                    String tipoValor = resultSet4.getString("tipovalor");
                    double primDeclinacion = resultSet4.getDouble("primdeclinacionoportunidad");

                    switch (tipoValor) {
                        case "MIN":
                            primDeclinacionMIN = primDeclinacion;

                            break;
                        case "MP":
                            primDeclinacionMP = primDeclinacion;
                            break;
                        case "MAX":
                            primDeclinacionMAX = primDeclinacion;
                            break;
                    }
                }

                statement5.setInt(1, idOportunidadObjetivo);
                statement5.setInt(2, actualIdVersion);
                ResultSet resultSet5 = statement5.executeQuery();

                if (resultSet5.next()) {
                    fcAceite = resultSet5.getDouble("fc_aceite");
                    fcGas = resultSet5.getDouble("fc_gas");
                    fcCondensado = resultSet5.getDouble("fc_condensado");
                }

                statement6.setInt(1, actualIdVersion);
                statement6.setInt(2, idOportunidadObjetivo);

                ResultSet resultSet6 = statement6.executeQuery();

                double infraestructuraMin = 0, infraestructuraMP = 0, infraestructuraMax = 0;
                double perforacionMin = 0, perforacionMP = 0, perforacionMax = 0;
                double terminacionMin = 0, terminacionMP = 0, terminacionMax = 0;

                while (resultSet6.next()) {
                    int idtipovalor = resultSet6.getInt("idtipovalor"); // 1 = MIN, 2 = MP, 3 = MAX
                    double infraestructura = resultSet6.getDouble("infraestructura");
                    double perforacion = resultSet6.getDouble("perforacion");
                    double terminacion = resultSet6.getDouble("terminacion");

                    switch (idtipovalor) {
                        case 1: // MIN
                            infraestructuraMin = infraestructura;
                            perforacionMin = perforacion;
                            terminacionMin = terminacion;
                            break;
                        case 2: // MP
                            infraestructuraMP = infraestructura;
                            perforacionMP = perforacion;
                            terminacionMP = terminacion;
                            break;
                        case 3: // MAX
                            infraestructuraMax = infraestructura;
                            perforacionMax = perforacion;
                            terminacionMax = terminacion;
                            break;

                    }
                }

                statement7.setInt(1, actualIdVersion);
                statement7.setInt(2, idOportunidadObjetivo);

                ResultSet resultSet7 = statement7.executeQuery();

                double infraestructuraMinDES = 0, infraestructuraMPDES = 0, infraestructuraMaxDES = 0;
                double perforacionMinDES = 0, perforacionMPDES = 0, perforacionMaxDES = 0;
                double terminacionMinDES = 0, terminacionMPDES = 0, terminacionMaxDES = 0;

                while (resultSet7.next()) {
                    int idtipovalor = resultSet7.getInt("idtipovalor"); // 1 = MIN, 2 = MP, 3 = MAX
                    double infraestructura = resultSet7.getDouble("infraestructura");
                    double perforacion = resultSet7.getDouble("perforacion");
                    double terminacion = resultSet7.getDouble("terminacion");

                    switch (idtipovalor) {
                        case 1: // MIN
                            infraestructuraMinDES = infraestructura;
                            perforacionMinDES = perforacion;
                            terminacionMinDES = terminacion;
                            break;
                        case 2: // MP
                            infraestructuraMPDES = infraestructura;
                            perforacionMPDES = perforacion;
                            terminacionMPDES = terminacion;
                            break;
                        case 3: // MAX
                            infraestructuraMaxDES = infraestructura;
                            perforacionMaxDES = perforacion;
                            terminacionMaxDES = terminacion;
                            break;

                    }
                }

                // Ejecutando la consulta
                // Declaración de las variables
                // Declaración de las variables
                int idtipovalor = 0;
                double plataformadesarrollo = 0.0;
                double lineadedescarga = 0.0;
                double estacioncompresion = 0.0;
                double ducto = 0.0;
                double bateria = 0.0;
                double arbolessubmarinos = 0.0;
                double manifolds = 0.0;
                double risers = 0.0;
                double sistemasdecontrol = 0.0;
                double cubiertadeproces = 0.0;
                double buquetanquecompra = 0.0;
                double buquetanquerenta = 0.0;

                // Variables para valores mínimos
                double plataformadesarrolloMin = 0.0;
                double lineadedescargaMin = 0.0;
                double estacioncompresionMin = 0.0;
                double ductoMin = 0.0;
                double bateriaMin = 0.0;
                double arbolessubmarinosMin = 0.0;
                double manifoldsMin = 0.0;
                double risersMin = 0.0;
                double sistemasdecontrolMin = 0.0;
                double cubiertadeprocesMin = 0.0;
                double buquetanquecompraMin = 0.0;
                double buquetanquerentaMin = 0.0;

                // Variables para valores promedio (MP)
                double plataformadesarrolloMp = 0.0;
                double lineadedescargaMp = 0.0;
                double estacioncompresionMp = 0.0;
                double ductoMp = 0.0;
                double bateriaMp = 0.0;
                double arbolessubmarinosMp = 0.0;
                double manifoldsMp = 0.0;
                double risersMp = 0.0;
                double sistemasdecontrolMp = 0.0;
                double cubiertadeprocesMp = 0.0;
                double buquetanquecompraMp = 0.0;
                double buquetanquerentaMp = 0.0;

                // Variables para valores máximos
                double plataformadesarrolloMax = 0.0;
                double lineadedescargaMax = 0.0;
                double estacioncompresionMax = 0.0;
                double ductoMax = 0.0;
                double bateriaMax = 0.0;
                double arbolessubmarinosMax = 0.0;
                double manifoldsMax = 0.0;
                double risersMax = 0.0;
                double sistemasdecontrolMax = 0.0;
                double cubiertadeprocesMax = 0.0;
                double buquetanquecompraMax = 0.0;
                double buquetanquerentaMax = 0.0;

                // Asumiendo que la consulta ya fue configurada y ejecutada
                statement8.setInt(1, actualIdVersion);
                statement8.setInt(2, idOportunidadObjetivo);

                ResultSet resultSet8 = statement8.executeQuery();

                while (resultSet8.next()) {
                    // Obtener los valores de cada columna del ResultSet
                    idtipovalor = resultSet8.getInt("idtipovalor"); // 1 = MIN, 2 = MP, 3 = MAX
                    plataformadesarrollo = resultSet8.getDouble("plataformadesarrollo");
                    lineadedescarga = resultSet8.getDouble("lineadedescarga");
                    estacioncompresion = resultSet8.getDouble("estacioncompresion");
                    ducto = resultSet8.getDouble("ducto");
                    bateria = resultSet8.getDouble("bateria");
                    arbolessubmarinos = resultSet8.getDouble("arbolessubmarinos");
                    manifolds = resultSet8.getDouble("manifolds");
                    risers = resultSet8.getDouble("risers");
                    sistemasdecontrol = resultSet8.getDouble("sistemasdecontrol");
                    cubiertadeproces = resultSet8.getDouble("cubiertadeproces");
                    buquetanquecompra = resultSet8.getDouble("buquetanquecompra");
                    buquetanquerenta = resultSet8.getDouble("buquetanquerenta");

                    // Asignar los valores a las variables correspondientes según el tipo de valor
                    switch (idtipovalor) {
                        case 1: // MIN
                            plataformadesarrolloMin = plataformadesarrollo;
                            lineadedescargaMin = lineadedescarga;
                            estacioncompresionMin = estacioncompresion;
                            ductoMin = ducto;
                            bateriaMin = bateria;
                            arbolessubmarinosMin = arbolessubmarinos;
                            manifoldsMin = manifolds;
                            risersMin = risers;
                            sistemasdecontrolMin = sistemasdecontrol;
                            cubiertadeprocesMin = cubiertadeproces;
                            buquetanquecompraMin = buquetanquecompra;
                            buquetanquerentaMin = buquetanquerenta;
                            break;

                        case 2: // MP
                            plataformadesarrolloMp = plataformadesarrollo;
                            lineadedescargaMp = lineadedescarga;
                            estacioncompresionMp = estacioncompresion;
                            ductoMp = ducto;
                            bateriaMp = bateria;
                            arbolessubmarinosMp = arbolessubmarinos;
                            manifoldsMp = manifolds;
                            risersMp = risers;
                            sistemasdecontrolMp = sistemasdecontrol;
                            cubiertadeprocesMp = cubiertadeproces;
                            buquetanquecompraMp = buquetanquecompra;
                            buquetanquerentaMp = buquetanquerenta;
                            break;

                        case 3: // MAX
                            plataformadesarrolloMax = plataformadesarrollo;
                            lineadedescargaMax = lineadedescarga;
                            estacioncompresionMax = estacioncompresion;
                            ductoMax = ducto;
                            bateriaMax = bateria;
                            arbolessubmarinosMax = arbolessubmarinos;
                            manifoldsMax = manifolds;
                            risersMax = risers;
                            sistemasdecontrolMax = sistemasdecontrol;
                            cubiertadeprocesMax = cubiertadeproces;
                            buquetanquecompraMax = buquetanquecompra;
                            buquetanquerentaMax = buquetanquerenta;
                            break;
                    }
                }

                InversionOportunidad InversionOportunidad = new InversionOportunidad(plataformadesarrolloMin,
                        lineadedescargaMin, estacioncompresionMin,
                        ductoMin, bateriaMin, arbolessubmarinosMin, manifoldsMin, risersMin, sistemasdecontrolMin,
                        cubiertadeprocesMin, buquetanquecompraMin, buquetanquerentaMin,
                        plataformadesarrolloMp, lineadedescargaMp, estacioncompresionMp, ductoMp, bateriaMp,
                        arbolessubmarinosMp, manifoldsMp, risersMp, sistemasdecontrolMp, cubiertadeprocesMp,
                        buquetanquecompraMp, buquetanquerentaMp,
                        plataformadesarrolloMax, lineadedescargaMax, estacioncompresionMax, ductoMax, bateriaMax,
                        arbolessubmarinosMax, manifoldsMax, risersMax, sistemasdecontrolMax, cubiertadeprocesMax,
                        buquetanquecompraMax, buquetanquerentaMax

                );

                oportunidadObj = new Oportunidad(
                        actualIdVersion, idOportunidadObjetivoResult, oportunidad, planDesarrollo, pce10, pce90, area10, area90,
                        hidrocarburo, tipoOportunidad, pg, gastoMIN, gastoMP, gastoMAX, idhidrocarburo,
                        primDeclinacionMIN, primDeclinacionMP, primDeclinacionMAX, fcAceite, fcGas, fcCondensado,
                        infraestructuraMin, infraestructuraMP, infraestructuraMax,
                        perforacionMin, perforacionMP, perforacionMax,
                        terminacionMin, terminacionMP, terminacionMax,
                        infraestructuraMinDES, infraestructuraMPDES, infraestructuraMaxDES,
                        perforacionMinDES, perforacionMPDES, perforacionMaxDES,
                        terminacionMinDES, terminacionMPDES, terminacionMaxDES, InversionOportunidad);

                return oportunidadObj;

            } else {
                System.err.println("No se encontraron resultados en la primera consulta.");
            }
        } catch (SQLException e) {
            System.err.println("Error en executeQuery(): " + e.getMessage());
        }
        return null;

    }

    public Map<Integer, Double> executeProductionQuery(int idVersion, int idOportunidadObjetivo, double cuota,
            double declinada, double pce, double area) {
        Map<Integer, Double> resultMap = new HashMap<>();
        String productionQuery = "SELECT aanio, ctotalanual FROM calculo.spp_produccionanual(?, ?, ?, ?, ?, ?)";
        String idVersionQuery = """
                SELECT idversion FROM catalogo.versiontbl WHERE nombreversion = ?
                """;
        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(idVersionQuery);
                PreparedStatement statement = connection.prepareStatement(productionQuery)) {

            ResultSet preparedStatementQuery = preparedStatement.executeQuery();
            int actualIdVersion = idVersion; // Valor por defecto
            if (preparedStatementQuery.next()) {
                actualIdVersion = preparedStatementQuery.getInt("idversion");
            } else {
                System.err.println("No se encontró idVersion para la versión 'VersionMontecarlos1'.");
                return null;
            }

            statement.setInt(1, actualIdVersion);
            statement.setInt(2, idOportunidadObjetivo);
            statement.setDouble(3, cuota);
            statement.setDouble(4, declinada);
            statement.setDouble(5, pce);
            statement.setDouble(6, area);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int anio = resultSet.getInt("aanio");
                    double ctotalanual = resultSet.getDouble("ctotalanual");

                    resultMap.put(anio, ctotalanual); // Guardamos el año y la producción anual
                }

            }
        } catch (SQLException e) {
            System.err.println("Error al ejecutar la consulta de producción anual: " + e.getMessage());
        }

        return resultMap; // Devolvemos el mapa con los resultados
    }

    public double getMediaTruncada(String idVersion, int idOportunidadObjetivo) {
        double resultado = 0.0;
        int idVersionAct = 0;
        String idVersionQuery = """

                SELECT idversion FROM catalogo.versiontbl WHERE nombreversion = ?

                """;

        String query = "SELECT mediaarea " +
                "FROM catalogo.mediavolumetriaoportunidadtbl " +
                "WHERE idversion = ? AND idoportunidadobjetivo = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(idVersionQuery);
            stmt.setString(1, idVersion);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    idVersionAct = resultSet.getInt("idversion");
                }
            }

            try (PreparedStatement stmt2 = connection.prepareStatement(query)) {
                stmt2.setInt(1, idVersionAct);
                stmt2.setInt(2, idOportunidadObjetivo);
                
                try (ResultSet rs = stmt2.executeQuery()) {
                    if (rs.next()) {
                        resultado = rs.getDouble("mediaarea");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en getMediaTruncada(): " + e.getMessage());
        }

        // Truncar el valor a 2 decimales (si es necesario)
        return resultado;
    }

    public double getKilometraje(String idVersion, int idOportunidadObjetivo) {
        double resultado = 0.0;
        int idVersionAct = 0;
        String idVersionQuery = """

                SELECT idversion FROM catalogo.versiontbl WHERE nombreversion = ?

                """;

        String kmQuery = """
                    SELECT idoportunidadobjetivo, idoportunidad,idversion, areakmasignacion
                    	FROM catalogo.reloportunidadobjetivotbl  where idoportunidadobjetivo = ? and idversion = ?;
                """;

        try (Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement(idVersionQuery);
                PreparedStatement stmt2 = connection.prepareStatement(kmQuery)) {

            stmt.setString(1, idVersion);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    idVersionAct = resultSet.getInt("idversion");
                }
            }

            stmt2.setInt(1, idOportunidadObjetivo);
            stmt2.setInt(2, idVersionAct);
            try (ResultSet rs = stmt2.executeQuery()) {
                if (rs.next()) {
                    resultado = rs.getDouble("areakmasignacion");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al ejecutar la consulta de kilometraje: " + e.getMessage());
        }

        return resultado;
    }

    public List<Map<String, Object>> getMultiOjbectivo(int idoportunidad) {
        List<Map<String, Object>> results = new ArrayList<>();
        String query = """
                select claveobjetivo, idoportunidad, idoportunidadobjetivo, profundidadpozodesarrollo, idversion
                from catalogo.reloportunidadobjetivotbl where idoportunidad = ?
                order by profundidadpozodesarrollo desc
                """;

        try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idoportunidad);
            try (ResultSet rsOportunidad = preparedStatement.executeQuery()) {
                while (rsOportunidad.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("claveobjetivo", rsOportunidad.getString("claveobjetivo"));
                    row.put("idoportunidad", rsOportunidad.getInt("idoportunidad"));
                    row.put("idoportunidadobjetivo", rsOportunidad.getInt("idoportunidadobjetivo"));
                    row.put("idversion", rsOportunidad.getInt("idversion"));
                    results.add(row);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en getMultiOjbectivo(): " + e.getMessage());
        }

        return results;
    }

}
