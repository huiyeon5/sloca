<!-- 
    Document   : about
    Created on : Sep 24, 2017, 2:30:00 PM
    Author     : Jia Xian and Keith
-->

<!DOCTYPE html>
<!-- Import external stylesheet (Start)-->
<link rel="stylesheet"  type="text/css" href="css/bootstrap.min.css">
<!-- Import external stylesheet (End)-->
<script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.11.0/umd/popper.min.js" integrity="sha384-b/U6ypiBEHpOf/4+1nzFpr53nxSS+GLCkfwBdFNTxtclqqenISfwAzpKaMNFNmj4" crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/js/bootstrap.min.js" integrity="sha384-h0AbiXch4ZDo7tp9hKZ4TsHbi047NrKGLO3SEJAg45jXxnGIfYzk4Si90RDIqNm1" crossorigin="anonymous"></script>

<!--protect from viewing pages without logging in-->
<%@include file="protect_admin.jsp"%>


<style>
    body{
        min-height: 100%;
    }
    #center {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translateX(-50%) translateY(-50%);
    }
</style>


<!--Navigation Bar (Start)-->
<!-- ------------------------------------------------------------------------------------------ -->
<nav class="navbar navbar-expand-lg navbar-light bg-light">
    <a class="navbar-brand" href="indexAdmin.jsp">
        <img src="img/SLOCA-Logo.png" width="140" height="60" alt="SLOCA-Logo">
    </a>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNavDropdown">
        <ul class="navbar-nav">
            <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="http://example.com" id="navbarDropdownMenuLink" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    Admin Functions
                </a>
                <div class="dropdown-menu" aria-labelledby="navbarDropdownMenuLink">
                    <a class="dropdown-item" href="update.jsp">Update</a>
                    <a class="dropdown-item" href="bootstrap.jsp">Bootstrap</a>
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

                        //For testing
                        name = "Pei Shan";

                        if (name != null) {
                            out.println("<b>Admin</b> " + name);
                        }
                    %>
                </a>

            </div>
        </ul>
    </div>
    <div class ="navbar-nav">   
        <a class="nav-link" href="login.jsp?logout=true">Logout</a>
    </div>

</nav>
<!--Navigation Bar (End)-->

