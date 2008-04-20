/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2008 The jabsorb team
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
package org.jabsorb.ext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.jabsorb.JSONRPCBridge;

/**
 * A servlet that just initializes things at startup and destroys things at 
 * shutdown.  It does not serve any Servlet resources.
 * 
 * @author Arthur Blake
 */
public class InitializationServlet extends HttpServlet
{
  /**
   * Undo all the setup that was done in init() in preparation 
   * for an application shutdown.
   */
  public void destroy() 
  {
    // get the global bridge
    JSONRPCBridge bridge = JSONRPCBridge.getGlobalBridge();
    bridge.unregisterObject("ProjectMetrics");
  }

  /**
   * Set up the global bridge and register objects that can be called 
   * through jabsorb.
   */
  public void init() throws ServletException 
  {
    // get the global bridge
    JSONRPCBridge bridge = JSONRPCBridge.getGlobalBridge();

    // register objects
    bridge.registerObject("ProjectMetrics", new ProjectMetricsHandler());
  }

}
