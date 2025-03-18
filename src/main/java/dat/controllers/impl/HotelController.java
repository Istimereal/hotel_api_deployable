package dat.controllers.impl;

import dat.config.HibernateConfig;
import dat.controllers.IController;
import dat.daos.impl.HotelDAO;
import dat.daos.impl.HotelPopulatorDAO;
import dat.dtos.HotelDTO;
import dat.entities.Hotel;
import dat.exceptions.ApiException;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.util.List;

public class HotelController implements IController<HotelDTO, Integer> {

    private final HotelDAO dao;

    public HotelController() {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        this.dao = HotelDAO.getInstance(emf);
    }

    @Override
    public void read(Context ctx) throws ApiException {

        try {
                int id = Integer.parseInt(ctx.pathParam("id"));
            HotelDTO hotelDTO = dao.read(id);
            ctx.res().setStatus(200);
            ctx.json(hotelDTO, HotelDTO.class);
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Missing required parameter: id");
        }
    }

    @Override
    public void readAll(Context ctx) throws ApiException {
        List<HotelDTO> hotelDTOS = dao.readAll();
        ctx.res().setStatus(200);
        ctx.json(hotelDTOS, HotelDTO.class);
    }

    @Override
    public void create(Context ctx) throws ApiException {
        HotelDTO jsonRequest = ctx.bodyAsClass(HotelDTO.class);
        HotelDTO hotelDTO = dao.create(jsonRequest);
        ctx.res().setStatus(201);
        ctx.json(hotelDTO, HotelDTO.class);
    }

    @Override
    public void update(Context ctx) throws ApiException {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            HotelDTO hotelDTOfromJson = ctx.bodyAsClass(HotelDTO.class);
            HotelDTO hotelDTO = dao.update(id, hotelDTOfromJson);
            ctx.res().setStatus(200);
            ctx.json(hotelDTO, HotelDTO.class);
            ctx.res().setStatus(200);
            ctx.json(hotelDTO, Hotel.class);
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
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Missing required parameter: id");
        }
    }

    public void populate(Context ctx) throws ApiException {
        try {
            Hotel[] hotels = HotelPopulatorDAO.populate();
            ctx.res().setStatus(200);
            ctx.json("{ \"message\": \"Database has been populated\" }");
        } catch (PersistenceException e) {
            throw new ApiException(400, "HotelPopulatorDAO went wrong, dude");
        }

    }
}

