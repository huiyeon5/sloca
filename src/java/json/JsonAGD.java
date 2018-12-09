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
import entity.AGDGroup;
import entity.AGDGroupComparator;
import entity.GroupLocationReport;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Jia Xian
 */
@WebServlet(name = "JsonAGD", urlPatterns = {"/json/group_detect"})
public class JsonAGD extends HttpServlet {

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
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            JsonObject jsonResult = new JsonObject();

            SLOCADate dt = null;

            //return error messages such as "invalid token", "blank date" & "missing date" etc.
            ArrayList<String> errorMsgs = new ArrayList<String>();

            //Creating a Map object to check if there are all request's names (token, floor and date)
            Map<String, String[]> requestNames = request.getParameterMap();

            if (requestNames.containsKey("token")) {
                String token = request.getParameter("token");

                if (token == null || token.trim().length() == 0) {
                    errorMsgs.add("blank token");

                } else {
                    try {
                        String username = JWTUtility.verify(token, "WELOVESESOMUCH");
                        if (username == null) {
                            throw new JWTException("");
                        }
                    } catch (JWTException e) {
                        //error thrown when token is modified or has expired
                        jsonResult.addProperty("status", "error");
                        jsonResult.addProperty("messages", "invalid token");
                        out.println(gson.toJson(jsonResult));
                        return;
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

            if (requestNames.containsKey("date")) {
                String inputDate = request.getParameter("date");
                String convertedDate = "";
                if (inputDate == null || inputDate.trim().length() == 0) {
                    errorMsgs.add("blank date");

                } else {
                    for (int i = 0; i < inputDate.length(); i++) {
                        if (inputDate.charAt(i) == 'T') {
                            convertedDate += " ";
                        } else {
                            convertedDate += inputDate.charAt(i);
                        }
                    }
                    //check if the date is in the right format
                    try {
                        //throws exception if the string cannot be parsed as it doesn't match to DateTimeFormatter's pattern
                        LocalDateTime LTD = LocalDateTime.parse(convertedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        dt = new SLOCADate(LTD);
                    } catch (DateTimeParseException e) {
                        errorMsgs.add("invalid date");
                    }
                }
            } else {
                errorMsgs.add("missing date");
            }

            //If there are no errors, proceed to process AGD
            if (errorMsgs.size() == 0) {
                AGDDAO agdDAO = new AGDDAO();

                HashMap<String, ArrayList<ArrayList<String>>> userMap = agdDAO.getEveryoneInTime(dt); //returns list of users <mac_add, arraylist of (locId, time)>
                HashMap<String, ArrayList<LocationReport>> reportMap = new HashMap<>();
                Iterator<String> iter1 = userMap.keySet().iterator();
                int totalUsers = userMap.size();
                while (iter1.hasNext()) {
                    String mac = iter1.next();
                    String firstLocId = userMap.get(mac).get(0).get(0);
                    SLOCADate start = new SLOCADate(userMap.get(mac).get(0).get(1));
                    SLOCADate end = start;
                    SLOCADate previous = start;
                    ArrayList<LocationReport> reports = new ArrayList<>();

                    for (int i = 1; i < userMap.get(mac).size(); i++) {
                        ArrayList<String> locUpdate = userMap.get(mac).get(i);
                        String locId = locUpdate.get(0);
                        SLOCADate dateTime = new SLOCADate(locUpdate.get(1));

                        if (i == userMap.get(mac).size() - 1) {
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

                        if (firstLocId.equals(locId)) {
                            end = dateTime;
                            if (SLOCADate.getDuration(previous, end) > 300) {
                                LocationReport report = new LocationReport(mac, locId, start, previous.retrieveMinutesAfter(5));
                                start = end;
                                reports.add(report);
                                start = dateTime;
                            }
                        } else {
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

                    reportMap.put(mac, reports);
                }

                Iterator<String> iter2 = reportMap.keySet().iterator();

                while (iter2.hasNext()) {
                    String mac = iter2.next();
                    ArrayList<LocationReport> allReport = reportMap.get(mac);

                    long sum = 0;
                    for (LocationReport report : allReport) {

                        sum += report.getDuration();
                    }

                    if (sum < 720) {
                        iter2.remove();
                    }
                }

                //<mac, ArrayList<LocationReport>> all that stays at least 12 mins
                Set<String> validMacsSet = reportMap.keySet();
                ArrayList<String> validMacsList = new ArrayList<String>(validMacsSet);

                ArrayList<AGDGroup> groupList = new ArrayList<AGDGroup>();

                // getting individual macaddress
                for (int i = 0; i < validMacsList.size(); i++) {
                    String mac = validMacsList.get(i);
                    ArrayList<LocationReport> userLocReports = reportMap.get(mac);

                    for (int j = i + 1; j < validMacsList.size(); j++) {
                        String compareMac = validMacsList.get(j);
                        long timeSpent = 0;
                        int count = 0;
                        ArrayList<GroupLocationReport> groupLocReport = new ArrayList<GroupLocationReport>();

                        //comparing user's reportsS to check if they are a group
                        for (LocationReport locReport : userLocReports) {
                            ArrayList<LocationReport> compareLocReports = reportMap.get(compareMac);

                            for (LocationReport compareReport : compareLocReports) {
                                if (locReport.compareTo(compareReport)) {
                                    count++;
                                    timeSpent += locReport.getTimeTogether(compareReport);
                                    GroupLocationReport grpLocReport = locReport.generateGroupLocationReport(compareReport);
                                    groupLocReport.add(grpLocReport);
                                }
                            }
                            if (timeSpent >= 720) { //creating Group(Pairs) 
                                ArrayList<String> members = new ArrayList<String>();
                                members.add(mac);
                                members.add(compareMac);
                                AGDGroup newGroup = new AGDGroup(members, groupLocReport);
                                boolean sameGroup = false;
                                for (AGDGroup group : groupList) {
                                    if (group.equals(newGroup)) {
                                        sameGroup = true;
                                        break;
                                    }
                                }
                                if (!sameGroup) {
                                    groupList.add(newGroup);
                                }
                            }
                        }
                    }
                }
                // As of now, there are only pairs inside GroupList 
                // The below method will group groups together if they share the same/similiar GroupLocationReport where time >= 12mins
                //ArrayList<AGDGroup> removeGroupList = new ArrayList<AGDGroup>();
                int noOfCombine = 1;

                while (noOfCombine > 0) {
                    noOfCombine = 0;
                    ArrayList<AGDGroup> tempGroupList = new ArrayList<AGDGroup>();
                    ArrayList<Integer> removeGroupPositionFromList = new ArrayList<Integer>();

                    //Combining pairs into groups and add them into tempGroupList
                    for (int i = 0; i < groupList.size(); i++) { // last 
                        AGDGroup group = groupList.get(i);
                        for (int j = i + 1; j < groupList.size(); j++) { // last + 1 
                            AGDGroup otherGroup = groupList.get(j);
                            if (!group.equals(otherGroup)) { //AB vs AC 
                                if (group.checkForCombination(otherGroup)) {
                                    AGDGroup newGroup = group.combine(otherGroup);
                                    noOfCombine++;
                                    tempGroupList.add(newGroup);
                                }
                            }
                        }
                    }

                    //getting postion of group's duplicate within tempGroupList 
                    for (int i = 0; i < tempGroupList.size(); i++) {
                        AGDGroup group = tempGroupList.get(i);
                        for (int j = i + 1; j < tempGroupList.size(); j++) {
                            AGDGroup compareGroup = tempGroupList.get(j);
                            if (group.equals(compareGroup)) {
                                if (!removeGroupPositionFromList.contains(j));
                                removeGroupPositionFromList.add(j);
                                break;
                            }
                        }
                    }

                    //removing duplicate within tempGroupList
                    for (int i = tempGroupList.size() - 1; i >= 0; i--) {
                        if (removeGroupPositionFromList.contains(i)) {
                            tempGroupList.remove(i);
                        }
                    }

                    //Checking for unique pairs from groupList to be added into tempGroupList
                    ArrayList<AGDGroup> uniqueGroups = new ArrayList<AGDGroup>();
                    for (AGDGroup uniqueGroup : groupList) {
                        boolean isUnique = true;
                        for (AGDGroup group : tempGroupList) {
                            if (uniqueGroup.equals(group)) {
                                isUnique = false;
                                break;
                            }
                        }
                        if (isUnique) {
                            uniqueGroups.add(uniqueGroup);
                        }
                    }

                    tempGroupList.addAll(uniqueGroups);

                    groupList = tempGroupList;
                }

                //FINAL REMOVABLE FOR DUPLICATE GROUPS 
                ArrayList<Integer> removeGroupPositionFromList = new ArrayList<Integer>();

                //getting postion of group's duplicate within groupList 
                for (int i = 0; i < groupList.size(); i++) {
                    AGDGroup group = groupList.get(i);
                    for (int j = i + 1; j < groupList.size(); j++) {
                        AGDGroup compareGroup = groupList.get(j);
                        if (group.equals(compareGroup)) {
                            if (!removeGroupPositionFromList.contains(j));
                            removeGroupPositionFromList.add(j);
                        }
                    }
                }

                //removing duplicate within groupList
                for (int i = groupList.size() - 1; i >= 0; i--) {
                    if (removeGroupPositionFromList.contains(i)) {
                        groupList.remove(i);
                    }
                }

                //if there are no groups within the time window
                if (groupList.size() != 0) {
                    jsonResult.addProperty("status", "success");


                    jsonResult.addProperty("total-users", totalUsers);
                    jsonResult.addProperty("total-groups", groupList.size());

                    JsonArray groups = new JsonArray();

                    UserDAO userDao = new UserDAO();

                    Collections.sort(groupList, new AGDGroupComparator());

                    for (AGDGroup group : groupList) {
                        JsonObject jsonGroup = new JsonObject();
                        JsonArray membersArray = new JsonArray();
                        ArrayList<String> members = group.getMembers();

                        int size = members.size();
                        long duration = group.getDuration();

                        TreeMap<String, String> sortedMemberMap = new TreeMap<>();

                        for (String member : members) {
                            String email = userDao.retrieveUserEmail(member);
                            String mac = member;
                            sortedMemberMap.put(email, mac);
                        }

                        Set<String> EmailKey = sortedMemberMap.keySet();

                        for (String email : EmailKey) {
                            JsonObject jsonMember = new JsonObject();
                            String mac = sortedMemberMap.get(email);
                            jsonMember.addProperty("email", email);
                            jsonMember.addProperty("mac-address", mac);
                            membersArray.add(jsonMember);
                        }

                        JsonArray locArray = new JsonArray();
                        ArrayList<GroupLocationReport> reports = group.getGroupLocationReports();

                        TreeMap<String, Long> sortedLocMap = new TreeMap<>();

                        for (GroupLocationReport report : reports) {

                            String location = report.getLocationId();
                            long reportDuration = report.getDuration();
                            if (sortedLocMap.containsKey(location)) {
                                long totalTime = sortedLocMap.get(location) + reportDuration;
                                sortedLocMap.put(location, totalTime);
                            } else {
                                sortedLocMap.put(location, reportDuration);
                            }
                        }

                        Set<String> LocKey = sortedLocMap.keySet();

                        for (String locId : LocKey) {
                            JsonObject jsonLoc = new JsonObject();
                            long timeSpent = sortedLocMap.get(locId);
                            jsonLoc.addProperty("location", locId);
                            jsonLoc.addProperty("time-spent", timeSpent);
                            locArray.add(jsonLoc);
                        }

                        jsonGroup.addProperty("size", size);
                        jsonGroup.addProperty("total-time-spent", duration);
                        jsonGroup.add("members", membersArray);
                        jsonGroup.add("locations", locArray);

                        groups.add(jsonGroup);
                    }

                    jsonResult.add("groups", groups);
                    out.println(gson.toJson(jsonResult));

                } else {
                    jsonResult.addProperty("status", "error");
                    jsonResult.addProperty("messages", "No groups found");
                    out.println(gson.toJson(jsonResult));
                }
            } else { //printing all the error messages from errorMsgs
                jsonResult.addProperty("status", "error");
                Collections.sort(errorMsgs);
                JsonArray jsonErrorArray = new JsonArray();
                for (String errorMsg : errorMsgs) {
                    JsonPrimitive msg = new JsonPrimitive(errorMsg);
                    jsonErrorArray.add(msg);
                }
                jsonResult.add("messages", jsonErrorArray);
                out.println(gson.toJson(jsonResult));
            }
        } catch (DateTimeParseException ex) {
            request.setAttribute("error", "Please enter a valid date (yyyy-mm-dd) or time (hh:mm:ss)");
            RequestDispatcher rs = request.getRequestDispatcher("agd.jsp");
            rs.forward(request, response);
        }
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
