package restful.api.eztrain.seeder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import restful.api.eztrain.entity.RoleEntity;
import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.repository.RoleRepository;
import restful.api.eztrain.repository.StationRepository;
import restful.api.eztrain.repository.UserRepository;

@Component
public class CustomSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StationRepository stationRepository;

    @Override
    public void run(String... args) throws Exception {
        final String roleAdmin = "ROLE_ADMIN";
        final String roleUser = "ROLE_USER";

        if (roleRepository.count() == 0) {
            RoleEntity adminRole = new RoleEntity();
            adminRole.setName(roleAdmin);
            roleRepository.save(adminRole);
            System.out.println("Seeded role: " + roleAdmin);

            RoleEntity userRole = new RoleEntity();
            userRole.setName(roleUser);
            roleRepository.save(userRole);
            System.out.println("Seeded role: " + roleUser);
        }

        if (userRepository.count() == 0) {
            RoleEntity fetchAdminRole = roleRepository.findByName(roleAdmin).orElseThrow(null);
            UserEntity userAdmin = new UserEntity();            
            userAdmin.setEmail("admin@gmail.com");      
            userAdmin.setPassword("th3_k0p_4t_springboot");        
            userAdmin.setRoles(Collections.singletonList(fetchAdminRole));
            userAdmin.setIsVerified(false);
            userAdmin.setIsActive(false);            

            RoleEntity fetchUserRole = roleRepository.findByName(roleUser).orElseThrow(null);
            UserEntity userUser = new UserEntity();            
            userUser.setEmail("user@gmail.com");      
            userUser.setPassword("th3_k0p_4t_springboot");        
            userUser.setRoles(Collections.singletonList(fetchUserRole));
            userUser.setIsVerified(false);
            userUser.setIsActive(false);

            userRepository.save(userAdmin);
            userRepository.save(userUser);
        }

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
