package cs505finaltemplate.httpcontrollers;

import com.google.gson.Gson;
import cs505finaltemplate.Launcher;
import jdk.nashorn.internal.objects.annotations.Getter;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;

@Path("/api")
public class API {

    @Inject
    private javax.inject.Provider<org.glassfish.grizzly.http.server.Request> request;

    private Gson gson;

    public API() {
        gson = new Gson();
    }

    @GET
    @Path("/getteam")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeam() {
        String responseString = "{}";
        try {
            // Team Description Hardcoded
            Map<String,String> responseMap = new HashMap<>();
            responseMap.put("team_name", "Foo Bar");
            responseMap.put("Team_members_sids", "[912286574]");
            responseMap.put("app_status_code","1");
            responseString = gson.toJson(responseMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset() {
        String responseString = "{}";
        try {
            Launcher.graphDBEngine.db.activateOnCurrentThread();
            Map<String,String> responseMap = new HashMap<>();
            Launcher.alerts = new ArrayList<>();
            Launcher.CEPList = new HashMap<>();
            responseMap.put("reset_status_code", String.valueOf(1));
            responseString = gson.toJson(responseMap);

            //Clear Databases
            if (Launcher.embedded != null) {
                Launcher.embedded.dropTable("hospitals");
                Launcher.embedded.initDB();
            }
            if (Launcher.graphDBEngine != null) {
                Launcher.graphDBEngine.clearDB();
            }

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getlastcep")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLastCEP(@HeaderParam("X-Auth-API-Key") String authKey) {
        String responseString = "{}";
        try {
            //get last CEP
            responseString = gson.toJson(Launcher.CEPList);
        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/zipalertlist")
    @Produces(MediaType.APPLICATION_JSON)
    public Response zipAlertList(@HeaderParam("X-Auth-API-Key") String authKey) {
        String responseString = "{}";
        try {
            Map<String,String> responseMap = new HashMap<>();
            String zipList = "[";
            int i = 1;
            int alertLimit = Launcher.alerts.size();
            for (String zipCode : Launcher.alerts) {
                zipList += zipCode;
                if (i < alertLimit) {
                    zipList += ",";
                }
                i++;
            }
            zipList += "]";
            responseMap.put("ziplist", zipList);
            responseString = gson.toJson(responseMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/alertlist")
    @Produces(MediaType.APPLICATION_JSON)
    public Response alertList(@HeaderParam("X-Auth-API-Key") String authKey) {
        String responseString = "{}";
        try {

            //generate a response
            Map<String,String> responseMap = new HashMap<>();
            Integer inAlert = 0;
            if (Launcher.alerts.size() >= 5) {
                inAlert = 1;
            }
            responseMap.put("state_status", String.valueOf(inAlert));
            responseString = gson.toJson(responseMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getconfirmedcontacts/{mrn}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfirmedContacts(@HeaderParam("X-Auth-API-Key") String authKey, @PathParam("mrn") String mrn) {
        String responseString = "{}";
        try {
            //generate a response
            Launcher.graphDBEngine.db.activateOnCurrentThread();
            Map<String, String> responseMap = new HashMap<>();
            ArrayList<String> contacts = Launcher.graphDBEngine.getContacts(mrn);
            String contactList = "[";
            int i = 1;
            for (String contact : contacts) {
                if (!mrn.equals(contact)) {
                    contactList += contact;
                    if (i < contacts.size()) {
                        contactList += ",";
                    }
                }
                i++;
            }
            contactList += "]";
            responseMap.put("contactlist", contactList);
            responseString = gson.toJson(responseMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getpossiblecontacts/{mrn}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPossibleContacts(@HeaderParam("X-Auth-API-Key") String authKey, @PathParam("mrn") String mrn) {
        String responseString = "{}";
        try {
            //generate a response
            Launcher.graphDBEngine.db.activateOnCurrentThread();
            Map<String, String> responseMap = new HashMap<>();
            ArrayList<String> events = Launcher.graphDBEngine.getEvents(mrn);
            String eventList = "[";
            int i = 1;
            for (String event : events) {
                ArrayList<String> attendees = Launcher.graphDBEngine.getAttendees(event);
                eventList += event + ":[";
                int j = 1;
                for (String attendee : attendees) {
                    if (!mrn.equals(attendee)) {
                        eventList += attendee;
                        if (j < attendees.size()) {
                            eventList += ",";
                        }
                    }
                    j++;
                }
                eventList += "]";
                if (i < events.size()) {
                    eventList += ",";
                }
                i++;
            }
            eventList += "]";
            responseMap.put("contactlist", eventList);
            responseString = gson.toJson(responseMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getpatientstatus/{hospital_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPatientStatus(@HeaderParam("X-Auth-API-Key") String authKey, @PathParam("hospital_id") String hospital_id) {
        String responseString = "{}";
        try {
            //generate a response
            Map<String, String> responseMap = new HashMap<>();

            Map<String, String> patientList = Launcher.embedded.getHospital(hospital_id);

            Integer in_patient_count = 100;
            if (patientList.get("in-patient_count") == null) {
                responseMap.put("in-patient_count", "0");
            }
            else {
                in_patient_count = Integer.valueOf(patientList.get("in-patient_count"));
                responseMap.put("in-patient_count", in_patient_count.toString());
            }
            Double in_patient_vax;
            if (patientList.get("in-patient_vax") == null) {
                responseMap.put("in-patient_vax", "0");
            }
            else {
                in_patient_vax = Double.parseDouble(patientList.get("in-patient_vax"));
                in_patient_vax = in_patient_vax / in_patient_count.doubleValue();
                responseMap.put("in-patient_vax", in_patient_vax.toString());
            }

            Integer icu_patient_count = 100;
            if (patientList.get("icu-patient_count") == null) {
                responseMap.put("icu-patient_count", "0");
            }
            else {
                icu_patient_count = Integer.valueOf(patientList.get("icu-patient_count"));
                responseMap.put("icu-patient_count", icu_patient_count.toString());
            }
            Double icu_patient_vax;
            if (patientList.get("icu-patient_vax") == null) {
                responseMap.put("icu-patient_vax", "0");
            }
            else {
                icu_patient_vax = Double.parseDouble(patientList.get("icu-patient_vax"));
                icu_patient_vax = icu_patient_vax / icu_patient_count.doubleValue();
                responseMap.put("icu-patient_vax", icu_patient_vax.toString());
            }

            Integer patient_vent_count = 100;
            if (patientList.get("patient_vent_count") == null) {
                responseMap.put("patient_vent_count", "0");
            }
            else {
                patient_vent_count = Integer.valueOf(patientList.get("patient_vent_count"));
                responseMap.put("patient_vent_count", patient_vent_count.toString());
            }
            Double patient_vent_vax;
            if (patientList.get("patient_vent_vax") == null) {
                responseMap.put("patient_vent_vax", "0");
            }
            else {
                patient_vent_vax = Double.parseDouble(patientList.get("patient_vent_vax"));
                patient_vent_vax = patient_vent_vax / patient_vent_count.doubleValue();
                responseMap.put("patient_vent_vax", patient_vent_vax.toString());
            }

            responseString = gson.toJson(responseMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getpatientstatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPatientStatusAll(@HeaderParam("X-Auth-API-Key") String authKey) {
        String responseString = "{}";
        try {
            //generate a response
            Map<String, String> responseMap = new HashMap<>();

            Map<String, String> patientList = Launcher.embedded.getHospitals();

            Integer in_patient_count = 100;
            if (patientList.get("in-patient_count") == null) {
                responseMap.put("in-patient_count", "0");
            }
            else {
                in_patient_count = Integer.valueOf(patientList.get("in-patient_count"));
                responseMap.put("in-patient_count", in_patient_count.toString());
            }
            Double in_patient_vax;
            if (patientList.get("in-patient_vax") == null) {
                responseMap.put("in-patient_vax", "0");
            }
            else {
                in_patient_vax = Double.parseDouble(patientList.get("in-patient_vax"));
                in_patient_vax = in_patient_vax / in_patient_count.doubleValue();
                responseMap.put("in-patient_vax", in_patient_vax.toString());
            }

            Integer icu_patient_count = 100;
            if (patientList.get("icu-patient_count") == null) {
                responseMap.put("icu-patient_count", "0");
            }
            else {
                icu_patient_count = Integer.valueOf(patientList.get("icu-patient_count"));
                responseMap.put("icu-patient_count", icu_patient_count.toString());
            }
            Double icu_patient_vax;
            if (patientList.get("icu-patient_vax") == null) {
                responseMap.put("icu-patient_vax", "0");
            }
            else {
                icu_patient_vax = Double.parseDouble(patientList.get("icu-patient_vax"));
                icu_patient_vax = icu_patient_vax / icu_patient_count.doubleValue();
                responseMap.put("icu-patient_vax", icu_patient_vax.toString());
            }

            Integer patient_vent_count = 100;
            if (patientList.get("patient_vent_count") == null) {
                responseMap.put("patient_vent_count", "0");
            }
            else {
                patient_vent_count = Integer.valueOf(patientList.get("patient_vent_count"));
                responseMap.put("patient_vent_count", patient_vent_count.toString());
            }
            Double patient_vent_vax;
            if (patientList.get("patient_vent_vax") == null) {
                responseMap.put("patient_vent_vax", "0");
            }
            else {
                patient_vent_vax = Double.parseDouble(patientList.get("patient_vent_vax"));
                patient_vent_vax = patient_vent_vax / patient_vent_count.doubleValue();
                responseMap.put("patient_vent_vax", patient_vent_vax.toString());
            }

            responseString = gson.toJson(responseMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }
}
