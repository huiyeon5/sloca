/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controller;

import DAO.UploadDAO;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import utility.UnzipUtility;
import utility.ValidateUtility;

/**
 *
 * @author Huiyeon Kim
 */
@WebServlet(name = "UploadServlet", urlPatterns = {"/UploadServlet"})
@MultipartConfig(fileSizeThreshold=1024*1024*2, // 2MB
                 maxFileSize=1024*1024*10,      // 10MB
                 maxRequestSize=1024*1024*50)   // 50MB

public class UploadServlet extends HttpServlet {

    private static final String SAVE_DIR = "/uploadFiles";

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
        
        ServletContext servletContext = this.getServletConfig().getServletContext();
        // gets absolute path of the web application
        //File appPath = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        String savePath = servletContext.getRealPath(SAVE_DIR);
        System.out.println(savePath);
        // constructs path of the directory to save uploaded file
        
        String funcType = request.getParameter("functionType");
        // creates the save directory if it does not exists
        File fileSaveDir = new File(savePath);
        if (fileSaveDir.exists()) {
        } else {
            fileSaveDir.mkdirs();
        }
        
        String fileName = "";
        Part part = request.getPart("bootstrapFile");
        fileName = extractFileName(part);
        
        // refines the fileName in case it is an absolute path
        fileName = new File(fileName).getName();
        try{  
            part.write(savePath + File.separator + fileName);
        }catch(FileNotFoundException e){
        }

        String zipFilePath = savePath + File.separator + fileName;
        String destPath = savePath;
        
        UnzipUtility unzipper = new UnzipUtility();
        
        ArrayList<String> files = unzipper.unzip(zipFilePath, destPath);
        UploadDAO uDAO = new UploadDAO();
        ValidateUtility validate = new ValidateUtility();
        if(funcType.equals("Bootstrap")){
            if(files.size() == 3){
                uDAO.deleteLocation();
                uDAO.deleteLocationData();
                uDAO.deleteUsers();
                validate.clearList();
            }else{
                request.setAttribute("files","Add all 3 files to BOOTSTRAP database!");
                request.getRequestDispatcher("bootstrap.jsp").forward(request,response);
                return;
            }
        }else{
            if(files.size() == 3){
                request.setAttribute("files","Adding all 3 files requires BOOTSTRAPPING of database!");
                request.getRequestDispatcher("update.jsp").forward(request,response);
                return;
            }
        }
        
        THashMap<Integer,ArrayList<String>> demoError = null; 
        THashMap<Integer,ArrayList<String>> locError = null;
        THashMap<Integer,ArrayList<String>> dataError = null;
        
        for(String filePath:files){
            validate.updateLocationLookupList();
            validate.updateUploadedLocation();
            if(filePath.contains("lookup")){
                locError = validate.validateLocation(filePath);
                String path = savePath + File.separator + "tempLoc.csv";
                String finalPath = getSQLPath(path);
                uDAO.uploadLocation(finalPath);
                validate.updateLocationLookupList();
                validate.updateUploadedLocation();
            }
            if(filePath.contains("location.csv")){
                dataError = validate.validateLocationData(filePath);
                String path = savePath + File.separator + "tempData.csv";
                String finalPath = getSQLPath(path);
                uDAO.uploadLocationData(finalPath);
            }
            if(filePath.contains("demo")){
                demoError = validate.validateUser(filePath);
                String path = savePath + File.separator + "tempUser.csv";
                String finalPath = getSQLPath(path);
                uDAO.uploadDemo(finalPath);
            }
        }
        
        HttpSession session = request.getSession();
        session.setAttribute("bootstrap", "done");
        request.setAttribute("demoError", demoError);
        request.setAttribute("locError", locError);
        request.setAttribute("locDataError",dataError);
        if(validate.getSuccessForDemo() > 0){
            request.setAttribute("demoS",validate.getSuccessForDemo());
        }
        if(validate.getSuccessForLocationLookup() > 0){
            request.setAttribute("locS",validate.getSuccessForLocationLookup());
        }
        
        if(validate.getSuccessForLocation() > 0){
            request.setAttribute("locDataS",validate.getSuccessForLocation());
        }
        if(funcType.equals("Bootstrap")){
            request.getRequestDispatcher("bootstrap.jsp").forward(request,response);
        }else{
            request.getRequestDispatcher("update.jsp").forward(request,response);
        }
        
        validate.setSuccessForDemoToZero();
        validate.setSuccessForLocLookToZero();
        validate.setSuccessForLocationToZero();
    }
    /**
     * Extracts file name from HTTP header content-disposition
     * @param part - part of file
     * @return the file name
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length()-1);
            }
        }
        return "";
    }
    /**
     * gets SQL readable path
     * @param path - path to file
     * @return sql readable path to file
     */
    private String getSQLPath(String path){
        String sqlPath = "";
        for(int i = 0; i < path.length(); i++){
            if(path.charAt(i) == '\\'){
                sqlPath+='/';
            }else{
                sqlPath+=path.charAt(i);
            }
        }
        return sqlPath;
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
