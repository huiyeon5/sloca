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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utility.ValidateUtility;

/**
 *
 * @author Huiyeon Kim
 */
@WebServlet(name = "jsonNextPlace", urlPatterns = {"/json/top-k-next-places"})
public class JsonNextPlace extends HttpServlet {

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

        SLOCADate dateTime = null;

        //return error messages such as "invalid token", "blank date" & "missing floor" 
        ArrayList<String> errorMsgs = new ArrayList<String>();

        //Creating a Map object to check if there are all request's names (token, k(optional), date and origin)
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
                    errorMsgs.add("invalid token");
                }
            }

        } else {
            errorMsgs.add("missing token");
        }
        if(!errorMsgs.isEmpty()){
            jsonResult.addProperty("status", "error");
            Collections.sort(errorMsgs);
            JsonArray jsonErrorArray = new JsonArray();
            for (String errorMsg : errorMsgs) {
                JsonPrimitive msg = new JsonPrimitive(errorMsg);
                jsonErrorArray.add(msg);
            }
            jsonResult.add("messages", jsonErrorArray);
            out.println(gson.toJson(jsonResult));
            return;
        }

        int k;
        if (requestNames.containsKey("k")) {
            k = 0;
            
            try {
                k = Integer.parseInt(request.getParameter("k"));
                if (k < 1 || k > 10) {
                    errorMsgs.add("invalid k");
                }
            } catch (NumberFormatException e) {
                if(request.getParameter("k") == null || request.getParameter("k").trim().length() == 0){
                    errorMsgs.add("blank k");
                }else{
                    errorMsgs.add("invalid k");
                }
            }

        } else {
            k = 3;
        }

        String semanticPlace = "";
        if (requestNames.containsKey("origin")) {
            String semPlace = request.getParameter("origin");
            if (semPlace == null || semPlace.trim().length() == 0) {
                errorMsgs.add("blank origin");
            } else {
                System.out.println(semPlace);
                ValidateUtility validate = new ValidateUtility();
                validate.updateSemSet();
                System.out.println(validate.containsSem(semPlace));
                if (validate.containsSem(semPlace)) {
                    semanticPlace = semPlace;
                } else {
                    errorMsgs.add("invalid origin");
                }
            }
        } else {
            errorMsgs.add("missing origin");
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
                    dateTime = new SLOCADate(LTD);
                } catch (DateTimeParseException e) {
                    errorMsgs.add("invalid date");
                }
            }
        } else {
            errorMsgs.add("missing date");
        }
        
        Collections.sort(errorMsgs);

        System.out.println("Going in");
        if (errorMsgs.isEmpty() && (k > 0 && k < 11) && !semanticPlace.equals("") && dateTime != null) {
            System.out.println("In");
            System.out.println(semanticPlace);
            System.out.println(dateTime);
            SLOCADate endDateTime = dateTime.retrieveMinutesAfter(15);

            BLRDAO bDAO = new BLRDAO();

            ArrayList<String> macAddresses = bDAO.retrieveMacBySemAndTime(semanticPlace, dateTime);
            System.out.println(macAddresses);
            int numOfPeopleTotal;
            if (macAddresses != null && macAddresses.size() > 0) {
                numOfPeopleTotal = macAddresses.size();
            } else {
                jsonResult.addProperty("status","error");
                jsonResult.addProperty("error","no data found");
                out.println(gson.toJson(jsonResult));
                return;
            }

            //key -> macAddress Value -> semanticPlace, dateTime
            HashMap<String, ArrayList<ArrayList<String>>> nextLocations = bDAO.retrieveNextLocations(macAddresses, dateTime);
            HashMap<String, Integer> countOfLocations = new HashMap<>();
            if(nextLocations == null || nextLocations.isEmpty()){
                jsonResult.addProperty("status","error");
                jsonResult.addProperty("error","no data found");
                out.println(gson.toJson(jsonResult));
                return;
            }
            
            
            Iterator<String> iter = nextLocations.keySet().iterator();
            String firstMac = "";
            ArrayList<ArrayList<String>> firstUpdate = null;
            String prevSemPlace = "";
            SLOCADate prevUpdateTime = null;
            HashMap<String, Long> countOfTime = new HashMap<>();
            while (iter.hasNext()) {
                firstMac = iter.next();
                firstUpdate = nextLocations.get(firstMac);
                prevSemPlace = firstUpdate.get(firstUpdate.size() - 1).get(0);
                String tempUpdateTime = firstUpdate.get(firstUpdate.size() - 1).get(1);
                prevUpdateTime = new SLOCADate(tempUpdateTime.substring(0, tempUpdateTime.length() - 2));

                if (SLOCADate.getDuration(prevUpdateTime, endDateTime) >= 300) {
                    if (countOfLocations.containsKey(prevSemPlace)) {
                        countOfLocations.put(prevSemPlace, countOfLocations.get(prevSemPlace) + 1);
                    } else {
                        countOfLocations.put(prevSemPlace, 1);
                    }
                    continue;
                } else {
                    countOfTime.put(prevSemPlace, SLOCADate.getDuration(prevUpdateTime, endDateTime));
                }

                for (int i = firstUpdate.size() - 2; i >= 0; i--) {
                    ArrayList<String> currentUpdate = firstUpdate.get(i);
                    String currSemPlace = currentUpdate.get(0);
                    SLOCADate currDateTime = new SLOCADate(currentUpdate.get(1).substring(0, currentUpdate.get(1).length() - 2));

                    if (prevSemPlace.equals(currSemPlace)) {
                        countOfTime.put(currSemPlace, countOfTime.get(currSemPlace) + SLOCADate.getDuration(currDateTime, prevUpdateTime));
                        if (countOfTime.get(currSemPlace) >= 300) {
                            if (countOfLocations.containsKey(currSemPlace)) {
                                countOfLocations.put(currSemPlace, countOfLocations.get(currSemPlace) + 1);
                            } else {
                                countOfLocations.put(currSemPlace, 1);
                            }
                            break;
                        }
                    } else {
                        countOfTime.remove(prevSemPlace);
                        countOfTime.put(currSemPlace, SLOCADate.getDuration(currDateTime, prevUpdateTime));
                        if (countOfTime.get(currSemPlace) >= 300) {
                            if (countOfLocations.containsKey(currSemPlace)) {
                                countOfLocations.put(currSemPlace, countOfLocations.get(currSemPlace) + 1);
                            } else {
                                countOfLocations.put(currSemPlace, 1);
                            }
                            break;
                        }
                    }
                    prevSemPlace = currSemPlace;
                    prevUpdateTime = currDateTime;
                }
            }

            TreeMap<Integer, ArrayList<String>> sortedCountOfLocations = new TreeMap<>();
            int nextPlacePeople = 0;
            for(String sem: countOfLocations.keySet()){
                nextPlacePeople += countOfLocations.get(sem);
            }
            iter = countOfLocations.keySet().iterator();
            ArrayList<String> temp = new ArrayList<>();
            while (iter.hasNext()) {
                String nextPlace = iter.next();
                int countOfNextPlace = countOfLocations.get(nextPlace);
                if (!sortedCountOfLocations.containsKey(countOfNextPlace)) {
                    temp = new ArrayList<>();
                    temp.add(nextPlace);
                    sortedCountOfLocations.put(countOfNextPlace, temp);
                } else {
                    temp = sortedCountOfLocations.get(countOfNextPlace);
                    temp.add(nextPlace);
                    sortedCountOfLocations.put(countOfNextPlace, temp);
                }
            }

            TreeMap<Integer, ArrayList<String>> resultMap = new TreeMap<>();
            for (int i = 0; i < k; i++) {
                Map.Entry<Integer, ArrayList<String>> entry = sortedCountOfLocations.lastEntry();
                if (entry != null) {
                    int count = entry.getKey();
                    ArrayList<String> semPlaces = entry.getValue();

                    resultMap.put(count, semPlaces);
                    sortedCountOfLocations.remove(count);
                } else {
                    break;
                }
            }

            System.out.println("Got output");

            if (resultMap.size() > 0) {
                jsonResult.addProperty("total-users", numOfPeopleTotal);
                jsonResult.addProperty("total-next-place-users", nextPlacePeople);
                JsonArray result = new JsonArray();
                int i = 1;
                for (int key : resultMap.descendingKeySet()) {

                    ArrayList<String> tempList = resultMap.get(key);

                    if (tempList.size() == 1) {
                        JsonObject output = new JsonObject();
                        output.addProperty("rank", i);
                        output.addProperty("semantic-place", tempList.get(0));
                        output.addProperty("count", countOfLocations.get(tempList.get(0)));
                        result.add(output);
                    } else {
                        ArrayList<String> sortList = tempList;
                        sortBySem(sortList);
                        for (int j = 0; j < sortList.size(); j++) {
                            JsonObject output = new JsonObject();
                            output.addProperty("rank", i);
                            String sem = tempList.get(j);
                            output.addProperty("semantic-place", sem);
                            output.addProperty("count", countOfLocations.get(sem));
                            result.add(output);
                        }
                    }
                    i++;
                }

                jsonResult.add("result", result);
                out.println(gson.toJson(jsonResult));
            } else {
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
        } else {
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
    }

    /**
     * Sorts the list by semantic place
     * @param temp - arrayList to sort
     * @return sorted arrayList
     */
    public ArrayList<String> sortBySem(ArrayList<String> temp) {

        for (int i = 0; i < temp.size(); i++) {
            for (int j = i + 1; j < temp.size(); j++) {
                if (temp.get(i).compareTo(temp.get(j)) > 0) {

                    String tempStr = temp.get(i);
                    temp.set(i, temp.get(j));
                    temp.set(j, tempStr);
                }
            }
        }

        return temp;

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
