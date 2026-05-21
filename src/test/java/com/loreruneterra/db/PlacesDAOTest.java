package com.loreruneterra.db;

import com.loreruneterra.model.Lugar;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlacesDAOTest {

    private static PlacesDAO dao;

    @BeforeAll
    static void setUp() {
        dao = new PlacesDAO();
    }

    @Test
    @Order(1)
    @DisplayName("getAllLugares devuelve lista no vacía")
    void testGetAllLugares() {
        List<Lugar> lugares = dao.getAllLugares();
        assertNotNull(lugares, "La lista no debe ser null");
        assertFalse(lugares.isEmpty(), "Debe haber al menos un lugar en la BD");
    }

    @Test
    @Order(2)
    @DisplayName("getAllLugares devuelve exactamente 10 regiones")
    void testGetAllLugaresCount() {
        List<Lugar> lugares = dao.getAllLugares();
        assertEquals(10, lugares.size(), "Debe haber exactamente 10 regiones de Runeterra");
    }

    @Test
    @Order(3)
    @DisplayName("cada lugar tiene id, nombre no nulos")
    void testLugaresNoNulos() {
        List<Lugar> lugares = dao.getAllLugares();
        for (Lugar lugar : lugares) {
            assertTrue(lugar.getId() > 0, "El id debe ser mayor que 0");
            assertNotNull(lugar.getNombre(), "El nombre no debe ser null");
            assertFalse(lugar.getNombre().isEmpty(), "El nombre no debe estar vacío");
        }
    }

    @Test
    @Order(4)
    @DisplayName("Ionia está en la lista de regiones")
    void testContieneIonia() {
        List<Lugar> lugares = dao.getAllLugares();
        boolean tieneIonia = lugares.stream()
                .anyMatch(l -> "Ionia".equalsIgnoreCase(l.getNombre()));
        assertTrue(tieneIonia, "Ionia debe estar en la lista de regiones");
    }

    @Test
    @Order(5)
    @DisplayName("Demacia está en la lista de regiones")
    void testContieneDemacia() {
        List<Lugar> lugares = dao.getAllLugares();
        boolean tieneDemacia = lugares.stream()
                .anyMatch(l -> "Demacia".equalsIgnoreCase(l.getNombre()));
        assertTrue(tieneDemacia, "Demacia debe estar en la lista de regiones");
    }
}