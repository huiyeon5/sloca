package json;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import entity.User;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import DAO.UserDAO;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import java.sql.SQLException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ong
 */
@WebServlet(name = "authenticate", urlPatterns = {"/json/authenticate"})
public class JsonAuthenticate extends HttpServlet {

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

        String errorMsg = "";
        String adminPassword = "admin123";
        String token = "";
        UserDAO userDAO = new UserDAO();

        User user = null;

        try {
            JsonObject json = new JsonObject();

            String username = request.getParameter("username");
            String password = request.getParameter("password");
            ArrayList<String> errorMsgs = new ArrayList<String>();

            if (username == null) {
                errorMsgs.add("missing username");
            } else if (username.isEmpty()) {
                errorMsgs.add("blank username");
            }

            if (password == null) {
                errorMsgs.add("missing password");
            } else if (password.isEmpty()) {
                errorMsgs.add("blank password");
            }

            if (errorMsgs.size() > 0) {
                json.addProperty("status", "error");
                Collections.sort(errorMsgs);

                JsonArray errorArray = new JsonArray();
                for (String error : errorMsgs) {
                    JsonPrimitive jsonError = new JsonPrimitive(error);
                    errorArray.add(jsonError);
                }
                json.add("messages", errorArray);
                out.println(gson.toJson(json));
                return;
            }
            
            if (username.equals("admin") && password.equals(adminPassword)) {
                token = JWTUtility.sign("WELOVESESOMUCH", username);
                json.addProperty("status", "success");
                json.addProperty("token", token);
                out.println(gson.toJson(json));
                return;
            }
            user = userDAO.retrieve(username);

            if (user != null) {
                String userPassword = user.getPassword();

                if (userPassword.equals(password)) {
                    token = JWTUtility.sign("WELOVESESOMUCH", username);
                    json.addProperty("status", "success");
                    json.addProperty("token", token);
                } else {
                    errorMsgs.add("invalid username/password");
                }
            } else {
                errorMsgs.add("invalid username/password");
            }

            if (errorMsgs.size() > 0) {
                json.addProperty("status", "error");
                Collections.sort(errorMsgs);

                JsonArray errorArray = new JsonArray();
                for (String error : errorMsgs) {
                    JsonPrimitive jsonError = new JsonPrimitive(error);
                    errorArray.add(jsonError);
                }
                json.add("messages", errorArray);
            }
            out.println(gson.toJson(json));
        } finally {
            out.close();
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
            Logger.getLogger(JsonAuthenticate.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(JsonAuthenticate.class.getName()).log(Level.SEVERE, null, ex);
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
