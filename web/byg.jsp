<%-- 
    Document   : byg
    Created on : 17 Oct, 2017, 5:17:27 PM
    Author     : Pei Shan
--%>

<%@page import="java.util.*"%>
<%@page import="java.lang.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Breakdown of Year and Gender</title>

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
            #form th,tr,td{
                text-align: left;
            }
            table#result th,tr{
                text-align: center;
                vertical-align: middle;
            }


        </style>

        <!--Checkbox Styling, toogling the choice options-->
        <style>
            .switch {
                position: relative;
                display: inline-block;
                width: 60px;
                height: 34px;
            }

            .switch input {display:none;}

            .slider {
                position: absolute;
                cursor: pointer;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background-color: #ccc;
                -webkit-transition: .4s;
                transition: .4s;
            }

            .slider:before {
                position: absolute;
                content: "";
                height: 26px;
                width: 26px;
                left: 4px;
                bottom: 4px;
                background-color: white;
                -webkit-transition: .4s;
                transition: .4s;
            }

            input:checked + .slider {
                background-color: #2196F3;
            }

            input:focus + .slider {
                box-shadow: 0 0 1px #2196F3;
            }

            input:checked + .slider:before {
                -webkit-transform: translateX(26px);
                -ms-transform: translateX(26px);
                transform: translateX(26px);
            }

            /* Rounded sliders */
            .slider.round {
                border-radius: 34px;
            }

            .slider.round:before {
                border-radius: 50%;
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

        <h1>Breakdown of Year and Gender</h1>
        <form action="BYGController" method="POST" id="form" >
            <table class="table table-striped">
                <tr>
                    <th scope="row">1.</th>
                    <td>
                        <select id="choiceOne" onchange = "myFunction('choiceTwo')" name="firstOrder" required="Required">
                            <option>--Select Choice--</option>
                        </select>
                    </td>
                    <td>   
                        <label class="switch">                        
                            <input type="checkbox" checked onchange="document.getElementById('choiceOne').disabled = !this.checked">
                            <span class="slider"></span>
                        </label>
                    </td>
                </tr>
                <tr>
                    <th scope="row">2.</th>
                    <td>
                        <select id="choiceTwo" onchange="myFunction('choiceThree')" name="secondOrder" disabled>
                            <option>--Select Choice--</option>
                        </select>
                    </td>
                    <td>   
                        <label class="switch">                        
                            <input type="checkbox" onchange="document.getElementById('choiceTwo').disabled = !this.checked">
                            <span class="slider"></span>
                        </label>
                    </td>
                </tr>
                <tr>
                    <th scope="row">3.</th>
                    <td>
                        <select id="choiceThree" name="thirdOrder" disabled>
                            <option>--Select Choice--</option>
                        </select>
                    </td>
                    <td>   
                        <label class="switch">                        
                            <input type="checkbox" onchange="document.getElementById('choiceThree').disabled = !this.checked">
                            <span class="slider"></span>
                        </label>
                    </td>
                </tr>
                <tr>
                    <th>Select Date & Time</th>
                    <td>
                        Date: <input type="text" id="datepicker" name="date" onkeydown="return false"><br/><br/>
                        Time: <input type="text" id="timepicker" name="time">
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <input type="reset" value="Clear form & reselect">
                        <input type="submit" value="Submit"/>
                    </td>
                </tr>
            </table>
        </form>

        <!-- Creating Drop down for selecting options, script is places after choiceForm-->
        <script>
            var choiceId = "choiceOne";
            var select = document.getElementById(choiceId);
            var options = ["year", "school", "gender"];


            for (var i = 0; i < options.length; i++) {
                var opt = options[i];
                var el = document.createElement("option");//<option>
                el.textContent = opt;//year
                el.value = opt;
                select.appendChild(el);
            }

            //method myFunction() takes in one parameter
            function myFunction(selectId) {
                var select = document.getElementById(selectId)
                var choice = document.getElementById(choiceId).value;
                var index = options.indexOf(choice);

//                if (select.hasChildNodes()) {
//                    while (select.hasChildNodes()) {
//                        select.removeChild(select.lastChild);
//                    }
//                    options = ["year", "school", "gender"];
//                }

                if (selectId !== choiceId) {
                    options.splice(index, 1);
                    for (var i = 0; i < options.length; i++) {
                        var opt = options[i];
                        var el = document.createElement("option");
                        el.textContent = opt;
                        el.value = opt;
                        select.appendChild(el);
                    }
                }
                choiceId = selectId;
            }
        </script>
        <br/>

        <%            TreeMap<String, Integer> firstOrderResult = (TreeMap<String, Integer>) request.getAttribute("firstOrderResult");
            TreeMap<String, TreeMap<String, Integer>> secondOrderResult = (TreeMap<String, TreeMap<String, Integer>>) request.getAttribute("secondOrderResult");
            TreeMap<String, TreeMap<String, Integer>> thirdOrderResult = (TreeMap<String, TreeMap<String, Integer>>) request.getAttribute("thirdOrderResult");

            String firstOrder = (String) request.getAttribute("firstOrder");
            String secondOrder = (String) request.getAttribute("secondOrder");
            String thirdOrder = (String) request.getAttribute("thirdOrder");

            String errorMessage = (String) request.getAttribute("error");

            if (errorMessage == null) {
                if (firstOrderResult != null && secondOrderResult != null && thirdOrderResult != null) {
                    int totalSLOCAUsers = (int) request.getAttribute("totalSLOCAUsers");
                    String date = (String) request.getAttribute("date");
                    String time = (String) request.getAttribute("time");
                    int height1 = 0;
                    int height2 = 0;
                    switch (secondOrder) {
                        case "school":
                            height1 = 6;
                            break;
                        case "year":
                            height1 = 5;
                            break;
                        default:
                            height1 = 2;
                            break;
                    }
                    switch (thirdOrder) {
                        case "school":
                            height2 = 6;
                            break;
                        case "year":
                            height2 = 5;
                            break;
                        default:
                            height2 = 2;
                            break;
                    }

                    int tableHeight = 50 * height1 * height2;
                    out.println("<center><h3>Showing results for    </h3></center>");
                    out.println("</br>");
                    out.print("<center><h4> <b>Date:</b> " + date + ", <b>Time:</b> " + time);
                    out.println(", <b>First Choice:</b> " + firstOrder + ", <b>Second Choice:</b> " + secondOrder + ", <b>Third Choice:</b> " + thirdOrder + "<h4></center>");
                    out.println("</br>");
                    out.println("<center><h4> Found " + totalSLOCAUsers + " users! </h4></center>");
                    out.println("</br>");
                    out.println("<center><table id='result' border=2>");
                    out.println("<tr>");
                    out.println("<th>" + firstOrder.toUpperCase() + "</th>");
                    out.println("<th> No. of Matches </th>");
                    out.println("<th> % </th>");
                    out.println("<th>" + secondOrder.toUpperCase() + "</th>");
                    out.println("<th> No. of Matches </th>");
                    out.println("<th> % </th>");
                    out.println("<th>" + thirdOrder.toUpperCase() + "</th>");
                    out.println("<th> No. of Matches </th>");
                    out.println("<th> % </th>");
                    out.println("</tr>");

                    Set<String> firstSet = firstOrderResult.keySet();
                    for (String key : firstSet) {
                        double count = firstOrderResult.get(key);
                        double percentage = count / totalSLOCAUsers * 100;
                        //Printing of firstOrder's attributes 
                        out.println("<tr>");
                        out.println("<td>" + key + "</td>");
                        out.println("<td>" + (int) count + "</td>");
                        out.println("<td>  " + (int) Math.round(percentage) + "% </td>");

                        TreeMap<String, Integer> secondChoiceHm = secondOrderResult.get(key); //<School, <year, count>>

                        Set<String> secondChoiceSet = secondChoiceHm.keySet(); // list of years OF school 

                        //Printing of Second Order attributes 
                        out.println("<td><table class='table table-striped' height=' " + tableHeight + "px'>");
                        for (String secondChoiceKey : secondChoiceSet) { //year
                            out.println("<tr><td>" + secondChoiceKey + "</td></tr>");
                        }
                        out.println("</table></td></height>");

                        //Printing of Second Order's count
                        out.println("<td><table class='table table-striped' height=' " + tableHeight + "px'>");
                        for (String secondChoiceKey : secondChoiceSet) { //year
                            int innerCount = secondChoiceHm.get(secondChoiceKey);
                            out.println("<tr><td>" + innerCount + "</td></tr>");
                        }
                        out.println("</table></td></height>");

                        //Printing of Second Order's Percentage
                        out.println("<td><table class='table table-striped' height=' " + tableHeight + "px'>");
                        for (String secondChoiceKey : secondChoiceSet) { //year
                            double innerPercentage = (double) secondChoiceHm.get(secondChoiceKey) / totalSLOCAUsers * 100;
                            out.println("<tr><td>" + (int) Math.round(innerPercentage) + "%</td></tr>");
                        }
                        out.println("</table></td></height></vertical-align>");

                        Set<String> thirdChoiceSet = thirdOrderResult.keySet(); //<"school year", <gender, count>>

                        for (String k : thirdChoiceSet) {
                            System.out.println("third choice key :" + k);
                        }

                        //Print of third Order's attributes
                        out.println("<td><table class='table table-striped' height=' " + tableHeight + "px'>");
                        for (String secondChoiceKey : secondChoiceSet) {
                            String keyToCheck = key + " " + secondChoiceKey;
                            for (String thirdChoiceKey : thirdChoiceSet) {
                                if (keyToCheck.equals(thirdChoiceKey)) {
                                    TreeMap<String, Integer> innerTreeMap = thirdOrderResult.get(keyToCheck);
                                    Set<String> innerKeys = innerTreeMap.keySet();
                                    for (String innerKey : innerKeys) {
                                        out.println("<tr><td>" + innerKey + "</td></tr>");
                                    }
                                }
                            }
                        }
                        out.println("</table></td>");

                        //Print third order's count
                        out.println("<td><table class='table table-striped' height=' " + tableHeight + "px'>");
                        for (String secondChoiceKey : secondChoiceSet) {
                            String keyToCheck = key + " " + secondChoiceKey;

                            for (String thirdChoiceKey : thirdChoiceSet) {
                                if (keyToCheck.equals(thirdChoiceKey)) {
                                    TreeMap<String, Integer> innerTreeMap = thirdOrderResult.get(keyToCheck);
                                    Set<String> innerKeys = innerTreeMap.keySet();
                                    for (String innerKey : innerKeys) {
                                        out.println("<tr><td>" + innerTreeMap.get(innerKey) + "</td></tr>");
                                    }
                                }
                            }
                        }
                        out.println("</table></td>");

                        //Print third order's percentage
                        out.println("<td><table class='table table-striped' height=' " + tableHeight + "px'>");
                        for (String secondChoiceKey : secondChoiceSet) {
                            String keyToCheck = key + " " + secondChoiceKey;
                            TreeMap<String, Integer> secondOrderValue = secondOrderResult.get(key);

                            for (String thirdChoiceKey : thirdChoiceSet) {
                                if (keyToCheck.equals(thirdChoiceKey)) {
                                    TreeMap<String, Integer> innerTreeMap = thirdOrderResult.get(keyToCheck); //<String (school year), <gender, count>>
                                    Set<String> innerKeys = innerTreeMap.keySet();
                                    for (String innerKey : innerKeys) {
                                        double innerPercentage = (double) innerTreeMap.get(innerKey) / totalSLOCAUsers * 100;
                                        out.println("<tr><td>" + (int) Math.round(innerPercentage) + "%</td></tr>");
                                    }
                                }
                            }
                        }
                        out.println("</table></td>");

                        out.println("</tr>");
                    }
                    out.println("</table></center></br>");

                } else if (firstOrderResult != null && secondOrderResult != null && thirdOrderResult == null) {
                    String date = (String) request.getAttribute("date");
                    String time = (String) request.getAttribute("time");
                    int totalSLOCAUsers = (int) request.getAttribute("totalSLOCAUsers");
                    out.println("<center><h3>Showing results for</h3></center>");
                    out.println("</br>");
                    out.print("<center><h4> <b>Date:</b> " + date + ", <b>Time:</b> " + time);
                    out.println(", <b>First Choice:</b> " + firstOrder + ", <b>Second Choice:</b> " + secondOrder + "<h4></center>");
                    out.println("</br>");
                    out.println("<center><h4> Found " + totalSLOCAUsers + " users! </h4></center>");
                    out.println("</br>");
                    out.println("<center><table border=2>");
                    out.println("<tr>");
                    out.println("<th>" + firstOrder.toUpperCase() + "</th>");
                    out.println("<th> No. of Matches </th>");
                    out.println("<th> % </th>");
                    out.println("<th>" + secondOrder.toUpperCase() + "</th>");
                    out.println("<th> No. of Matches </th>");
                    out.println("<th> % </th>");
                    out.println("</tr>");

                    Set<String> firstSet = firstOrderResult.keySet();
                    for (String key : firstSet) {
                        double count = firstOrderResult.get(key);
                        double percentage = count / totalSLOCAUsers * 100;

                        //Printing of firstOrder's attributes 
                        out.println("<tr>");
                        out.println("<td>" + key + "</td>");
                        out.println("<td>" + (int) count + "</td>");
                        out.println("<td>  " + (int) Math.round(percentage) + "% </td>");

                        TreeMap<String, Integer> secondChoiceTm = secondOrderResult.get(key);

                        Set<String> secondChoiceSet = secondChoiceTm.keySet();

                        //Printing of SecondsecondChoiceSet Order attributes 
                        out.println("<td><table class='table table-striped'>");
                        for (String secondChoiceKey : secondChoiceSet) { //year
                            out.println("<tr><td>" + secondChoiceKey + "</td></tr>");
                        }
                        out.println("</table></td>");

                        //Printing of Second Order's count
                        out.println("<td><table class='table table-striped'><height='99%'>");
                        for (String secondChoiceKey : secondChoiceSet) { //year
                            int innerCount = secondChoiceTm.get(secondChoiceKey);
                            out.println("<tr><td>" + innerCount + "</td></tr>");
                        }
                        out.println("</table></td></height>");

                        //Printing of Second Order's Percentage
                        out.println("<td><table class='table table-striped'>");
                        for (String secondChoiceKey : secondChoiceSet) { //year
                            double innerPercentage = (double) secondChoiceTm.get(secondChoiceKey) / totalSLOCAUsers * 100;
                            out.println("<tr><td>" + (int) Math.round(innerPercentage) + "%</td></tr>");
                        }
                        out.println("</table></td>");
                        out.println("</tr>");

                    }
                    out.println("</table></center></br>");

                } else if (firstOrderResult != null && secondOrderResult == null && thirdOrderResult == null) {
                    int totalSLOCAUsers = (int) request.getAttribute("totalSLOCAUsers");
                    String date = (String) request.getAttribute("date");
                    String time = (String) request.getAttribute("time");
                    firstOrder.toUpperCase();
                    out.println("<center><h3>Showing results for</h3></center>");
                    out.println("</br>");
                    out.print("<center><h4> <b>Date:</b> " + date + ", <b>Time:</b> " + time);
                    out.println(", <b>First Choice:</b> " + firstOrder + "<h4></center>");
                    out.println("</br>");
                    out.println("<center><h4> Found " + totalSLOCAUsers + " users! </h4></center>");
                    out.println("</br>");
                    out.println("<center><table border=2>");
                    out.println("<tr>");
                    out.println("<th>" + firstOrder.toUpperCase() + "</th>");
                    out.println("<th> No. of Matches </th>");
                    out.println("<th> % </th>");
                    out.println("</tr>");

                    Set<String> firstChoiceSet = firstOrderResult.keySet();
                    for (String key : firstChoiceSet) {
                        String firstChoice = key;
                        int count = firstOrderResult.get(key);
                        double percentage = (double) count / totalSLOCAUsers * 100;
                        out.println("<tr>");
                        out.println("<td>" + firstChoice + "</td>"); //type of school
                        out.println("<td>" + count + "</td>");
                        out.println("<td>" + (int) Math.round(percentage) + "</td>");
                        out.println("</tr>");
                    }
                    out.println("</table></center></br>");
                }
            } else {
                out.println("<center><h3>" + errorMessage + "</h3></center>");
            }
        %>
    </body>
</html>


