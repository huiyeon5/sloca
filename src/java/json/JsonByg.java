package json;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import DAO.BLRDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import entity.SLOCADate;
import entity.User;
import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

/**
 *
 * @author Wei Ming
 */
@WebServlet(name = "basic-loc-report", urlPatterns = {"/json/basic-loc-report"})
public class JsonByg extends HttpServlet {

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

        SLOCADate dateTime = null;
        String inputDate = "";

        ArrayList<String> errorMsgs = new ArrayList<>(); //storing error messages
        Map<String, String[]> requestNames = request.getParameterMap();

        //Check for token in the URL. If there is, the token will be verified. 
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

        if (errorMsgs.size() != 0) {
            jsonResult.addProperty("status", "error");
            JsonArray jsonErrorArray = new JsonArray();
            for (String message : errorMsgs) {
                JsonPrimitive msg = new JsonPrimitive(message);
                jsonErrorArray.add(msg);
            }
            jsonResult.add("messages", jsonErrorArray);
            out.println(gson.toJson(jsonResult));
            return;
        }

        //Check for date in the URL
        if (requestNames.containsKey("date")) {
            inputDate = request.getParameter("date");
            if (inputDate == null || inputDate.trim().length() == 0) { // Check for blank date
                errorMsgs.add("blank date");
            } else {
                inputDate = inputDate.replace('T', ' ');
                try { // Check for correct date format
                    LocalDateTime LTD = LocalDateTime.parse(inputDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    dateTime = new SLOCADate(LTD);
                } catch (DateTimeParseException e) { //throws exception if the string cannot be parsed as it doesn't match to DateTimeFormatter's pattern
                    errorMsgs.add("invalid date");
                }
            }
        } else { // Check for missing date
            errorMsgs.add("missing date");
        }

        ArrayList<String> storeOrders = new ArrayList<>();

        //Check for order in URL
        if (requestNames.containsKey("order")) {
            String selectedOrder = request.getParameter("order");
            if (selectedOrder == null || selectedOrder.trim().length() == 0) {
                errorMsgs.add("blank order");
            } else {
                String[] orderList = selectedOrder.split(",");
                for (String order : orderList) {
                    boolean invalidOrder = true;
                    if (order.equals("school")) {
                        storeOrders.add("school");
                        invalidOrder = false;
                    }
                    if (order.equals("year")) {
                        storeOrders.add("year");
                        invalidOrder = false;
                    }
                    if (order.equals("gender")) {
                        storeOrders.add("gender");
                        invalidOrder = false;
                    }
                    if (invalidOrder) {
                        errorMsgs.add("invalid order");
                    }
                }
            }
        } else { //Check for missing order
            errorMsgs.add("missing order");
        }

        if (errorMsgs.isEmpty()) { // if there no error message error being added
            BLRDAO blrDAO = new BLRDAO();
            JsonArray orderArray = new JsonArray();

            String[] splitDatetime = inputDate.split(" ", 2);
            String date = splitDatetime[0];
            String time = splitDatetime[1];

            ArrayList<User> userList = blrDAO.retrieveSLOCAUsersFromDateTime(date, time);

            ArrayList<String> sequence = storeOrders;

            if (!sequence.isEmpty()) {

                if (sequence.size() == 1) {
                    blrDAO.processOneOrderResult(sequence.get(0), userList, true);
                    TreeMap<String, Integer> firstOrderResult = blrDAO.getFirstOrderResult();
                    if (!firstOrderResult.isEmpty()) {
                        JsonArray firstArray = new JsonArray();
                        Set<String> firstKeys = firstOrderResult.keySet();

                        for (String firstKey : firstKeys) {
                            JsonObject firstObject = new JsonObject();
                            int count = firstOrderResult.get(firstKey);
                            if (sequence.get(0).equals("year")) {
                                int year = Integer.parseInt(firstKey);
                                firstObject.addProperty(sequence.get(0), year);
                            } else {
                                firstObject.addProperty(sequence.get(0), firstKey);
                            }
                            firstObject.addProperty("count", count);
                            firstArray.add(firstObject);
                        }

                        jsonResult.addProperty("status", "success");
                        jsonResult.add("breakdown", firstArray);
                        out.println(gson.toJson(jsonResult));
                    } else {
                        jsonResult.addProperty("status", "error");
                        jsonResult.addProperty("messages", "No Data Found");
                        out.println(gson.toJson(jsonResult));
                    }
                }

                if (sequence.size() == 2) {
                    JsonObject byg = new JsonObject();
                    blrDAO.processOneOrderResult(sequence.get(0), userList, true);
                    blrDAO.processTwoOrderResult(sequence.get(0), sequence.get(1), userList, true);
                    TreeMap<String, Integer> firstOrderResult = blrDAO.getFirstOrderResult();
                    TreeMap<String, TreeMap<String, Integer>> secondOrderResult = blrDAO.getSecondOrderResult();

                    if (!firstOrderResult.isEmpty()) {
                        JsonArray firstArray = new JsonArray();
                        Set<String> firstKeys = firstOrderResult.keySet();

                        for (String firstKey : firstKeys) {
                            JsonObject firstObject = new JsonObject();
                            int count = firstOrderResult.get(firstKey);
                            if (sequence.get(0).equals("year")) {
                                int year = Integer.parseInt(firstKey);
                                firstObject.addProperty(sequence.get(0), year);
                            } else {
                                firstObject.addProperty(sequence.get(0), firstKey);
                            }
                            firstObject.addProperty("count", count);

                            //getting 
                            TreeMap<String, Integer> secondResultMap = secondOrderResult.get(firstKey);
                            Set<String> secondKeys = secondResultMap.keySet();
                            JsonArray secArray = new JsonArray();

                            for (String secondKey : secondKeys) {
                                JsonObject secObject = new JsonObject();
                                int secCount = secondResultMap.get(secondKey);
                                if (sequence.get(1).equals("year")) {
                                    int year = Integer.parseInt(secondKey);
                                    secObject.addProperty(sequence.get(1), year);
                                } else {
                                    secObject.addProperty(sequence.get(1), secondKey);
                                }
                                secObject.addProperty("count", secCount);
                                secArray.add(secObject);
                            }

                            firstObject.add("breakdown", secArray);
                            firstArray.add(firstObject);
                        }

                        jsonResult.addProperty("status", "success");
                        jsonResult.add("breakdown", firstArray);
                        out.println(gson.toJson(jsonResult));
                    } else {
                        jsonResult.addProperty("status", "error");
                        jsonResult.addProperty("messages", "No Data Found");
                        out.println(gson.toJson(jsonResult));
                    }
                }

                if (sequence.size() == 3) {
                    JsonObject byg = new JsonObject();
                    blrDAO.processOneOrderResult(sequence.get(0), userList, true);
                    blrDAO.processTwoOrderResult(sequence.get(0), sequence.get(1), userList, true);
                    blrDAO.processThreeOrderResult(sequence.get(0), sequence.get(1), sequence.get(2), userList);
                    TreeMap<String, Integer> firstOrderResult = blrDAO.getFirstOrderResult();
                    TreeMap<String, TreeMap<String, Integer>> secondOrderResult = blrDAO.getSecondOrderResult();
                    TreeMap<String, TreeMap<String, Integer>> thirdOrderResult = blrDAO.getThirdOrderResult();

                    if (!firstOrderResult.isEmpty()) {
                        JsonArray firstArray = new JsonArray();
                        Set<String> firstKeys = firstOrderResult.keySet();

                        for (String firstKey : firstKeys) {
                            JsonObject firstObject = new JsonObject();
                            int count = firstOrderResult.get(firstKey);
                            if (sequence.get(0).equals("year")) {
                                int year = Integer.parseInt(firstKey);
                                firstObject.addProperty(sequence.get(0), year);
                            } else {
                                firstObject.addProperty(sequence.get(0), firstKey);
                            }
                            firstObject.addProperty("count", count);

                            //getting 
                            TreeMap<String, Integer> secondResultMap = secondOrderResult.get(firstKey);
                            Set<String> secondKeys = secondResultMap.keySet();
                            JsonArray secArray = new JsonArray();

                            for (String secondKey : secondKeys) {
                                JsonObject secObject = new JsonObject();
                                int secCount = secondResultMap.get(secondKey);
                                if (sequence.get(1).equals("year")) {
                                    int year = Integer.parseInt(secondKey);
                                    secObject.addProperty(sequence.get(1), year);
                                } else {
                                    secObject.addProperty(sequence.get(1), secondKey);
                                }
                                secObject.addProperty("count", secCount);
                                secArray.add(secObject);

                                String keyToCheck = firstKey + " " + secondKey;
                                TreeMap<String, Integer> thirdResultMap = thirdOrderResult.get(keyToCheck);
                                Set<String> thirdKeys = thirdResultMap.keySet();
                                JsonArray thirdArray = new JsonArray();

                                for (String thirdKey : thirdKeys) {
                                    JsonObject thirdObject = new JsonObject();
                                    int thirdCount = thirdResultMap.get(thirdKey);
                                    if (sequence.get(2).equals("year")) {
                                        int year = Integer.parseInt(thirdKey);
                                        thirdObject.addProperty(sequence.get(2), year);
                                    } else {
                                        thirdObject.addProperty(sequence.get(2), thirdKey);
                                    }
                                    thirdObject.addProperty("count", thirdCount);
                                    thirdArray.add(thirdObject);
                                }

                                secObject.add("breakdown", thirdArray);

                            }

                            firstObject.add("breakdown", secArray);
                            firstArray.add(firstObject);
                        }

                        jsonResult.addProperty("status", "success");
                        jsonResult.add("breakdown", firstArray);
                        out.println(gson.toJson(jsonResult));
                    } else {
                        jsonResult.addProperty("status", "error");
                        jsonResult.addProperty("messages", "No Data Found");
                        out.println(gson.toJson(jsonResult));
                    }
                }
            } else {
                jsonResult.addProperty("status", "error");
                JsonArray jsonErrorArray = new JsonArray();
                for (String message : errorMsgs) {
                    JsonPrimitive msg = new JsonPrimitive(message);
                    jsonErrorArray.add(msg);
                }
                jsonResult.add("messages", jsonErrorArray);
                out.println(gson.toJson(jsonResult));
            }
        } else {
            jsonResult.addProperty("status", "error");
            JsonArray jsonErrorArray = new JsonArray();
            for (String message : errorMsgs) {
                JsonPrimitive msg = new JsonPrimitive(message);
                jsonErrorArray.add(msg);
            }
            jsonResult.add("messages", jsonErrorArray);
            out.println(gson.toJson(jsonResult));
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
        try {
            processRequest(request, response);

        } catch (JWTException ex) {
            Logger.getLogger(JsonByg.class
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
            Logger.getLogger(JsonByg.class
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
