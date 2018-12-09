package json;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import DAO.HeatmapDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import entity.Heatmap;
import entity.SLOCADate;
import is203.JWTException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jia Xian
 */
@WebServlet(name = "heatmap", urlPatterns = {"/json/heatmap"})
public class JsonHeatmap extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws SQLException if sql error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        response.setContentType("text/json;charset=UTF-8");

        PrintWriter out = response.getWriter();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonResult = new JsonObject();

        String floor = "";
        SLOCADate dateTime = null;

        //return error messages such as "invalid token", "blank date" & "missing floor" 
        ArrayList<String> errorMsgs = new ArrayList<String>();

        //Creating a Map object to check if there are all request's names (token, floor and date)
        Map<String, String[]> requestNames = request.getParameterMap();

        //Check if there token in the URL. If there is, the token will be verified. 
        if (requestNames.containsKey("token")) {
            String token = request.getParameter("token");

            if (token == null || token.trim().length() == 0) {
                errorMsgs.add("blank token");

            } else {
                try {
                    String username = JWTUtility.verify(token, "WELOVESESOMUCH");
                    if (username == null) {
                        errorMsgs.add("invalid token");
                    }
                } catch (JWTException e) {
                    //error thrown when token is modified or has expired
                    errorMsgs.add("invalid token");
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

        //Check if there floor in the URL. If there is, check if the floor is valid. 
        if (requestNames.containsKey("floor")) {
            //verify if the floor is a number between 0 (inclusive) to 5 (inclusive). 0 means basement.
            String floorNo = request.getParameter("floor");

            if (floorNo == null || floorNo.trim().length() == 0) {
                errorMsgs.add("blank floor");
            } else {
                //converting url's number into readable floor level
                floor = "";
                switch (floorNo) {
                    case "0":
                        floor = "B1";
                        break;
                    case "1":
                        floor = "L1";
                        break;
                    case "2":
                        floor = "L2";
                        break;
                    case "3":
                        floor = "L3";
                        break;
                    case "4":
                        floor = "L4";
                        break;
                    case "5":
                        floor = "L5";
                        break;
                    default:
                        errorMsgs.add("invalid floor");
                        break;
                }
            }
        } else {
            errorMsgs.add("missing floor");
        }

        if (requestNames.containsKey("date")) {
            String inputDate = request.getParameter("date");
            String convertedDate = "";
            if (inputDate == null || inputDate.trim().length() == 0) {
                errorMsgs.add("blank date");

            } else {
                inputDate = inputDate.replace('T', ' ');
                //check if the date is in the right format

                try {
                    //throws exception if the string cannot be parsed as it doesn't match to DateTimeFormatter's pattern
                    LocalDateTime LTD = LocalDateTime.parse(inputDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    dateTime = new SLOCADate(LTD);
                } catch (DateTimeParseException e) {
                    errorMsgs.add("invalid date");
                }
            }
        } else {
            errorMsgs.add("missing date");
        }

        if (errorMsgs.size() == 0) {

            jsonResult.addProperty("status", "success");
            HeatmapDAO heatmapDAO = new HeatmapDAO();
            TreeMap<String, Integer> resultMap = heatmapDAO.retrieve(dateTime, floor);
            System.out.println(resultMap.size());
            int countZero = 0;

            if (resultMap != null) {
                Iterator<String> iter = resultMap.keySet().iterator();
                int density = 0;
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

                    Heatmap heatmap = new Heatmap(semanticPlace, numPeople, density);
                    heatmapDAO.add(heatmap);
                }
                
                if(countZero == resultMap.size()){
                    jsonResult.addProperty("status", "error");
                    jsonResult.addProperty("messages", "No Data Found");
                    out.println(gson.toJson(jsonResult));
                    return;
                }
                ArrayList<Heatmap> resultList = heatmapDAO.retrieveResult();

                if (resultList != null) {
                    //Collection.sort(resultList);
                    JsonArray heatmapArray = new JsonArray();
                    for (Heatmap heatmap : resultList) {
                        JsonObject hm = new JsonObject();
                        String semPlace = heatmap.getSemanticPlace();
                        hm.addProperty("semantic-place", semPlace);
                        hm.addProperty("num-people", heatmap.getNumOfPeople());
                        hm.addProperty("crowd-density", heatmap.getDensity());
                        heatmapArray.add(hm);
                        //System.out.println(hm);
                    }

                    jsonResult.add("heatmap", heatmapArray);
                    out.println(gson.toJson(jsonResult));
                } else {
                    jsonResult.addProperty("status", "error");
                    //Collections.sort(errorMsgs);
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
                //Collections.sort(errorMsgs);
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
            //Collections.sort(errorMsgs);
            JsonArray jsonErrorArray = new JsonArray();
            for (String errorMsg : errorMsgs) {
                JsonPrimitive msg = new JsonPrimitive(errorMsg);
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
        } catch (SQLException ex) {
            Logger.getLogger(JsonHeatmap.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (SQLException ex) {
            Logger.getLogger(JsonHeatmap.class.getName()).log(Level.SEVERE, null, ex);
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
