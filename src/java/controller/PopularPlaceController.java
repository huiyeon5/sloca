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
import javax.servlet.http.HttpSession;

/**
 *
 * @author Wei Ming
 */
@WebServlet(name = "PopularPlaceController", urlPatterns = {"/PopularPlaceController"})
public class PopularPlaceController extends HttpServlet {

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
        //get parameter from BLRPopularPlace.jsp
        String date = request.getParameter("date");
        String time = request.getParameter("time");
        String num = request.getParameter("k");
        int k = Integer.parseInt(num);
        try{
            SLOCADate dateTime = new SLOCADate(date,time);
        }catch(DateTimeParseException e){
            request.setAttribute("error", "Please enter a valid Date/Time! (yyyy-mm-dd HH: mm : ss)");
            RequestDispatcher rd = request.getRequestDispatcher("topkpopular.jsp");
            rd.forward(request,response);
            return;
        }
        BLRDAO blrdao = new BLRDAO();
        HashMap<String, Integer> popMap = blrdao.retrieveAll(date, time);

        if (popMap != null && !popMap.isEmpty()) {
            Iterator<String> iter = popMap.keySet().iterator();
            TreeMap<Integer, ArrayList<String>> sortedMap = new TreeMap();
            while(iter.hasNext()){
                String semanticPlace = iter.next();
                int numOfPeople = popMap.get(semanticPlace);
                
                if(!sortedMap.containsKey(numOfPeople)){
                    ArrayList<String> places = new ArrayList<>();
                    places.add(semanticPlace);
                    sortedMap.put(numOfPeople,places);
                }else{
                    ArrayList<String> places = sortedMap.get(numOfPeople);
                    places.add(semanticPlace);
                    sortedMap.put(numOfPeople,places);
                }
            }

            TreeMap<Integer, ArrayList<String>> resultMap = new TreeMap();
            for(int i = 0;i < k;i++){
                Map.Entry<Integer, ArrayList<String>> entry = sortedMap.lastEntry();
                if(entry!=null){
                    int numPeople = entry.getKey();
                    ArrayList<String> semanticPlaces = entry.getValue();
                    resultMap.put(numPeople, semanticPlaces);
                    sortedMap.remove(numPeople);
                }
            }
            HttpSession session;
            session = request.getSession();
            session.setAttribute("popPlace", "done");
            
            request.setAttribute("k", num);
            request.setAttribute("date",date);
            request.setAttribute("time", time);
            request.setAttribute("resultMap", resultMap);
            RequestDispatcher rd = request.getRequestDispatcher("topkpopular.jsp");
            rd.forward(request,response);
            return;
        }
        HttpSession session;
        session = request.getSession();
        session.setAttribute("popPlace", "done");
        request.setAttribute("resultMap", null);
        RequestDispatcher rd = request.getRequestDispatcher("topkpopular.jsp");
        rd.forward(request,response);
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
