package com.loreruneterra.db;

import com.loreruneterra.model.Campeon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Capa de acceso a datos (DAO) para la entidad Campeón.
 * Implementa las 4 operaciones CRUD contra PostgreSQL usando JDBC.
 * Todas las operaciones de escritura usan transacciones explícitas.
 * Todas las queries usan PreparedStatements (prevención de inyección SQL).
 */
public class ChampionDAO {

    // ══════════════════════════════════════════
    //  READ — Leer campeones
    // ══════════════════════════════════════════

    /**
     * Carga TODOS los campeones ordenados por nombre.
     * Incluye los nuevos campos: clase, lugar_id, dificultad.
     */
    public List<Campeon> getAllCampeones() {
        List<Campeon> lista = new ArrayList<>();
        String sql = """
                SELECT id, key, nombre, titulo, imagen, clase, lugar_id, dificultad
                FROM campeones
                ORDER BY nombre ASC
                """;
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar campeones: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca un campeón por su clave única (key de DataDragon).
     * Devuelve null si no existe.
     */
    public Campeon getCampeonByKey(String key) {
        String sql = """
                SELECT id, key, nombre, titulo, imagen, clase, lugar_id, dificultad
                FROM campeones
                WHERE key = ?
                """;
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error buscando campeón '" + key + "': " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca campeones cuyo nombre contiene el texto indicado (búsqueda simple).
     * Insensible a mayúsculas/minúsculas gracias a ILIKE de PostgreSQL.
     */
    public List<Campeon> searchByNombre(String texto) {
        List<Campeon> lista = new ArrayList<>();
        String sql = """
                SELECT id, key, nombre, titulo, imagen, clase, lugar_id, dificultad
                FROM campeones
                WHERE nombre ILIKE ?
                ORDER BY nombre ASC
                """;
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en búsqueda '" + texto + "': " + e.getMessage());
        }
        return lista;
    }

    /**
     * Comprueba si ya existe un campeón con esa key (usado por el importer
     * para evitar duplicados al importar desde DataDragon).
     */
    public boolean existsByKey(String key) {
        String sql = "SELECT 1 FROM campeones WHERE key = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error comprobando existencia de key '" + key + "': " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════
    //  CREATE — Insertar campeón
    // ══════════════════════════════════════════

    /**
     * Inserta un nuevo campeón en la BD con transacción explícita.
     * Actualiza el campo 'id' del objeto con el valor generado por la secuencia.
     *
     * @param campeon Objeto Campeon con los datos a insertar (id será ignorado)
     * @return true si la inserción fue exitosa, false en caso de error
     */
    public boolean createCampeon(Campeon campeon) {
        String sql = """
                INSERT INTO campeones (key, nombre, titulo, imagen, clase, lugar_id, dificultad)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);  // Inicio de transacción

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, campeon.getKey());
                ps.setString(2, campeon.getNombre());
                ps.setString(3, campeon.getTitulo());
                ps.setString(4, campeon.getImagen());
                ps.setString(5, campeon.getClase());
                //ps.setInt   (6, campeon.getLugarId());
                if (campeon.getLugarId() > 0) {
                    ps.setInt(6, campeon.getLugarId());
                } else {
                    ps.setNull(6, java.sql.Types.INTEGER);
                }
                ps.setInt   (7, campeon.getDificultad());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        campeon.setId(rs.getInt("id"));  // Guardar el ID generado
                    }
                }
            }

            conn.commit();  // Confirmar transacción
            System.out.println("Campeón creado: " + campeon.getNombre() + " (id=" + campeon.getId() + ")");
            return true;

        } catch (SQLException e) {
            System.err.println("Error creando campeón '" + campeon.getNombre() + "': " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignorar */ }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
            }
        }
    }

    // ══════════════════════════════════════════
    //  UPDATE — Actualizar campeón
    // ══════════════════════════════════════════

    /**
     * Actualiza todos los campos editables de un campeón existente.
     * Usa transacción explícita para garantizar consistencia.
     *
     * @param campeon Objeto con los nuevos valores (el id debe estar asignado)
     * @return true si se actualizó al menos 1 fila, false en caso contrario
     */
    public boolean updateCampeon(Campeon campeon) {
        String sql = """
                UPDATE campeones
                SET nombre = ?, titulo = ?, imagen = ?, clase = ?,
                    lugar_id = ?, dificultad = ?
                WHERE id = ?
                """;
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, campeon.getNombre());
                ps.setString(2, campeon.getTitulo());
                ps.setString(3, campeon.getImagen());
                ps.setString(4, campeon.getClase());
                //ps.setInt   (5, campeon.getLugarId());
                if (campeon.getLugarId() > 0) {
                    ps.setInt(5, campeon.getLugarId());
                } else {
                    ps.setNull(5, java.sql.Types.INTEGER);
                }
                ps.setInt   (6, campeon.getDificultad());
                ps.setInt   (7, campeon.getId());

                int filas = ps.executeUpdate();
                conn.commit();

                if (filas > 0) {
                    System.out.println("Campeón actualizado: " + campeon.getNombre());
                    return true;
                } else {
                    System.err.println("No se encontró campeón con id=" + campeon.getId());
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error actualizando campeón id=" + campeon.getId() + ": " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignorar */ }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
            }
        }
    }

    // ══════════════════════════════════════════
    //  DELETE — Eliminar campeón
    // ══════════════════════════════════════════

    /**
     * Elimina un campeón por su id.
     * La FK de 'biografias' tiene ON DELETE CASCADE, así que las biografías
     * asociadas se eliminan automáticamente por la BD.
     *
     * @param id Identificador primario del campeón a eliminar
     * @return true si se eliminó correctamente, false en caso de error
     */
    public boolean deleteCampeon(int id) {
        String sql = "DELETE FROM campeones WHERE id = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                int filas = ps.executeUpdate();
                conn.commit();

                if (filas > 0) {
                    System.out.println("Campeón eliminado (id=" + id + ")");
                    return true;
                } else {
                    System.err.println("No se encontró campeón con id=" + id + " para eliminar.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error eliminando campeón id=" + id + ": " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignorar */ }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
            }
        }
    }

    // ══════════════════════════════════════════
    //  BIOGRAFÍAS (métodos existentes, mejorados)
    // ══════════════════════════════════════════

    /** Carga la biografía CORTA más reciente de un campeón. */
    public String getBiografiaCorta(String keyCampeon) {
        return getBiografiaCampo(keyCampeon, "biografia_corta");
    }

    /** Carga la biografía COMPLETA más reciente de un campeón. */
    public String getBiografiaCompleta(String keyCampeon) {
        return getBiografiaCampo(keyCampeon, "biografia_completa");
    }

    /** Carga la biografía en PRIMERA PERSONA más reciente (solo para algunos campeones). */
    public String getBiografiaPrimeraPersona(String keyCampeon) {
        return getBiografiaCampo(keyCampeon, "biografia_primera_persona");
    }

    /** Método privado genérico para leer cualquier columna de la tabla biografias. */
    private String getBiografiaCampo(String keyCampeon, String campo) {
        String sql = String.format("""
                SELECT b.%s
                FROM biografias b
                JOIN campeones c ON b.campeon_id = c.id
                WHERE c.key = ?
                ORDER BY b.ultima_actualizacion DESC
                LIMIT 1
                """, campo);
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyCampeon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(campo);
            }
        } catch (SQLException e) {
            System.err.println("Error cargando " + campo + " para '" + keyCampeon + "': " + e.getMessage());
        }
        return "";
    }

    /**
     * Guarda o actualiza las 3 biografías de un campeón.
     * Usa INSERT ... ON CONFLICT para hacer upsert (insertar o actualizar).
     * Mantiene el comportamiento original pero añade transacción explícita.
     */
    public boolean saveBiografia(String keyCampeon,
                                 String textoCorto,
                                 String textoCompleto,
                                 String textoPrimeraPersona) {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            // Obtener el id del campeón
            int campeonId;
            try (PreparedStatement psId = conn.prepareStatement(
                    "SELECT id FROM campeones WHERE key = ?")) {
                psId.setString(1, keyCampeon);
                try (ResultSet rs = psId.executeQuery()) {
                    if (!rs.next()) {
                        System.err.println("Campeón no encontrado: " + keyCampeon);
                        conn.rollback();
                        return false;
                    }
                    campeonId = rs.getInt("id");
                }
            }

            // Upsert de las 3 biografías
            String sql = """
                    INSERT INTO biografias (campeon_id, biografia_corta, biografia_completa,
                                            biografia_primera_persona, ultima_actualizacion)
                    VALUES (?, ?, ?, ?, CURRENT_DATE)
                    ON CONFLICT (campeon_id) DO UPDATE SET
                        biografia_corta            = EXCLUDED.biografia_corta,
                        biografia_completa         = EXCLUDED.biografia_completa,
                        biografia_primera_persona  = EXCLUDED.biografia_primera_persona,
                        ultima_actualizacion       = CURRENT_DATE
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt   (1, campeonId);
                ps.setString(2, textoCorto);
                ps.setString(3, textoCompleto);
                ps.setString(4, textoPrimeraPersona);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Biografías guardadas para: " + keyCampeon);
            return true;

        } catch (SQLException e) {
            System.err.println("Error guardando biografías de '" + keyCampeon + "': " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignorar */ }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
            }
        }
    }

    // ══════════════════════════════════════════
    //  Helper privado — mapear ResultSet → Campeon
    // ══════════════════════════════════════════

    /** Convierte una fila del ResultSet en un objeto Campeon. */
    private Campeon mapRow(ResultSet rs) throws SQLException {
        return new Campeon(
                rs.getInt   ("id"),
                rs.getString("key"),
                rs.getString("nombre"),
                rs.getString("titulo"),
                rs.getString("imagen"),
                rs.getString("clase"),
                rs.getInt   ("lugar_id"),
                rs.getInt   ("dificultad")
        );
    }
}