/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package json;

import DAO.AGDDAO;
import DAO.UserDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import entity.LocationReport;
import entity.SLOCADate;
import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utility.ValidateUtility;

/**
 *
 * @author Wei Ming
 */
@WebServlet(name = "top-k-companions", urlPatterns = {"/json/top-k-companions"})
public class JsonTopKCompanions extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/json;charset=UTF-8");

        PrintWriter out = response.getWriter();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonResult = new JsonObject();

        String macAddress = null;
        SLOCADate dt = null;

        ArrayList<String> errorMsgs = new ArrayList<>();
        Map<String, String[]> requestNames = request.getParameterMap();
        // <k,[3]> <date,[2017-03-01T12:00:00]> <mac-address,[]>
        // ArrayList <JAVA>
        // JsonArray <Json>
        //check token
        if (requestNames.containsKey("token")) {
            String token = request.getParameter("token");
            if (token == null || token.trim().length() == 0) {
                errorMsgs.add("blank token");
            } else {
                try {
                    String username = JWTUtility.verify(token, "WELOVESESOMUCH");
                    if (username == null) {
                        errorMsgs.add("invalid token");
                    }
                } catch (JWTException e) {
                    //error thrown when token is modified or has expired
                    errorMsgs.add("invalid token");
                }
            }
        } else {
            errorMsgs.add("missing token");
        }

        //return immediately if anything wrong w the token
        if (errorMsgs != null && errorMsgs.size() != 0) {
            jsonResult.addProperty("status", "error");
            JsonArray jsonErrorArray = new JsonArray();
            for (String errorMsg : errorMsgs) {
                JsonPrimitive msg = new JsonPrimitive(errorMsg);
                jsonErrorArray.add(msg);
            }
            jsonResult.add("messages", jsonErrorArray);
            out.println(gson.toJson(jsonResult));
            return;
        }

        LinkedHashMap<String, Long> sortTimeSpent = new LinkedHashMap<>();
        int k = 3;

        if (requestNames.containsKey("date")) {
            String date = request.getParameter("date");
            if (date == null || date.trim().length() == 0) {
                errorMsgs.add("blank date");
            } else {
                date = date.replace('T', ' ');

                try { // Check for correct date format
                    LocalDateTime LTD = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    dt = new SLOCADate(LTD);
                } catch (DateTimeParseException e) { //throws exception if the string cannot be parsed as it doesn't match to DateTimeFormatter's pattern
                    errorMsgs.add("invalid date");
                }
            }
        } else {
            errorMsgs.add("missing date");
        }

        if (requestNames.containsKey("mac-address")) {
            macAddress = request.getParameter("mac-address");
            if (macAddress == null || macAddress.trim().length() == 0) {
                errorMsgs.add("blank mac address");
            } else {
                ValidateUtility vu = new ValidateUtility();
                vu.updateMacSet();
                if (!(vu.checkMacAddress(macAddress) && vu.containsMac(macAddress))) {
                    errorMsgs.add("invalid mac address");
                }
            }
        } else {
            errorMsgs.add("missing mac address");
        }

        if (requestNames.containsKey("k")) {
            String kNum = request.getParameter("k");
            if (kNum == null || kNum.trim().length() == 0) {
                errorMsgs.add("blank k");
            } else {
                try {
                    k = Integer.parseInt(kNum);
                    if (k > 10 || k < 1) {
                        errorMsgs.add("invalid k");
                    }
                } catch (NumberFormatException e) {
                    errorMsgs.add("invalid k");
                }
            }

        }

        if (errorMsgs.size() == 0) {
            //from here!
            AGDDAO agdDAO = new AGDDAO();
            HashMap<String, ArrayList<ArrayList<String>>> userMap = agdDAO.getEveryoneInTime(dt); //returns list of users <mac_add, arraylist of (locId, time)>
            HashMap<String, ArrayList<LocationReport>> reportList = new HashMap<>();
            Iterator<String> iter1 = userMap.keySet().iterator();

            while (iter1.hasNext()) {
                String mac = iter1.next(); //6f014016fcb0497324aaa605896beca7e84e59bb
                String firstLocId = userMap.get(mac).get(0).get(0); //1010300135
                SLOCADate start = new SLOCADate(userMap.get(mac).get(0).get(1)); //2017-02-06 10:45:03
                SLOCADate end = start; //2017-02-06 10:56:00
                SLOCADate previous = start;
                ArrayList<LocationReport> reports = new ArrayList<>();//checking of current reports of mac (temp array)

                for (int i = 0; i < userMap.get(mac).size(); i++) { //getting the no. of (locId and time)
                    ArrayList<String> locUpdate = userMap.get(mac).get(i); // arraylist of (locId, time)
                    String locId = locUpdate.get(0); //1010300135 
                    SLOCADate dateTime = new SLOCADate(locUpdate.get(1)); // 2017-02-06 10:59:57

                    if (i == userMap.get(mac).size() - 1) { // last update // now is 10:45:03 - 10:59:57
                        end = dateTime;

                        if (SLOCADate.getDuration(previous, end) > 300) {
                            reports.add(new LocationReport(mac, firstLocId, start, previous.retrieveMinutesAfter(5)));
                            start = end;
                        } else if (!firstLocId.equals(locId)) {
                            reports.add(new LocationReport(mac, firstLocId, start, end));
                            start = end;
                        }
                        if (SLOCADate.getDuration(end, dt) > 300) {
                            reports.add(new LocationReport(mac, locId, start, end.retrieveMinutesAfter(5)));
                        } else {
                            reports.add(new LocationReport(mac, locId, start, dt));
                        }
                        break;
                    }
                    if (firstLocId.equals(locId)) { //if loc is the same, increase end time 
                        end = dateTime; //2017-02-06 10:56:00 
                        if (SLOCADate.getDuration(previous, end) > 300) {
                            LocationReport report = new LocationReport(mac, locId, start, previous.retrieveMinutesAfter(5));
                            start = end;
                            reports.add(report);
                            start = dateTime;
                        }
                    } else { //user moved to another location. End the report.  
                        end = dateTime;
                        LocationReport report = null;
                        if (SLOCADate.getDuration(previous, end) > 300) {
                            report = new LocationReport(mac, firstLocId, start, previous.retrieveMinutesAfter(5));
                        } else {
                            report = new LocationReport(mac, firstLocId, start, end);
                        }
                        reports.add(report);
                        start = end;
                        firstLocId = locId;
                    }
                    previous = end;
                }
                reportList.put(mac, reports);
            }

            ArrayList<LocationReport> requestedReports = reportList.get(macAddress);
            HashMap<String, Long> listTimeSpent = new HashMap<>();

            if (requestedReports != null) {
                for (LocationReport selectedLR : requestedReports) {
                    for (String mac : reportList.keySet()) {
                        if (!mac.equals(macAddress)) {
                            for (LocationReport curLR : reportList.get(mac)) {
                                if (selectedLR.compareTo(curLR)) {
                                    long timeTogether = selectedLR.getTimeTogether(curLR);
                                    if (timeTogether != 0) {
                                        if (listTimeSpent.containsKey(mac)) {
                                            long temp_timeSpent = listTimeSpent.get(mac);
                                            temp_timeSpent += timeTogether;
                                            listTimeSpent.put(mac, temp_timeSpent);
                                        } else {
                                            listTimeSpent.put(mac, timeTogether);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HashMap<String, String> emailList = new HashMap<>();
                UserDAO userDAO = new UserDAO();
                //sorting the hashmap by value
                sortTimeSpent = sortByValues(listTimeSpent);
                Set set = sortTimeSpent.keySet();
                Iterator<String> iter = set.iterator();
                String temp_email = "";
                while (iter.hasNext()) {
                    String mac = iter.next();
                    //query and take email
                    temp_email = userDAO.retrieveUserEmail(mac);
                    if (temp_email.equals("null")) {
                        temp_email = "";
                    }
                    emailList.put(mac, temp_email);
                }
                //add from here if needed!

                jsonResult.addProperty("status", "success");
                JsonArray companionArray = new JsonArray();
                //okay a new one using treemap if it doesn't work then i will sleep
                TreeMap<Long, ArrayList<String>> sortedMap = new TreeMap<>();
                if (sortTimeSpent != null && sortTimeSpent.size() != 0) {
                    Iterator iter2 = sortTimeSpent.keySet().iterator();
                    while (iter2.hasNext()) {
                        String mAddress = (String) iter2.next();
                        Long time = sortTimeSpent.get(mAddress);
                        if (sortedMap.containsKey(time)) {
                            ArrayList<String> maList = sortedMap.get(time);
                            maList.add(mAddress);
                        } else {
                            ArrayList<String> maList = new ArrayList<>();
                            maList.add(mAddress);
                            sortedMap.put(time, maList);
                        }
                    }
                    System.out.println(sortedMap.size());
                    for (int i = 1; i <= k; i++) {
                        Map.Entry<Long, ArrayList<String>> entry = sortedMap.lastEntry();
                        if (entry != null) {
                            Long time = entry.getKey();
                            ArrayList<String> companions = entry.getValue();
                            for (String com : companions) {
                                JsonObject companion = new JsonObject();
                                companion.addProperty("rank", i);
                                String email = emailList.get(com);
                                companion.addProperty("companion", email);
                                companion.addProperty("mac-address", com);
                                companion.addProperty("time-together", time);
                                companionArray.add(companion);
                            }
                            sortedMap.remove(time);
                        }
                    }
                }
                System.out.println(companionArray.size());
                jsonResult.add("results", companionArray);

                out.println(gson.toJson(jsonResult));
            } else {
                jsonResult.addProperty("status", "error");
                jsonResult.addProperty("messages", "no data found");

                out.println(gson.toJson(jsonResult));
            }
        } else {
            System.out.println("printed here");
            jsonResult.addProperty("status", "error");
            Collections.sort(errorMsgs);

            JsonArray errorArray = new JsonArray();
            for (String error : errorMsgs) {
                JsonPrimitive jsonError = new JsonPrimitive(error);
                errorArray.add(jsonError);
            }
            jsonResult.add("messages", errorArray);

            out.println(gson.toJson(jsonResult));
        }
    }

    private static LinkedHashMap<String, Long> sortByValues(HashMap<String, Long> map) {
        TreeMap<Long, ArrayList<String>> tempMap = new TreeMap<>();
        LinkedHashMap<String, Long> returnMap = new LinkedHashMap<>();
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String mac = iter.next();
            long time = map.get(mac);
            if (tempMap.containsKey(time)) {
                ArrayList<String> tempMac = tempMap.get(time);
                tempMac.add(mac);
                tempMap.put(time, tempMac);
            } else {
                ArrayList<String> tempMac = new ArrayList<>();
                tempMac.add(mac);
                tempMap.put(time, tempMac);
            }
        }

        Iterator<Long> iter2 = tempMap.descendingKeySet().iterator();
        while (iter2.hasNext()) {
            long time = iter2.next();
            ArrayList<String> macs = tempMap.get(time);
            Collections.sort(macs);
            for (int i = 0; i < macs.size(); i++) {
                returnMap.put(macs.get(i), time);
            }
        }

        return returnMap;
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
