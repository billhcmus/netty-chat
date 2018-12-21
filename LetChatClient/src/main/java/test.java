/**
 * Created by IntelliJ IDEA.
 * User: Thuyen Phan
 * Date: 11/09/2018
 * Time: 14:24
 */
public class test {
    public static void main(String[] args) {
        String str = "abc.user1:bcd.user2";

        String [] s = str.split(":");
        for (String x:s) {
            if (x.contains("bcd")) {
                System.out.println(x.split("\\.")[1]);
            }
        }
    }
}
