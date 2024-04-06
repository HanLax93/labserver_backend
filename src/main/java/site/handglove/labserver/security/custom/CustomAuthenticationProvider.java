package site.handglove.labserver.security.custom;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.util.Assert;

import site.handglove.labserver.exception.CustomAuthenticationException;

public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    private UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();
    private UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

    @Override
    public Authentication authenticate(Authentication authentication) {
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                () -> this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.onlySupports",
                        "Only UsernamePasswordAuthenticationToken is supported"));
        String username = (String) authentication.getPrincipal();
        UserDetails user = null;
        try {
            user = retrieveUser(username, (UsernamePasswordAuthenticationToken) authentication);
        } catch (UsernameNotFoundException ex) {
            throw ex;
        }

        try {
            this.preAuthenticationChecks.check(user);
            additionalAuthenticationChecks(user, (UsernamePasswordAuthenticationToken) authentication);
        } catch (AuthenticationException ex) {
            throw ex;
        }

        this.postAuthenticationChecks.check(user);
        Object principalToReturn = user;

        return createSuccessAuthentication(principalToReturn, authentication, user);
    }

    private class DefaultPreAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails user) {
            if (!user.isAccountNonLocked()) {
                throw new CustomAuthenticationException("账户已锁定");
            }
            if (!user.isEnabled()) {
                throw new CustomAuthenticationException("账户已启用");
            }
            if (!user.isAccountNonExpired()) {
                throw new CustomAuthenticationException("账户已过期");
            }
        }
    }

    private class DefaultPostAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails user) {
            if (!user.isCredentialsNonExpired()) {
                throw new CustomAuthenticationException("账户凭证已过期");
            }
        }
    }
}
