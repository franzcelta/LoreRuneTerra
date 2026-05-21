package com.loreruneterra.db;

import com.loreruneterra.model.Campeon;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CampeonPersonalDAOTest {

    private static CampeonPersonalDAO dao;
    private static String testKey;

    @BeforeAll
    static void setUp() {
        dao = new CampeonPersonalDAO();
        testKey = "custom_junit_test_" + System.currentTimeMillis();
    }

    @Test
    @Order(1)
    @DisplayName("getAll devuelve lista no null")
    void testGetAll() {
        List<Campeon> lista = dao.getAll();
        assertNotNull(lista, "La lista no debe ser null");
    }

    @Test
    @Order(2)
    @DisplayName("existsByKey devuelve false para key inexistente")
    void testExistsByKeyFalse() {
        assertFalse(dao.existsByKey("custom_key_que_no_existe_jamás"));
    }

    @Test
    @Order(3)
    @DisplayName("create inserta campeón personalizado correctamente")
    void testCreate() {
        Campeon c = new Campeon(testKey, "Campeon Personal Test", "Titulo Test", "");
        c.setClase("Asesino");
        c.setBioCorta("Bio corta de prueba");
        c.setBioCompleta("Bio completa de prueba");
        c.setBioPrimera("Bio primera persona de prueba");
        boolean ok = dao.create(c);
        assertTrue(ok, "El INSERT debe devolver true");
    }

    @Test
    @Order(4)
    @DisplayName("existsByKey devuelve true tras insertar")
    void testExistsByKeyTrue() {
        assertTrue(dao.existsByKey(testKey));
    }

    @Test
    @Order(5)
    @DisplayName("getByKey devuelve el campeón correcto")
    void testGetByKey() {
        Campeon c = dao.getByKey(testKey);
        assertNotNull(c, "Debe encontrar el campeón insertado");
        assertEquals("Campeon Personal Test", c.getNombre());
        assertEquals("Asesino", c.getClase());
        assertEquals("Bio corta de prueba", c.getBioCorta());
    }

    @Test
    @Order(6)
    @DisplayName("update actualiza correctamente")
    void testUpdate() {
        Campeon c = dao.getByKey(testKey);
        assertNotNull(c);
        c.setNombre("Campeon Personal Actualizado");
        c.setClase("Mago");
        boolean ok = dao.update(c);
        assertTrue(ok);
        Campeon actualizado = dao.getByKey(testKey);
        assertEquals("Campeon Personal Actualizado", actualizado.getNombre());
        assertEquals("Mago", actualizado.getClase());
    }

    @Test
    @Order(7)
    @DisplayName("searchByNombre devuelve resultados para texto parcial")
    void testSearchByNombre() {
        List<Campeon> resultados = dao.searchByNombre("Campeon Personal");
        assertFalse(resultados.isEmpty(), "Debe encontrar al menos un resultado");
    }

    @Test
    @Order(8)
    @DisplayName("delete elimina correctamente")
    void testDelete() {
        boolean ok = dao.delete(testKey);
        assertTrue(ok);
        assertFalse(dao.existsByKey(testKey));
        assertNull(dao.getByKey(testKey));
    }
}