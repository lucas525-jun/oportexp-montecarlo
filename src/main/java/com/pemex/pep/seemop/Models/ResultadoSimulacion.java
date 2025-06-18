package com.pemex.pep.seemop.Models;

import java.util.ArrayList;
import java.util.List;

public class ResultadoSimulacion {
    private int iteracion;
    private String resultado;
    private double pce;
    private double gastoInicial;
    private double declinacion;
    private double area;
    private double ExploratoriaInfra;
    private double ExploratoriaPerf;
    private double ExploratoriaTer;
    private double DesarrolloInfra;
    private double DesarrolloPerf;
    private double DesarrolloTer;
    private double bateria;
    private double plataformaDesarrollo;
    private double LineaDescarga;
    private double EComprension;
    private double ducto;

    // Nuevos atributos
    private double arbolesSubmarinos;
    private double manifolds;
    private double risers;
    private double sistemasDeControl;
    private double cubiertaDeProces;
    private double buqueTanqueCompra;
    private double buqueTanqueRenta;

    private List<CtoAnualResultado> CtoAnualRes = new ArrayList<CtoAnualResultado>();

    // Getters y Setters existentes
    public double getPce() { return pce; }
    public void setPce(double recurso) { this.pce = recurso; }

    public double getGastoInicial() { return gastoInicial; }
    public void setGastoInicial(double gastoTriangular) { this.gastoInicial = gastoTriangular; }

    public double getDeclinacion() { return declinacion; }
    public void setDeclinacion(double declinacion) { this.declinacion = declinacion; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public double getExploratoriaInfra() { return ExploratoriaInfra; }
    public void setExploratoriaInfra(double ExploratoriaInfra) { this.ExploratoriaInfra = ExploratoriaInfra; }

    public double getExploratoriaPerf() { return ExploratoriaPerf; }
    public void setExploratoriaPerf(double ExploratoriaPerf) { this.ExploratoriaPerf = ExploratoriaPerf; }

    public double getExploratoriaTer() { return ExploratoriaTer; }
    public void setExploratoriaTer(double ExploratoriaTer) { this.ExploratoriaTer = ExploratoriaTer; }

    public double getDesarrolloInfra() { return DesarrolloInfra; }
    public void setDesarrolloInfra(double DesarrolloInfra) { this.DesarrolloInfra = DesarrolloInfra; }

    public double getDesarrolloPerf() { return DesarrolloPerf; }
    public void setDesarrolloPerf(double DesarrolloPerf) { this.DesarrolloPerf = DesarrolloPerf; }

    public double getDesarrolloTer() { return DesarrolloTer; }
    public void setDesarrolloTer(double DesarrolloTer) { this.DesarrolloTer = DesarrolloTer; }

    public double getBateria() { return bateria; }
    public void setBateria(double bateria) { this.bateria = bateria; }

    public double getPlataformaDesarrollo() { return plataformaDesarrollo; }
    public void setPlataformaDesarrollo(double plataformaDesarrollo) { this.plataformaDesarrollo = plataformaDesarrollo; }

    public double getLineaDescarga() { return LineaDescarga; }
    public void setLineaDescarga(double LineaDescarga) { this.LineaDescarga = LineaDescarga; }

    public double getEComprension() { return EComprension; }
    public void setEComprension(double EComprension) { this.EComprension = EComprension; }

    public double getDucto() { return ducto; }
    public void setDucto(double ducto) { this.ducto = ducto; }

    // Nuevos getters y setters
    public double getArbolesSubmarinos() { return arbolesSubmarinos; }
    public void setArbolesSubmarinos(double arbolesSubmarinos) { this.arbolesSubmarinos = arbolesSubmarinos; }

    public double getManifolds() { return manifolds; }
    public void setManifolds(double manifolds) { this.manifolds = manifolds; }

    public double getRisers() { return risers; }
    public void setRisers(double risers) { this.risers = risers; }

    public double getSistemasDeControl() { return sistemasDeControl; }
    public void setSistemasDeControl(double sistemasDeControl) { this.sistemasDeControl = sistemasDeControl; }

    public double getCubiertaDeProces() { return cubiertaDeProces; }
    public void setCubiertaDeProces(double cubiertaDeProces) { this.cubiertaDeProces = cubiertaDeProces; }

    public double getBuqueTanqueCompra() { return buqueTanqueCompra; }
    public void setBuqueTanqueCompra(double buqueTanqueCompra) { this.buqueTanqueCompra = buqueTanqueCompra; }

    public double getBuqueTanqueRenta() { return buqueTanqueRenta; }
    public void setBuqueTanqueRenta(double buqueTanqueRenta) { this.buqueTanqueRenta = buqueTanqueRenta; }

    // Getter y setter para lista
    public List<CtoAnualResultado> getCtoAnualList() {
        return CtoAnualRes;
    }

    public void setCtoAnualList(List<CtoAnualResultado> ctoAnualList) {
        this.CtoAnualRes = ctoAnualList;
    }
}
