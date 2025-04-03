package dat.routes;

import dat.config.HibernateConfig;
import dat.utils.Populator;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import static io.javalin.apibuilder.ApiBuilder.get;

public class PopulatorRoutes {


    protected EndpointGroup getRoutes() {

        // Obs: denne ville ikke fungere med test, da den er bundet til den "rigtige db"
        // Alternativt skal man lave en constructor og skylde emf ind udefra
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        return () -> {
            get("/", ctx -> {
                    Populator.populateUsers(emf);
                    ctx.result("Populated");
                });
        };
    }
}
