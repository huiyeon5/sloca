/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import DAO.BLRDAO;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import entity.SLOCADate;
import entity.User;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.RequestDispatcher;

/**
 *
 * @author Jia Xian
 */

@WebServlet(name = "BYGController", urlPatterns = {"/BYGController"})
public class BYGController extends HttpServlet {

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
        String firstOrder = request.getParameter("firstOrder"); //School
        String secondOrder = request.getParameter("secondOrder"); //Year
        String thirdOrder = request.getParameter("thirdOrder"); //Gender
            
        BLRDAO blrDAO = new BLRDAO();
        
        try{
            ArrayList<String> sequence = new ArrayList<String>(); //no of ordersaa
            if(firstOrder.equals("school") || firstOrder.equals("year") || firstOrder.equals("gender") ){
                sequence.add(firstOrder);
            }

            if (secondOrder != null) {
                sequence.add(secondOrder);
            }

            if (thirdOrder != null) {
                sequence.add(thirdOrder);
            }
            
            if (sequence.isEmpty()){
                request.setAttribute("error", "Please select at least one choice.");
                RequestDispatcher rs = request.getRequestDispatcher("byg.jsp");
                rs.forward(request, response);
            }
                    
            ArrayList<User> userList = blrDAO.retrieveSLOCAUsersFromDateTime(date, time); // throws DateTimeParseException
            if(userList.isEmpty()){
                request.setAttribute("error", "We found no data!");
                RequestDispatcher rs = request.getRequestDispatcher("byg.jsp");
                rs.forward(request, response);
            }
            

            if (sequence.size() == 1) {
                blrDAO.processOneOrderResult(firstOrder, userList, true);
                TreeMap<String, Integer> firstOrderResult = blrDAO.getFirstOrderResult();
                request.setAttribute("firstOrder", firstOrder);
                request.setAttribute("firstOrderResult", firstOrderResult);
                request.setAttribute("date", time);
                request.setAttribute("time", date);
                request.setAttribute("totalSLOCAUsers", userList.size());

                RequestDispatcher rs = request.getRequestDispatcher("byg.jsp");
                rs.forward(request, response);
            }

            if (sequence.size() == 2) {
                blrDAO.processTwoOrderResult(firstOrder, secondOrder, userList, true);
                TreeMap<String, Integer> firstOrderResult = blrDAO.getFirstOrderResult();
                TreeMap<String, TreeMap<String, Integer>> secondOrderResult = blrDAO.getSecondOrderResult();
                request.setAttribute("firstOrder", firstOrder);
                request.setAttribute("firstOrderResult", firstOrderResult);
                request.setAttribute("secondOrder", secondOrder);
                request.setAttribute("secondOrderResult", secondOrderResult);
                request.setAttribute("date", time);
                request.setAttribute("time", date);
                request.setAttribute("totalSLOCAUsers", userList.size());

                RequestDispatcher rs = request.getRequestDispatcher("byg.jsp");
                rs.forward(request, response);
            }

            if (sequence.size() == 3) {
                blrDAO.processThreeOrderResult(firstOrder, secondOrder, thirdOrder, userList);
                TreeMap<String, Integer> firstOrderResult = blrDAO.getFirstOrderResult();
                TreeMap<String, TreeMap<String, Integer>> secondOrderResult = blrDAO.getSecondOrderResult();
                TreeMap<String, TreeMap<String, Integer>> thirdOrderResult = blrDAO.getThirdOrderResult();

                request.setAttribute("firstOrder", firstOrder);
                request.setAttribute("firstOrderResult", firstOrderResult);
                request.setAttribute("secondOrder", secondOrder);
                request.setAttribute("secondOrderResult", secondOrderResult);
                request.setAttribute("thirdOrder", thirdOrder);
                request.setAttribute("thirdOrderResult", thirdOrderResult);
                request.setAttribute("print", "print");
                request.setAttribute("date", time);
                request.setAttribute("time", date);
                int userSize = userList.size();
                request.setAttribute("totalSLOCAUsers", userSize);

                RequestDispatcher rs = request.getRequestDispatcher("byg.jsp");
                rs.forward(request, response);
            }
        }catch(DateTimeParseException ex){
            request.setAttribute("error", "Please enter a valid Date/Time! (yyyy-mm-dd HH:mm:ss)");
            RequestDispatcher rs = request.getRequestDispatcher("byg.jsp");
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
