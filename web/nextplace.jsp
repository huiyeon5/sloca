<%-- 
    Document   : nextplace.jsp
    Created on : Oct 24, 2017, 4:33:46 PM
    Author     : Huiyeon Kim
--%>

<%@page import="java.util.Collections"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="utility.ValidateUtility"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Top-k Next Place</title>
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
        <script>
            function populateDropdown() {
                var select = document.getElementById("topK");
                for (var i = 1; i <= 10; i++) {
                    var el = document.createElement("option");
                    el.textContent = i;
                    el.value = i;
                    if (i == 3) {
                        el.selected = true;
                    }
                    select.appendChild(el);
                }
            }
        </script>
        <h1>Top K Next Place</h1>
        <form action ="NextPlaceController" method = "POST" id="form">
            <table class="table table-striped">
                <tr>
                    <th>Date</th>
                    <td>
                        <input type="text" id="datepicker" name="date" onkeydown="return false">
                    </td>
                </tr>
                <tr>
                    <th>Time</th>
                    <td>
                        <input type="text" id="timepicker" name="time">
                    </td>
                </tr>
                <tr>
                    <th>Semantic Place</th>
                    <td>
                        <select name="semanticPlace">
                            <%                                ValidateUtility vu = new ValidateUtility();
                                vu.updateSemSet();
                                Set<String> semSet = vu.returnSemSet();
                                List<String> semList = new ArrayList<>(semSet);
                                Collections.sort(semList);
 
                                for (String sem : semList) {
                                    out.println("<option value='" + sem + "'>" + sem + "</option>");
                                }

                            %>
                        </select>
                    </td>
                </tr>
                <tr>
                    <th>Top ..</th>
                    <td>
                        <select name="topk">
                            <%                            for (int i = 1; i <= 10; i++) {
                                    if (i == 3) {
                                        out.println("<option value=\"" + i + "\" selected=\"selected\">" + i + "</option>");
                                    } else {
                                        out.println("<option value=\"" + i + "\">" + i + "</option>");
                                    }
                                }
                            %>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <input type="submit" value="Submit">
                    </td>
                </tr>
            </table>
        </form>

        <%
            TreeMap<Integer, ArrayList<String>> resultMap = (TreeMap<Integer, ArrayList<String>>) request.getAttribute("resultMap");
            String dateError = (String) request.getAttribute("dateError");
            String error = (String) request.getAttribute("error");
            if (dateError != null) {
                out.println("<center><h3>" + dateError + "</h3></center>");
            } else if (error != null) {
                out.println("<center><h3>" + error + "</h3></center>");
            } else {
                if (resultMap != null) {
                    if (resultMap.size() > 0) {
                        String date = request.getParameter("date");
                        String time = request.getParameter("time");
                        String semanticPlace = request.getParameter("semanticPlace");
                        int k = (Integer) request.getAttribute("k");
                        out.println("<center><h3>Showing Results for:<h3></br></center>");
                        out.println("<center><h4><b>Date:</b> " + date + ", <b>Time:</b> " + time + ", <b>Semantic Place:</b> " + semanticPlace + ", <b>Top:</b> " + k + "</center></br>");
                        int totalPeople = (Integer) request.getAttribute("numOfPeople");
                        int countNumWhoMove = (Integer) request.getAttribute("countNumWhoMove");

                        out.println("<center><h3> <b>Number of people at the origin:</b> " + totalPeople + "</h3></center></br>");
                        out.println("<center><h3> <b>Number of people who visited other places:</b> " + countNumWhoMove + "</h3></center></br>");
                        out.println("<center><table border=2><tr><th>Top K</th><th> Semantic Place</th><th> Number Of People</th><th> Percentage</th></tr>");
                        int i = 1;
                        for (int key : resultMap.descendingKeySet()) {
                            out.println("<tr><td>" + i + "</td><td>");
                            for (int j = 0; j < resultMap.get(key).size(); j++) {
                                out.println(resultMap.get(key).get(j) + "</br>");
                            }
                            out.println("</td><td> " + key + "</td><td> " + (Math.round((100.0 * (key * 1.0 / totalPeople)) * 100.0) / 100.0) + "%</td></tr>");
                            i++;
                        }
                        out.println("</table></center></br>");

                        if (k > i - 1) {
                            out.println("<center><h2> There is data only for Top " + (i - 1) + "!</h2><center>");
                        }
                    } else {
                        out.println("We found no data!");
                    }
                }
            }
        %>
    </body>
</html>
