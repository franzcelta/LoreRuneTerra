package com.loreruneterra.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Campeon {
    private final StringProperty key = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty titulo = new SimpleStringProperty();
    private final StringProperty imagen = new SimpleStringProperty();

    // Biografías
    private String bioCorta = "";
    private String bioCompleta = "";
    private String bioPrimera = "";

    public Campeon(String key, String nombre, String titulo, String imagen) {
        this.key.set(key);
        this.nombre.set(nombre);
        this.titulo.set(titulo);
        this.imagen.set(imagen);
    }

    // Propiedades básicas
    public String getKey() { return key.get(); }
    public StringProperty keyProperty() { return key; }

    public String getNombre() { return nombre.get(); }
    public StringProperty nombreProperty() { return nombre; }

    public String getTitulo() { return titulo.get(); }
    public StringProperty tituloProperty() { return titulo; }

    public String getImagen() { return imagen.get(); }
    public StringProperty imagenProperty() { return imagen; }

    private String imagenIcono = "";
    private String imagenSplash = "";

    // Getters y setters de biografías
    public String getBioCorta() { return bioCorta; }
    public void setBioCorta(String bioCorta) { this.bioCorta = bioCorta; }

    public String getBioCompleta() { return bioCompleta; }
    public void setBioCompleta(String bioCompleta) { this.bioCompleta = bioCompleta; }

    public String getBioPrimera() { return bioPrimera; }
    public void setBioPrimera(String bioPrimera) { this.bioPrimera = bioPrimera; }

    //---setters de icono
    public String getImagenIcono() { return imagenIcono; }
    public void setImagenIcono(String imagenIcono) { this.imagenIcono = imagenIcono; }

    public String getImagenSplash() { return imagenSplash; }
    public void setImagenSplash(String imagenSplash) { this.imagenSplash = imagenSplash; }
}