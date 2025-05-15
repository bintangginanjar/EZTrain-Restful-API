package restful.api.eztrain.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import restful.api.eztrain.entity.RoleEntity;
import restful.api.eztrain.repository.RoleRepository;

@Component
public class RoleSeeder implements CommandLineRunner {

    private RoleRepository roleRepository;

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

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
    }    

}
