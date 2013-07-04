<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Display Content</title>
</head>
<%
	request.setCharacterEncoding("UTF-8"); 
	String title = request.getParameter("title");
	String content = request.getParameter("content");
%>
<center>
<table width="600" border="0" bordercolor="000000"
	style="table-layout: fixed;">
	<tr>
		<td width="100" bordercolor="ffffff">主题:</td>
		<td width="500" bordercolor="ffffff"><%=title %></td>
	</tr>
	<tr>
		<td valign="top" bordercolor="ffffff">内容:</td>
		<td valign="top" bordercolor="ffffff"><%=content %></td>
	</tr>
</table>
</center>
</html>