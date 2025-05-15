package in.co.avis.Vehicle_Reservation_Producer.repository;

import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}

