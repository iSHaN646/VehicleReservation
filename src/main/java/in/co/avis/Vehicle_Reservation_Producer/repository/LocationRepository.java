package in.co.avis.Vehicle_Reservation_Producer.repository;


import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    Page<Location> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCityContainingIgnoreCaseOrStateContainingIgnoreCaseOrZipContainingIgnoreCase(
            String name, String address, String city, String state,String zip,Pageable pageable);
}

