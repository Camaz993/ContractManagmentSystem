/** 
 * The authenticationfacade service class for accessing the authentication table in the database
 * @author Alice, Caleb, Laurie, Natalie, Poppy
 */
package contracts.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade implements IAuthenticationFacade {
 
    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
