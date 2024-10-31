package dat.daos.impl;

import dat.daos.IDAO;
import dat.dtos.HotelDTO;
import dat.entities.Hotel;
import dat.entities.Room;
import dat.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class HotelDAO implements IDAO<HotelDTO, Integer> {

    private static HotelDAO instance;
    private static EntityManagerFactory emf;

    Set<Room> calRooms = getCalRooms();
    Set<Room> hilRooms = getHilRooms();

    public static HotelDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new HotelDAO();
        }
        return instance;
    }

    @Override
    public HotelDTO read(Integer integer) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            Hotel hotel = em.find(Hotel.class, integer);
            if (hotel == null) {
                throw new ApiException(404, "Hotel not found");
            }
            return new HotelDTO(hotel);
        }
    }

    @Override
    public List<HotelDTO> readAll() throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<HotelDTO> query = em.createQuery("SELECT new dat.dtos.HotelDTO(h) FROM Hotel h", HotelDTO.class);
            return query.getResultList();
        } catch (PersistenceException e) {
            throw new ApiException(400, "Something went wrong during readAll");
        }
    }

    @Override
    public HotelDTO create(HotelDTO hotelDTO) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Hotel hotel = new Hotel(hotelDTO);
            em.persist(hotel);
            em.getTransaction().commit();
            return new HotelDTO(hotel);
        } catch (PersistenceException e) {
            throw new ApiException(400, "Hotel already exists or something else went wrong");
        }
    }

    @Override
    public HotelDTO update(Integer integer, HotelDTO hotelDTO) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Hotel h = em.find(Hotel.class, integer);
            if (h == null) {
                throw new ApiException(404, "Hotel with that id is not found");
            }
            h.setHotelName(hotelDTO.getHotelName());
            h.setHotelAddress(hotelDTO.getHotelAddress());
            h.setHotelType(hotelDTO.getHotelType());
            Hotel mergedHotel = em.merge(h);
            em.getTransaction().commit();
            return mergedHotel != null ? new HotelDTO(mergedHotel) : null;
        } catch (PersistenceException e) {
            throw new ApiException(400, "Hotel not found or something else went wrong during update");
        }
    }

    @Override
    public void delete(Integer integer) throws ApiException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Attempt to find the room entity by ID
            Room room = em.find(Room.class, integer);
            if (room == null) {
                throw new ApiException(404, "Room not found");
            }

            // Remove the room and attempt to commit the transaction
            em.remove(room);
            em.getTransaction().commit();

        } catch (Exception e) {
            // Roll back the transaction if it is still active, meaning an exception interrupted commit
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // Wrap and rethrow the exception as an ApiException
            throw new ApiException(500, "An error occurred while deleting the room");

        } finally {
            // Ensure the EntityManager is closed to free resources
            em.close();
        }
    }

    public void populate() throws ApiException {
        try (var em = emf.createEntityManager()) {
            em.getTransaction().begin();
                Hotel california = new Hotel("Hotel California", "California", Hotel.HotelType.LUXURY);
                Hotel hilton = new Hotel("Hilton", "Copenhagen", Hotel.HotelType.STANDARD);
                california.setRooms(calRooms);
                hilton.setRooms(hilRooms);
                em.persist(california);
                em.persist(hilton);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            throw new ApiException(400, "Populator went wrong, dude");
        }
    }

    private static Set<Room> getCalRooms() {
        Room r100 = new Room(100, new BigDecimal(2520), Room.RoomType.SINGLE);
        Room r101 = new Room(101, new BigDecimal(2520), Room.RoomType.SINGLE);
        Room r102 = new Room(102, new BigDecimal(2520), Room.RoomType.SINGLE);
        Room r103 = new Room(103, new BigDecimal(2520), Room.RoomType.SINGLE);
        Room r104 = new Room(104, new BigDecimal(3200), Room.RoomType.DOUBLE);
        Room r105 = new Room(105, new BigDecimal(4500), Room.RoomType.SUITE);

        Room[] roomArray = {r100, r101, r102, r103, r104, r105};
        return Set.of(roomArray);
    }

    private static Set<Room> getHilRooms() {
        Room r111 = new Room(111, new BigDecimal(2520), Room.RoomType.SINGLE);
        Room r112 = new Room(112, new BigDecimal(2520), Room.RoomType.SINGLE);
        Room r113 = new Room(113, new BigDecimal(2520), Room.RoomType.SINGLE);
        Room r114 = new Room(114, new BigDecimal(2520), Room.RoomType.DOUBLE);
        Room r115 = new Room(115, new BigDecimal(3200), Room.RoomType.DOUBLE);
        Room r116 = new Room(116, new BigDecimal(4500), Room.RoomType.SUITE);

        Room[] roomArray = {r111, r112, r113, r114, r115, r116};
        return Set.of(roomArray);
    }
}
