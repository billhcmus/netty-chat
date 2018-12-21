package server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Thuyen Phan
 * Date: 13/09/2018
 * Time: 15:52
 */
public class Authentication {
    public static String username = "";
    public String createToken(String payload, long timeExpires) {
        Algorithm algorithm = Algorithm.HMAC256("secret");
        String token = JWT.create()
                .withClaim("user", payload)
                .withExpiresAt(new Date(new Date().getTime() + timeExpires))
                .withIssuer("auth0")
                .sign(algorithm);
        return token;
    }

    public boolean verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);

            Claim claim = jwt.getClaim("user");
            username = claim.asString();
            return true;
        } catch (JWTVerificationException exception){
            //Invalid signature/claims
            return false;
        }
    }
}
