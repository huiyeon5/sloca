/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package json;

import DAO.UploadDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import entity.SLOCADate;
import gnu.trove.map.hash.THashMap;
import is203.JWTException;
import is203.JWTUtility;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import utility.UnzipUtility;
import utility.ValidateUtility;

/**
 *
 * @author Huiyeon Kim
 */
@MultipartConfig
@WebServlet(name = "jsonBootstrap", urlPatterns = {"/json/bootstrap"})
public class jsonBootstrap extends HttpServlet {

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
        response.setContentType("text/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonResult = new JsonObject();

            SLOCADate dateTime = null;
            System.out.println("where do i stop1");
            //return error messages such as "invalid token", "blank date" & "missing floor" 
            ArrayList<String> errorMsgs = new ArrayList<String>();

            //Creating a Map object to check if there are all request's names (token, k(optional), date and origin)
            Map<String, String[]> requestNames = request.getParameterMap();
            
            System.out.println(requestNames);
            
            if (requestNames.containsKey("token")) {
                System.out.println("where do i stop2");
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
            
            if (!errorMsgs.isEmpty()) {
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
            
            System.out.println("where do i stop3");
            Part part = request.getPart("bootstrap-file");
            if (part!=null) {
                System.out.println("where do i stop4");
                ServletContext servletContext = this.getServletConfig().getServletContext();
                // gets absolute path of the web application
                //File appPath = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
                String savePath = servletContext.getRealPath(SAVE_DIR);
                // constructs path of the directory to save uploaded file

                // creates the save directory if it does not exists
                File fileSaveDir = new File(savePath);
                if (fileSaveDir.exists()) {
                } else {
                    fileSaveDir.mkdirs();
                }

                String fileName = "";
                
                fileName = extractFileName(part);
                // refines the fileName in case it is an absolute path
                fileName = new File(fileName).getName();
                try {
                    part.write(savePath + File.separator + fileName);
                } catch (FileNotFoundException e) {
                }

                String zipFilePath = savePath + File.separator + fileName;
                String destPath = savePath;

                UnzipUtility unzipper = new UnzipUtility();

                ArrayList<String> files = unzipper.unzip(zipFilePath, destPath);
                UploadDAO uDAO = new UploadDAO();
                ValidateUtility validate = new ValidateUtility();

                if (files.size() == 3) {
                    uDAO.deleteLocation();
                    uDAO.deleteLocationData();
                    uDAO.deleteUsers();
                    validate.clearList();
                }else{
                    jsonResult.addProperty("status","error");
                    jsonResult.addProperty("error","When bootstrapping, please add all the 3 files!");
                    out.println(gson.toJson(jsonResult));
                    return;
                }

                THashMap<Integer, ArrayList<String>> demoError = null;
                THashMap<Integer, ArrayList<String>> locError = null;
                THashMap<Integer, ArrayList<String>> dataError = null;

                for (String filePath : files) {
                    if (filePath.contains("lookup")) {
                        locError = validate.validateLocation(filePath);
                        String path = savePath + File.separator + "tempLoc.csv";
                        String finalPath = getSQLPath(path);
                        uDAO.uploadLocation(finalPath);
                        validate.updateLocationLookupList();
                    }
                    if (filePath.contains("location.csv")) {
                        dataError = validate.validateLocationData(filePath);
                        String path = savePath + File.separator + "tempData.csv";
                        String finalPath = getSQLPath(path);
                        uDAO.uploadLocationData(finalPath);
                    }
                    if (filePath.contains("demo")) {
                        demoError = validate.validateUser(filePath);
                        String path = savePath + File.separator + "tempUser.csv";
                        String finalPath = getSQLPath(path);
                        uDAO.uploadDemo(finalPath);
                    }
                }

                int dSuccess = validate.getSuccessForDemo();
                int locSuccess = validate.getSuccessForLocationLookup();
                int locDataSuccess = validate.getSuccessForLocation();
                validate.setSuccessForDemoToZero();
                validate.setSuccessForLocLookToZero();
                validate.setSuccessForLocationToZero();
                
                if (demoError.isEmpty() && dataError.isEmpty() && locError.isEmpty()) {
                    jsonResult.addProperty("status", "success");
                    JsonArray array = new JsonArray();
                    JsonObject successResult1 = new JsonObject();
                    JsonObject successResult2 = new JsonObject();
                    JsonObject successResult3 = new JsonObject();
                    successResult1.addProperty("demographics.csv", dSuccess);
                    successResult2.addProperty("location-lookup.csv", locSuccess);
                    successResult3.addProperty("location.csv", locDataSuccess);
                    array.add(successResult1);
                    array.add(successResult2);
                    array.add(successResult3);
                    jsonResult.add("num-record-loaded",array);
                    out.println(gson.toJson(jsonResult));
                }else{
                    jsonResult.addProperty("status", "error");
                    System.out.println("IM HERE");
                    JsonArray array = new JsonArray();
                    JsonObject successResult1 = new JsonObject();
                    JsonObject successResult2 = new JsonObject();
                    JsonObject successResult3 = new JsonObject();
                    successResult1.addProperty("demographics.csv", dSuccess);
                    successResult2.addProperty("location-lookup.csv", locSuccess);
                    successResult3.addProperty("location.csv", locDataSuccess);
                    System.out.println(dSuccess+" "+locSuccess+" "+locDataSuccess);
                    array.add(successResult1);
                    array.add(successResult2);
                    array.add(successResult3);
                    jsonResult.add("num-record-loaded",array);
                    
                    JsonArray error = new JsonArray();
                    List<Integer> list1 = new ArrayList<>(demoError.keySet());
                    List<Integer> list2 = new ArrayList<>(dataError.keySet());
                    List<Integer> list3 = new ArrayList<>(locError.keySet());
                    
                    Collections.sort(list1);
                    Collections.sort(list2);
                    Collections.sort(list3);
                   
                    for(int line: list1){
                        JsonObject obj = new JsonObject();
                        obj.addProperty("file", "demographics.csv");
                        obj.addProperty("line", line);
                        ArrayList<String> errors = demoError.get(line);
                        System.out.println("here1");
                        JsonArray errorMessage = new JsonArray();
                        System.out.println(errors.size());
                       
                        for(int i = 0; i < errors.size();i++){
                            JsonPrimitive s = new JsonPrimitive(errors.get(i));
                            errorMessage.add(s);
                            System.out.println("here5");
                        }
                        
                        System.out.println("here4");
                        obj.add("message", errorMessage);
                        error.add(obj);  
                        System.out.println("here3");
                    }
                    
                    for(int line:list3){
                        JsonObject obj = new JsonObject();
                        obj.addProperty("file", "location-lookup.csv");
                        obj.addProperty("line", line);
                        ArrayList<String> errors = locError.get(line);
                        JsonArray errorMessage = new JsonArray();
                        for(int i = 0; i<errors.size();i++){
                            JsonPrimitive s = new JsonPrimitive(errors.get(i));
                            errorMessage.add(s);
                        }
                        obj.add("message", errorMessage);
                        error.add(obj);
                    }
                    
                    for(int line:list2){
                        JsonObject obj = new JsonObject();
                        obj.addProperty("file", "location.csv");
                        obj.addProperty("line", line);
                        ArrayList<String> errors = dataError.get(line);
                        JsonArray errorMessage = new JsonArray();
                        for(int i = 0; i<errors.size();i++){
                            JsonPrimitive s = new JsonPrimitive(errors.get(i));
                            errorMessage.add(s);
                        }
                        obj.add("message", errorMessage);
                        error.add(obj);
                    }
                    
                    jsonResult.add("error", error);
                    System.out.println("DO I EVEN COME HERE");
                    System.out.println(jsonResult);
                    out.println(gson.toJson(jsonResult));
                }
            }

        }
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
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return "";
    }
    /**
     * gets SQL readable path
     * @param path - path to file
     * @return sql readable path to file
     */
    private String getSQLPath(String path) {
        String sqlPath = "";
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '\\') {
                sqlPath += '/';
            } else {
                sqlPath += path.charAt(i);
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
