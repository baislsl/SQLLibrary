package load;

import mysql.SQLConnection;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by baislsl on 17-5-14.
 * <p>
 * <p>A util class to login as root user. This class isdebug used only.
 * For common user, this class is never accessible.</p>
 */
public class RootUser {
    private final static String dataBasePropsPath = "res/property/database.properties";

    public static SQLConnection getRootConnection() throws SQLException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get(dataBasePropsPath))) {
            props.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String userName = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");
        return new SQLConnection(userName, password);

    }
}
