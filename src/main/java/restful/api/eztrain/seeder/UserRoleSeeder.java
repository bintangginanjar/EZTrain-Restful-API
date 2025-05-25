package restful.api.eztrain.seeder;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import restful.api.eztrain.entity.RoleEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.repository.RoleRepository;
import restful.api.eztrain.repository.UserRepository;

@Component
@Order(1)
public class UserRoleSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

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
            userAdmin.setPassword(passwordEncoder.encode("rahasia"));        
            userAdmin.setRoles(Collections.singletonList(fetchAdminRole));
            userAdmin.setIsVerified(false);
            userAdmin.setIsActive(false);
            userRepository.save(userAdmin);            

            RoleEntity fetchUserRole = roleRepository.findByName(roleUser).orElseThrow(null);
            UserEntity userUser = new UserEntity();            
            userUser.setEmail("user@gmail.com");      
            userUser.setPassword(passwordEncoder.encode("rahasia"));        
            userUser.setRoles(Collections.singletonList(fetchUserRole));
            userUser.setIsVerified(false);
            userUser.setIsActive(false);            
            userRepository.save(userUser);
        }
        
    }

}
