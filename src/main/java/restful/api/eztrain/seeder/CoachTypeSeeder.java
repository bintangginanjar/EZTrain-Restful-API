package restful.api.eztrain.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import restful.api.eztrain.entity.CoachTypeEntity;
import restful.api.eztrain.repository.CoachTypeRepository;

@Component
public class CoachTypeSeeder implements CommandLineRunner {

    private CoachTypeRepository coachTypeRepository;

    public CoachTypeSeeder(CoachTypeRepository coachTypeRepository) {
        this.coachTypeRepository = coachTypeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        final String eks = "Eksekutif";
        final String pan = "Panoramic";
        final String prem = "Premium";
        
        if (coachTypeRepository.count() == 0) {
            CoachTypeEntity eksType = new CoachTypeEntity();
            eksType.setName(eks);
            coachTypeRepository.save(eksType);
            System.out.println("Seeded coach type: " + eks);    
        
            CoachTypeEntity panType = new CoachTypeEntity();
            panType.setName(pan);
            coachTypeRepository.save(panType);
            System.out.println("Seeded coach type: " + pan);
                
            CoachTypeEntity premType = new CoachTypeEntity();
            premType.setName(prem);
            coachTypeRepository.save(premType);
            System.out.println("Seeded coach type: " + prem);
        }
    }

}
