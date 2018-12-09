<%-- 
    Author     : Keith
--%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashMap"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Top K Companions</title>

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
        <h1>Top K Companions</h1>
        <form action ="CompanionsServlet" method = "POST" id="form">
            <table class="table table-striped">
                <tr>
                    <th>Mac Address</th>
                    <td>
                        <input type="text" name="mac_address">
                    </td>
                </tr>
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
                        <select name="topk" id="topK">
                            <%                                for (int i = 1; i <= 10; i++) {
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
            String error = (String) request.getAttribute("error");
            if (error != null) {
                out.print("<h3><center>" + error + "</h3></center>");
            }
            LinkedHashMap<String, Long> results = (LinkedHashMap<String, Long>) request.getAttribute("return");
            if (results != null) {
                if (results.size() == 0) {
                    out.println("<h3><center>We found no data!</center></h3>");
                } else {
                    int topK = Integer.parseInt((String) request.getAttribute("topK"));
                    String date = (String) request.getAttribute("date");
                    String timeHeader = (String) request.getAttribute("time");
                    String macAddress = (String) request.getAttribute("macAddress");
                    HashMap<String, String> emailList = (HashMap<String, String>) request.getAttribute("email");
                    List<String> keys = new ArrayList<>(results.keySet());

                    out.print("<h3><center>Showing results for: </center></h3>");
                    out.print("</br>");
                    out.print("<center><h4><b>Mac Address: </b>" + macAddress + ", <b>Date: </b> " + date + ", <b>Time: </b>" + timeHeader + ", <b>Top: </b>" + topK + "</h4></center>");
                    out.print("</br>");
                    if (results.size() < topK) {
                        out.print("<center><h3> There is data only for top " + results.size() + "!</h3></center>");
                    }
                    out.print("<center><table id='result' border=2>"
                            + "<tr>"
                            + "<th>Rank</th>"
                            + "<th>Mac-Address</th>"
                            + "<th>Email</th>"
                            + "<th>Time Spent</th>"
                            + "</tr>");
                    long curLongest = 0;
                    long curTime = 0;
                    int i = 0;
                    int index = 0;
                    int prevIndex = index;
                    while (i < topK) {
                        String curMac = "";
                        out.print("<tr>");
                        out.print("<td>");
                        out.print(i + 1);
                        out.print("</td>");
                        out.print("<td>");

                        curTime = results.get(keys.get(index));
                        Iterator<String> iter2 = results.keySet().iterator();
                        while (iter2.hasNext()) {
                            String mac = iter2.next();
                            long time = results.get(mac);
                            if (curTime == time) {
                                out.println(mac + "</br>");
                                index++;
                            }
                        }

                        out.print("</td>");
                        out.print("<td align=center>");
                        for (int j = prevIndex; j < index; j++) {
                            curMac = keys.get(j);
                            if (emailList.containsKey(curMac)) {
                                out.print(emailList.get(curMac) + "</br>");
                            } else {
                                out.print("-");
                            }
                        }

                        out.print("</td>");
                        out.print("<td>");
                        out.println(results.get(curMac));
                        out.print("</td>");
                        out.print("</tr>");
                        i++;
                        prevIndex = index;
                    }
                    out.print("</table></centre>");
                    out.print("</br>");
                }
            }
        %>
    </body>
</html>
