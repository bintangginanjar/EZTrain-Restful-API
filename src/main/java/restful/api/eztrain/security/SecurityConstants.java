package restful.api.eztrain.security;

import org.springframework.beans.factory.annotation.Value;

public class SecurityConstants {

    @Value("${jwt.expiration}")
    private static Integer jwtExpiration;

    @Value("${jwt.secret}")
    private static String jwtSecret;

    public static final Integer JWTexpiration = jwtExpiration;      

    public static final String JWTsecret = jwtSecret;    

}
