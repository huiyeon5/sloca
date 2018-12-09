/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import DAO.AGDDAO;
import DAO.UserDAO;
import entity.LocationReport;
import entity.SLOCADate;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utility.ValidateUtility;

/**
 *
 * @author Keith
 */
@WebServlet(name = "CompanionsServlet", urlPatterns = {"/CompanionsServlet"})
public class CompanionsServlet extends HttpServlet {

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
        PrintWriter out = response.getWriter();

        String mac_address = request.getParameter("mac_address");
        String date = request.getParameter("date");
        String time = request.getParameter("time");
        String topk = request.getParameter("topk");
        
        ValidateUtility vu = new ValidateUtility();
        vu.updateMacSet();
        System.out.println((!vu.checkMacAddress(mac_address)) +" "+ (date == null) +" "+ (date.trim().length() == 0) +" "+ (time == null) +" "+ (time.trim().length() == 0));
        if (!vu.checkMacAddress(mac_address) || date == null || date.trim().length() == 0 || time == null || time.trim().length() == 0) {
            request.setAttribute("error", "Please enter a valid mac address or date (yyyy-mm-dd) or time (hh:mm:ss)");
            RequestDispatcher rd = request.getRequestDispatcher("topkcompanions.jsp");
            rd.forward(request, response);
            return;
        } else {
            System.out.println(!vu.containsMac(mac_address));
            if (!vu.containsMac(mac_address)) {
                request.setAttribute("error", "We found no data!");
                RequestDispatcher rd = request.getRequestDispatcher("topkcompanions.jsp");
                rd.forward(request, response);
                return;
            }
        }
        
        SLOCADate requestedDate = null;
        try {
            SLOCADate dt = new SLOCADate(date, time);

            AGDDAO agdDAO = new AGDDAO();

            HashMap<String, ArrayList<ArrayList<String>>> userMap = agdDAO.getEveryoneInTime(dt); //returns list of users <mac_add, arraylist of (locId, time)>
            HashMap<String, ArrayList<LocationReport>> reportList = new HashMap<>();
            Iterator<String> iter1 = userMap.keySet().iterator();
            
            if(userMap == null || userMap.isEmpty()){
                request.setAttribute("error", "We found no data!");
                RequestDispatcher rd = request.getRequestDispatcher("topkcompanions.jsp");
                rd.forward(request, response);
                return;
            }

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
    
                System.out.println("Mac :" + mac);
                System.out.println("Location Reports : " + reports.size());
                System.out.println("");
                reportList.put(mac, reports);
            }

            // --------------------------------------------------------------------------------------------------
            /*
            out.print("<h2>After - Ouput</h2>");

            for (String tempKey : reportList.keySet()) {

                out.println("<b>Mac: " + tempKey + "</b></br>");
                out.print("Size: " + reportList.get(tempKey).size());

                for (int i = 0; i < reportList.get(tempKey).size(); i++) {
                    LocationReport tempTempReport = reportList.get(tempKey).get(i);
                    //out.println(tempTempReport);
                    out.print("Mac_address:" + tempTempReport.getMacAddress());
                    out.println("</br>");
                    out.print(" Location: " + tempTempReport.getLocationId());
                    out.println("</br>");
                    out.print(" Start: " + tempTempReport.getStartTime());
                    out.println("</br>");
                    out.print(" End: " + tempTempReport.getEndTime());
                    out.println("</br>");
                    out.print(" Duration: " + tempTempReport.getDuration());
                    out.println("</br>");
                }
            }
             */
            // --------------------------------------------------------------------------------------------------
            ArrayList<LocationReport> requestedReports = reportList.get(mac_address);   //the person in question
            /*
            out.print("<b>Mac: " + mac_address + "</b></br>");
            for (LocationReport a : requestedReports) {
                out.print("[" + a.getLocationId() + " , " + a.getStartTime() + " , " + a.getEndTime() + "]</br>");
            }
            out.print("</br>");
            out.print("<b>Mac: b32d33336582892ac9074a86dd3c070bf2f6d0d1 </b></br>");
            for (LocationReport b : reportList.get("b32d33336582892ac9074a86dd3c070bf2f6d0d1")) {
                out.print("[" + b.getLocationId() + " , " + b.getStartTime() + " , " + b.getEndTime() + "]</br>");
            }
            out.print("</br></br></br>");
             */
            HashMap<String, Long> listTimeSpent = new HashMap<>();

            for (LocationReport selectedLR : requestedReports) {
                for (String mac : reportList.keySet()) {
                    if (!mac.equals(mac_address)) {
                        for (LocationReport curLR : reportList.get(mac)) {
                            if (selectedLR.compareTo(curLR)) {
                                long timeTogether = selectedLR.getTimeTogether(curLR);
                                //out.print("Mac: " + mac + "</br>");
                                //out.print("Location:" + curLR.getLocationId() + "</br>");
                                //out.print("Start: " + curLR.getStartTime() + "</br>");
                                //out.print("Start: " + curLR.getEndTime() + "</br>");
                                //out.print("Time Spd: " + timeTogether + "</br>");
                                //out.print("<hr>");
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
            /*
            out.print("<h2><b>BEFORE</b> SORTING</h2>");
            for (String a : listTimeSpent.keySet()) {
                out.print("Mac: " + a);
                out.print("</br>");
                out.print("Time Spd: " + listTimeSpent.get(a));
                out.print("</br>");
                out.print("<hr>");
            }
             */
            //create emailList
            if(listTimeSpent == null || listTimeSpent.isEmpty()){
                request.setAttribute("error", "We found no data!");
                RequestDispatcher rd = request.getRequestDispatcher("topkcompanions.jsp");
                rd.forward(request, response);
                return;
            }
            
            HashMap<String, String> emailList = new HashMap<>();
            UserDAO userDAO = new UserDAO();
            //sorting the hashmap by value
            LinkedHashMap<String, Long> sortTimeSpent = sortByValues(listTimeSpent);
            System.out.println("After Sorting:");
            Set set = sortTimeSpent.keySet();
            Iterator<String> iter = set.iterator();
            while (iter.hasNext()) {
                String mac = iter.next();
                //query and take email
                emailList.put(mac, userDAO.retrieveUserEmail(mac));
                //query and take email
                System.out.print(mac + ": ");
                System.out.println(sortTimeSpent.get(mac));
            }
            /*
            out.print("<h2><b>AFTER</b> SORTING</h2>");
            for (String a : sortTimeSpent.keySet()) {
                out.print("Mac: " + a);
                out.print("</br>");
                out.print("Time Spd: " + sortTimeSpent.get(a));
                out.print("</br>");
                out.print("<hr>");
            }
             */
            request.setAttribute("return", sortTimeSpent);
            request.setAttribute("topK", topk);
            request.setAttribute("email", emailList);
            request.setAttribute("date", date);
            request.setAttribute("time", time);
            request.setAttribute("macAddress", mac_address);
            
            RequestDispatcher rd = request.getRequestDispatcher("topkcompanions.jsp");
            rd.forward(request, response);

        } catch (DateTimeParseException e) {
            request.setAttribute("error", "Please enter a valid Date/Time! (yyyy-mm-dd HH:mm:ss)");
            RequestDispatcher rd = request.getRequestDispatcher("topkcompanions.jsp");
            rd.forward(request, response);
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
