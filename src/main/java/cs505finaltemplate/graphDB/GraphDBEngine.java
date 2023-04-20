package cs505finaltemplate.graphDB;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import java.util.ArrayList;

public class GraphDBEngine {

    public static OrientDB orient = new OrientDB("remote:localhost", OrientDBConfig.defaultConfig());
    public static ODatabaseSession db = orient.open("test", "root", "rootpwd");
    public GraphDBEngine() {

        //launch a docker container for orientdb, don't expect your data to be saved unless you configure a volume
        //docker run -d --name orientdb -p 2424:2424 -p 2480:2480 -e ORIENTDB_ROOT_PASSWORD=rootpwd orientdb:3.0.0

        //use the orientdb dashboard to create a new database
        //see class notes for how to use the dashboard
        initDB();
        
    }
    
    public void initDB(){
        //create classes
        OClass patient = db.getClass("patient");

        if (patient == null) {
            patient = db.createVertexClass("patient");
        }

        if (patient.getProperty("patient_mrn") == null) {
            patient.createProperty("patient_mrn", OType.STRING);
            patient.createIndex("patient_name_index", OClass.INDEX_TYPE.NOTUNIQUE, "patient_mrn");
        }

        if (db.getClass("contact_with") == null) {
            db.createEdgeClass("contact_with");
        }

        OClass event = db.getClass("event");
        if (event == null) {
            db.createVertexClass("event");
        }

        if (event.getProperty("event_id") == null) {
            event.createProperty("event_id", OType.STRING);
            event.createIndex("event_index", OClass.INDEX_TYPE.NOTUNIQUE, "event_id");
        }

        if (db.getClass("attend") == null) {
            db.createEdgeClass("attend");
        }
    }

    public void clearDB() {

        String query = "DELETE VERTEX FROM patient";
        db.command(query);

        query = "DELETE VERTEX FROM event";
        db.command(query);

    }

    public boolean isPatient(String patient_mrn) {
        String query = "select from patient where patient_mrn = \"" + patient_mrn + "\"";
        OResultSet rs = db.query(query);

        while (rs.hasNext()) {
            OResult item = rs.next();
            if (item.isVertex()) {
                rs.close();
                return true;
            }
        }
        rs.close();
        return false;
    }

    public boolean isEvent(String event_id) {
        String query = "select from event where event_id = \"" + event_id + "\"";
        OResultSet rs = db.query(query);

        while (rs.hasNext()) {
            OResult item = rs.next();
            if (item.isVertex()) {
                rs.close();
                return true;
            }
        }
        rs.close();
        return false;
    }

    /* 
        Functions to add new components to the Graph
        -------------------------------------------
    */
    public OVertex createEvent(String event_id) {
        OVertex result = db.newVertex("event");
        result.setProperty("event_id", event_id);
        result.save();
        return result;
    }

    public void createAttend(OVertex patient, OVertex event) {
        OEdge edge = patient.addEdge(event, "attend");
        edge.save();
    }

    public void createContact(OVertex patient_1, OVertex patient_2) {
        OEdge edge = patient_1.addEdge(patient_2, "contact_with");
        edge.save();
    }
    public OVertex createPatient(String patient_mrn) {
        OVertex result = db.newVertex("patient");
        result.setProperty("patient_mrn", patient_mrn);
        result.save();
        return result;
    }

    /* 
        Functions to retreive data from the Graph
        -------------------------------------------
    */
    public OVertex getPatient(String patient_mrn) {
        String query = "select from patient where patient_mrn = \"" + patient_mrn + "\"";
        OResultSet rs = db.query(query);

        OVertex patient = null;

        while (rs.hasNext()) {
            OResult item = rs.next();
            if (item.isVertex()) {
                patient = item.getVertex().get();
                break;
            }
        }
        rs.close();
        return patient;
    }

    public OVertex getEvent(String event_id) {
        String query = "select from event where event_id = \"" + event_id + "\"";
        OResultSet rs = db.query(query);

        OVertex event = null;

        while (rs.hasNext()) {
            OResult item = rs.next();
            if (item.isVertex()) {
                event = item.getVertex().get();
                break;
            }
        }
        rs.close();
        return event;
    }

    public ArrayList<String> getEvents(String patient_mrn) {
        ArrayList<String> events = new ArrayList<>();
        String query = "TRAVERSE inE(), outE(), inV(), outV() " +
                "FROM (select from patient where patient_mrn = ?) " +
                "WHILE $depth <= 2";
        OResultSet rs = db.query(query, patient_mrn);

        while (rs.hasNext()) {
            OResult item = rs.next();
            if (item.getProperty("event_id") != null) {
                events.add(item.getProperty("event_id"));
            }
        }
        rs.close(); 
        return events;
    }

    public ArrayList<String> getAttendees(String event_id) {
        ArrayList<String> events = new ArrayList<>();
        String query = "TRAVERSE inE(), outE(), inV(), outV() " +
                "FROM (select from event where event_id = ?) " +
                "WHILE $depth <= 2";
        OResultSet rs = db.query(query, event_id);

        while (rs.hasNext()) {
            OResult item = rs.next();
            if (item.getProperty("patient_mrn") != null) {
                events.add(item.getProperty("patient_mrn"));
            }
        }
        rs.close(); 
        return events;
    }
    
    public ArrayList<String> getContacts(String patient_mrn) {

        ArrayList<String> contacts = new ArrayList<>();
        String query = "TRAVERSE inE(), outE(), inV(), outV() " +
            "FROM (select from patient where patient_mrn = ?) " +
            "WHILE $depth <= 2";
        OResultSet rs = db.query(query, patient_mrn);

        while (rs.hasNext()) {
            OResult item = rs.next();
            if (item.getProperty("patient_mrn") != null) {
                contacts.add(item.getProperty("patient_mrn"));
            }
        }
        rs.close(); //REMEMBER TO ALWAYS CLOSE THE RESULT SET!!!
        return contacts;
    }

} //END GRAPHDBENGINE
