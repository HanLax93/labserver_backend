package site.handglove.labserver.security.custom;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import site.handglove.labserver.model.User;

public class CustomUser extends org.springframework.security.core.userdetails.User {
    private User user;

    public CustomUser(User user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getUsername(), user.getPasswordHash(), authorities);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
