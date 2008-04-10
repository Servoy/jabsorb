package org.jabsorb.test;

import org.apache.jasper.servlet.JspServlet;
import org.jabsorb.JSONRPCServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * A basic embedded jetty implementation which runs the jabsorb webapp
 */
public class JabsorbTestServer
{
  /**
   * The directory on which the webapp is found
   */
  public final String BASE_CONTEXT = "";

  /**
   * Runs the webserver on port 8084
   * 
   * @param args
   *          Not used
   */
  public static void main(String args[])
  {
    int port;
    try
    {
      port=Integer.parseInt(args[0]);
    }
    catch(Exception e)
    {
      port=8084;
    }
    new JabsorbTestServer(port);
  }

  /**
   * The port the server runs on
   */
  private final int          port;

  /**
   * The web server
   */
  private Server             server;

  /**
   * Creates a new webserver and starts it
   * 
   * @param port
   *          The port the server runs on
   */
  public JabsorbTestServer(int port)
  {
    this.port = port;

    try
    {
      this.server = new Server(port);
      createBaseContext();
      this.server.start();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Puts the necessary servlets on the server
   */
  private void createBaseContext()
  {
    Context context = new Context(this.server, BASE_CONTEXT, Context.SESSIONS);
    context.setContextPath(BASE_CONTEXT);
    context.setResourceBase("webapps/jsonrpc/");
    context.setAttribute("copyWebDir", "true");
    ServletHolder defaultServlet = new ServletHolder(new DefaultServlet());
    context.addServlet(defaultServlet, "/");

    // do jsps
    ServletHolder jspServlet = new ServletHolder(new JspServlet());
    jspServlet.setInitParameter("auto-session-bridge", "0");
    context.addServlet(jspServlet, "*.jsp");

    // do static content
    ServletHolder jsonRpcServlet = new ServletHolder(new JSONRPCServlet());
    jsonRpcServlet.setInitParameter("auto-session-bridge", "0");
    context.addServlet(jsonRpcServlet, "/JSON-RPC/*");
  }

  /**
   * Stops the server
   * 
   * @throws Exception
   *           if jetty has issues stopping
   */
  public void stop() throws Exception
  {
    this.server.stop();
  }

}
