/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import DAO.UploadDAO;
import connection.ConnectionManager;
import entity.SLOCADate;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author Huiyeon Kim
 */
public class ValidateUtility {

    static int totalNumOfLinesForDemo = 0;
    static int totalNumOfLinesForLoc = 0;
    static int totalNumOfLinesForLocLookUp = 0;
    static int successForDemo = 0;
    static int successForLocation = 0;
    static int successForLocationLookup = 0;
    static boolean listvalue = true;

    static ArrayList<String> locationLookUpList = new ArrayList<>();
    static THashSet<String> uploadedLocation = new THashSet<String>();
    static Set<String> macSet = new HashSet<>();
    static Set<String> semSet = new HashSet<>();

    private static final HashMap<Integer, String> ERRORMSGSFORDEMO = new HashMap<Integer, String>() {
        {
            put(1, "invalid mac address");
            put(2, "name is blank");
            put(3, "invalid password");
            put(4, "invalid email");
            put(5, "invalid gender");
        }
    };

    private static final HashMap<Integer, String> ERRORMSGSFORLOC = new HashMap<Integer, String>() {
        {
            put(1, "invalid timestamp");
            put(2, "invalid mac address");
            put(3, "invalid location");
        }
    };

    private static final HashMap<Integer, String> ERRORMSGSFORLOCLOOK = new HashMap<Integer, String>() {
        {
            put(1, "invalid location id");
            put(2, "invalid semantic place");
        }
    };
    
    public void updateUploadedLocation(){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String dup = "";

        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("select dateTime,mac_address from location_data");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                dup = rs.getString(1)+rs.getString(2);
                uploadedLocation.add(dup);
            }

        } catch (SQLException ex) {
            Logger.getLogger(UploadDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }
    }

    /**
     * Method call clears all the list which gets updated while validating
     */
    public void clearList() {
        locationLookUpList.clear();
        uploadedLocation.clear();
    }

    /**
     * Method call returns the Set of Semantic Places
     * @return semSet - set of semantic places
     */
    public Set<String> returnSemSet() {
        return semSet;
    }

    /**
     *Method call updates the Set of Semantic Places by amking connections to MySQL.
     */
    public void updateSemSet() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sem = "";

        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("select distinct(semantic_place) from location order by semantic_place");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                sem = "" + rs.getString(1);
                semSet.add(sem.trim());
            }
        } catch (SQLException ex) {
            Logger.getLogger(UploadDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }
    }

    /**
     * Check if the data base contains the semantic place
     * @param sem - semantic place
     * @return boolean - if semantic place exists
     */
    public boolean containsSem(String sem) {
        return semSet.contains(sem);
    }

    /**
     * Method call updates set of mac addresses
     */
    public void updateMacSet() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String mac = "";

        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("select DISTINCT(mac_address) from location_data");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                mac = "" + rs.getString(1);
                macSet.add(mac.trim());
            }
        } catch (SQLException ex) {
            Logger.getLogger(UploadDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }
    }

    /**
     * method call checks if set of mac addresses contains the mac
     * @param mac - mac address
     * @return boolean - if mac address exists
     */
    public boolean containsMac(String mac) {
        return macSet.contains(mac);
    }

    /**
     * Method call updates LocationLookUpList
     */
    public void updateLocationLookupList() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String locID = "";

        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement("select * from location");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                locID = "" + rs.getInt(1);
                locationLookUpList.add(locID);
            }

        } catch (SQLException ex) {
            Logger.getLogger(UploadDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ConnectionManager.close(conn, pstmt, rs);
        }
    }

    /**
     * Method call will check if the param macAddress is a valid macAdress and
     * return a boolean
     *
     * @param macAddress - mac address to check
     * @return true if it is a valid mac Address
     */
    public boolean checkMacAddress(String macAddress) {
        if (macAddress == null) {
            return false;
        }
        return macAddress.matches("[a-fA-F0-9]{40}");
    }

    /**
     * Method call will check if the param name is empty or not.
     *
     * @param name - name to check
     * @return true if name is not null
     */
    public boolean checkName(String name) {
        return name != null;
    }

    /**
     * Method call will check if the param password is a valid password
     *
     * @param password - password to check
     * @return true if password is valid
     */
    public boolean checkPassword(String password) {
        return !(password.length() < 8 || password.indexOf(' ') != -1);
    }

    /**
     * Method call will check if the param email is a valid Email
     *
     * @param email - email to check
     * @return true if email is a valid email
     */
    public boolean checkEmail(String email) {
        Pattern p = Pattern.compile("^[A-Za-z0-9.]+\\.+[2]{1}+[0]{1}+[1]{1}+[3-7]{1}+@(?:{1}|business|accountancy|sis|economics|law|socsc)+\\.+smu\\.+edu\\.+sg+$");
        Matcher m = p.matcher(email);
        return (m.matches());
    }

    /**
     * Method call will check if the param gender is either m or f
     * (case-insensitive)
     *
     * @param gender - gender to check
     * @return true if gender is valid
     */
    public boolean checkGender(String gender) {
        return gender.equalsIgnoreCase("m") || gender.equalsIgnoreCase("f");
    }

    /**
     * Method call will check if parameter location is in the location lookup
     * list
     *
     * @param location - location id to check
     * @return true if it is in the list
     */
    public boolean checkLocation(String location) {
        return locationLookUpList.contains(location);
    }

    /**
     * Method call will check if parameter time is in a correct format.
     *
     * @param time -  timestamp to check
     * @return true if it is in the right format
     */
    public boolean checkTimeStamp(String time) {
        try {
            SLOCADate dateTime = new SLOCADate(time);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Method call will validate the semantic place by checking if the place
     * follows to rules.
     *
     * @param semanticPlace -  semantic place to check
     * @return true if semantic place is valid
     */
    public boolean checkSemanticPlace(String semanticPlace) {
        String start = semanticPlace.substring(0, 6);
        if (!start.equals("SMUSIS")) {
            return false;
        }

        String level = semanticPlace.substring(6, 8);
        String[] levels = {"B1", "L1", "L2", "L3", "L4", "L5"};

        for (String validLevel : levels) {
            if (validLevel.equals(level)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Method call checks if location id is valid
     * 
     * @param locationId - location id to check
     * @return boolean
     */
    public boolean checkLocationId(String locationId) {
        try {
            int locId = Integer.parseInt(locationId);
            return locId > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Method call checks if User is valid
     * @param macAddress - macaddress to check
     * @param name - name to check
     * @param password - password to check
     * @param email - email to check
     * @param gender - gender to check
     * @return treemap of booleans of errors
     */
    public TreeMap<Integer, Boolean> checkUser(String macAddress, String name, String password, String email, String gender) {
        TreeMap<Integer, Boolean> result = new TreeMap<>();
        result.put(1, checkMacAddress(macAddress));
        result.put(2, checkName(name));
        result.put(3, checkPassword(password));
        result.put(4, checkEmail(email));
        result.put(5, checkGender(gender));

        return result;
    }

    /**
     * Method call checks if User is valid
     * @param filePath - path to the file to upload
     * @return thashmap of errors
     * @throws IOException - if file not found
     */
    public THashMap<Integer, ArrayList<String>> validateUser(String filePath) throws IOException {
        File file = new File(filePath);
        ArrayList<String> errorMsgs = new ArrayList<>();
        TreeMap<Integer, Boolean> result = null;
        THashMap<Integer, ArrayList<String>> toReturn = new THashMap<>();
        int p = 0;
        try (
                Reader reader = Files.newBufferedReader(Paths.get(filePath));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withHeader("mac_address", "name", "password", "email", "gender")
                        .withIgnoreHeaderCase()
                        .withTrim());) {
            try (
                    BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getParent() + File.separator + "tempUser.csv"));
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.
                            withHeader("mac_address", "name", "password", "email", "gender"));) {
                int i = 1;
                for (CSVRecord csvRecord : csvParser.getRecords()) {

                    errorMsgs = new ArrayList<>();
                    if (i != 1) {

                        // Accessing values by the names assigned to each column
                        String mac = csvRecord.get("mac_address");
                        String name = csvRecord.get("name");
                        String pw = csvRecord.get("password");
                        String email = csvRecord.get("email");
                        String gender = csvRecord.get("gender");

                        result = checkUser(mac, name, pw, email, gender);
                        boolean success = true;

                        if (mac.equals("")) {
                            errorMsgs.add("blank mac address");
                            success = false;
                        }
                        if (name.equals("")) {
                            errorMsgs.add("blank name");
                            success = false;
                        }
                        if (pw.equals("")) {
                            errorMsgs.add("blank password");
                            success = false;
                        }
                        if (email.equals("")) {
                            errorMsgs.add("blank email");
                            success = false;
                        }
                        if (gender.equals("")) {
                            errorMsgs.add("blank gender");
                            success = false;
                        }

                        int count = 1;
                        p++;
                        if (result.containsValue(false) || !success) {
                            while (count <= result.size()) {
                                boolean b = result.get(count);
                                if (!b) {
                                    success = false;
                                    errorMsgs.add(ERRORMSGSFORDEMO.get(count));
                                }
                                count++;
                            }
                        }
                        result.clear();
                        if (!success) {
                            toReturn.put(i, errorMsgs);
                        } else {
                            successForDemo++;
                            csvPrinter.printRecord(mac, name, pw, email, gender);
                        }

                    }
                    i++;
                }
            }
        }
        return toReturn;
    }

    /**
     * Method call will check if Location is valid
     * @param locationId -locationid to check
     * @param semanticPlace - semantic place to check 
     * @return treemap of boolean of errors
     */
    public TreeMap<Integer, Boolean> checkLocation(String locationId, String semanticPlace) {
        TreeMap<Integer, Boolean> result = new TreeMap<>();
        result.put(1, checkLocationId(locationId));
        result.put(2, checkSemanticPlace(semanticPlace));

        return result;
    }

    /**
     * Method call will validate the file
     * @param filePath - path to file to upload
     * @return thashmap of errors
     * @throws IOException - if file not found
     */
    public THashMap<Integer, ArrayList<String>> validateLocation(String filePath) throws IOException {
        File file = new File(filePath);
        ArrayList<String> errorMsgs = new ArrayList<>();
        TreeMap<Integer, Boolean> result = null;
        THashMap<Integer, ArrayList<String>> toReturn = new THashMap<>();

        try (
                Reader reader = Files.newBufferedReader(Paths.get(filePath));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withHeader("location_Id", "semantic_place")
                        .withIgnoreHeaderCase()
                        .withTrim());) {
            try (
                    BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getParent() + File.separator + "tempLoc.csv"));
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.
                            withHeader("location_Id", "semantic_place"));) {
                int i = 1;

                for (CSVRecord csvRecord : csvParser.getRecords()) {
                    // Accessing values by the names assigned to each column
                    if (i != 1) {
                        String locId = csvRecord.get("location_Id");
                        String sem = csvRecord.get("semantic_place");

                        result = checkLocation(locId, sem);
                        boolean success = true;
                        if (locId.equals("")) {
                            errorMsgs.add("blank location id");
                            success = false;
                        }
                        if (sem.equals("")) {
                            errorMsgs.add("blank semantic place");
                            success = false;
                        }

                        int count = 1;

                        if (result.containsValue(false) || !success) {
                            while (count <= result.size()) {
                                boolean b = result.get(count);
                                if (!b) {
                                    success = false;
                                    errorMsgs.add(ERRORMSGSFORLOCLOOK.get(count));
                                }
                                count++;
                            }
                        }

                        if (!success) {
                            toReturn.put(i, errorMsgs);
                        } else {
                            successForLocationLookup++;
                            csvPrinter.printRecord(locId, sem);
                        }

                        errorMsgs = new ArrayList<>();
                        result.clear();
                    }
                    i++;
                }
            }
        }
        return toReturn;
    }

    /**
     * Method call with check if the data is valid
     * @param timeStamp - timestamp to check
     * @param macAddress - macaddress to check
     * @param locationId -location id to check
     * @return treemap of boolean of errors
     */
    public TreeMap<Integer, Boolean> checkLocationData(String timeStamp, String macAddress, String locationId) {
        TreeMap<Integer, Boolean> result = new TreeMap<>();
        result.put(1, checkTimeStamp(timeStamp));
        result.put(2, checkMacAddress(macAddress));
        result.put(3, checkLocation(locationId));

        return result;
    }

    /**
     * Method call will validate location data file
     * @param filePath - path to file to upload
     * @return THashMap of errors
     * @throws IOException - if file not found
     */
    public THashMap<Integer, ArrayList<String>> validateLocationData(String filePath) throws IOException {
        File file = new File(filePath);
        ArrayList<String> errorMsgs = new ArrayList<>();
        TreeMap<Integer, Boolean> result = null;
        THashMap<Integer, ArrayList<String>> toReturn = new THashMap<>();
        ArrayList<String> temp = new ArrayList<>();
        HashMap<String, ArrayList<Integer>> dupMap = new HashMap<>();
        List<ArrayList<String>> toUpload = new ArrayList<>();
        Set<String> uploadSet = new THashSet<>();
        HashMap<String, Integer> map = new HashMap<>();

        updateLocationLookupList();
        try (
                Reader reader = Files.newBufferedReader(Paths.get(filePath));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withHeader("timestamp", "mac_address", "location_Id")
                        .withIgnoreHeaderCase()
                        .withTrim());) {
            try (
                    BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getParent() + File.separator + "tempData.csv"));
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.
                            withHeader("dateTime", "mac_address", "location_Id"));) {
                int i = 1;

                for (CSVRecord csvRecord : csvParser.getRecords()) {
                    // Accessing values by the names assigned to each column
                    if (i != 1) {
                        String dateTime = csvRecord.get("timestamp");
                        String mac = csvRecord.get("mac_address");
                        String locId = csvRecord.get("location_Id");

                        result = checkLocationData(dateTime, mac, locId);
                        boolean success = true;

                        if (dateTime.equals("")) {
                            errorMsgs.add("blank timestamp");
                            success = false;
                        }
                        if (mac.equals("")) {
                            errorMsgs.add("blank mac address");
                            success = false;
                        }
                        if (locId.equals("")) {
                            errorMsgs.add("blank location id");
                            success = false;
                        }

                        int count = 1;

                        if (result.containsValue(false) || !success) {
                            while (count <= result.size()) {
                                boolean b = result.get(count);
                                if (!b) {
                                    success = false;
                                    errorMsgs.add(ERRORMSGSFORLOC.get(count));
                                }
                                count++;
                            }
                        }

                        result.clear();
                        if (!success) {
                            toReturn.put(i, errorMsgs);
                        } else {
                            String dupString = dateTime + mac;
                            boolean addLoc = uploadedLocation.add(dupString);
                            boolean addSet = uploadSet.add(dupString);
                            
                            if (!addLoc && addSet) {
                                System.out.println("here?");
                                errorMsgs.add("duplicate row");
                                toReturn.put(i,errorMsgs);
                                if(dupMap.containsKey(dupString)){
                                    ArrayList<Integer> dup = dupMap.get(dupString);
                                    dup.add(i);
                                    dupMap.put(dupString,dup);
                                }else{
                                    ArrayList<Integer> dup = new ArrayList<>();
                                    dup.add(i);
                                    dupMap.put(dupString,dup);
                                }
                                ArrayList<String> correct = new ArrayList<>();
                                correct.add(dateTime);
                                correct.add(mac);
                                correct.add(locId);
                                toUpload.add(correct);
                            } else if(!addLoc && !addSet){
                                System.out.println("or here?");
                                if(map.containsKey(dupString)){
                                    int line = map.get(dupString);
                                    if(!toReturn.containsKey(line)){
                                        ArrayList<String> list = new ArrayList<>();
                                        list.add("duplicate row");
                                        toReturn.put(line, list);
                                    } else {
                                        if(!toReturn.get(line).contains("duplicate row")){
                                            ArrayList<String> list = new ArrayList<>();
                                            list.add("duplicate row");
                                            toReturn.put(line, list);
                                        }
                                    }
                                }
                                errorMsgs.add("duplicate row");
                                toReturn.put(i,errorMsgs);
                                if(dupMap.containsKey(dupString)){
                                    ArrayList<Integer> dup = dupMap.get(dupString);
                                    dup.add(i);
                                    dupMap.put(dupString,dup);
                                }else{
                                    ArrayList<Integer> dup = new ArrayList<>();
                                    dup.add(i);
                                    dupMap.put(dupString,dup);
                                }
                            } else {
                                System.out.println("here?");
                                map.put(dupString,i);
                                successForLocation++;
                                ArrayList<String> correct = new ArrayList<>();
                                correct.add(dateTime);
                                correct.add(mac);
                                correct.add(locId);
                                toUpload.add(correct);
                            }
                        }

                        errorMsgs = new ArrayList<>();
                        result.clear();
                    }
                    i++;
                }
                Iterator<String> iter = dupMap.keySet().iterator();
                while (iter.hasNext()) {
                   String s = iter.next();
                   ArrayList<Integer> dup = dupMap.get(s);
                   if(dup.size() > 1){
                       int lineToDelete = dup.get(dup.size()-1);
                       if(toReturn.containsKey(lineToDelete)){
                           toReturn.remove(lineToDelete);
                       }
                   }
                }
                csvPrinter.printRecords(toUpload);
            }
        }
        return toReturn;
    }

    /**
     * Method call returns successForDemo
     * @return int successForDemo
     */
    public int getSuccessForDemo() {
        return successForDemo;
    }

    /**
     * Method call returns success for LocationLookup
     * @return int - number of success for location lookup
     */
    public int getSuccessForLocationLookup() {
        return successForLocationLookup;
    }

    /**
     * Returns Success for Location
     * @return int - number of success
     */
    public int getSuccessForLocation() {
        return successForLocation;
    }

    /**
     * Method call sets sucessForDemo to Zero
     */
    public void setSuccessForDemoToZero() {
        successForDemo = 0;
    }

    /**
     * Method Call sets successFor location lookup to zero
     */
    public void setSuccessForLocLookToZero() {
        successForLocationLookup = 0;
    }

    /**
     * Method call sets success for location to zero
     */
    public void setSuccessForLocationToZero() {
        successForLocation = 0;
    }
}
