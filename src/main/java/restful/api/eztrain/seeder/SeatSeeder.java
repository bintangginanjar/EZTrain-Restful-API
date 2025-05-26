package restful.api.eztrain.seeder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import restful.api.eztrain.entity.SeatEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.repository.SeatRepository;
import restful.api.eztrain.repository.UserRepository;

@Component
@Order(6)
public class SeatSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Override
    public void run(String... args) throws Exception {
        UserEntity adminUser = userRepository.findByEmail("admin@gmail.com").orElse(null);

        Integer iterNum = 20;
        
        if (seatRepository.count() == 0) {
            List<SeatEntity> seats = new ArrayList<>();

            for (int i = 1; i <= iterNum; i++) {
                seats.add(createSeat(i + "A", adminUser));
                seats.add(createSeat(i + "B", adminUser));
                seats.add(createSeat(i + "C", adminUser));
                seats.add(createSeat(i + "D", adminUser));
            }

            seatRepository.saveAll(seats);
            System.out.println(iterNum * 4 + " seats seeded successfully");
        }        
    }

    private SeatEntity createSeat(String seatNumber, UserEntity user) {
        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber(seatNumber);
        seat.setUserEntity(user);

        return seat;
    }

}
