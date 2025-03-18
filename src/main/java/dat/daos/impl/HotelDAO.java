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
            Hotel hotel = em.find(Hotel.class, integer);
            if (hotel == null) {
                throw new ApiException(404, "Hotel not found");
            }
            hotel.getRooms().forEach(room -> {
                em.remove(room);
            });
            em.remove(hotel);
            em.getTransaction().commit();

        } catch (Exception e) {
            // Roll back the transaction if it is still active, meaning an exception interrupted commit
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // Wrap and rethrow the exception as an ApiException
            throw new ApiException(500, "An error occurred while deleting the hotel");

        } finally {
            // Ensure the EntityManager is closed to free resources
            em.close();
        }
    }

}
