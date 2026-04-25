package com.loreruneterra.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo de entidad Campeón.
 * Refleja la tabla 'campeones' de PostgreSQL.
 * Usa JavaFX Properties para binding con ListView/TableView.
 */
public class Campeon {

    // --- Clave primaria de la BD (autoincremental) ---
    private int id;

    // --- Propiedades JavaFX (binding con UI) ---
    private final StringProperty key    = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty titulo = new SimpleStringProperty();
    private final StringProperty imagen = new SimpleStringProperty();

    // --- Campos adicionales (sin binding, solo getters/setters) ---
    private String clase      = "";
    private int    lugarId    = 0;
    private int    dificultad = 0;

    // --- Imágenes locales ---
    private String imagenIcono  = "";
    private String imagenSplash = "";

    // --- Biografías (cargadas bajo demanda desde ChampionDAO) ---
    private String bioCorta    = "";
    private String bioCompleta = "";
    private String bioPrimera  = "";

    // ══════════════════════════════════════════
    //  Constructores
    // ══════════════════════════════════════════

    /** Constructor completo — usado por ChampionDAO al leer de la BD. */
    public Campeon(int id, String key, String nombre, String titulo,
                   String imagen, String clase, int lugarId, int dificultad) {
        this.id = id;
        this.key.set(key);
        this.nombre.set(nombre);
        this.titulo.set(titulo);
        this.imagen.set(imagen);
        this.clase     = clase     != null ? clase : "";
        this.lugarId   = lugarId;
        this.dificultad = dificultad;
    }

    /** Constructor reducido — compatibilidad con código existente (importer, etc.). */
    public Campeon(String key, String nombre, String titulo, String imagen) {
        this(0, key, nombre, titulo, imagen, "", 0, 0);
    }

    // ══════════════════════════════════════════
    //  Getters / Setters — id
    // ══════════════════════════════════════════
    public int getId()          { return id; }
    public void setId(int id)   { this.id = id; }

    // ══════════════════════════════════════════
    //  JavaFX Properties — key, nombre, titulo, imagen
    // ══════════════════════════════════════════
    public String getKey()                  { return key.get(); }
    public void setKey(String v)            { key.set(v); }
    public StringProperty keyProperty()     { return key; }

    public String getNombre()               { return nombre.get(); }
    public void setNombre(String v)         { nombre.set(v); }
    public StringProperty nombreProperty()  { return nombre; }

    public String getTitulo()               { return titulo.get(); }
    public void setTitulo(String v)         { titulo.set(v); }
    public StringProperty tituloProperty()  { return titulo; }

    public String getImagen()               { return imagen.get(); }
    public void setImagen(String v)         { imagen.set(v); }
    public StringProperty imagenProperty()  { return imagen; }

    // ══════════════════════════════════════════
    //  Clase, lugar, dificultad
    // ══════════════════════════════════════════
    public String getClase()                { return clase; }
    public void setClase(String clase)      { this.clase = clase != null ? clase : ""; }

    public int getLugarId()                 { return lugarId; }
    public void setLugarId(int lugarId)     { this.lugarId = lugarId; }

    public int getDificultad()              { return dificultad; }
    public void setDificultad(int d)        { this.dificultad = d; }

    // ══════════════════════════════════════════
    //  Imágenes locales
    // ══════════════════════════════════════════
    public String getImagenIcono()               { return imagenIcono; }
    public void setImagenIcono(String v)         { this.imagenIcono = v != null ? v : ""; }

    public String getImagenSplash()              { return imagenSplash; }
    public void setImagenSplash(String v)        { this.imagenSplash = v != null ? v : ""; }

    // ══════════════════════════════════════════
    //  Biografías
    // ══════════════════════════════════════════
    public String getBioCorta()              { return bioCorta; }
    public void setBioCorta(String v)        { this.bioCorta = v != null ? v : ""; }

    public String getBioCompleta()           { return bioCompleta; }
    public void setBioCompleta(String v)     { this.bioCompleta = v != null ? v : ""; }

    public String getBioPrimera()            { return bioPrimera; }
    public void setBioPrimera(String v)      { this.bioPrimera = v != null ? v : ""; }

    // ══════════════════════════════════════════
    //  toString — útil para debug
    // ══════════════════════════════════════════
    @Override
    public String toString() {
        return String.format("Campeon{id=%d, key='%s', nombre='%s', clase='%s'}",
                id, getKey(), getNombre(), clase);
    }
}