package in.co.avis.Vehicle_Reservation_Producer.repository;


import in.co.avis.Vehicle_Reservation_Producer.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, Integer> {
}

