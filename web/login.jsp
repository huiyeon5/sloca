<%-- 
    Document   : login
    Created on : 10 Sep, 2017, 7:12:16 PM
    Author     : Iceorada
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="controller.LoginServlet"%>
<!DOCTYPE html>

<style>
    body, html {
        height: 100%;
        margin: 0;
    }
    .bg {
        background-image: url("img/pexels-photo.jpg");
        background-color: wheat;
        height: 100%; 
        background-position: center;
        background-repeat: no-repeat;
        background-size: cover;
        -webkit-filter: blur(1px);
        -moz-filter: blur(1px);
        -o-filter: blur(1px);
        -ms-filter: blur(1px);
        filter: blur(1px);
    }
    div#login{
        border-radius: 25px;
        border: 1px solid white;
        padding: 20px; 
        width: 400px;
        height: 430px;
        display: flex;
        flex-wrap: wrap;
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translateX(-50%) translateY(-50%);
        color: white;
    }
    div#title{
        width:100%;
        height:10%;
        text-align: center;
    }
    .full-width-div {
        position: relative;
        width: 100%;
        left: 0;
    }
    form#loginForm th{
        text-align: center;
        color: black;
    }
    form#loginForm td{
        text-align: center;
    }


</style>
<!-- Import external stylesheet (Start)-->
<link rel="stylesheet"  type="text/css" href="css/bootstrap.min.css">
<!-- Import external stylesheet (End)-->
<script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.11.0/umd/popper.min.js" integrity="sha384-b/U6ypiBEHpOf/4+1nzFpr53nxSS+GLCkfwBdFNTxtclqqenISfwAzpKaMNFNmj4" crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/js/bootstrap.min.js" integrity="sha384-h0AbiXch4ZDo7tp9hKZ4TsHbi047NrKGLO3SEJAg45jXxnGIfYzk4Si90RDIqNm1" crossorigin="anonymous"></script>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login</title>
    </head>
    <body>
        <div class="bg"></div>
        <div id="login">
            <div id="title">
                <img src="img/SLOCA-Logo.png" height="120px" width="300px" class="img img-responsive">
            </div>
            <div class="full-width-div ">
                <form action="LoginServlet" method="post" id="loginForm">
                    <table class="full-width-div">
                        <tr>                   
                            <th><label for="username">Username:</label></th>
                        </tr>
                        <tr>
                            <td><input type="text" name="username" class="form-control" id="exampleDropdownFormEmail2" placeholder="Username" /></td>
                        </tr>
                        <tr>
                            <th><label for="password">Password:</label></th>
                        </tr>
                        <tr>
                            <td><input type="password" name="password" class="form-control" id="exampleDropdownFormEmail2" placeholder="Password"/></td>
                        </tr>
                        <tr>
                            <td><input type="Submit" value="Log In" class="btn btn-primary"/></td>
                        </tr>
                        <tr>
                            <td>
                                <%
                                    String errorMsg = (String) request.getAttribute("error");
                                    if (errorMsg != null) {
                                        out.println("<strong style='color:red'>" + errorMsg + "</strong>");
                                    }
                                    String logout = request.getParameter("logout");
                                    if (logout != null) {
                                        session.invalidate();
                                    }
                                %>
                            </td>
                        </tr>
                    </table>
                </form>
            </div>
        </div>
    </body>
</html>
