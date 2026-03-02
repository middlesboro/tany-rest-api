package sk.tany.rest.api.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class MagicLinkAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;

    public MagicLinkAuthenticationToken(String exchangeToken) {
        super((Collection<? extends GrantedAuthority>) null);
        this.principal = exchangeToken;
        setAuthenticated(false);
    }

    public MagicLinkAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
