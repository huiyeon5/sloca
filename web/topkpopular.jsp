<%-- 
    Document   : topkpopular
    Created on : Oct 22, 2017, 10:38:13 AM
    Author     : Keith
--%>

<%@page import="java.util.TreeMap"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<!--protect from viewing pages without logging in-->
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Top K Popular Places</title>

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
            table{
                border: 2 black;
            }
        </style>
    </head>
    <body onload="populateDropdown()">
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

        <h1>Top K Popular Places</h1>
        <form action="PopularPlaceController" id="form">      
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
                    <th>Top ..</th>
                    <td>
                        <select name="k" id="topK" placeholder="3">
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
        <%            if (session.getAttribute("popPlace") != null) {
                String rank = (String) request.getAttribute("num");
                int k = 0;
                if (rank != null) {
                    k = Integer.parseInt(rank);
                }

                TreeMap<Integer, ArrayList<String>> resultMap = (TreeMap<Integer, ArrayList<String>>) request.getAttribute("resultMap");
                if (resultMap == null || resultMap.size() == 0) {
                    out.println("<center><h3> We found no data! </h3></center>");
                } else {

                    out.println("<center><h3>Showing results for: </br></h3></center>");
                    out.println("</br>");
                    out.println("<center><h3><b>Date:</b> " + request.getAttribute("date") + " <b>Time:</b> " + request.getAttribute("time") + " <b>Top:</b> " + resultMap.size() + "</h3></center></br>");
                    out.println("<center><table border = 2>");
                    out.println("<tr> <th>Top K</th> <th> Number of people </th> <th> Semantic Places </th> </tr>");

                    int i = 1;
                    
                    for (int numPeople : resultMap.descendingKeySet()) {

                        ArrayList<String> semPlaces = resultMap.get(numPeople);

                        out.println("<tr><td>" + i + "</td><td>" + numPeople + "</td><td>");
                        String semanticPlaces = "";
                        for (String semPlace : semPlaces) {
                            semanticPlaces += "\"" + semPlace + "\",";
                        }
                        out.println(semanticPlaces.substring(0, semanticPlaces.length() - 1) + "</td></tr>");
                        i++;
                    }

                    if (k > (i - 1)) {
                        out.println("<h3> There is data only for Top " + (i - 1) + "!</h3>");
                    }
                    out.println("</table></center>");
                }
            } else {
                String error = (String) request.getAttribute("error");
                if (error != null) {
                    out.println("<center><h3>" + error + "</h3></center>");
                }
            }

            session.setAttribute("popPlace", null);
        %>
    </body>
</html>
