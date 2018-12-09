/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import DAO.AGDDAO;
import DAO.UserDAO;
import entity.AGDGroup;
import entity.AGDGroupComparator;
import entity.GroupLocationReport;
import entity.LocationReport;
import entity.SLOCADate;
import java.io.IOException;
import static java.lang.System.out;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Keith
 */
@WebServlet(name = "AGDServlet", urlPatterns = {"/AGDServlet"})
public class AGDServlet extends HttpServlet {

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

        response.setContentType("text/html;charset=UTF-8");

        String date = request.getParameter("date");
        String time = request.getParameter("time");

        try {
            SLOCADate dt = new SLOCADate(date, time);

            AGDDAO agdDAO = new AGDDAO();

            HashMap<String, ArrayList<ArrayList<String>>> userMap = agdDAO.getEveryoneInTime(dt); //returns list of users <mac_add, arraylist of (locId, time)>
            HashMap<String, ArrayList<LocationReport>> reportMap = new HashMap<>();
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

//                System.out.println("Mac :" + mac);
//                System.out.println("Location Reports : " + reports.size());
//                System.out.println("");
                reportMap.put(mac, reports);
            }

//            System.out.println("");
//            System.out.println("before clearing : " + reportMap.size());
//            System.out.println("");
            Iterator<String> iter2 = reportMap.keySet().iterator();

            while (iter2.hasNext()) {
                String mac = iter2.next();
                ArrayList<LocationReport> allReport = reportMap.get(mac);
//                System.out.println("Mac:" + mac);
//                System.out.println("report size: " + allReport.size());
                long sum = 0;
                for (LocationReport report : allReport) {
//                    System.out.println("LocId:" + report.getLocationId());
//                    System.out.println("Start:" + report.getStartTime());
//                    System.out.println("End:" + report.getEndTime());
//                    System.out.println("");
                    sum += report.getDuration();
                }
//                System.out.println("Duration:" + sum);
                if (sum < 720) { //removes if user from reportMap if users stay school for less than 12 mins 
                    iter2.remove(); //unsure to remove from iter or reportMap
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

//                                System.out.println("Members : " + mac + " & " + compareMac);
//                                System.out.println("Location: " + grpLocReport.getLocationId());
//                                System.out.println("Start: " + grpLocReport.getStartTime());
//                                System.out.println("End: " + grpLocReport.getEndTime());
//                                System.out.println("Current timeSpent: " + timeSpent);
//                                System.out.println("count : " + count);
//                                System.out.println(""); 
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

            if (groupList.size() != 0) {
                LinkedHashMap<AGDGroup, ArrayList<HashMap<String, String>>> toJsp = new LinkedHashMap<>();
                Collections.sort(groupList, new AGDGroupComparator());

                UserDAO userDao = new UserDAO();

                for (AGDGroup group : groupList) {
                    ArrayList<String> members = group.getMembers();
                    ArrayList<GroupLocationReport> grpReports = group.getGroupLocationReports();

                    ArrayList<HashMap<String, String>> asValue = new ArrayList<>();
                    HashMap<String, String> membersHm = new HashMap<>();
                    HashMap<String, String> reportsHm = new HashMap<>();

                    //getting all members. If macAddress is a valid SLOCA user, if not, email will be "null".
                    for (String macAdd : members) {
                        String email = userDao.retrieveUserEmail(macAdd);
                        membersHm.put(macAdd, email);
                    }

                    //getting all reports' details, locationId and duration spent as a group
                    for (GroupLocationReport grpReport : grpReports) {
                        String locId = grpReport.getLocationId();
                        double duration = grpReport.getDuration();
                        String strDuration = "";
                        strDuration += duration;

                        if (reportsHm.containsKey(locId)) {
                            double newDuration = Double.parseDouble(reportsHm.get(locId)) + duration;
                            String newValue = "";
                            newValue += newDuration;
                            reportsHm.put(locId, newValue);
                        } else {
                            reportsHm.put(locId, strDuration);
                        }

                    }
                    asValue.add(membersHm);
                    asValue.add(reportsHm);
                    toJsp.put(group, asValue);
                }

                request.setAttribute("date", date);
                request.setAttribute("time", time);
                request.setAttribute("groups", toJsp);
                request.setAttribute("users", userMap.size());
                RequestDispatcher rs = request.getRequestDispatcher("agd.jsp");
                rs.forward(request, response);
            } else {
                request.setAttribute("error", "We found no data!");
                RequestDispatcher rs = request.getRequestDispatcher("agd.jsp");
                rs.forward(request, response);
            }
        } catch (DateTimeParseException ex) {
            request.setAttribute("error", "Please enter a valid Date/Time! (yyyy-mm-dd HH:mm:ss)");
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
