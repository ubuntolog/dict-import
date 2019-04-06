package top.alexcloud;

import org.slf4j.LoggerFactory;

import java.sql.*;

public class Database {
    static String fileName;
    public Database(String dbName) {
        fileName = dbName;
    }

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Database.class);

    public static void createNewDatabase() throws ClassNotFoundException {

        String url = "jdbc:sqlite:" + fileName;

        Class.forName("org.sqlite.JDBC");
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                log.info("Driver name: " + meta.getDriverName());
                log.info("A new database has been successfully created");
            }

        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public static void createDictionaryTable() {
        String url = "jdbc:sqlite:" + fileName;

        String sql = "CREATE TABLE IF NOT EXISTS dictionary (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	name varchar(50) NOT NULL,\n"
                + "	description text NOT NULL,\n"
                + "	src_lang varchar(50) NOT NULL,\n"
                + "	target_lang varchar(50) NOT NULL\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }


}
