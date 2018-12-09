<%-- 
    Document   : heatmap
    Created on : Oct 6, 2017, 5:05:27 PM
    Author     : Wei Ming
--%>

<%@page import="entity.Heatmap"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>HeatMap</title>

        <!-- Date picker (Start)-->
        <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        <link rel="stylesheet" href="/resources/demos/style.css">
        <!-- Date picker (End)-->

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
                    changeYear: true,

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

        <h1>Heatmap</h1>
        <form action ="HeatmapServlet" method ="POST" id="form">
            <table class="table table-striped">
                <tr>
                    <th>Floor: </th> 
                    <td>
                        <select name = "floor">
                            <option value ="B1">B1</option>
                            <option value ="L1">L1</option>
                            <option value ="L2">L2</option>
                            <option value ="L3">L3</option>
                            <option value ="L4">L4</option>
                            <option value ="L5">L5</option>
                        </select>
                    </td> 
                </tr>
                <tr>
                    <th>Date: </th>
                    <td><input type="text" id="datepicker" name="date" onkeydown="return false"></td>

                </tr>
                <tr>
                    <th>Time: </th>
                    <td>
                        <input type="text" id="timepicker" name="time">
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <input type = "submit" value="Submit">
                    </td>
                </tr>
            </table>
        </form>

        <%            ArrayList<Heatmap> result = (ArrayList<Heatmap>) request.getAttribute("crowdDensity");
            String floor = (String) request.getAttribute("floor");
            String date = (String) request.getAttribute("date");
            String time = (String) request.getAttribute("time");
            if (result != null) {
                if (result.size() != 0) {
                    out.println("<center><h3>Showing results for: </h3></center></br>");
                    out.println("<center><h4><b>Date:</b> " + date + ", <b>Time:</b> " + time + ", <b>Floor:</b> " + floor + "</ h4 ></center></br>");
                    out.println("<center><table border=2>");
                    out.println("<tr>");
                    out.println("<th>Semantic Place</th>");
                    out.println("<th>Number of People</th>");
                    out.println("<th>Crowd Density</th>");
                    out.println("</tr>");
                    for (int i = 0; i < result.size(); i++) {
                        Heatmap heatmap = result.get(i);
                        String semanticPlace = heatmap.getSemanticPlace();
                        int numOfPeople = heatmap.getNumOfPeople();
                        int density = heatmap.getDensity();
                        out.println("<tr>");
                        out.println("<td>" + semanticPlace + "</td>");
                        out.println("<td>" + numOfPeople + "</td>");
                        out.println("<td>" + density + "</td>");
                        out.println("</tr>");
                    }
                    out.println("</table></center>");
                } else {
                    out.println("<center><h3>" + request.getAttribute("error") + "</h3></center>");
                }
            } else {
                String errorMsg = (String) request.getAttribute("error");
                if (errorMsg != null) {
                    out.println("<center><h3>" + errorMsg + "</h3></center>");
                }
            }
        %>
    </body>
</html>
