/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package json;

import DAO.BLRDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Wei Ming
 */
@WebServlet(name = "top-k-popular-places", urlPatterns = {"/json/top-k-popular-places"})
public class JsonPopularPlaces extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws JWTException if token not verified
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JWTException {
        response.setContentType("text/json;charset=UTF-8");

        PrintWriter out = response.getWriter();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonResult = new JsonObject();
        ArrayList<String> errorMsg = new ArrayList<>();
        Map<String, String[]> requestNames = request.getParameterMap();
        String inputDate = "";
        String forMethod = "";
        SLOCADate dateTime = null;

        //try use if request.getParameter is null later
        //check if token is valid
        if (requestNames.containsKey("token")) {
            String token = request.getParameter("token");
            if (token == null || token.trim().length() == 0) { //check for blank token
                errorMsg.add("blank token");
            } else {
                try {
                    String username = JWTUtility.verify(token, "WELOVESESOMUCH");
                    if (username == null) { // check for invalid token
                        errorMsg.add("invalid token");
                    }
                } catch (JWTException e) {
                    errorMsg.add("invalid token");
                }
            }
        } else { // if token is missing
            errorMsg.add("missing token");
        }

        //if anything wrong w the token, return
        if (errorMsg != null && errorMsg.size() != 0) {
            jsonResult.addProperty("status", "error");
            JsonArray jsonErrorArray = new JsonArray();
            for (String eMsg : errorMsg) {
                JsonPrimitive msg = new JsonPrimitive(eMsg);
                jsonErrorArray.add(msg);
            }
            jsonResult.add("messages", jsonErrorArray);
            out.println(gson.toJson(jsonResult));
            return;
        }

        //check if date field is valid, missing or blank
        if (requestNames.containsKey("date")) {
            inputDate = request.getParameter("date");
            forMethod = inputDate;
            if (inputDate == null || inputDate.trim().length() == 0) { // Check for blank date
                errorMsg.add("blank date");
            } else {
                inputDate = inputDate.replace('T', ' ');
                try { // Check for correct date format
                    LocalDateTime LTD = LocalDateTime.parse(inputDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    dateTime = new SLOCADate(LTD);
                } catch (DateTimeParseException e) { //throws exception if the string cannot be parsed as it doesn't match to DateTimeFormatter's pattern
                    errorMsg.add("invalid date");
                }
            }

        } else { // Check for missing date
            errorMsg.add("missing date");
        }

        //Checking for topK
        int topkNum = 3;
        if (requestNames.containsKey("k")) {
            String topRank = request.getParameter("k");
            if (topRank == null || topRank.trim().length() == 0) {
                errorMsg.add("blank k");
            } else {
                try {
                    topkNum = Integer.parseInt(topRank);
                    if (topkNum < 1 || topkNum > 10) {
                        errorMsg.add("invalid k");
                    }
                } catch (NumberFormatException e) {
                    errorMsg.add("invalid k");
                }
            }
        }

        //logic validation
        if (errorMsg.size() == 0 && inputDate != "") {
            String[] splitDatetime = forMethod.split("T", 2);
            String date = splitDatetime[0];
            String time = splitDatetime[1];
            if (topkNum >= 1 && topkNum <= 10) {
                BLRDAO blrdao = new BLRDAO();
                HashMap<String, Integer> popMap = blrdao.retrieveAll(date, time);
                if (popMap != null && !popMap.isEmpty()) {
                    Iterator<String> iter = popMap.keySet().iterator();
                    TreeMap<Integer, ArrayList<String>> sortedMap = new TreeMap();
                    while (iter.hasNext()) {
                        String semanticPlace = iter.next();
                        int numOfPeople = popMap.get(semanticPlace);

                        if (!sortedMap.containsKey(numOfPeople)) {
                            ArrayList<String> places = new ArrayList<>();
                            places.add(semanticPlace);
                            sortedMap.put(numOfPeople, places);
                        } else {
                            ArrayList<String> places = sortedMap.get(numOfPeople);
                            places.add(semanticPlace);
                            sortedMap.put(numOfPeople, places);
                        }
                    }

                    JsonArray resultArray = new JsonArray();
                    for (int i = 1; i <= topkNum; i++) {

                        Map.Entry<Integer, ArrayList<String>> entry = sortedMap.lastEntry();
                        if (entry != null) {
                            ArrayList<String> semanticPlaces = entry.getValue();
                            Collections.sort(semanticPlaces);
                            int numPeople = entry.getKey();
                            for (String semPlace : semanticPlaces) {
                                JsonObject result = new JsonObject();
                                result.addProperty("rank", i);
                                result.addProperty("semantic-place", semPlace);
                                result.addProperty("count", numPeople);
                                resultArray.add(result);
                            }
                            sortedMap.remove(numPeople);
                        }
                    }
                    jsonResult.addProperty("status", "success");
                    jsonResult.add("results", resultArray);
                } else {
                    jsonResult.addProperty("status", "error");
                    jsonResult.addProperty("messages", "no data found");
                }

            }
        } else {
            jsonResult.addProperty("status", "error");
            Collections.sort(errorMsg);
            JsonArray jsonErrorArray = new JsonArray();
            for (String message : errorMsg) {
                JsonPrimitive msg = new JsonPrimitive(message);
                jsonErrorArray.add(msg);
            }
            jsonResult.add("messages", jsonErrorArray);
        }
        out.println(gson.toJson(jsonResult));
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
        try {
            processRequest(request, response);

        } catch (JWTException ex) {
            Logger.getLogger(JsonPopularPlaces.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
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
        try {
            processRequest(request, response);

        } catch (JWTException ex) {
            Logger.getLogger(JsonPopularPlaces.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
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
