/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import DAO.UserDAO;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Wei Ming
 */
@WebServlet(urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    final private String adminPassword = "admin123";

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
        HttpSession session = request.getSession();
        try (PrintWriter out = response.getWriter()) {

            //Retrieving the data from UserDAO
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            UserDAO userDAO = new UserDAO();
            User user = null;

            //Check for User's validity and direct to "Index.jsp"
            if (username != null && password != null) {

                //checks if its admin
                if (username.equals("admin") && password.equals(adminPassword)) {
                    session.setAttribute("username", "admin");
                    response.sendRedirect("indexAdmin.jsp"); //send to Admin page
                    return;
                }

                if (username.indexOf('@') != -1) {
                    request.setAttribute("error", "Invalid Username/Password.");
                    RequestDispatcher dispatch = request.getRequestDispatcher("login.jsp");
                    dispatch.forward(request, response);
                }
                //Retrieves User from UserDAO
                user = userDAO.retrieve(username);
                if (user != null) {
                    //Retrieves user's password
                    String userPassword = user.getPassword();

                    //Checks validity of the user with the password and redirects to the corresponding page.
                    if (userPassword.equals(password)) {
                        session.setAttribute("username", username);
                        response.sendRedirect("index.jsp"); //send to student page
                        
                    } else {
                        request.setAttribute("error", "Invalid Username/Password.");
                        RequestDispatcher dispatch = request.getRequestDispatcher("login.jsp");
                        dispatch.forward(request, response); //returns back with error message
                    }

                } else {
                    request.setAttribute("error", "Invalid Username/Password.");
                    RequestDispatcher dispatch = request.getRequestDispatcher("login.jsp");
                    dispatch.forward(request, response);
                }
            } else {
                request.setAttribute("error", "Invalid Username/Password.");
                RequestDispatcher dispatch = request.getRequestDispatcher("login.jsp");
                dispatch.forward(request, response); //returns back with error message
            }
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
