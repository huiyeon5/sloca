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
            div#slider{
                background-color:grey;
            }

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
        </style>
    </head>
    <body>


        <!--Navigation Bar (Start)-->
        <%@include file="navbar_admin.jsp" %>
        <!--Navigation Bar (End)-->



        <div clas<!--Slider Row (Start)-->
             <div class="container-fluid" id="slider">
                <div id="carouselExampleIndicators" class="carousel slide" data-ride="carousel">
                    <ol class="carousel-indicators">
                        <li data-target="#carouselExampleIndicators" data-slide-to="0" class="active"></li>
                        <li data-target="#carouselExampleIndicators" data-slide-to="1"></li>
                        <li data-target="#carouselExampleIndicators" data-slide-to="2"></li>
                    </ol>
                    <div class="carousel-inner">
                        <div class="carousel-item active">
                            <img class="d-block w-100" src="img/photo_2017-09-07_17-27-00.jpg" height="500px" alt="First slide">
                            <div class="carousel-caption d-none d-md-block">
                                <h3>ADMIN</h3>
                                <p>... ...</p>
                            </div>
                        </div>
                        <div class="carousel-item">
                            <img class="d-block w-100" src="img/photo_2017-09-07_17-27-00.jpg" height="500px" alt="Second slide">
                            <div class="carousel-caption d-none d-md-block">
                                <h3>ADMIN</h3>
                                <p>... ...</p>
                            </div>
                        </div>
                        <div class="carousel-item">
                            <img class="d-block w-100" src="img/photo_2017-09-07_17-27-00.jpg" height="500px" alt="Third slide">
                            <div class="carousel-caption d-none d-md-block">
                                <h3>ADMIN</h3>
                                <p>... ...</p>
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

            <!--Banner Row(Start) -->
            <div class="container-fluid" id="banner">
                <h1>Admin Function</h1>
            </div>
            <!--Banner Row(End) -->

            <!-- Main Row(Start) -->
            <div class="container-fluid" id="main">
                <div class="container">
                    <div class="row">
                        <div class="col">
                            <br>
                            <center><b>BOOTSTRAP DATABASE</b></center>
                            <br>
                            <a href="bootstrap.jsp">
                                <img src="img/bootstrap.png">
                            </a>
                            <br></br>

                            <center><b>UPDATE DATABASE</b></center> 
                            <br>
                            <a href="update.jsp">
                                <img src="img/update.png">
                            </a>
                        </div>
                    </div>
                </div>
            </div>
            <!-- Main Row -->
    </body>
</html>
