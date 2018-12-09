<%-- 
    Document   : index 
    Created on : Sep 24, 2017, 2:30:00 PM
    Author     : Jia Xian and Keith
--%>

<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->
<html>
    <head>
        <title>SLOCA</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">


        <style>
            div#banner h1{
                text-align: center;
            }
            div#banner{
                text-align: center;
                border: dotted black;
            }
            div#main img{
                height: 200px; 
                width: 200px;
                display: block;
                margin-left: auto;
                margin-right: auto
            }
            .container{
                background-color: wheat;
            }
            div.height20{
                height: 20px;
            }
            div#main div#middlebtn,h4{
                text-align: center;
            }
            div#middlebtn button{
                border: none;
                background: none;
            }
            div#main button:hover {
                cursor: pointer;
                opacity: 0.5;
                color: white;
            }   
            div#main a:hover {
                cursor: pointer;
                opacity: 0.5;
                background: white;
            }
            .fixed200{
                width: 200px;
                height: 200px;
                #footer{
                    background-color: white;
                }
            }
        </style>
    </head>
    <body>


        <!--Navigation Bar (Start)-->
        <%@include file="navbar.jsp" %>
        <!--Navigation Bar (End)-->

        <!-- Retrieve user's name (Start) -->
        <%            String username = (String) session.getAttribute("username");
            String display = "";

            if (username != null) {
                if (username.equals("admin")) {
                    session.invalidate();
                } else {
                    int firstDot = username.indexOf('.');
                    display = username.substring(0, firstDot);
                }
            }
        %>
        <!-- Retrieve user's name (End) -->

        <div class="container">
            <div class="height20"></div>
            <!--Slider Row (Start)-->
            <div class="container-fluid" id="slider">
                <div id="carouselExampleIndicators" class="carousel slide" data-ride="carousel">
                    <ol class="carousel-indicators">
                        <li data-target="#carouselExampleIndicators" data-slide-to="0" class="active"></li>
                        <li data-target="#carouselExampleIndicators" data-slide-to="1"></li>
                        <li data-target="#carouselExampleIndicators" data-slide-to="2"></li>
                    </ol>
                    <div class="carousel-inner">
                        <div class="carousel-item active">
                            <img class="d-block w-100" src="img/HaveAGreatTime.png" height="500px" width="700px" alt="First slide">
                            <div class="carousel-caption d-none d-md-block">
                                <h3>HELLO, <%=display.toUpperCase()%></h3>
                                <p>SLOCA</p>
                            </div>
                        </div>
                        <div class="carousel-item">
                            <img class="d-block w-100" src="img/SunsetDJSession.png" height="500px" width="700px" alt="Second slide">
                            <div class="carousel-caption d-none d-md-block">
                                <h3>HELLO, <%=display.toUpperCase()%></h3>
                                <p>SLOCA</p>
                            </div>
                        </div>
                        <div class="carousel-item">
                            <img class="d-block w-100" src="img/slider_user_1.jpeg" height="500px" width="700px" alt="Third slide">
                            <div class="carousel-caption d-none d-md-block">
                                <h3>HELLO, <%=display.toUpperCase()%></h3>
                                <p>SLOCA</p>
                            </div>
                        </div>
                    </div>
                    <a class="carousel-control-prev" href="#carouselExampleIndicators" role="button" data-slide="prev">
                        <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                        <span class="sr-only">Previous</span>
                    </a>
                    <a class="carousel-control-next" href="#carouselExampleIndicators" role="button" data-slide="next">
                        <span class="carousel-control-next-icon" aria-hidden="true"></span>
                        <span class="sr-only">Next</span>
                    </a>
                </div>
            </div>
            <!--Slider Row (End)-->

            <div class="height20"></div>

            <!--Banner Row(Start) -->
            <div class="container-fluid" id="banner">
                <h1>Our Services</h1>
            </div>
            <!--Banner Row(End) -->

            <div class="height20"></div>

            <!-- Main Row(Start) -->
            <div class="container-fluid" id="main">
                <div class="container">
                    <div class="row">
                        <div class="col">
                            <a href="heatmap.jsp" id="heatmaplink">
                                <img src="img/heat-map.png">
                            </a>
                            <h4>HEATMAP</h4>
                        </div>
                        <div class="col" id="middlebtn">
                            <button type="button" data-toggle="modal" data-target="#exampleModal">
                                <img src="img/location.png">
                            </button>
                            <h4>LOCATION</h4>
                        </div>
                        <div class="col">
                            <a href="agd.jsp" id="groupdetectionlink">
                                <img src="img/group-id.png">
                            </a>
                            <h4>GROUP DETECTION</h4>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!-- Main Row (End) -->
        <!-- Modal -->
        <div class="modal fade" id="exampleModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="exampleModalLabel">Basic Location Report</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <p><a href="byg.jsp">Breakdown by Year and Gender</a></p>
                        <p><a href="topkpopular.jsp">Top-k Popular Places</a></p>
                        <p><a href="topkcompanions.jsp">Top-k Companions</a></p>
                        <p><a href="nextplace.jsp">Top-k Next Places</a></p>
                    </div>
                </div>
            </div>
        </div>
        <!--Footer Bar (Start)-->
        <%@include file="footer.jsp" %>
        <!--Footer Bar (End)-->
    </body>
</html>
