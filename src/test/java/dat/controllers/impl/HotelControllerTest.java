package dat.controllers.impl;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.daos.impl.HotelDAO;
import dat.daos.impl.HotelPopulatorDAO;
import dat.dtos.HotelDTO;
import dat.entities.Hotel;
import dat.security.controllers.SecurityController;
import dat.security.daos.SecurityPopulatorDAO;
import dat.security.daos.SecurityDAO;
import dat.security.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;
import io.javalin.Javalin;
import io.restassured.common.mapper.TypeRef;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HotelControllerTest {

    private final static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private final static SecurityController securityController = SecurityController.getInstance();
    private final static SecurityDAO securityDAO = new SecurityDAO(emf);
    private static Javalin app;
    private static Hotel[] hotels;
    private static Hotel california, hilton;
    private static UserDTO userDTO, adminDTO;
    private static String userToken, adminToken;
    private static final String BASE_URL = "http://localhost:7070/api";

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
        UserDTO[] users = SecurityPopulatorDAO.populateUsers(emf);
        userDTO = users[0];
        adminDTO = users[1];

        try {
            UserDTO verifiedUser = securityDAO.getVerifiedUser(userDTO.getUsername(), userDTO.getPassword());
            UserDTO verifiedAdmin = securityDAO.getVerifiedUser(adminDTO.getUsername(), adminDTO.getPassword());
            userToken = "Bearer " + securityController.createToken(verifiedUser);
            adminToken = "Bearer " + securityController.createToken(verifiedAdmin);
        }
        catch (ValidationException e) {
            throw new RuntimeException(e);
        }

    }

    @AfterEach
    void tearDown() {
        try (EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Room ").executeUpdate();
            em.createQuery("DELETE FROM Hotel ").executeUpdate();
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
    void readAll() {
        System.out.println("usertoken: " + userToken);
        System.out.println("admintoken: " + adminToken);
        List<HotelDTO> hotelDTO =
                given()
                        .when()
                        .header("Authorization", userToken)
                        .get(BASE_URL + "/hotels")
                        .then()
                        .statusCode(200)
                        .body("size()", is(2))
                        .log().all()
                        .extract()
                        .as(new TypeRef<List<HotelDTO>>() {});

        assertThat(hotelDTO.size(), is(2));
        assertThat(hotelDTO.get(0).getHotelName(), is("Hotel California"));
        assertThat(hotelDTO.get(1).getHotelName(), is("Hilton"));
    }


    @Test
    void read() {
        HotelDTO hotelDTO =
                given()
                        .when()
                        .header("Authorization", userToken)
                        .get(BASE_URL + "/hotels/"+ california.getId())
                        .then()
                        .statusCode(200)
                        .log().all()
                        .extract()
                        .as(HotelDTO.class);

        assertThat(hotelDTO, is(new HotelDTO(california)));
        assertThat(hotelDTO.getHotelName(), is("Hotel California"));
        assertThat(hotelDTO.getRooms().size(), is(6));
    }

    @Test
    void create() {
        HotelDTO hotelDTO = new HotelDTO("New Hotel", "New City", Hotel.HotelType.STANDARD);
        HotelDTO createdHotel =
                given()
                        .when()
                        .header("Authorization", userToken)
                        .contentType("application/json")
                        .body(hotelDTO)
                        .post(BASE_URL + "/hotels")
                        .then()
                        .statusCode(201)
                        .log().all()
                        .extract()
                        .as(HotelDTO.class);

        assertThat(createdHotel.getId(), is(3));
        assertThat(createdHotel.getHotelName(), is("New Hotel"));
        assertThat(createdHotel.getHotelAddress(), is("New City"));
        assertThat(createdHotel.getHotelType(), is(Hotel.HotelType.STANDARD));
    }

    @Test
    void update() {
        HotelDTO hotelDTO = new HotelDTO("New Hotel California", "New City", Hotel.HotelType.STANDARD);
        HotelDTO updatedHotel =
                given()
                        .when()
                        .contentType("application/json")
                        .body(hotelDTO)
                        .put(BASE_URL + "/hotels/" + california.getId())
                        .then()
                        .statusCode(200)
                        .log().all()
                        .extract()
                        .as(HotelDTO.class);

        assertThat(updatedHotel.getId(), is(1));
        assertThat(updatedHotel.getHotelName(), is("New Hotel California"));
        assertThat(updatedHotel.getHotelAddress(), is("New City"));
        assertThat(updatedHotel.getHotelType(), is(Hotel.HotelType.STANDARD));
    }

    @Test
    void delete() {

        given()
                .when()
                .delete(BASE_URL + "/hotels/" + california.getId())
                .then()
                .statusCode(204);

        List<HotelDTO> lastHotels =
        given()
                .when()
                .header("Authorization", userToken)
                .get(BASE_URL + "/hotels")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .extract()
                .as(new TypeRef<List<HotelDTO>>() {});
        assertThat(lastHotels.size(), is(1));
        assertThat(lastHotels, contains(new HotelDTO(hilton)));
    }

    @Test
    void populate() {
        try (EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Room ").executeUpdate();
            em.createQuery("DELETE FROM Hotel ").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE hotel_hotel_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE room_room_id_seq RESTART WITH 1").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        given()
                .when()
                .post(BASE_URL + "/hotels/populate")
                .then()
                .statusCode(200);

        List<HotelDTO> hotelDTOs =
                given()
                        .when()
                        .header("Authorization", userToken)
                        .get(BASE_URL + "/hotels")
                        .then()
                        .statusCode(200)
                        .body("size()", is(2))
                        .log().all()
                        .extract()
                        .as(new TypeRef<List<HotelDTO>>() {});

        assertThat(hotelDTOs.size(), is(2));
        assertThat(hotelDTOs, containsInAnyOrder(new HotelDTO(california), new HotelDTO(hilton)));
    }
}