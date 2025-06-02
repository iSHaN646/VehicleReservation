package in.co.avis.Vehicle_Reservation_Producer.graphqlController;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.service.CarService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class CarGraphController {

    private final CarService carService;

    public CarGraphController(CarService carService) {
        this.carService = carService;
    }

    @QueryMapping
    public List<Car> findAll() {
        return carService.getAllCars();
    }

    @QueryMapping
    public Car findOne(@Argument int id) {
        return carService.getCarById(id);
    }

    @MutationMapping
    public Car create(@Argument String name,@Argument String model,@Argument String type,@Argument String status,@Argument Float pricePerDay) {
        Car car = new Car();
        car.setName(name);
        car.setModel(model);
        car.setType(type);
        car.setStatus(Car.CarStatus.valueOf(status));
        car.setPricePerDay(BigDecimal.valueOf(pricePerDay));
        carService.addCar(car);
        return car;
    }

    @MutationMapping
    public Car update(@Argument int id,@Argument String name,@Argument String model,@Argument String type,@Argument String status,@Argument Float pricePerDay) {
        Car car = carService.getCarById(id);
        car.setName(name);
        car.setModel(model);
        car.setType(type);
        car.setStatus(Car.CarStatus.valueOf(status));
        car.setPricePerDay(BigDecimal.valueOf(pricePerDay));
        carService.updateCar(car);
        return car;
    }

    @MutationMapping
    public Car delete(@Argument int id) {
        Car car = carService.getCarById(id);
        carService.deleteCar(id);
        return car;
    }
}
