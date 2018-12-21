package Storage;

/**
 * Created by IntelliJ IDEA.
 * User: Thuyen Phan
 * Date: 09/09/2018
 * Time: 23:30
 */
interface Storage {
    boolean put(String key, String value);
    String get(String key);
    boolean remove(String key);
    boolean contains(String key);
    void close();
}
