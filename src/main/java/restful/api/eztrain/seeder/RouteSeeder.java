package restful.api.eztrain.seeder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import restful.api.eztrain.entity.RouteEntity;
import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.repository.RouteRepository;
import restful.api.eztrain.repository.StationRepository;
import restful.api.eztrain.repository.UserRepository;

@Component
@Order(4)
public class RouteSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Override
    public void run(String... args) throws Exception {
        UserEntity adminUser = userRepository.findByEmail("admin@gmail.com").orElse(null);

        if (routeRepository.count() == 0) {
            List<StationEntity> stations = stationRepository.findAll();

            if (stations.size() < 10 || adminUser == null) {
                System.out.println("Not enough data to seed routes (requires 10 stations + 1 user).");
                return;
            }

            List<RouteEntity> routes = new ArrayList<>();

            routes.add(createRoute(stations.get(0), stations.get(1), 180.0, 3.0, adminUser));
            routes.add(createRoute(stations.get(1), stations.get(2), 120.0, 2.5, adminUser));
            routes.add(createRoute(stations.get(2), stations.get(3), 250.0, 6.0, adminUser));
            routes.add(createRoute(stations.get(3), stations.get(4), 170.0, 3.5, adminUser));
            routes.add(createRoute(stations.get(4), stations.get(5), 200.0, 3.6, adminUser));
            routes.add(createRoute(stations.get(5), stations.get(6), 90.0, 3.3, adminUser));
            routes.add(createRoute(stations.get(6), stations.get(7), 100.0, 1.8, adminUser));
            routes.add(createRoute(stations.get(7), stations.get(8), 130.0, 2.3, adminUser));
            routes.add(createRoute(stations.get(8), stations.get(9), 160.0, 6.0, adminUser));
            routes.add(createRoute(stations.get(9), stations.get(0), 300.0, 5.0, adminUser));

            routeRepository.saveAll(routes);
            System.out.println("10 routes seeded successfully.");
        }
    }

    private RouteEntity createRoute(StationEntity origin, StationEntity destination, Double distance, Double duration, UserEntity user) {
        RouteEntity route = new RouteEntity();
        route.setOrigin(origin);
        route.setDestination(destination);
        route.setTripDistance(distance);
        route.setTripDuration(duration);
        route.setUserEntity(user);

        return route;
    }
}
