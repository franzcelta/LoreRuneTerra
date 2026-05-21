package com.loreruneterra.db;

import com.loreruneterra.model.Campeon;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChampionDAOTest {

    private static ChampionDAO dao;
    private static String testKey;

    @BeforeAll
    static void setUp() {
        dao = new ChampionDAO();
        testKey = "test_junit_" + System.currentTimeMillis();
    }

    @Test
    @Order(1)
    @DisplayName("getAllCampeones devuelve lista no vacía")
    void testGetAllCampeones() {
        List<Campeon> lista = dao.getAllCampeones();
        assertNotNull(lista, "La lista no debe ser null");
        assertFalse(lista.isEmpty(), "Debe haber al menos un campeón en la BD");
    }

    @Test
    @Order(2)
    @DisplayName("existsByKey devuelve false para key inexistente")
    void testExistsByKeyFalse() {
        assertFalse(dao.existsByKey("key_que_no_existe_jamás_12345"));
    }

    @Test
    @Order(3)
    @DisplayName("createCampeon inserta correctamente")
    void testCreateCampeon() {
        Campeon c = new Campeon(testKey, "Campeon Test JUnit", "Titulo Test", "");
        c.setClase("Mago");
        boolean ok = dao.createCampeon(c);
        assertTrue(ok, "El INSERT debe devolver true");
        assertTrue(c.getId() > 0, "El id debe haberse asignado tras el INSERT");
    }

    @Test
    @Order(4)
    @DisplayName("existsByKey devuelve true tras insertar")
    void testExistsByKeyTrue() {
        assertTrue(dao.existsByKey(testKey));
    }

    @Test
    @Order(5)
    @DisplayName("getCampeonByKey devuelve el campeón correcto")
    void testGetCampeonByKey() {
        Campeon c = dao.getCampeonByKey(testKey);
        assertNotNull(c, "Debe encontrar el campeón insertado");
        assertEquals("Campeon Test JUnit", c.getNombre());
        assertEquals("Mago", c.getClase());
    }

    @Test
    @Order(6)
    @DisplayName("updateCampeon actualiza correctamente")
    void testUpdateCampeon() {
        Campeon c = dao.getCampeonByKey(testKey);
        assertNotNull(c);
        c.setNombre("Campeon Test Actualizado");
        boolean ok = dao.updateCampeon(c);
        assertTrue(ok);
        Campeon actualizado = dao.getCampeonByKey(testKey);
        assertEquals("Campeon Test Actualizado", actualizado.getNombre());
    }

    @Test
    @Order(7)
    @DisplayName("saveBiografia hace upsert correctamente")
    void testSaveBiografia() {
        boolean ok = dao.saveBiografia(testKey,
                "Bio corta test",
                "Bio completa test",
                "Bio primera persona test");
        assertTrue(ok, "El upsert de biografía debe devolver true");
    }

    @Test
    @Order(8)
    @DisplayName("getBiografiaCorta devuelve el texto guardado")
    void testGetBiografiaCorta() {
        String bio = dao.getBiografiaCorta(testKey);
        assertEquals("Bio corta test", bio);
    }

    @Test
    @Order(9)
    @DisplayName("deleteCampeon elimina correctamente")
    void testDeleteCampeon() {
        Campeon c = dao.getCampeonByKey(testKey);
        assertNotNull(c);
        boolean ok = dao.deleteCampeon(c.getId());
        assertTrue(ok);
        assertFalse(dao.existsByKey(testKey));
    }

    @Test
    @Order(10)
    @DisplayName("searchByNombre devuelve resultados para texto parcial")
    void testSearchByNombre() {
        List<Campeon> resultados = dao.searchByNombre("Ahri");
        assertFalse(resultados.isEmpty(), "Debe encontrar al menos un resultado para 'Ahri'");
    }
}