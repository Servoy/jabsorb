<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
 "http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="com.metaparadigm.jsonrpc.JSONRPCBridge" %>
<%@ page import="com.metaparadigm.jsonrpc.test.Unicode" %>
<jsp:useBean id="JSONRPCBridge" scope="session"
     class="com.metaparadigm.jsonrpc.JSONRPCBridge" />
<jsp:useBean id="unicode" scope="session"
     class="com.metaparadigm.jsonrpc.test.Unicode" />
<%
   response.setDateHeader ("Expires", 0);
   //JSONRPCBridge.setDebug(true);
   JSONRPCBridge.registerObject("unicode", unicode);
%>
<html>
  <head>
    <link rel="stylesheet" type="text/css" href="css/site.css">
    <link rel="stylesheet" type="text/css" href="css/unicode.css">
    <script type="text/javascript" src="jsonrpc.js"></script>
    <script type="text/javascript" src="unicode.js"></script>
    <title>JSON-RPC-Java Unicode Tests</title>
   </head>
   <body bgcolor="#ffffff" onLoad="onLoad()">

    <h1><img align="left" src="images/json.png" width="55" height="55" hspace="6" vspace="0" alt="JSON logo"/>JSON-RPC-Java</h1>
    <div class="tagline">JavaScript to Java remote communication.</div>
    <hr />
    <div class="menu"><a href="index.html">Home</a> | <a href="tutorial.html">Tutorial</a> | <a href="manual.html">Manual</a> | <a href="demos.html">Demos</a> | <a href="docs/">API Documentation</a> | <a href="http://oss.metaparadigm.com/mailman/listinfo/json-rpc-java">Mailing List</a> | <a href="CHANGES.txt">Changelog</a></div>

    <h2>JSON-RPC-Java Unicode Tests</h2>

    <p>The tests run automatically when you load the page. It may take a second or so to run the tests and display the table.</p>

    <h3>Description of table fields</h3>
    <ul>
      <li><em>Description</em> - what character set should be expected.</li>
      <li><em>Recieve</em> - unicode data recieved from the server.</li>
      <li><em>Echo Compare</em> - recieved data send back and compared with the server-side data.</li>
      <li><em>Pass</em> - server-side compare result.</li>
    </ul>

    <table class="test_table">
      <thead>
       <tr>
        <th class="test_th" width="150"><div class="desc_heading">Description</div></th>
        <th class="test_th" width="50%"><div class="recv_heading">Recieve</div></th>
        <th class="test_th" width="50%"><div class="echo_heading">Echo Compare</div></th>
        <th class="test_th" width="32"><div class="pass_heading">Pass</div></th>
       </tr>
      </thead>
      <tbody id="tests"></tbody>
    </table>

    <br>
    <hr>
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
	<td><code>$Id: unicode.jsp,v 1.3 2005/02/13 01:26:47 mclark Exp $</code></td>
	<td><div class="copyright">Copyright 2005 <a href="http://www.metaparadigm.com/">Metaparadigm Pte Ltd</a></div></td>
      </tr>
    </table>
  </body>
</html>
