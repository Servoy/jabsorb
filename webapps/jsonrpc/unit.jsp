<%@page contentType="text/html; charset=UTF-8" %>
<%@page language="java" %>
<%@page import="org.slf4j.LoggerFactory" %>
<jsp:useBean id="JSONRPCBridge" scope="session"
     class="org.jabsorb.JSONRPCBridge"
/>
<jsp:useBean id="testObject" scope="session"
     class="org.jabsorb.test.Test"
/>
<%
   response.setDateHeader ("Expires", 0);
   JSONRPCBridge.registerObject("test", testObject);
   JSONRPCBridge.registerReference(org.jabsorb.test.Test.RefTest.class);
   JSONRPCBridge.registerCallableReference(org.jabsorb.test.Test.CallableRefTest.class);
%>
<%!
  String title = "Unit Tests";
  String head =
    "    <link rel=\"stylesheet\" type=\"text/css\" href=\"css/unit.css\">\n" +
    "    <script type=\"text/javascript\" src=\"jsonrpc.js\"></script>\n" +
    "    <script type=\"text/javascript\" src=\"unitTests.js\"></script>\n" +    
    "    <script type=\"text/javascript\" src=\"unit.js\"></script>\n";
  String onLoad = "onLoad()";
%>
<%@ include file="header.jspinc" %>

    <h2><%=appName%> <%=title%></h2>

    <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
        <td align="left">
          <input type="button" value="Run All Tests" onclick="runAllTests()" />
        </td>
        <td align="left">
          <input type="button" value="Clear All Results" onclick="clearAllResults()" />
        </td>
        <td align="right">
          <div>(
          <input type="checkbox" id="showSuccesses" checked="1" />
          Show successes  |
          <input type="checkbox" id="hideUnrun"  />
          Hide unrun  |
          <input type="checkbox" id="profile" />
          Profile  |
          <input type="checkbox" id="async" checked />
          Asynchronous
          )
          </div>
        </td>
      </tr>
      <tr>
        <td align="left">
          <input type="button" value="Expand All" onclick="expandAllResults()" />
        </td>
        <td align="left" >
          <input type="button" value="Collapse All" onclick="collapseAllResults()" />
        </td>
        <td style="text-align:right">
          Max parallel async requests
          <input type="text" id="max_requests" value="8" size="2" />
        </td> 
      </tr>
    </table>

    <p></p>

    <div id="results"></div>

<%@ include file="footer.jspinc" %>
