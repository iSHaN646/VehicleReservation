package in.co.avis.Vehicle_Reservation_Producer.repository;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CarRepository extends JpaRepository<Car, Integer> {
    List<Car> findByStatus(Car.CarStatus status);
    Page<Car> findByNameContainingIgnoreCaseOrModelContainingIgnoreCaseOrTypeContainingIgnoreCase(
            String name, String model, String type, Pageable pageable);


}

