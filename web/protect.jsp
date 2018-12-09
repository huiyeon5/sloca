<%-- 
    Document   : protect.jsp
    Created on : Sep 23, 2017, 4:11:59 PM
    Author     : Huiyeon Kim
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
    if(session.getAttribute("username")==null){
        response.sendRedirect("login.jsp");
    }else{
        String name = (String)session.getAttribute("username");
        if(name.equals("admin")){
            response.sendRedirect("login.jsp");
        }
    }
%>
