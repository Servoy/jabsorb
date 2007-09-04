/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007 Arthur Blake and William Becker
 *
 * based on original code from
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.jabsorb;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jabsorb.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This servlet handles JSON-RPC requests over HTTP and hands them to a
 * JSONRPCBridge instance (either a global instance or one in the user's
 * HttpSession).
 * </p>
 * <p>
 * The following can be added to your web.xml to export the servlet under the
 * URI &quot;<code>/JSON-RPC</code>&quot;
 * </p>
 * 
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;/JSON-RPC&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </pre>
 * 
 * </p>
 * The JSONRPCServlet looks for a session specific bridge object under the
 * attribute <code>"JSONRPCBridge"</code> in the HttpSession associated with
 * the request (without creating a session if one does not already exist). If it
 * can't find a session specific bridge instance, it will default to invoking
 * against the global bridge.
 * </p>
 * <p>
 * Using a session specific bridge allows you to export certain object instances
 * or classes only to specific users, and of course these instances could be
 * stateful and contain data specific to the user's session.
 * </p>
 * <p>
 * An example or creating a session specific bridge in JSP is as follows:
 * </p>
 * <code>
 * &lt;jsp:useBean id="JSONRPCBridge" scope="session"
 *   class="com.metaparadigm.jsonrpc.JSONRPCBridge"/&gt;
 * </code>
 * <p>
 * An example in Java (i.e. in another Servlet):
 * </p>
 * <code>
 * HttpSession session = request.getSession();<br />
 * JSONRPCBridge bridge = (JSONRPCBridge) session.getAttribute("JSONRPCBridge");<br>
 * if(bridge == null) {<br />
 * &nbsp;&nbsp;&nbsp;&nbsp;bridge = new JSONRPCBridge();<br />
 * &nbsp;&nbsp;&nbsp;&nbsp;session.setAttribute("JSONRPCBridge", bridge);<br />
 * }<br />
 * </code>
 */

public class JSONRPCServlet extends HttpServlet
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * The logger for this class
   */
  private final static Logger log = LoggerFactory
      .getLogger(JSONRPCServlet.class);

  /**
   * The size of the buffer used for reading requests
   */
  private final static int buf_size = 4096;

  public void service(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ClassCastException
  {

    // Use protected method in case someone wants to override it
    JSONRPCBridge json_bridge = findBridge(request);

    // Encode using UTF-8, although We are actually ASCII clean as
    // all unicode data is JSON escaped using backslash u. This is
    // less data efficient for foreign character sets but it is
    // needed to support naughty browsers such as Konqueror and Safari
    // which do not honour the charset set in the response
    response.setContentType("text/plain;charset=utf-8");
    OutputStream out = response.getOutputStream();

    // Decode using the charset in the request if it exists otherwise
    // use UTF-8 as this is what all browser implementations use.
    // The JSON-RPC-Java JavaScript client is ASCII clean so it
    // although here we can correctly handle data from other clients
    // that do not escape non ASCII data
    String charset = request.getCharacterEncoding();
    if (charset == null)
    {
      charset = "UTF-8";
    }
    BufferedReader in = new BufferedReader(new InputStreamReader(request
        .getInputStream(), charset));

    // Read the request
    CharArrayWriter data = new CharArrayWriter();
    char buf[] = new char[buf_size];
    int ret;
    while ((ret = in.read(buf, 0, buf_size)) != -1)
    {
      data.write(buf, 0, ret);
    }
    if (json_bridge.isDebug())
    {
      log.trace("recieve: " + data.toString());
    }

    // Process the request
    JSONObject json_req = null;
    JSONRPCResult json_res = null;
    try
    {
      json_req = new JSONObject(data.toString());
      json_res = json_bridge.call(new Object[] { request, response }, json_req);
    }
    catch (ParseException e)
    {
      log.error("can't parse call: " + data);
      json_res = new JSONRPCResult(JSONRPCResult.CODE_ERR_PARSE, null,
          JSONRPCResult.MSG_ERR_PARSE);
    }

    // Write the response
    if (json_bridge.isDebug())
    {
      log.trace("send: " + json_res.toString());
    }
    byte[] bout = json_res.toString().getBytes("UTF-8");
    response.setIntHeader("Content-Length", bout.length);

    out.write(bout);
    out.flush();
    out.close();
  }

  /**
   * Find the JSONRPCBridge from the servlet request.
   * 
   * @param request The message received
   * @return the JSONRPCBridge to use for this request
   */
  protected JSONRPCBridge findBridge(HttpServletRequest request)
  {
    // Find the JSONRPCBridge for this session or create one
    // if it doesn't exist
    HttpSession session = request.getSession(false);
    JSONRPCBridge json_bridge = null;
    if (session != null)
    {
      json_bridge = (JSONRPCBridge) session.getAttribute("JSONRPCBridge");
    }
    if (json_bridge == null)
    {
      // Use the global bridge if we can't find a bridge in the session.
      json_bridge = JSONRPCBridge.getGlobalBridge();
      if (json_bridge.isDebug())
      {
        log.info("Using global bridge.");
      }
    }
    return json_bridge;
  }
}
