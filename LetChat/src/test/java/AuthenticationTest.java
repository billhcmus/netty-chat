import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import server.Authentication;

/**
 * Created by IntelliJ IDEA.
 * User: Thuyen Phan
 * Date: 13/09/2018
 * Time: 20:28
 */
public class AuthenticationTest extends TestCase {

    public void testAuthentication() {
        Authentication authentication = new Authentication();
        String token = authentication.createToken("bill", 30000);
        assertTrue(authentication.verifyToken(token));
    }

    public static void main(String[] args) {
        TestRunner.run(new TestSuite(AuthenticationTest.class));
    }
}
