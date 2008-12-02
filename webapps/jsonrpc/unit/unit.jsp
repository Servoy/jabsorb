<%@page contentType="text/html; charset=UTF-8" %>
<%@page language="java" %>
<%@page import="org.slf4j.LoggerFactory" %>
<%@page import="org.jabsorb.JSONRPCBridge" %>


<jsp:useBean id="JSONRPCBridge_Default" scope="session"
     class="org.jabsorb.JSONRPCBridge"
/>
<jsp:useBean id="JSONRPCBridge_CircRefs" scope="session"
     class="org.jabsorb.JSONRPCBridge"
/>
<jsp:useBean id="JSONRPCBridge_Flat" scope="session"
     class="org.jabsorb.JSONRPCBridge"
/>
<jsp:useBean id="testObject" scope="session"
     class="org.jabsorb.test.Test"
/>
<%
   response.setDateHeader ("Expires", 0);
   JSONRPCBridge bridges[] = 
     {JSONRPCBridge_Default, JSONRPCBridge_CircRefs, JSONRPCBridge_Flat};
   Class<?> states[]=
   {org.jabsorb.serializer.response.NoCircRefsOrDupes.class,
       org.jabsorb.serializer.response.fixups.FixupCircRefAndNonPrimitiveDupes.class,
       org.jabsorb.serializer.response.flat.FlatSerializerState.class
       };
   org.jabsorb.serializer.request.RequestParser[] requestParsers={
       new org.jabsorb.serializer.request.DefaultRequestParser(),
       new org.jabsorb.serializer.request.fixups.FixupsCircularReferenceHandler(),
       new org.jabsorb.serializer.request.flat.FlatRequestParser()
   };
   int i=0;
   for(JSONRPCBridge bridge: bridges)
   {
     bridge.setSerializerStateClass((Class<? extends org.jabsorb.serializer.SerializerState>)states[i]);
     bridge.getSerializer().setRequestParser(requestParsers[i]);
     bridge.registerObject("test", testObject);
	   bridge.registerReference(org.jabsorb.test.Test.RefTest.class);
	   bridge.registerCallableReference(org.jabsorb.test.Test.CallableRefTest.class);
	   bridge.registerCallableReference(org.jabsorb.test.ConstructorTest.class);
	   bridge.registerClass("ConstructorTest",org.jabsorb.test.ConstructorTest.class);
	   i++;
   }
%>
<%
  String title = "Unit Tests";
  String[][] jabsorbLibraries={
      {"jabsorb","JSON-RPC-Default"},
      {"jabsorb-flat","JSON-RPC-Flat"},
      {"jabsorb-circrefs","JSON-RPC"}};
  
  String head =
    "    <link rel=\"stylesheet\" type=\"text/css\" href=\"unit.css\">\n"+
    "    <script type=\"text/javascript\" src=\"utils.js\"></script>\n"+    
    "    <script type=\"text/javascript\" src=\"unitTests.js\"></script>\n" +    
    "    <script type=\"text/javascript\" src=\"TestVisibility.js\"></script>\n"+    
    "    <script type=\"text/javascript\" src=\"TestExpanding.js\"></script>\n"+    
    "    <script type=\"text/javascript\" src=\"ClearResults.js\"></script>\n"+    
    "    <script type=\"text/javascript\" src=\"ConstructorSelector.js\"></script>\n"+    
    "    <script type=\"text/javascript\" src=\"Menu.js\"></script>\n"+    
    "    <script type=\"text/javascript\" src=\"Summary.js\"></script>\n"+    
    "    <script type=\"text/javascript\" src=\"PostResults.js\"></script>\n"+    
    "    <script type=\"text/javascript\" src=\"RunTests.js\"></script>\n"+    
    "    <script type=\"text/javascript\" src=\"TestTable.js\"></script>\n"+    
    "    <script type=\"text/javascript\" src=\"unit.js\"></script>\n";
    
   String code="var j_absorbLibraries={};";
   for(String[] s:jabsorbLibraries)
   {
     head+="    <script type=\"text/javascript\" src=\"../"+s[0]+".js\"></script>\n";
     code+="j_absorbLibraries[\""+s[0]+"\"]=\"/"+s[1]+"\";\n";
   }
   head+="    <script type=\"text/javascript\">"+code+"</script>\n";
    
  String onLoad = "onLoad()";
%>
<%@ include file="../header.jspinc" %>
<%@ include file="unit.jspinc" %>
<%@ include file="../footer.jspinc" %>
