/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import connection.ConnectionManager;
import entity.SLOCADate; //remove
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; //remove
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Keith
 */
public class AGDDAO {


    /**
     * Retrieves all the users in the time query alone with your location id and timestamps
     * @param end - the end time of query
     * @return hashmap of user as key and arraylist of arraylist of location -id and timestamp
     */
    public HashMap<String, ArrayList<ArrayList<String>>> getEveryoneInTime(SLOCADate end){
        HashMap<String,ArrayList<ArrayList<String>>> toReturn = new HashMap<>();
        SLOCADate start = end.retrieveMinutesBefore(15);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try{
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("select mac_address, location_Id, dateTime"
                    + " from location_data"
                    + " where dateTime >= '"+start+"' and dateTime < '"+end+"'"
                    + " order by dateTime");
            
            rs = pstmt.executeQuery();
            
            while(rs.next()){
                String mac = rs.getString(1);
                String locId = rs.getString(2);
                String dt = rs.getString(3);
                String dateTime = dt.substring(0, dt.indexOf('.'));
                ArrayList<String> temp = new ArrayList<>();
                temp.add(locId);
                temp.add(dateTime);
                if(toReturn.containsKey(mac)){
                    ArrayList<ArrayList<String>> value = toReturn.get(mac);
                    value.add(temp);
                    toReturn.put(mac,value);
                }else{
                    ArrayList<ArrayList<String>> value = new ArrayList<>();
                    value.add(temp);
                    toReturn.put(mac,value);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AGDDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }
        return toReturn;
    }
}

