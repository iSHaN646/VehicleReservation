package in.co.avis.Vehicle_Reservation_Producer.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserInfo implements UserDetails {

    private final User theUser;

    public UserInfo(User theUser) {
        this.theUser = theUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_"+ theUser.getRole()));
    }

    @Override
    public String getPassword() {
        return theUser.getPassword();
    }

    public User getUser(){
        return this.theUser;
    }

    @Override
    public String getUsername() {
        return theUser.getEmail();
    }
}
