package com.pemex.pep.seemop.Models;

public class InversionOportunidad {

    // Variables para los valores Min, MP, MAX
    private double plataformadesarrolloMin;
    private double lineadedescargaMin;
    private double estacioncompresionMin;
    private double ductoMin;
    private double bateriaMin;
    private double arbolessubmarinosMin = 0.0;
    private double manifoldsMin = 0.0;
    private  double risersMin = 0.0;
    private  double sistemasdecontrolMin = 0.0;
    private  double cubiertadeprocesMin = 0.0;
    private  double buquetanquecompraMin = 0.0;
    private  double buquetanquerentaMin = 0.0;

    private double plataformadesarrolloMp;
    private double lineadedescargaMp;
    private double estacioncompresionMp;
    private double ductoMp;
    private double bateriaMp;
    private double arbolessubmarinosMp = 0.0;
    private double manifoldsMp = 0.0;
    private  double risersMp = 0.0;
    private  double sistemasdecontrolMp = 0.0;
    private  double cubiertadeprocesMp = 0.0;
    private  double buquetanquecompraMp = 0.0;
    private  double buquetanquerentaMp = 0.0;

    private double plataformadesarrolloMax;
    private double lineadedescargaMax;
    private double estacioncompresionMax;
    private double ductoMax;
    private double bateriaMax;
    private double arbolessubmarinosMax = 0.0;
    private double manifoldsMax = 0.0;
    private  double risersMax = 0.0;
    private  double sistemasdecontrolMax = 0.0;
    private  double cubiertadeprocesMax = 0.0;
    private  double buquetanquecompraMax = 0.0;
    private  double buquetanquerentaMax = 0.0;

    // Constructor para inicializar los valores
    public InversionOportunidad(double plataformadesarrolloMin, double lineadedescargaMin, double estacioncompresionMin,
                                double ductoMin, double bateriaMin, double arbolessubmarinosMin, double manifoldsMin, double risersMin,double sistemasdecontrolMin,
                                double cubiertadeprocesMin, double buquetanquecompraMin, double buquetanquerentaMin,
                                double plataformadesarrolloMp, double lineadedescargaMp, double estacioncompresionMp,
                                double ductoMp, double bateriaMp,double arbolessubmarinosMp, double manifoldsMp, double risersMp,double sistemasdecontrolMp,double cubiertadeprocesMp, double buquetanquecompraMp,
                                double buquetanquerentaMp,
                                double plataformadesarrolloMax, double lineadedescargaMax, double estacioncompresionMax,
                                double ductoMax, double bateriaMax,
                                double arbolessubmarinosMax, double manifoldsMax, double risersMax,double sistemasdecontrolMax,double cubiertadeprocesMax, double buquetanquecompraMax,
                                double buquetanquerentaMax ) {

        this.plataformadesarrolloMin = plataformadesarrolloMin;
        this.lineadedescargaMin = lineadedescargaMin;
        this.estacioncompresionMin = estacioncompresionMin;
        this.ductoMin = ductoMin;
        this.bateriaMin = bateriaMin;
        this.arbolessubmarinosMin = arbolessubmarinosMin;
        this.manifoldsMin = manifoldsMin;
        this.risersMin = risersMin;
        this.sistemasdecontrolMin = sistemasdecontrolMin;
        this.cubiertadeprocesMin = cubiertadeprocesMin;
        this.buquetanquecompraMin = buquetanquecompraMin;
        this.buquetanquerentaMin = buquetanquerentaMin;

        this.plataformadesarrolloMp = plataformadesarrolloMp;
        this.lineadedescargaMp = lineadedescargaMp;
        this.estacioncompresionMp = estacioncompresionMp;
        this.ductoMp = ductoMp;
        this.bateriaMp = bateriaMp;
        this.arbolessubmarinosMp = arbolessubmarinosMp;
        this.manifoldsMp = manifoldsMp;
        this.risersMp = risersMp;
        this.sistemasdecontrolMp = sistemasdecontrolMp;
        this.cubiertadeprocesMp = cubiertadeprocesMp;
        this.buquetanquecompraMp = buquetanquecompraMp;
        this.buquetanquerentaMp = buquetanquerentaMp;

        this.plataformadesarrolloMax = plataformadesarrolloMax;
        this.lineadedescargaMax = lineadedescargaMax;
        this.estacioncompresionMax = estacioncompresionMax;
        this.ductoMax = ductoMax;
        this.bateriaMax = bateriaMax;
        this.arbolessubmarinosMax = arbolessubmarinosMax;
        this.manifoldsMax = manifoldsMax;
        this.risersMax = risersMax;
        this.sistemasdecontrolMax = sistemasdecontrolMax;
        this.cubiertadeprocesMax = cubiertadeprocesMax;
        this.buquetanquecompraMax = buquetanquecompraMax;
        this.buquetanquerentaMax = buquetanquerentaMax;
    }

    // Getters para los valores Min, MP, MAX
    public double getPlataformadesarrolloMin() {
        return plataformadesarrolloMin;
    }

    public double getLineadedescargaMin() {
        return lineadedescargaMin;
    }

    public double getEstacioncompresionMin() {
        return estacioncompresionMin;
    }

    public double getDuctoMin() {
        return ductoMin;
    }

    public double getBateriaMin() {
        return bateriaMin;
    }

    public double getPlataformadesarrolloMp() {
        return plataformadesarrolloMp;
    }

    public double getLineadedescargaMp() {
        return lineadedescargaMp;
    }

    public double getEstacioncompresionMp() {
        return estacioncompresionMp;
    }

    public double getDuctoMp() {
        return ductoMp;
    }

    public double getBateriaMp() {
        return bateriaMp;
    }

    public double getPlataformadesarrolloMax() {
        return plataformadesarrolloMax;
    }

    public double getLineadedescargaMax() {
        return lineadedescargaMax;
    }

    public double getEstacioncompresionMax() {
        return estacioncompresionMax;
    }

    public double getDuctoMax() {
        return ductoMax;
    }

    public double getBateriaMax() {
        return bateriaMax;
    }

    public double getArbolessubmarinosMin() {
        return arbolessubmarinosMin;
    }

    public double getManifoldsMin() {
        return manifoldsMin;
    }

    public double getRisersMin() {
        return risersMin;
    }

    public double getSistemasdecontrolMin() {
        return sistemasdecontrolMin;
    }

    public double getCubiertadeprocesMin() {
        return cubiertadeprocesMin;
    }

    public double getBuquetanquecompraMin() {
        return buquetanquecompraMin;
    }

    public double getBuquetanquerentaMin() {
        return buquetanquerentaMin;
    }

    public double getArbolessubmarinosMp() {
        return arbolessubmarinosMp;
    }

    public double getManifoldsMp() {
        return manifoldsMp;
    }

    public double getRisersMp() {
        return risersMp;
    }

    public double getSistemasdecontrolMp() {
        return sistemasdecontrolMp;
    }

    public double getCubiertadeprocesMp() {
        return cubiertadeprocesMp;
    }

    public double getBuquetanquecompraMp() {
        return buquetanquecompraMp;
    }

    public double getBuquetanquerentaMp() {
        return buquetanquerentaMp;
    }

    public double getArbolessubmarinosMax() {
        return arbolessubmarinosMax;
    }

    public double getManifoldsMax() {
        return manifoldsMax;
    }

    public double getRisersMax() {
        return risersMax;
    }

    public double getSistemasdecontrolMax() {
        return sistemasdecontrolMax;
    }

    public double getCubiertadeprocesMax() {
        return cubiertadeprocesMax;
    }

    public double getBuquetanquecompraMax() {
        return buquetanquecompraMax;
    }

    public double getBuquetanquerentaMax() {
        return buquetanquerentaMax;
    }



}
