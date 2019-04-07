package top.alexcloud;

import org.slf4j.LoggerFactory;

import java.sql.*;

public class Database {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Database.class);
    private static Connection dbConnection;

    public Database(String dbName) {
        String url = "jdbc:sqlite:" + dbName;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
        }
        try {
            dbConnection = DriverManager.getConnection(url);

            if (dbConnection != null) {
                DatabaseMetaData meta = dbConnection.getMetaData();
                log.info("Driver name: " + meta.getDriverName());
                log.info("A new database has been successfully created");
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

    }

    public static void createTable(String sql) {
        try (Statement stmt = dbConnection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public static void insertDictionary(String name, String description, String srcLang, String targetLang) {
        PreparedStatement st = null;
        try {
            st = dbConnection.prepareStatement("INSERT INTO dictionary (name, description, src_lang, target_lang) VALUES (?, ?, ?, ?)");
            st.setString(1, name);
            st.setString(2, description);
            st.setString(3, srcLang);
            st.setString(4, targetLang);
            st.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
    }

    public static int getDictLastId() {
        int number = -1;
        Statement stmt = null;
        try {
            stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM dictionary ORDER BY id DESC LIMIT 1");
            while (rs.next()) {
                number = rs.getInt("id");
            }
            rs.close();

        } catch (SQLException e) {
            log.error(e.getMessage());
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return number;
    }

    public static void insertEntry(Integer dictId, String word, String meaning, String textContent) {
        PreparedStatement st = null;
        try {
            st = dbConnection.prepareStatement("INSERT INTO entry (dictionary_id, word, meaning, text_content) VALUES (?, ?, ?, ?)");
            st.setInt(1, dictId);
            st.setString(2, word);
            st.setString(3, meaning);
            st.setString(4, textContent);
            st.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
    }

    public static void closeConnection() {
        try {
            dbConnection.close();
            log.info("Closing the database connection");
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }
}
