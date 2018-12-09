<%-- 
    Document   : about
    Created on : Sep 24, 2017, 2:30:00 PM
    Author     : Jia Xian and Keith
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <link href="https://fonts.googleapis.com/css?family=PT+Sans" rel="stylesheet">
    <style>
        h1, h2{
            color: skyblue;
            font-family: 'PT Sans', sans-serif;

        }
        body{
            font-family: 'PT Sans', sans-serif;
            text-align: center;
        }
        tr{
            text-align: left;
        }
        div#background{
            background-image: url(img/about-bg.jpeg);
            background-size: 100% 100%;
        }
        div#transbox{
            background-color: #efd0b4;
            opacity: 0.75;
        }
        div#transbox, h1, h2, h4, p, td{
            color: black;
            font-weight: bold;
        }
    </style>
    <head>
        <meta http-equiv="Content-Type" content="text/; charset=UTF-8">
        <!-- Import external stylesheet (Start)-->
        <link rel="stylesheet"  type="text/css" href="css/bootstrap.min.css">
        <!-- Import external stylesheet (End)-->
        <title>About</title>
    </head>
    <body>
        <!--Navigation Bar (Start)-->
        <%@include file="navbar.jsp" %>
        <!--Navigation Bar (End)-->

        <div id="background" class="container-fluid">
            <div id="transbox" class="container">
                <h1><center>WHAT IS SLOCA</center></h1>
                <br/>
                <p>SLOCA (SMU LOCation Analytics Service) is an web application that can 
                    be used by any valid user to obtain
                    <br/>
                    diverse statistics of the locations
                    of people inside the SIS building.
                </p>
                <h4>SLOCA provides three functions: <br/>
                    Heatmap, Location Report and Group Identification.
                </h4>


                <h2>HEATMAP</h2>
                <p>Check out which rooms in SIS building is crowded so that you can find place to study or work on your projects. With Heatmap, it shows</p>
                <p>you the crowd density of a specific floor in SIS building given a particular date and time.</p> 
                <br/>
                <h2>LOCATION REPORT</h2>
                <p>It will allow users to view four different types of basic location reports on a given date and time.</p>
                <p>Users can produce report on:</p>
                <br/>

                <table align="center" border="0">
                    <tr>
                        <td>1. Users of a specific demographic in your specified location</td> 
                    </tr>
                    <tr>
                        <td>2. Popular places used by students in SIS building</td>
                    </tr>
                    <tr>
                        <td>3. Top users spending the most time together in SIS building</td>
                    </tr>
                    <tr>
                        <td>4. Popular next location from a specified location by users</td>
                    </tr>
                </table>

                <br>

                <h2>GROUP IDENTIFICATION</h2>

                <br/>

                <p>This function helps to detect groups of users (at least 2) who spends time together in SIS building.</p>

                <br><br>
            </div>
        </div>
    </body>
</html>
