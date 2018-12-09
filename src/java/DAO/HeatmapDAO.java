/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import connection.ConnectionManager;
import entity.Heatmap;
import entity.SLOCADate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Huiyeon Kim
 */
public class HeatmapDAO {

    ArrayList<Heatmap> heatmaps = new ArrayList<>();

    /**
     * This method retrieves the number of people
     * in each semantic place on the required floor
     * in the query window
     * @param date the input from users in correct format
     * @param floor the input floor from users
     * @return the number of people in each semantic place
     * in a TreeMap in which the key is the name of semantic place
     * and the value is the number of people in the place
     */
    public TreeMap<String, Integer> retrieve(SLOCADate date, String floor) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        TreeMap<String, Integer> result = getSelectedFloorList(floor);
        
        SLOCADate fifteenMinBeforeDate = date.retrieveMinutesBefore(15);
        String start = fifteenMinBeforeDate.getDateTime().toString();
        String end = date.getDateTime().toString();
        
        

        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("select temp.semantic_place,count(temp.mac_address) from "
                    + "(select dateTime, mac_address, d.location_Id, semantic_place "
                    + "from location_data d, location l "
                    + "where dateTime >= '" + start + "' and dateTime < '" + end + "' and l.location_Id = d.location_id "
                    + "and l.semantic_place like 'SMUSIS" + floor + "%') as temp"
                    + " inner join "
                    + "(select max(dateTime) as maxDate, mac_address, d2.location_Id, semantic_place "
                    + "from location_data d2, location l2 "
                    + "where dateTime >= '" + start + "' and dateTime < '" + end + "' and l2.location_Id = d2.location_id "
                    + "and l2.semantic_place like 'SMUSIS" + floor + "%' "
                    + "group by mac_address) as md on temp.dateTime = md.maxDate and temp.mac_address = md.mac_address "
                    + "and temp.location_Id = md.location_Id and temp.semantic_place = md.semantic_place "
                    + "group by temp.semantic_place");

            rs = pstmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getInt(2));
            }
            //create list of floor 
        } catch (SQLException ex) {
            Logger.getLogger(HeatmapDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * This method adds a new heatmap object to the heatmap list
     * @param heatmap the new heatmap to be added
     */
    public void add(Heatmap heatmap) {
        heatmaps.add(heatmap);
    }

    /**
     * This method retrieves the heatmap list
     * @return the existing heatmap list in arraylist
     */
    public ArrayList<Heatmap> retrieveResult() {
        return heatmaps;
    }

    /**
     *  This method deletes all the heatmaps in the heatmap list
     */
    public void deleteList() {
        heatmaps.clear();
    }

    /**
     * This method retrieves the semantic places
     * with the number of people inside 
     * on the required floor
     * @param floor the specified floor
     * @return the number of people in 
     * each semantic place on the required floor
     * in a TreeMap in which the key is the name of semantic place
     * and the value is the number of people in the place
     */
    public TreeMap<String, Integer> getSelectedFloorList(String floor) {
        String floorName = "SMUSIS" + floor;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        TreeMap<String, Integer> result = new TreeMap<>();
        try {
            conn = ConnectionManager.getConnection();
           
            pstmt = conn.prepareStatement("select distinct semantic_place " 
                    + "from location "
                    + "where semantic_place like '" + floorName + "%'"); 

            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                result.put(rs.getString(1), 0);
            }

        } catch (SQLException ex) {
            Logger.getLogger(HeatmapDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }
        
        return result;
    }
}
