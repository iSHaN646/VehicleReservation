package in.co.avis.Vehicle_Reservation_Producer.service;

import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.entity.UserInfo;
import in.co.avis.Vehicle_Reservation_Producer.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsersServiceDetails implements UserDetailsService {

    private final UserRepository userRepository;

    public UsersServiceDetails(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Email is: " + email);

        User theUser = userRepository.findByEmail(email);
        if(theUser == null){
            throw  new UsernameNotFoundException("user not found");
        }

        return new UserInfo(theUser);
    }
}

