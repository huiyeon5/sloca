package controller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import DAO.HeatmapDAO;
import entity.Heatmap;
import entity.SLOCADate;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 * @author Wei Ming
 */
@WebServlet(urlPatterns = {"/HeatmapServlet"})
public class HeatmapServlet extends HttpServlet {

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

        //never settle the name
        String floor = request.getParameter("floor");
        String date = request.getParameter("date");
        String time = request.getParameter("time");

        String dateTime = date + " " + time;

        SLOCADate requestedDate = null;

        try {
            requestedDate = new SLOCADate(dateTime);
        } catch (DateTimeParseException e) {
            request.setAttribute("crowdDensity", null);
            request.setAttribute("error", "Please enter a valid date (yyyy-mm-dd) or time (hh:mm:ss)");
            RequestDispatcher rd = request.getRequestDispatcher("heatmap.jsp");
            rd.forward(request, response);
            return;
        }

        System.out.println(date);
        System.out.println(time);

        //never import dao class
        HeatmapDAO heatmapDAO = new HeatmapDAO();

        TreeMap<String, Integer> resultMap = heatmapDAO.retrieve(requestedDate, floor);
        System.out.println(resultMap.size());

        if (resultMap != null) {
            Iterator<String> iter = resultMap.keySet().iterator();
            int density = 0;
            int countZero = 0;
            while (iter.hasNext()) {
                String semanticPlace = iter.next();
                int numPeople = resultMap.get(semanticPlace);

                if (numPeople == 0) {
                    density = 0;
                    countZero++;
                } else if (numPeople > 0 && numPeople <= 2) {
                    density = 1;
                } else if (numPeople > 2 && numPeople <= 5) {
                    density = 2;
                } else if (numPeople > 5 && numPeople <= 10) {
                    density = 3;
                } else if (numPeople > 10 && numPeople <= 20) {
                    density = 4;
                } else if (numPeople > 20 && numPeople <= 30) {
                    density = 5;
                } else if (numPeople > 30) {
                    density = 6;
                }

                //never import Heatmap class
                Heatmap heatmap = new Heatmap(semanticPlace, numPeople, density);
                heatmapDAO.add(heatmap);
            }

            ArrayList<Heatmap> resultList = heatmapDAO.retrieveResult();

            request.setAttribute("floor", floor);
            request.setAttribute("date", date);
            request.setAttribute("time", time);
            if (countZero == resultMap.size()) {
                request.setAttribute("error", "We found no data!");
            } else {
                request.setAttribute("crowdDensity", resultList);
            }
            RequestDispatcher rd = request.getRequestDispatcher("heatmap.jsp");
            rd.forward(request, response);
        } else {
            request.setAttribute("crowdDensity", null);
            request.setAttribute("error", "No data found.");
            RequestDispatcher rd = request.getRequestDispatcher("heatmap.jsp");
            rd.forward(request, response);
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
