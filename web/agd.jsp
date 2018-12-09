<%-- 
    Document   : agd
    Created on : 26 Oct, 2017, 4:43:19 PM
    Author     : Pei Shan
--%>

<%@page import="DAO.UserDAO"%>
<%@page import="entity.User"%>
<%@page import="entity.GroupLocationReport"%>
<%@page import="entity.AGDGroup"%>
<%@page import="java.util.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Auto Group Detection</title>

        <!-- Date picker (Start) -->
        <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        <link rel="stylesheet" href="/resources/demos/style.css">
        <!-- Date picker (End) -->

        <!-- Time picker (Start)-->
        <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/timepicker/1.3.5/jquery.timepicker.min.css">
        <!-- Time picker (End)-->

        <style>
            h1,p{
                text-align: center;
            }
            #form th{
                font-size: 20px;
            }
        </style>
    </head>
    <body>
        <!--Navigation Bar (Start)-->
        <%@include file="navbar.jsp" %>
        <!--Navigation Bar (End)-->

        <script>
            $(function () {
                $("#datepicker").datepicker({
                    dateFormat: 'yy-mm-dd',
                    changeMonth: true,
                    changeYear: true
                });
                $("#timepicker").timepicker({
                    timeFormat: 'HH:mm:ss',
                    interval: 1,
                    defaultTime: '11',
                    startTime: '10:00',
                    dynamic: true,
                    dropdown: true,
                    scrollbar: true
                });
            });
        </script>

        <h1>Auto Group Detection</h1>
        <form action="AGDServlet" method="POST" id="form">
            <table class="table table-striped">
                <tr>
                    <th>Date</th>
                    <td><input type="text" id="datepicker" name="date" onkeydown="return false"></td>
                </tr>
                <tr>
                    <th>Time</th>
                    <td><input type="text" id="timepicker" name="time"></td>
                </tr>
                <tr>
                    <td colspan="2">
                        <input type="submit" value="Submit"/>
                    </td>
                </tr>
            </table>
        </form>

        <%    HashMap<AGDGroup, ArrayList<HashMap<String, String>>> groupList = (HashMap<AGDGroup, ArrayList<HashMap<String, String>>>) request.getAttribute("groups");

            String errorMsg = (String) request.getAttribute("error");

            if (errorMsg == null) {
                if (groupList != null) {
                    //Display title with user selected date and time
                    String date = (String) request.getAttribute("date");
                    String time = (String) request.getAttribute("time");
                    int users = (int) request.getAttribute("users");
                    out.println("<center><h3>Showing results for</h3></center>");
                    out.println("</br>");
                    out.print("<center><h4> <b>Date:</b> " + date + ", <b>Time:</b> " + time + "<h4></center>");
                    out.print("<center><h4> <b>Number of users in SIS: </b>" + users + "<h4></center>");
                    out.print("<center><h4> <b>Number of groups found: </b>" + groupList.size() + "<h4></center>");
                    out.println("</br>");

                    Set<AGDGroup> keys = groupList.keySet();

                    int count = 1;

                    //ArrayList<User> userList = UserDAO.retrieveAll();
                    for (AGDGroup group : keys) {
                        out.println("<br>");
                        out.println("<h3><center><b> Group " + count + "</b></center></h3>");
                        out.println("<h3><center>No of users: " + group.getMembers().size() + "</center></h3>");
                        ArrayList<HashMap<String, String>> value = groupList.get(group);
                        out.println("<center><table border=2>");
                        out.println("<tr>");
                        out.println("<th> Members </th>");
                        out.println("<th> Email </th>");
                        out.println("<th> Location ID </th>");
                        out.println("<th> Time Spent (Sec) </th>");
                        out.println("</tr>");

                        HashMap<String, String> membersHm = value.get(0);
                        Set<String> memberKeys = membersHm.keySet();

                        //Printing in Members' column
                        out.println("<td>");
                        for (String macAdd : memberKeys) {
                            out.println(macAdd + "</br>");
                        }
                        out.println("</td>");

                        //Printing in Email's column 
                        out.println("<td>");
                        for (String macAdd : memberKeys) {
                            out.println(membersHm.get(macAdd) + "</br>");
                        }
                        out.println("</td>");

                        HashMap<String, String> reportsHm = value.get(1);
                        Set<String> reportKeys = reportsHm.keySet();

                        //Printing in Location ID's column 
                        out.println("<td>");
                        for (String locId : reportKeys) {
                            out.println(locId + "</br>");
                        }
                        out.println("</td>");

                        //Printing in Time Spent's column 
                        out.println("<td>");
                        for (String locId : reportKeys) {
                            out.println(reportsHm.get(locId) + "</br>");
                        }
                        out.println("</td>");

                        out.println("</center></table>");
                        out.println("</br>");
                        count++;
                    }
                }
            } else {
                out.println("<center><h3>" + errorMsg + "</h3></center>");
            }
        %>
    </body>
</html>
