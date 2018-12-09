package DAO;

import connection.ConnectionManager;
import entity.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 *
 * @author Jia Xian
 */
public class UserDAO {

    ArrayList<User> userList = new ArrayList<User>(); //all users listed in the userList
    private static final String TBL = "user";

    /**
     * Retrieve a list of User from UserDAO
     *
     * @return userList
     */
    public ArrayList retrieveAll() {
        return userList;
    }

    /**
     * Retrieve User by emailID
     *
     * @param emailID Other report to be compared to the current Location report     * @return User
     * @return an user according to the email ID
     */
    public User retrieve(String emailID) {
        User user = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                return null;
            }
            stmt = conn.prepareStatement("select * from " + TBL + " where email like '" + emailID + "%'");
            rs = stmt.executeQuery();

            if (rs.next()) {
                user = new User(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5).charAt(0));
            }
        } catch (SQLException e) {
        } finally {
            ConnectionManager.close(conn, stmt, rs);
        }
        return user;
    }

    /**
     * Retrieves user email by mac address
     *
     * @param macAdd retrieve User email with mac address     * @return String email
     * @return email of the queried mac address
     */
    public String retrieveUserEmail(String macAdd) {
        String result = "";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                return null;
            }
            stmt = conn.prepareStatement("select email from " + TBL + " where mac_address = '" + macAdd + "'");
            rs = stmt.executeQuery();

            if (rs.next()) {
                result = rs.getString(1);
                System.out.println(result);
            } else {
                result = "null";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, rs);
        }

        return result;
    }

}
