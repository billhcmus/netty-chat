import junit.framework.TestCase;
import org.junit.BeforeClass;
import server.NettyServer;

/**
 * Created by IntelliJ IDEA.
 * User: Thuyen Phan
 * Date: 13/09/2018
 * Time: 21:42
 */
public class NettyServerTest extends TestCase {

    @BeforeClass
    public void testRunServer() {
        try {
            NettyServer.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
