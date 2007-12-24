package org.jabsorb.client;

import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import junit.framework.TestCase;

/**
 * Test case that requires starting the jabsorb server
 */
public class ServerTestBase extends TestCase
{

  Server  server;

  Context context;

  int     port;

  public ServerTestBase()
  {
    port = 8083;
  }

  static final String JABSORB_CONTEXT = "/jabsorb-trunk";

  protected void setUp() throws Exception
  {
    if (server == null)
    {
      // Based on the patch by http://code.google.com/u/cameron.taggart/
      // located at http://code.google.com/p/json-rpc-client/issues/detail?id=1
      JSONRPCBridge.getGlobalBridge().registerObject("test",
          new org.jabsorb.test.TestImpl());
      server = new Server(port);
      context = new Context(server, JABSORB_CONTEXT, Context.SESSIONS);
      ServletHolder jsonRpcServlet = new ServletHolder(new JSONRPCServlet());
      jsonRpcServlet.setInitParameter("auto-session-bridge", "0");
      context.addServlet(jsonRpcServlet, "/*");
      server.start();
    }
    super.setUp();
  }

  protected void tearDown() throws Exception
  {
    if (server != null)
    {
      server.stop();
      server = null;
    }
    super.tearDown();
  }

  public String getServiceRootURL()
  {
    return "http://localhost:" + Integer.toString(port) + JABSORB_CONTEXT;
  }

}
