<%-- 
    Document   : navbar
    Created on : Sep 24, 2017, 2:30:00 PM
    Author     : Jia Xian and Keith
--%>

<!DOCTYPE html>
<!-- Import external stylesheet (Start)-->
<link rel="stylesheet"  type="text/css" href="css/bootstrap.css">
<!-- Import external stylesheet (End)-->

<script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.11.0/umd/popper.min.js" integrity="sha384-b/U6ypiBEHpOf/4+1nzFpr53nxSS+GLCkfwBdFNTxtclqqenISfwAzpKaMNFNmj4" crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/js/bootstrap.min.js" integrity="sha384-h0AbiXch4ZDo7tp9hKZ4TsHbi047NrKGLO3SEJAg45jXxnGIfYzk4Si90RDIqNm1" crossorigin="anonymous"></script>

<!-- Import Datepicker (Start) -->
<script src="https://code.jquery.com/jquery-1.12.4.js"></script>
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
<!-- Import Datepicker (End) -->

<!-- Import Timepicker (Start) -->
<script src="//cdnjs.cloudflare.com/ajax/libs/timepicker/1.3.5/jquery.timepicker.min.js"></script>
<!-- Import Timepicker (End) -->

<!--protect from viewing pages without logging in-->
<%@include file="protect.jsp"%>


<style>
    body{
        position: relative;
        margin: 0;
        padding-bottom: 6rem;
        min-height: 100%;
    }
    #center {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translateX(-50%) translateY(-50%);
    }
    .container, .main{
        padding-bottom: 100px;
    }
</style>


<!--Navigation Bar (Start)-->
<!-- ------------------------------------------------------------------------------------------ -->
<nav class="navbar navbar-expand-lg navbar-light bg-light">
    <a class="navbar-brand" href="index.jsp">
        <img src="img/SLOCA-Logo.png" width="140" height="60" alt="SLOCA-Logo">
    </a>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNavDropdown">
        <ul class="navbar-nav">
            <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="http://example.com" id="navbarDropdownMenuLink" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    Functions
                </a>
                <div class="dropdown-menu" aria-labelledby="navbarDropdownMenuLink">
                    <a class="dropdown-item" href="heatmap.jsp">Heatmap</a>
                    <div class="dropdown-divider"></div>
                    <a class="dropdown-item" href="agd.jsp">Automatic Group Detection</a>
                    <div class="dropdown-divider"></div>
                    <a class="dropdown-item" href="byg.jsp">Breakdown by Year and Gender</a>
                    <a class="dropdown-item" href="topkpopular.jsp">Top-k Popular Places</a>
                    <a class="dropdown-item" href="topkcompanions.jsp">Top-k Companions</a>
                    <a class="dropdown-item" href="nextplace.jsp">Top-k Next Places</a>
                </div>
            </li>


            <!--
            Logic required! 
            Goal: Send user to profile page.
            1) Retrieve name from session
            2) display as <a>.
            -->
            <div id="center">
                <a class="text-secondary" href="#">
                    <%                        String name = (String) session.getAttribute("username");
                        if (name != null) {
                            out.println(name);
                        }
                    %>
                </a>

            </div>
        </ul>
    </div>
    <div class ="navbar-nav">
        <a class="nav-link" href="about.jsp">About SLOCA</a>
    </div>
    <div class ="navbar-nav">
        <a class="nav-link" href="login.jsp?logout=true">Logout</a>
    </div>

</nav>
<!--Navigation Bar (End)-->

