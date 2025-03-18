package dat.daos.impl;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.dtos.HotelDTO;
import dat.entities.Hotel;
import dat.exceptions.ApiException;
import io.javalin.Javalin;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HotelDAOTest {

    private final static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static Javalin app;
    private static Hotel[] hotels;
    private static Hotel california, hilton;
    private static final String BASE_URL = "http://localhost:7070/api";
    private static final HotelDAO hotelDAO = HotelDAO.getInstance(emf);

    @BeforeAll
    void setUpAll() {
        HibernateConfig.setTest(true);

        // Start api
        app = ApplicationConfig.startServer(7070);
    }

    @BeforeEach
    void setUp() {
        // Populate the database with hotels and rooms
        System.out.println("Populating database with hotels and rooms");
        hotels = HotelPopulatorDAO.populate();
        california = hotels[0];
        hilton = hotels[1];
    }

    @AfterEach
    void tearDown() {
        try (EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Room ").executeUpdate();
            em.createQuery("DELETE FROM Hotel ").executeUpdate();
            em.createQuery("DELETE FROM User ").executeUpdate();
            em.createQuery("DELETE FROM Role ").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    void tearDownAll() {
        ApplicationConfig.stopServer(app);
    }

    @Test
    void getInstance() {
        HotelDAO hotelDAO = HotelDAO.getInstance(emf);
        assertNotNull(hotelDAO);
    }

    @Test
    void read() throws ApiException {
        HotelDTO hotelDTO = hotelDAO.read(california.getId());
        assertEquals(california.getId(), hotelDTO.getId());
    }

    @Test
    void readNotFound() {
        assertThrows(ApiException.class, () -> hotelDAO.read(100));
    }

    @Test
    void readAll() throws ApiException {
        List<HotelDTO> hotelDTOS = hotelDAO.readAll();
        assertEquals(2, hotelDTOS.size());
        HotelDTO californiaDTO = hotelDTOS.get(0);
        HotelDTO hiltonDTO = hotelDTOS.get(1);
        assertThat(hotelDTOS, containsInAnyOrder(californiaDTO, hiltonDTO));
    }

    @Test
    void create() throws ApiException {
        HotelDTO dangle = new HotelDTO("Hotel Dangleterre", "Kgs Nytorv", Hotel.HotelType.LUXURY);
        HotelDTO hotelDTO = hotelDAO.create(dangle);
        assertThat(hotelDTO.getHotelName(), is("Hotel Dangleterre"));
        assertThat(hotelDTO.getHotelAddress(), is("Kgs Nytorv"));
        assertThat(hotelDTO.getHotelType(), is(Hotel.HotelType.LUXURY));
    }

    @Test
    void update() throws ApiException {
        HotelDTO resultDTO = hotelDAO.update(california.getId(), new HotelDTO("Hotel Californias", "Californiana", Hotel.HotelType.STANDARD));
        assertThat(resultDTO.getHotelName(), is("Hotel Californias"));
        assertThat(resultDTO.getHotelAddress(), is("Californiana"));
        assertThat(resultDTO.getHotelType(), is(Hotel.HotelType.STANDARD));
    }

    @Test
    void delete() {
        assertDoesNotThrow(() -> hotelDAO.delete(california.getId()));
        assertThrows(ApiException.class, () -> hotelDAO.read(california.getId()));
    }
}