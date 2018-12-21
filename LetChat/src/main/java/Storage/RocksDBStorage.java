package Storage;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;


/**
 * Created by IntelliJ IDEA.
 * User: Thuyen Phan
 * Date: 09/09/2018
 * Time: 23:34
 */
public class RocksDBStorage implements Storage {
    public RocksDB rocksDB;
    public String path = "";

    public RocksDBStorage(String path) {
        this.path = path;
        RocksDB.loadLibrary();
        final Options options = new Options().setCreateIfMissing(true);
        try {
            this.rocksDB = RocksDB.open(options, this.path);
        } catch (RocksDBException e) {
            System.out.println("Can't not connect to DB.");
            e.printStackTrace();
        }
    }

    @Override
    public boolean put(String key, String value) {
        try {
            byte[] byteKey = key.getBytes("utf-8");
            byte[] byteValue = value.getBytes("utf-8");

            this.rocksDB.put(byteKey, byteValue);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String get(String key) {
        try {
            byte[] byteKey = key.getBytes("utf-8");
            byte[] byteValue = this.rocksDB.get(byteKey);

            if (byteValue != null) {
                return new String(byteValue, "utf-8");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean remove(String key) {
        try {
            byte[] byteKey = key.getBytes("utf-8");
            byte[] byteValue = this.rocksDB.get(byteKey);

            if (byteValue != null) {
                this.rocksDB.delete(byteKey);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean contains(String key) {
        try {
            byte[] byteKey = key.getBytes("utf-8");
            byte[] byteValue = this.rocksDB.get(byteKey);
            return byteValue != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
        this.rocksDB.close();
    }
}
