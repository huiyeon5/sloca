/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import connection.ConnectionManager;
import entity.SLOCADate;
import entity.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pei Shan
 */
public class BLRDAO {

    private TreeMap<String, Integer> firstOrderResult;
    private TreeMap<String, TreeMap<String, Integer>> secondOrderResult;
    private TreeMap<String, TreeMap<String, Integer>> thirdOrderResult;

    /**
     * Retrieves the TreeMap for selected first choice from BLRDAO
     *
     * @return TreeMap firstOrderResult
     */
    public TreeMap<String, Integer> getFirstOrderResult() {
        return firstOrderResult;
    }

    /**
     * Retrieves the TreeMap for second choice user had selected from BLRDAO
     *
     * @return TreeMap secondOrder
     */
    public TreeMap<String, TreeMap<String, Integer>> getSecondOrderResult() {
        return secondOrderResult;
    }

    /**
     * Retrieves the TreeMap for third choice user had selected from BLRDAO
     *
     * @return TreeMap thirdOrderResult
     */
    public TreeMap<String, TreeMap<String, Integer>> getThirdOrderResult() {
        return thirdOrderResult;
    }

    /**
     * Retrieves all the stored BLRDAO object
     *
     * @param date Date within the time query window
     * @param time Time within the time query window
     * @return HashMap of String and Integer
     */
    public HashMap<String, Integer> retrieveAll(String date, String time) {
        String dateTime = date + " " + time;
        SLOCADate endDate = null;
        try {
            endDate = new SLOCADate(dateTime);
        } catch (DateTimeParseException e) {
            return null;
        }
        SLOCADate startDate = endDate.retrieveMinutesBefore(15);
        HashMap<String, Integer> toReturn = new HashMap<>();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("select temp.semantic_place, count(temp.mac_address)"
                    + " from (select mac_address, semantic_place, d.dateTime"
                    + " from location_data d, location l"
                    + " where d.dateTime >= '" + startDate + "' and d.dateTime < '" + endDate + "'"
                    + " and d.location_Id = l.location_Id) as temp"
                    + " INNER JOIN"
                    + " (select mac_address, MAX(d.dateTime) as maxDate"
                    + " from location_data d, location l"
                    + " where d.dateTime >= '" + startDate + "' and d.dateTime < '" + endDate + "'"
                    + " and d.location_Id = l.location_Id"
                    + " group by mac_address) as md"
                    + " where temp.mac_address = md.mac_address"
                    + " and temp.dateTime = md.maxDate"
                    + " group by temp.semantic_place");

            rs = pstmt.executeQuery();

            while (rs.next()) {
                toReturn.put(rs.getString(1), rs.getInt(2));
            }
        } catch (SQLException ex) {
            Logger.getLogger(BLRDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }

        return toReturn;
    }

    /**
     * Retrieves macAddresses by inputing semanticPlace and time
     *
     * @param semanticPlace To retrieve by semantic place specified
     * @param end The end time of the time query
     * @return ArrayList of macAddresses that are are the requested
     * semanticPlace and time
     */
    public ArrayList<String> retrieveMacBySemAndTime(String semanticPlace, SLOCADate end) {
        ArrayList<String> macAddresses = new ArrayList<>();
        SLOCADate start = end.retrieveMinutesBefore(15);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("SELECT d.mac_address "
                    + "FROM location_data d , location l , "
                    + "(SELECT mac_address, MAX(dateTime) as latestTime "
                    + "FROM location_data d , location l "
                    + "WHERE d.location_Id = l.location_Id "
                    + "AND dateTime >= \""+start+"\"  "
                    + "AND dateTime < \""+end+"\" "
                    + "GROUP BY mac_address ) temp "
                    + "WHERE l.location_Id = d.location_Id "
                    + "AND d.mac_address = temp.mac_address "
                    + "AND dateTime = latesttime "
                    + "AND semantic_place = '"+semanticPlace+"'");
            System.out.println(pstmt);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                macAddresses.add(rs.getString(1));
            }

        } catch (SQLException ex) {
            Logger.getLogger(BLRDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }

        return macAddresses;
    }  
    

    /**
     * Retrieves next locations from BLRDAO object
     *
     * @param macAddresses Check if mac addresses specified appears in the next location
     * @param start The start of the time query of the next location
     * @return HashMap containing mac address and arraylist of value
     */
    public HashMap<String, ArrayList<ArrayList<String>>> retrieveNextLocations(ArrayList<String> macAddresses, SLOCADate start) {
        HashMap<String, ArrayList<ArrayList<String>>> toReturn = new HashMap<>();
        SLOCADate end = start.retrieveMinutesAfter(15);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList<String> temp = new ArrayList<>();

        String inClauseSql = "(";
        for (int i = 0; i < macAddresses.size() - 1; i++) {
            inClauseSql += "\"" + macAddresses.get(i) + "\",";
        }
        inClauseSql += "\"" + macAddresses.get(macAddresses.size() - 1) + "\")";

        //System.out.println(inClauseSql);
        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("SELECT d.dateTime,mac_address,semantic_place"
                    + " FROM location l, location_data d"
                    + " WHERE l.location_Id = d.location_Id"
                    + " AND d.dateTime >= '" + start
                    + "' AND d.dateTime < '" + end + "' "
                    + " AND d.mac_address IN " + inClauseSql
                    + " ORDER BY d.dateTime");
            System.out.println(pstmt);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String dateTime = rs.getString(1);
                String mac = rs.getString(2);
                String semanticPlace = rs.getString(3);
                temp.add(semanticPlace);
                temp.add(dateTime);
                
                if (toReturn.containsKey(mac)) {
                    ArrayList<ArrayList<String>> value = toReturn.get(mac);
                    value.add(temp);
                    toReturn.put(mac, value);
                } else {
                    ArrayList<ArrayList<String>> value = new ArrayList<>();
                    value.add(temp);
                    toReturn.put(mac, value);
                }
                temp = new ArrayList<>();
            }
        } catch (SQLException ex) {
            Logger.getLogger(BLRDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }

        return toReturn;
    }

    /**
     * Retrieves SLOCA users from inputing date time from BLRDAO object
     *
     * @param date Date within the time query window
     * @param time Time within the time query window
     * @return ArrayList of users
     * @throws DateTimeParseException Throws DateTimeParseException when date and time are not in the correct format
     */
    public ArrayList<User> retrieveSLOCAUsersFromDateTime(String date, String time) throws DateTimeParseException {
        ArrayList<User> result = new ArrayList<User>();
        SLOCADate end = new SLOCADate(date, time);
        SLOCADate start = end.retrieveMinutesBefore(15);
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("SELECT DISTINCT u.mac_address, name, password, email, gender "
                    + "FROM location_data ld, user u "
                    + "WHERE ld.dateTime >= ? and ld.dateTime < ? "
                    + "and ld.mac_address = u.mac_address "
                    + "ORDER BY name ASC");

            pstmt.setString(1, start.toString());
            pstmt.setString(2, end.toString());

            rs = pstmt.executeQuery();

            while (rs.next()) {
                String temp = rs.getString(5);
                char gender = temp.charAt(0);
                User user = new User(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), gender);
                result.add(user);
            }
        } catch (SQLException ex) {
            Logger.getLogger(BLRDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }
        return result;
    }

    /**
     * Generate data when user selects only the 1 choice from BLRDAO object
     *
     * @param firstOrder First order to be sorted by
     * @param userList - list of the users
     * @param calledFromServlet - checks if the method is called from servlet
     */
    public void processOneOrderResult(String firstOrder, ArrayList<User> userList, boolean calledFromServlet) {

        createTempUserDatabase(userList);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        ArrayList<String> tallyList = new ArrayList<>();

        try {
            conn = ConnectionManager.getConnection();

            //if user selects school as first choice
            switch (firstOrder) {
                case "school":
                    tallyList = listOfSchools(userList);
                    firstOrderResult = new TreeMap<>();
                    break;
                case "year":
                    tallyList = listOfYears(userList);
                    firstOrderResult = new TreeMap<>();
                    break;
                default:
                    tallyList = listOfGenders();
                    firstOrderResult = new TreeMap<>(Collections.reverseOrder());
                    break;
            }

            pstmt = conn.prepareStatement("SELECT " + firstOrder + " , count(mac_address)"
                    + "FROM tempusers "
                    + "GROUP BY " + firstOrder);

            rs = pstmt.executeQuery();

            TreeMap<String, Integer> temp = null;

            if (firstOrder.equals("gender")) {
                temp = new TreeMap<>(Collections.reverseOrder());
            } else {
                temp = new TreeMap<>();
            }

            ArrayList<String> missingUnits = new ArrayList<String>();
            missingUnits.addAll(tallyList);

            while (rs.next()) {
                String firstCol = rs.getString(1);
                if (missingUnits.contains(firstCol)) {
                    missingUnits.remove(firstCol);
                }
                int count = rs.getInt(2);
                temp.put(firstCol, count);
            }

            for (String missingUnit : missingUnits) {
                temp.put(missingUnit, 0);
            }

            firstOrderResult.putAll(temp);

            if (calledFromServlet) {
                Statement stmt = conn.createStatement();
                stmt.execute("DROP TABLE tempusers;");
            }

        } catch (SQLException ex) {
            System.out.println("Errors: " + ex.getMessage());
        } finally {

            ConnectionManager.close(conn, pstmt, rs);
        }
    }

    /**
     * Generate data when user selects 2 choices from BLRDAO object
     *
     * @param firstOrder - the first selection
     * @param secondOrder - second selection
     * @param userList - the list of users
     * @param calledFromServlet - checks if method is called from servlet
     */
    public void processTwoOrderResult(String firstOrder, String secondOrder, ArrayList<User> userList, boolean calledFromServlet) {

        processOneOrderResult(firstOrder, userList, false);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList<String> firstOrderList = new ArrayList<>();
        ArrayList<String> secondOrderList = new ArrayList<>();

        try {
            conn = ConnectionManager.getConnection();

            switch (firstOrder) {
                case "school":
                    firstOrderList = listOfSchools(userList);
                    break;
                case "year":
                    firstOrderList = listOfYears(userList);
                    break;
                default:
                    firstOrderList = listOfGenders();
                    break;
            }
            switch (secondOrder) {
                case "school":
                    secondOrderList = listOfSchools(userList);
                    break;
                case "year":
                    secondOrderList = listOfYears(userList);
                    break;
                default:
                    secondOrderList = listOfGenders();
                    break;
            }

            if (secondOrder.equals("gender")) {
                secondOrderResult = new TreeMap(Collections.reverseOrder());
            } else {
                secondOrderResult = new TreeMap<>();
            }

            for (String firstOrderUnit : firstOrderList) {

                pstmt = conn.prepareStatement("SELECT " + secondOrder + ", count(mac_address) \n"
                        + "FROM tempusers "
                        + "WHERE " + firstOrder + " = '" + firstOrderUnit + "' "
                        + "GROUP BY " + secondOrder);

                rs = pstmt.executeQuery();

                TreeMap<String, Integer> temp = null;

                if (secondOrder.equals("gender")) {
                    temp = new TreeMap<>(Collections.reverseOrder());
                } else {
                    temp = new TreeMap<>();
                }

                ArrayList<String> missingUnits = new ArrayList<String>();
                missingUnits.addAll(secondOrderList);

                while (rs.next()) {
                    String firstCol = rs.getString(1);
                    if (missingUnits.contains(firstCol)) {
                        missingUnits.remove(firstCol);
                    }
                    int count = rs.getInt(2);
                    temp.put(firstCol, count);
                }
                for (String missingUnit : missingUnits) {
                    temp.put(missingUnit, 0);
                }
                System.out.println("SECOND RESULT'S KEY: " + firstOrderUnit);

                secondOrderResult.put(firstOrderUnit, temp);
            }

            if (calledFromServlet) {
                Statement stmt = conn.createStatement();
                stmt.execute("DROP TABLE tempusers;");
            }

        } catch (SQLException ex) {
            System.out.println("Errors: " + ex.getMessage());
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }
    }

    /**
     * Generate data when user selects only the 3 choice from BLRDAO object
     *
     * @param firstOrder - the first selection
     * @param secondOrder - the second selection
     * @param thirdOrder - the third selection
     * @param userList - the list of users
     */
    public void processThreeOrderResult(String firstOrder, String secondOrder, String thirdOrder, ArrayList<User> userList) {

        processTwoOrderResult(firstOrder, secondOrder, userList, false);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList<String> firstOrderList = new ArrayList<>();
        ArrayList<String> secondOrderList = new ArrayList<>();
        ArrayList<String> thirdOrderList = new ArrayList<>();

        try {
            conn = ConnectionManager.getConnection();

            //if user selects school as second choice
            switch (firstOrder) {
                case "school":
                    firstOrderList = listOfSchools(userList);
                    break;
                case "year":
                    firstOrderList = listOfYears(userList);
                    break;
                default:
                    firstOrderList = listOfGenders();
                    break;
            }

            switch (secondOrder) {
                case "school":
                    secondOrderList = listOfSchools(userList);
                    break;
                case "year":
                    secondOrderList = listOfYears(userList);
                    break;
                default:
                    secondOrderList = listOfGenders();
                    break;
            }

            switch (thirdOrder) {
                case "school":
                    thirdOrderList = listOfSchools(userList);
                    break;
                case "year":
                    thirdOrderList = listOfYears(userList);
                    break;
                default:
                    thirdOrderList = listOfGenders();
                    break;
            }
            thirdOrderResult = new TreeMap<>();

            for (String firstOrderUnit : firstOrderList) {
                for (String secondOrderUnit : secondOrderList) {

                    pstmt = conn.prepareStatement("SELECT " + thirdOrder + ", count(mac_address) \n"
                            + "FROM tempusers "
                            + "WHERE " + firstOrder + " = '" + firstOrderUnit + "' "
                            + " AND " + secondOrder + " = '" + secondOrderUnit + "' "
                            + "GROUP BY " + thirdOrder);

                    rs = pstmt.executeQuery();

                    String key = firstOrderUnit + " " + secondOrderUnit;

                    TreeMap<String, Integer> temp = null;

                    if (thirdOrder.equals("gender")) {
                        temp = new TreeMap<>(Collections.reverseOrder());
                    } else {
                        temp = new TreeMap<>();
                    }

                    ArrayList<String> missingUnits = new ArrayList<String>();
                    missingUnits.addAll(thirdOrderList);

                    while (rs.next()) {
                        String firstCol = rs.getString(1);
                        if (missingUnits.contains(firstCol)) {
                            missingUnits.remove(firstCol);
                        }
                        int count = rs.getInt(2);
                        temp.put(firstCol, count);
                    }

                    for (String missingUnit : missingUnits) {
                        temp.put(missingUnit, 0);
                    }
                    thirdOrderResult.put(key, temp);
                }
            }

            Statement stmt = conn.createStatement();
            stmt.execute("DROP TABLE tempusers;");

        } catch (SQLException ex) {
            System.out.println("Errors: " + ex.getMessage());
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }
    }

    private void createTempUserDatabase(ArrayList<User> userList) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionManager.getConnection();

            Statement stmt = conn.createStatement();

            stmt.execute("CREATE TABLE tempusers "
                    + "(mac_address char(40) PRIMARY KEY, school varchar(12), year char(4), gender char(1));");

            for (User user : userList) {
                String macAdd = user.getMacAddress();
                String school = user.getSchool();
                int year = user.getYear();
                String strYear = "";
                strYear += year;
                char gender = user.getGender();
                String strGender = "";
                strGender += gender;

                Statement stmt2 = conn.createStatement();

                stmt2.execute("INSERT INTO tempusers (mac_address, school, year, gender) "
                        + "VALUES ('" + macAdd + "', '" + school + "', '" + strYear + "', '" + strGender + "');");

            }

        } catch (SQLException ex) {
            System.out.println("Errors: " + ex.getMessage());
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }
    }

    private ArrayList<String> listOfSchools(ArrayList<User> userList) {
        ArrayList<String> result = new ArrayList<String>();

        result.add("accountancy");
        result.add("business");
        result.add("economics");
        result.add("law");
        result.add("sis");
        result.add("socsc");

        Collections.sort(result);

        return result;
    }

    private ArrayList<String> listOfYears(ArrayList<User> userList) {
        ArrayList<String> result = new ArrayList<String>();

        result.add("2013");
        result.add("2014");
        result.add("2015");
        result.add("2016");
        result.add("2017");
        Collections.sort(result);

        return result;
    }

    private ArrayList<String> listOfGenders() {
        ArrayList<String> result = new ArrayList<String>();
        result.add("M");
        result.add("F");
        return result;
    }
}