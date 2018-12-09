<%-- 
    Document   : BootStrap_UI
    Created on : 28 Sep, 2017, 5:00:00 PM
    Author     : Jia Xian and Keith 
--%>

<%@page import="java.util.Collections"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="gnu.trove.map.hash.THashMap"%>
<%@page import="java.util.ArrayList"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!--protect from viewing pages without logging in (CREATE ProtectAdmin)-->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>BootStrap Database</title>
    </head>
    <body> 
        <!--Navigation Bar (Start)-->
        <%@include file="navbar_admin.jsp" %>
        <!--Navigation Bar (End)-->

        <script>
            var validFileExtension = ".zip";
            function Validate(oForm) {
                var arrInputs = oForm.getElementsByTagName("input");
                for (var i = 0; i < arrInputs.length; i++) {
                    var oInput = arrInputs[i];
                    if (oInput.type == "file") {
                        var sFileName = oInput.value;
                        if (sFileName.length > 0) {
                            var blnValid = false;
                            if (sFileName.substr(-validFileExtension.length).toLowerCase() == validFileExtension.toLowerCase()) {
                                blnValid = true;
                                break;
                            }

                            if (!blnValid) {
                                alert("Sorry! " + sFileName + " is invalid file format. Please upload " + validFileExtension + " file!");
                                return false;
                            }
                        }
                    }
                }

                return true;
            }
        </script>

        <form action="UploadServlet"  method="post" enctype="multipart/form-data" onsubmit="return Validate(this);">
            Filename:
            <input type="file" name="bootstrapFile" accept="application/zip,application/x-zip,application/x-zip-compressed,application/octet-stream"/>
            <br><br>
            <input type="submit" name="functionType" value="Bootstrap"/>  
        </form>

        <%  THashMap<Integer, ArrayList<String>> lookUpError = (THashMap<Integer, ArrayList<String>>) request.getAttribute("locError");
            THashMap<Integer, ArrayList<String>> locError = (THashMap<Integer, ArrayList<String>>) request.getAttribute("locDataError");
            THashMap<Integer, ArrayList<String>> demoError = (THashMap<Integer, ArrayList<String>>) request.getAttribute("demoError");
            int demoS = 0;
            int locS = 0;
            int locDataS = 0;
            if(request.getAttribute("demoS") != null){
                demoS = (Integer)request.getAttribute("demoS");
            }
            if(request.getAttribute("locS") != null){
                locS = (Integer)request.getAttribute("locS");
            }
            if(request.getAttribute("locDataS") != null){
                locDataS = (Integer)request.getAttribute("locDataS");
            }
            if (session.getAttribute("bootstrap") != null) {
                if ((lookUpError == null || lookUpError.size() == 0) && (locError == null || locError.size() == 0) && (demoError == null || demoError.size() == 0)) {
                    out.println("<h3>We have successfully uploaded ALL the data in the CSV Files!</h3>");
                    if(demoS!=0){
                        out.println("<h4>Number of Demographics uploaded: "+demoS+"</h4>");
                    }
                    if(locS!=0){
                        out.println("<h4>Number of Location-lookup uploaded: "+locS+"</h4>");
                    }
                    if(locDataS!=0){
                        out.println("<h4>Number of Location uploaded: "+locDataS+"</h4></br>");
                    }
                } else {
                    out.println("<h3>We were able to upload the data but some of the records did not pass our validation! They are listed below: </h3></br>");
                    if(demoS!=0){
                        out.println("<h4>Number of Demographics uploaded: "+demoS+"</h4>");
                    }
                    if(locS!=0){
                        out.println("<h4>Number of Location-lookup uploaded: "+locS+"</h4>");
                    }
                    if(locDataS!=0){
                        out.println("<h4>Number of Location uploaded: "+locDataS+"</h4></br>");
                    }
                    if (demoError != null && demoError.size() != 0) {
                        out.println("<h2> Demographics.csv has "+ demoError.size() +" errors!</h2><br/>");
                        out.println("<table>");
                        out.println("<tr><th>Line Number</th><th>Error Messages</th></tr>");

                        Set<Integer> demoSet = demoError.keySet();
                        List<Integer> demoList = new ArrayList<>(demoSet);
                        Collections.sort(demoList);

                        for (int line : demoList) {
                            out.println("<tr><td>" + line + "</td>");
                            out.println("<td>");
                            String output = "";
                            for (String msg : demoError.get(line)) {
                                output += "\"" + msg + "\",";
                            }
                            out.println(output.substring(0, output.length() - 1) + "</td></tr>");
                        }
                        out.println("</table>");
                    }
                    if (lookUpError != null && lookUpError.size() != 0) {
                        out.println("<h2> Location-LookUp.csv has "+lookUpError.size()+" errors!</h2><br/>");
                        out.println("<table>");
                        out.println("<tr><th>Line Number</th><th>Error Messages</th></tr>");
                        Set<Integer> lookUpSet = lookUpError.keySet();
                        List<Integer> lookUpList = new ArrayList<>(lookUpSet);
                        Collections.sort(lookUpList);
                        for (int line : lookUpList) {
                            out.println("<tr><td>" + line + "</td>");
                            out.println("<td>");
                            String output = "";
                            for (String msg : lookUpError.get(line)) {
                                output += "\"" + msg + "\",";
                            }
                            out.println(output.substring(0, output.length() - 1) + "</td></tr>");
                        }
                        out.println("</table>");
                    }
                    if (locError != null && locError.size() != 0) {
                        out.println("<h2> Location.csv has "+ locError.size() +" errors!</h2><br/>");
                        out.println("<table>");
                        out.println("<tr><th>Line Number</th><th>Error Messages</th></tr>");
                        Set<Integer> locSet = locError.keySet();
                        List<Integer> locList = new ArrayList<>(locSet);
                        Collections.sort(locList);

                        for (int line : locList) {
                            out.println("<tr><td>" + line + "</td>");
                            out.println("<td>");
                            String output = "";
                            for (String msg : locError.get(line)) {
                                output += "\"" + msg + "\",";
                            }
                            out.println(output.substring(0, output.length() - 1) + "</td></tr>");
                        }
                        out.println("</table>");
                    }
                    
                }
                session.setAttribute("bootstrap", null);
            } else {
                String fileError = (String) request.getAttribute("files");
                if (fileError != null) {
                    out.println("<h3>" + fileError + "</h3>");
                }
            }
        %>
    </body>
</html>
