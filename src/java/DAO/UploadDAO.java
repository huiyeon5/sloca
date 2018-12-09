/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import connection.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Huiyeon Kim
 */
public class UploadDAO {
    
    /**
     * method uploads the file on to the database
     * @param filePath - path to the file
     */
    public void uploadDemo(String filePath){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try{
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("LOAD DATA LOCAL INFILE '" +filePath+"' "
                    + " REPLACE INTO TABLE user"
                    + " FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\r\\n' "
                    + "IGNORE 1 LINES;");
                    
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UploadDAO.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            ConnectionManager.close(conn,pstmt,rs);
        }
    }

   /**
     * method uploads the file on to the database
     * @param filePath - path to the file
     */
    public void uploadLocation(String filePath){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try{
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("LOAD DATA LOCAL INFILE '" +filePath+"' "
                    + " REPLACE INTO TABLE location"
                    + " FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\r\\n' "
                    + "IGNORE 1 LINES");
                    
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UploadDAO.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            ConnectionManager.close(conn,pstmt,rs);
        }
    }

    /**
     * method uploads the file on to the database
     * @param filePath - path to the file
     */
    public void uploadLocationData(String filePath){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try{
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("LOAD DATA LOCAL INFILE '" +filePath+"' "
                    + " REPLACE INTO TABLE location_data"
                    + " FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\r\\n' "
                    + "IGNORE 1 LINES");
                    
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UploadDAO.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            ConnectionManager.close(conn,pstmt,rs);
        }
    }
    
    /**
     * method call truncates user table
     */
    public void deleteUsers(){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("truncate user");
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UploadDAO.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            ConnectionManager.close(conn,pstmt,rs);
        }
    }
    
    /**
     * method call truncates location table
     */
    public void deleteLocation(){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("truncate location");
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UploadDAO.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            ConnectionManager.close(conn,pstmt,rs);
        }
    }
    
    /**
     * method call truncates location data table
     */
    public void deleteLocationData(){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("truncate location_data");
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UploadDAO.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            ConnectionManager.close(conn,pstmt,rs);
        }
    }
}
