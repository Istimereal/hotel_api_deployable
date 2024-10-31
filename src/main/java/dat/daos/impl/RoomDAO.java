package dat.daos.impl;


import dat.daos.IDAO;
import dat.dtos.HotelDTO;
import dat.dtos.RoomDTO;
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

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class RoomDAO implements IDAO<RoomDTO, Integer> {

    private static RoomDAO instance;
    private static EntityManagerFactory emf;

    public static RoomDAO getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new RoomDAO();
        }
        return instance;
    }

    public HotelDTO addRoomToHotel(Integer hotelId, RoomDTO roomDTO ) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
                Room room = new Room(roomDTO);
                Hotel hotel = em.find(Hotel.class, hotelId);
                if (hotel == null) {
                    throw new ApiException(404, "Hotel not found");
                }
                hotel.addRoom(room);
                em.persist(room);
                Hotel mergedHotel = em.merge(hotel);
            em.getTransaction().commit();
            return new HotelDTO(mergedHotel);
        }
    }

    @Override
    public RoomDTO read(Integer integer) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            Room room = em.find(Room.class, integer);
            if (room == null) {
                throw new ApiException(404, "Room not found");
            }
            return  new RoomDTO(room);
        } catch (PersistenceException e) {
            throw new ApiException(400, "Something went wrong during reading rooms");
        }
    }

    @Override
    public List<RoomDTO> readAll() throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<RoomDTO> query = em.createQuery("SELECT new dat.dtos.RoomDTO(r) FROM Room r", RoomDTO.class);
            return query.getResultList();
        } catch (PersistenceException e) {
            throw new ApiException(400, "Something went wrong during reading rooms");
        }
    }

    @Override
    public RoomDTO create(RoomDTO roomDTO) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
                Room room = new Room(roomDTO);
                em.persist(room);
            em.getTransaction().commit();
            return new RoomDTO(room);
        } catch (PersistenceException e) {
            throw new ApiException(400, "Room already exists or something else went wrong");
        }
    }

    @Override
    public RoomDTO update(Integer integer, RoomDTO roomDTO) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
                Room r = em.find(Room.class, integer);
                r.setRoomNumber(roomDTO.getRoomNumber());
                r.setRoomType(roomDTO.getRoomType());
                r.setRoomPrice(BigDecimal.valueOf(roomDTO.getRoomPrice()));
                Room mergedRoom = em.merge(r);
            em.getTransaction().commit();
            return new RoomDTO(mergedRoom);
        } catch (PersistenceException e) {
            throw new ApiException(400, "Room not found or something else went wrong during update");
        }
    }

    @Override
    public void delete(Integer integer) throws ApiException {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Room room = em.find(Room.class, integer);
            if (room == null) {
                throw new ApiException(404, "Room not found");
            }
            em.remove(room);
            em.getTransaction().commit();
        }
    }
}
