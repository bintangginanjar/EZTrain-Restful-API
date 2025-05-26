package restful.api.eztrain.seeder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import restful.api.eztrain.entity.CoachEntity;
import restful.api.eztrain.entity.CoachTypeEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.repository.CoachRepository;
import restful.api.eztrain.repository.CoachTypeRepository;
import restful.api.eztrain.repository.UserRepository;

@Component
@Order(5)
public class CoachSeeder implements CommandLineRunner{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private CoachTypeRepository coachTypeRepository;

	@Override
	public void run(String... args) throws Exception {
		UserEntity adminUser = userRepository.findByEmail("admin@gmail.com").orElse(null);
        CoachTypeEntity coachEks = coachTypeRepository.findByName("Eksekutif").orElse(null);
        CoachTypeEntity coachPan = coachTypeRepository.findByName("Panoramic").orElse(null);
        CoachTypeEntity coachPre = coachTypeRepository.findByName("Premium").orElse(null);

        if (coachTypeRepository.count() == 0) {
            System.out.println("CoachType must be seeded first.");
            return;
        }

        if (coachRepository.count() == 0) {
            Integer iterNum = 5;

            List<CoachEntity> coaches = new ArrayList<>();
            
            for (int i = 1; i <= iterNum; i++) {
                coaches.add(createCoach(coachEks.getName() + " " + i, i, coachEks, true, adminUser));
                coaches.add(createCoach(coachPan.getName() + " " + i, i, coachPan, true, adminUser));
                coaches.add(createCoach(coachPre.getName() + " " + i, i, coachPre, true, adminUser));
            }

            coachRepository.saveAll(coaches);
            System.out.println(iterNum * 3 + " coaches seeded successfully.");
        }
	}

    private CoachEntity createCoach(String coachName, Integer coachNumber, CoachTypeEntity coachType, Boolean isActive, UserEntity user) {
        CoachEntity coach = new CoachEntity();
        coach.setCoachName(coachName);
        coach.setCoachNumber(coachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(isActive);
        coach.setUserEntity(user);

        return coach;
    }

    

}
