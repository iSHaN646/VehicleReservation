package in.co.avis.Vehicle_Reservation_Producer.repository;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(String email);
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRoleContainingIgnoreCase(
            String name, String email, String role, Pageable pageable);
}

