/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import DAO.BLRDAO;
import entity.SLOCADate;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Huiyeon Kim
 */
@WebServlet(name = "NextPlaceController", urlPatterns = {"/NextPlaceController"})
public class NextPlaceController extends HttpServlet {

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

        String semanticPlace = request.getParameter("semanticPlace");
        String date = request.getParameter("date");
        String time = request.getParameter("time");
        String kValue = request.getParameter("topk");
        int k = Integer.parseInt(kValue);
        SLOCADate dateTime = null;

        try {
            dateTime = new SLOCADate(date, time);
        } catch (DateTimeParseException e) {
            request.setAttribute("dateError", "Enter a valid Date/Time! (yyyy-mm-dd HH:mm:ss)");
            request.getRequestDispatcher("nextplace.jsp").forward(request, response);
            return;
        }
        SLOCADate endDateTime = dateTime.retrieveMinutesAfter(15);

        BLRDAO bDAO = new BLRDAO();

        ArrayList<String> macAddresses = bDAO.retrieveMacBySemAndTime(semanticPlace, dateTime);

        int numOfPeopleTotal;
        if (macAddresses != null && macAddresses.size() > 0) {
            numOfPeopleTotal = macAddresses.size();
        } else {
            request.setAttribute("error", "We found no data!");
            request.getRequestDispatcher("nextplace.jsp").forward(request, response);
            return;
        }

        //key -> macAddress Value -> semanticPlace, dateTime
        HashMap<String, ArrayList<ArrayList<String>>> nextLocations = bDAO.retrieveNextLocations(macAddresses, dateTime);
        HashMap<String, Integer> countByLocations = new HashMap<>();

        ArrayList<ArrayList<String>> tempLocationData = null;
        String tempSemPlace = "";
        SLOCADate tempFormatedTime = null;
        HashMap<String, Long> countOfTime = new HashMap<>();
        int countNumWhoMove = 0;
        for (String curMac : nextLocations.keySet()) {
            tempLocationData = nextLocations.get(curMac); //First location update in 2nd location: <Sem Place,dateTime>
            for (int i = tempLocationData.size() - 1; i >= 0; i--) {
                tempSemPlace = tempLocationData.get(i).get(0); //Take last semPlace from the location update
                String tempUpdateTime = tempLocationData.get(i).get(1); //Take last dateTime from the location update
                tempFormatedTime = new SLOCADate(tempUpdateTime.substring(0, tempUpdateTime.length() - 2));

                if (SLOCADate.getDuration(tempFormatedTime, endDateTime) >= 300) {
                    if (countByLocations.containsKey(tempSemPlace)) {
                        int tempCount = countByLocations.get(tempSemPlace);
                        countByLocations.put(tempSemPlace, tempCount + 1);
                    } else {
                        countByLocations.put(tempSemPlace, 1);
                    }
                    countNumWhoMove++;
                    break;
                }
            }

            //System.out.println(countByLocations);
        }
        // Swap value and key around
        TreeMap<Integer, ArrayList<String>> swapedValueAndKey = new TreeMap<>();
        for (String tempMac_address : countByLocations.keySet()) {
            ArrayList<String> macs = new ArrayList<>();
            int curLength = countByLocations.get(tempMac_address);
            if (swapedValueAndKey.containsKey(curLength)) {
                macs = swapedValueAndKey.get(curLength);
                macs.add(tempMac_address);
                swapedValueAndKey.put(curLength, macs);
            } else {
                macs.add(tempMac_address);
                swapedValueAndKey.put(curLength, macs);
            }
        }

        request.setAttribute("date", date);
        request.setAttribute("time", time);
        request.setAttribute("semanticPlace", semanticPlace);
        request.setAttribute("resultMap", swapedValueAndKey);
        request.setAttribute("numOfPeople", numOfPeopleTotal);
        request.setAttribute("countNumWhoMove", countNumWhoMove);
        request.setAttribute("k", k);
        RequestDispatcher rd = request.getRequestDispatcher("nextplace.jsp");
        rd.forward(request, response);
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
