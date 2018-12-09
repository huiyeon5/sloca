<%-- 
    Document   : protect_admin.jsp
    Created on : Nov 4, 2017, 5:52:44 PM
    Author     : Huiyeon Kim
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
    if(session.getAttribute("username")==null){
        response.sendRedirect("login.jsp");
    }else{
        String name = (String)session.getAttribute("username");
        if(!name.equals("admin")){
            response.sendRedirect("login.jsp");
        }
    }
%>
