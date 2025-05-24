package restful.api.eztrain.seeder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.repository.StationRepository;
import restful.api.eztrain.repository.UserRepository;

@Component
@Order(3)
public class StationSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StationRepository stationRepository;

    @Override
    public void run(String... args) throws Exception {
        UserEntity adminUser = userRepository.findByEmail("admin@gmail.com").orElse(null);

        if (stationRepository.count() == 0) {
            List<StationEntity> stations = new ArrayList<>();
            stations.add(createStation("GMR", "Gambir", "Jakarta", "DKI Jakarta", adminUser));
            stations.add(createStation("BD", "Bandung", "Bandung", "Jawa Barat", adminUser));
            stations.add(createStation("YK", "Yogyakarta", "Yogyakarta", "DI Yogyakarta", adminUser));
            stations.add(createStation("SGU", "Surabaya Gubeng", "Surabaya", "Jawa Timur", adminUser));
            stations.add(createStation("SMT", "Semarang Tawang", "Semarang", "Jawa Tengah", adminUser));
            stations.add(createStation("ML", "Malang", "Malang", "Jawa Timur", adminUser));
            stations.add(createStation("PDL", "Padalarang", "Bandung Barat", "Jawa Barat", adminUser));
            stations.add(createStation("CMB", "Cirebon", "Cirebon", "Jawa Barat", adminUser));
            stations.add(createStation("KTA", "Kutoarjo", "Purworejo", "Jawa Tengah", adminUser));
            stations.add(createStation("SR", "Solo Balapan", "Surakarta", "Jawa Tengah", adminUser));
            
            stationRepository.saveAll(stations);
            System.out.println("10 stations seeded successfully.");
        }        
    }

    private StationEntity createStation(String code, String name, String city, String province, UserEntity user) {
        StationEntity station = new StationEntity();
        station.setCode(code);
        station.setName(name);
        station.setCity(city);
        station.setProvince(province);
        station.setIsActive(true);
        station.setUserEntity(user);
        return station;
    }

}
