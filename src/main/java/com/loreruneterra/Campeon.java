package com.loreruneterra;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Campeon {
    private final StringProperty key = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty titulo = new SimpleStringProperty();
    private final StringProperty imagen = new SimpleStringProperty();

    public Campeon(String key, String nombre, String titulo, String imagen) {
        this.key.set(key);
        this.nombre.set(nombre);
        this.titulo.set(titulo);
        this.imagen.set(imagen);
    }

    public String getKey() { return key.get(); }
    public StringProperty keyProperty() { return key; }

    public String getNombre() { return nombre.get(); }
    public StringProperty nombreProperty() { return nombre; }

    public String getTitulo() { return titulo.get(); }
    public StringProperty tituloProperty() { return titulo; }

    public String getImagen() { return imagen.get(); }
    public StringProperty imagenProperty() { return imagen; }
}