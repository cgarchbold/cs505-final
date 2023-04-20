package cs505finaltemplate.embeddedDB;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DBEngine {

    private DataSource ds;

    public DBEngine() {

        try {
            //Name of database
            String databaseName = "myDatabase";

            //Driver needs to be identified in order to load the namespace in the JVM
            String dbDriver = "org.apache.derby.jdbc.EmbeddedDriver";
            Class.forName(dbDriver);

            //Connection string pointing to a local file location
            String dbConnectionString = "jdbc:derby:memory:" + databaseName + ";create=true";
            ds = setupDataSource(dbConnectionString);

            initDB();
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static DataSource setupDataSource(String connectURI) {
        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = null;
        connectionFactory = new DriverManagerConnectionFactory(connectURI, null);


        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);

        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource<PoolableConnection> dataSource =
                new PoolingDataSource<>(connectionPool);

        return dataSource;
    }

    public void initDB() {

        String createRNode = "CREATE TABLE hospitals" +
                "(" +
                "   hospital_id varchar(255)," +
                "   patient_mrn varchar(255)," +
                "   patient_status bigint," +
                "   vax_status bigint" +
                ")";

        try {
            try(Connection conn = ds.getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(createRNode);
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    public int executeUpdate(String stmtString) {
        int result = -1;
        try {
            Connection conn = ds.getConnection();
            try {
                Statement stmt = conn.createStatement();
                result = stmt.executeUpdate(stmtString);
                stmt.close();
            } catch (Exception e) {

                e.printStackTrace();
            } finally {
                conn.close();
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return  result;
    }

    public int dropTable(String tableName) {
        int result = -1;
        try {
            Connection conn = ds.getConnection();
            try {
                String stmtString = null;

                stmtString = "DROP TABLE " + tableName;

                Statement stmt = conn.createStatement();

                result = stmt.executeUpdate(stmtString);

                stmt.close();
            } catch (Exception e) {

                e.printStackTrace();
            } finally {
                conn.close();
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public boolean databaseExist(String databaseName)  {
        boolean exist = false;
        try {

            if(!ds.getConnection().isClosed()) {
                exist = true;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return exist;
    }

    public boolean tableExist(String tableName)  {
        boolean exist = false;

        ResultSet result;
        DatabaseMetaData metadata = null;

        try {
            metadata = ds.getConnection().getMetaData();
            result = metadata.getTables(null, null, tableName.toUpperCase(), null);

            if(result.next()) {
                exist = true;
            }
        } catch(java.sql.SQLException e) {
            e.printStackTrace();
        }

        catch(Exception ex) {
            ex.printStackTrace();
        }
        return exist;
    }

    public boolean isPatient(String patient_mrn) {
        try {

            String queryString = null;

            queryString = "SELECT patient_mrn " +
                "FROM hospitals " +
                "WHERE patient_mrn = '" + patient_mrn + "'";

            try(Connection conn = ds.getConnection()) {
                try (Statement stmt = conn.createStatement()) {

                    try(ResultSet rs = stmt.executeQuery(queryString)) {

                        while (rs.next()) {
                            return true;
                        }

                    }
                }
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /* 
    Function to retrieve the count of patients and their statuses at hospital_id
    */
    public Map<String,String> getHospital(String hospital_id) {
        Map<String,String> accessMap = new HashMap<>();
        try {

            String queryString = "SELECT patient_status, COUNT(patient_mrn) as status_count " +
                "FROM hospitals WHERE hospital_id = '" + hospital_id +
                "' GROUP BY patient_status ORDER BY patient_status ASC";

            try(Connection conn = ds.getConnection()) {
                try (Statement stmt = conn.createStatement()) {

                    try(ResultSet rs = stmt.executeQuery(queryString)) {

                        while (rs.next()) {
                            if (rs.getString("patient_status").equals("1")) {
                                accessMap.put("in-patient_count", rs.getString("status_count"));
                            }
                            else if (rs.getString("patient_status").equals("2")) {
                                accessMap.put("icu-patient_count", rs.getString("status_count"));
                            }
                            else if (rs.getString("patient_status").equals("3")){
                                accessMap.put("patient_vent_count", rs.getString("status_count"));
                            }
                        }

                    }
                }
            }

            queryString = "SELECT patient_status, COUNT(patient_mrn) as vax_count " +
                "FROM hospitals WHERE hospital_id = '" + hospital_id +
                "' AND vax_status = 1 GROUP BY patient_status ORDER BY patient_status ASC";

            try(Connection conn = ds.getConnection()) {
                try (Statement stmt = conn.createStatement()) {

                    try(ResultSet rs = stmt.executeQuery(queryString)) {

                        while (rs.next()) {
                            if (rs.getString("patient_status").equals("1")) {
                                accessMap.put("in-patient_vax", rs.getString("vax_count"));
                            }
                            else if (rs.getString("patient_status").equals("2")) {
                                accessMap.put("icu-patient_vax", rs.getString("vax_count"));
                            }
                            else if (rs.getString("patient_status").equals("3")) {
                                accessMap.put("patient_vent_vax", rs.getString("vax_count"));
                            }
                        }

                    }
                }
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return accessMap;
    }

    /* 
    Function to retrieve the count of patients and their statuses at all hosptials
    */
    public Map<String,String> getHospitals() {
        Map<String,String> accessMap = new HashMap<>();
        try {

            String queryString = "SELECT patient_status, COUNT(patient_mrn) as status_count " +
                "FROM hospitals " +
                "GROUP BY patient_status ORDER BY patient_status ASC";

            try(Connection conn = ds.getConnection()) {
                try (Statement stmt = conn.createStatement()) {

                    try(ResultSet rs = stmt.executeQuery(queryString)) {

                        while (rs.next()) {
                            if (rs.getString("patient_status").equals("1")) {
                                accessMap.put("in-patient_count", rs.getString("status_count"));
                            }
                            else if (rs.getString("patient_status").equals("2")) {
                                accessMap.put("icu-patient_count", rs.getString("status_count"));
                            }
                            else if (rs.getString("patient_status").equals("3")) {
                                accessMap.put("patient_vent_count", rs.getString("status_count"));
                            }
                        }

                    }
                }
            }

            queryString = "SELECT patient_status, COUNT(patient_mrn) as vax_count " +
                "FROM hospitals WHERE vax_status = 1 " +
                "GROUP BY patient_status ORDER BY patient_status ASC";

            try(Connection conn = ds.getConnection()) {
                try (Statement stmt = conn.createStatement()) {

                    try(ResultSet rs = stmt.executeQuery(queryString)) {

                        while (rs.next()) {
                            if (rs.getString("patient_status").equals("1")) {
                                accessMap.put("in-patient_vax", rs.getString("vax_count"));
                            }
                            else if (rs.getString("patient_status").equals("2")) {
                                accessMap.put("icu-patient_vax", rs.getString("vax_count"));
                            }
                            else if (rs.getString("patient_status").equals("3")) {
                                accessMap.put("patient_vent_vax", rs.getString("vax_count"));
                            }
                        }

                    }
                }
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return accessMap;
    }

}

