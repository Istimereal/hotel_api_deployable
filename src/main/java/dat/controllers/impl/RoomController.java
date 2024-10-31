package dat.controllers.impl;

import dat.config.HibernateConfig;
import dat.controllers.IController;
import dat.daos.impl.RoomDAO;
import dat.dtos.HotelDTO;
import dat.dtos.RoomDTO;
import dat.exceptions.ApiException;
import dat.exceptions.Message;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;


public class RoomController implements IController<RoomDTO, Integer> {

    private RoomDAO dao;

    public RoomController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dao = RoomDAO.getInstance(emf);
    }

    @Override
    public void read(Context ctx) throws ApiException {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            RoomDTO roomDTO = dao.read(id);
            ctx.res().setStatus(200);
            ctx.json(roomDTO, RoomDTO.class);
        } catch (NumberFormatException e) {
            ctx.json(new Message(400, "Missing required parameter: id"));
        }
    }

    @Override
    public void readAll(Context ctx) throws ApiException {
        List<RoomDTO> roomDTOS = dao.readAll();
        ctx.res().setStatus(200);
        ctx.json(roomDTOS, RoomDTO.class);
    }

    @Override
    public void create(Context ctx) throws ApiException {
        RoomDTO jsonRequest = ctx.bodyAsClass(RoomDTO.class);
        try {
            int hotelId = Integer.parseInt(ctx.pathParam("id"));
            HotelDTO hotelDTO = dao.addRoomToHotel(hotelId, jsonRequest);
            ctx.res().setStatus(201);
            ctx.json(hotelDTO, HotelDTO.class);
        }
        catch (NumberFormatException e) {
            throw new ApiException(400, "Missing required parameter: id");
        }
    }

    @Override
    public void update(Context ctx) throws ApiException {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            RoomDTO roomDTOfromJson = ctx.bodyAsClass(RoomDTO.class);
            RoomDTO roomDTO = dao.update(id, roomDTOfromJson);
            ctx.res().setStatus(200);
            ctx.json(roomDTO, RoomDTO.class);
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Missing required parameter: id");
        }
    }

    @Override
    public void delete(Context ctx) throws ApiException {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            dao.delete(id);
            ctx.res().setStatus(204);
            ctx.json("{\"message\": \"Room deleted successfully\"}");
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Missing required parameter: id");
        }
    }
}

