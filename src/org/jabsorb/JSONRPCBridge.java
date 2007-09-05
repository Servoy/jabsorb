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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.jabsorb.callback.CallbackController;
import org.jabsorb.callback.InvocationCallback;
import org.jabsorb.localarg.LocalArgController;
import org.jabsorb.localarg.LocalArgResolver;
import org.jabsorb.reflect.ClassAnalyzer;
import org.jabsorb.reflect.ClassData;
import org.jabsorb.reflect.MethodKey;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.Serializer;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class implements a bridge that unmarshalls JSON objects in JSON-RPC
 * request format, invokes a method on the exported object, and then marshalls
 * the resulting Java objects to JSON objects in JSON-RPC result format.
 * </p>
 * <p>
 * There is a global bridge singleton object that allows exporting classes and
 * objects to all HTTP clients. In addition to this, an instance of the
 * JSONRPCBridge can optionally be placed in a users' HttpSession object
 * registered under the attribute "JSONRPCBridge" to allow exporting of classes
 * and objects to specific users. A session specific bridge will delegate
 * requests for objects it does not know about to the global singleton
 * JSONRPCBridge instance.
 * </p>
 * <p>
 * Using session specific bridge instances can improve the security of
 * applications by allowing exporting of certain objects only to specific
 * HttpSessions as well as providing a convenient mechanism for JavaScript
 * clients to access stateful data associated with the current user.
 * </p>
 * <p>
 * You can create a HttpSession specific bridge in JSP with the usebean tag:
 * </p>
 * <code>&lt;jsp:useBean id="JSONRPCBridge" scope="session"
 * class="com.metaparadigm.jsonrpc.JSONRPCBridge" /&gt;</code>
 * <p>
 * Then export an object for your JSON-RPC client to call methods on:
 * </p>
 * <code>JSONRPCBridge.registerObject("test", testObject);</code>
 * <p>
 * This will make available all public methods of the object as
 * <code>test.&lt;methodnames&gt;</code> to JSON-RPC clients. This approach
 * should generally be performed after an authentication check to only export
 * objects to clients that are authorised to use them.
 * </p>
 * <p>
 * Alternatively, the global bridge singleton object allows exporting of classes
 * and objects to all HTTP clients. It can be fetched with
 * <code>JSONRPCBridge.getGlobalBridge()</code>.
 * </p>
 * <p>
 * To export all public instance methods of an object to <b>all</b> clients:
 * </p>
 * <code>JSONRPCBridge.getGlobalBridge().registerObject("myObject",
 * myObject);</code>
 * <p>
 * To export all public static methods of a class to <b>all</b> clients:
 * </p>
 * <code>JSONRPCBridge.getGlobalBridge().registerClass("MyClass",
 * com.example.MyClass.class);</code>
 */
public class JSONRPCBridge implements Serializable
{

  /**
   * Used to determine whether two methods match
   * TODO: There ought to be a better way of doing this!
   */
  protected static class MethodCandidate
  {
    /**
     * The method
     */
    Method method;

    /**
     * The match data for each parameter of the method.
     */
    ObjectMatch match[];

    /**
     * Creatse a new MethodCandidate
     * 
     * @param method The method for this candidate
     */
    public MethodCandidate(Method method)
    {
      this.method = method;
      match = new ObjectMatch[method.getParameterTypes().length];
    }

    /**
     * Gets an object Match for the method.
     * 
     * @return An object match with the amount of mismatches
     */
    public ObjectMatch getMatch()
    {
      int mismatch = -1;
      for (int i = 0; i < match.length; i++)
      {
        mismatch = Math.max(mismatch, match[i].getMismatch());
      }
      if (mismatch == -1)
      {
        return ObjectMatch.OKAY;
      }
      return new ObjectMatch(mismatch);
    }
  }

  /**
   * Container for objects of which instances have been made
   */
  protected static class ObjectInstance implements Serializable
  {
    /**
     * Unique serialisation id. 
     */
    private final static long serialVersionUID = 2;

    /**
     * The object for the instance
     */
    protected Object o;

    /**
     * The class the object is of
     */
    protected Class clazz;

    /**
     * Creates a new ObjectInstance
     * 
     * @param o The object for the instance
     */
    public ObjectInstance(Object o)
    {
      this.o = o;
      this.clazz = o.getClass();
    }

    /**
     * Creates a new ObjectInstance
     * 
     * @param o The object for the instance
     * @param clazz The class the object is of
     */
    public ObjectInstance(Object o, Class clazz)
    {
      if (!clazz.isInstance(o))
      {
        throw new ClassCastException(
            "Attempt to register jsonrpc object with invalid class.");
      }
      this.o = o;
      this.clazz = clazz;
    }
  }

  /**
   * Unique serialisation id. 
   */
  private final static long serialVersionUID = 2;

  /**
   * A simple transformer that makes no change
   */
  private static final ExceptionTransformer IDENTITY_EXCEPTION_TRANSFORMER = new ExceptionTransformer()
  {
    /**
     * Unique serialisation id. 
     */
    private final static long serialVersionUID = 2;

    public Object transform(Throwable t)
    {
      return t;
    }
  };

  /**
   * The logger for this class
   */
  private final static Logger log = LoggerFactory
      .getLogger(JSONRPCBridge.class);

  /**
   * Global bridge (for exporting to all users)
   */
  private final static JSONRPCBridge globalBridge = new JSONRPCBridge();

  /**
   * Global JSONSerializer instance
   */
  private static JSONSerializer ser = new JSONSerializer();

  static
  {
    try
    {
      ser.registerDefaultSerializers();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * This method retrieves the global bridge singleton. <p/> It should be used
   * with care as objects should generally be registered within session specific
   * bridges for security reasons.
   * 
   * @return returns the global bridge object.
   */
  public static JSONRPCBridge getGlobalBridge()
  {
    return globalBridge;
  }

  /**
   * Get the global JSONSerializer object.
   * 
   * @return the global JSONSerializer object.
   */
  public static JSONSerializer getSerializer()
  {
    return ser;
  }

  /* Inner classes */

  /**
   * Registers a Class to be removed from the exported method signatures and
   * instead be resolved locally using context information from the transport.
   * 
   * @param argClazz The class to be resolved locally
   * @param argResolver The user defined class that resolves the and returns the
   *          method argument using transport context information
   * @param contextInterface The type of transport Context object the callback
   *          is interested in eg. HttpServletRequest.class for the servlet
   *          transport
   */
  public static void registerLocalArgResolver(Class argClazz,
      Class contextInterface, LocalArgResolver argResolver)
  {
    LocalArgController.registerLocalArgResolver(argClazz, contextInterface,
        argResolver);
  }

  /**
   * Set the global JSONSerializer object.
   * 
   * @param ser the global JSONSerializer object.
   */
  public static void setSerializer(JSONSerializer ser)
  {
    JSONRPCBridge.ser = ser;
  }

  /* Implementation */

  /**
   * Unregisters a LocalArgResolver</b>.
   * 
   * @param argClazz The previously registered local class
   * @param argResolver The previously registered LocalArgResolver object
   * @param contextInterface The previously registered transport Context
   *          interface.
   */
  public static void unregisterLocalArgResolver(Class argClazz,
      Class contextInterface, LocalArgResolver argResolver)
  {
    LocalArgController.unregisterLocalArgResolver(argClazz, contextInterface,
        argResolver);
  }

  /**
   * Creates a signature for an array of arguements
   * 
   * @param arguments The argumnts
   * @return A comma seperated string listing the arguments TODO: Arthur: Do you
   *         need to add your list/map things here? - WB
   */
  private static String argSignature(JSONArray arguments)
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < arguments.length(); i += 1)
    {
      if (i > 0)
      {
        buf.append(",");
      }
      Object jso;

      try
      {
        jso = arguments.get(i);
      }
      catch (JSONException e)
      {
        throw (NoSuchElementException)new NoSuchElementException(e.getMessage()).initCause(e);
      }

      if (jso == null)
      {
        buf.append("java.lang.Object");
      }
      else if (jso instanceof String)
      {
        buf.append("java.lang.String");
      }
      else if (jso instanceof Number)
      {
        buf.append("java.lang.Number");
      }
      else if (jso instanceof JSONArray)
      {
        buf.append("java.lang.Object[]");
      }
      else
      {
        buf.append("java.lang.Object");
      }
    }
    return buf.toString();
  }

  /**
   * Display a method call argument signature for a method as a String for
   * debugging/logging purposes. The string contains the comma separated list of
   * argument types that the given method takes.
   * 
   * @param method Method instance to display the argument signature for.
   * @return the argument signature for the method, as a String.
   */
  private static String argSignature(Method method)
  {
    Class param[] = method.getParameterTypes();
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < param.length; i++)
    {
      if (i > 0)
      {
        buf.append(",");
      }
      buf.append(param[i].getName());
    }
    return buf.toString();
  }

  /**
   * Create unique method names by appending the given prefix to the keys from
   * the given HashMap and adding them all to the given HashSet.
   * 
   * @param m HashSet to add unique methods to.
   * @param prefix prefix to append to each method name found in the methodMap.
   * @param methodMap a HashMap containing MethodKey keys specifying methods.
   */
  private static void uniqueMethods(HashSet m, String prefix, HashMap methodMap)
  {
    Iterator i = methodMap.entrySet().iterator();
    while (i.hasNext())
    {
      Map.Entry mentry = (Map.Entry) i.next();
      MethodKey mk = (MethodKey) mentry.getKey();
      m.add(prefix + mk.getMethodName());
    }
  }

  /**
   * Whether debugging enabled on this bridge
   */
  private boolean debug = false;

  /**
   * The functor used to convert exceptions
   */
  private ExceptionTransformer exceptionTransformer = IDENTITY_EXCEPTION_TRANSFORMER;

  /**
   * Bridge state
   */
  private JSONRPCBridgeState state = new JSONRPCBridgeState(this);

  /**
   * The callback controller
   */
  private CallbackController cbc = null;

  /**
   * Call a method using a JSON-RPC request object.
   * 
   * @param context The transport context (the HttpServletRequest object in the
   *          case of the HTTP transport).
   * @param jsonReq The JSON-RPC request structured as a JSON object tree.
   * @return a JSONRPCResult object with the result of the invocation or an
   *         error.
   */
  public JSONRPCResult call(Object context[], JSONObject jsonReq)
  {
    JSONRPCResult result = null;
    String encodedMethod = null;
    Object requestId = null;
    JSONArray arguments = null;

    try
    {
      // Get method name, arguments and request id
      encodedMethod = jsonReq.getString("method");
      arguments = jsonReq.getJSONArray("params");
      requestId = jsonReq.opt("id");
    }
    catch (JSONException e)
    {
      log.error("no method or parameters in request");
      return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD, null,
          JSONRPCResult.MSG_ERR_NOMETHOD);
    }

    if (isDebug())
    {
      log.trace("call " + encodedMethod + "(" + arguments + ")"
          + ", requestId=" + requestId);
    }

    String className = null;
    String methodName = null;
    int objectID = 0;

    // Parse the class and methodName
    StringTokenizer t = new StringTokenizer(encodedMethod, ".");
    if (t.hasMoreElements())
    {
      className = t.nextToken();
    }
    if (t.hasMoreElements())
    {
      methodName = t.nextToken();
    }

    // See if we have an object method in the format ".obj#<objectID>"
    if (encodedMethod.startsWith(".obj#"))
    {
      t = new StringTokenizer(className, "#");
      t.nextToken();
      objectID = Integer.parseInt(t.nextToken());
    }

    // one of oi or cd will resolve (first oi is attempted, and if that fails,
    // then cd is attempted)

    // object instance of object being invoked
    ObjectInstance oi = null;

    // ClassData for resolved object instance, or if object instance cannot
    // resolve, class data for
    // class instance (static method) we are resolving to
    ClassData cd = null;

    HashMap methodMap = null;
    Method method = null;
    Object itsThis = null;

    if (objectID == 0)
    {
      // Handle "system.listMethods"
      // this is called by the browser side javascript
      // when a new JSONRpcClient object is initialized.
      if (encodedMethod.equals("system.listMethods"))
      {
        HashSet m = new HashSet();
        globalBridge.allInstanceMethods(m);
        if (globalBridge != this)
        {
          globalBridge.allStaticMethods(m);
          globalBridge.allInstanceMethods(m);
        }
        allStaticMethods(m);
        allInstanceMethods(m);
        JSONArray methods = new JSONArray();
        Iterator i = m.iterator();
        while (i.hasNext())
        {
          methods.put(i.next());
        }
        return new JSONRPCResult(JSONRPCResult.CODE_SUCCESS, requestId, methods);
      }
      // Look up the class, object instance and method objects
      if (className == null
          || methodName == null
          || ((oi = resolveObject(className)) == null && (cd = resolveClass(className)) == null))
      {
        return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD, requestId,
            JSONRPCResult.MSG_ERR_NOMETHOD);
      }
      if (oi != null)
      {
        itsThis = oi.o;
        cd = ClassAnalyzer.getClassData(oi.clazz);
        methodMap = cd.getMethodMap();
      }
      else
      {
        if (cd != null)
        {
          methodMap = cd.getStaticMethodMap();
        }
      }
    }
    else
    {
      if ((oi = resolveObject(new Integer(objectID))) == null)
      {
        return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD, requestId,
            JSONRPCResult.MSG_ERR_NOMETHOD);
      }
      itsThis = oi.o;
      cd = ClassAnalyzer.getClassData(oi.clazz);
      methodMap = cd.getMethodMap();
      // Handle "system.listMethods"
      // this is called by the browser side javascript
      // when a new JSONRpcClient object with an objectID is initialized.

      if (methodName != null && methodName.equals("listMethods"))
      {
        HashSet m = new HashSet();
        uniqueMethods(m, "", cd.getStaticMethodMap());
        uniqueMethods(m, "", cd.getMethodMap());
        JSONArray methods = new JSONArray();
        Iterator i = m.iterator();
        while (i.hasNext())
        {
          methods.put(i.next());
        }
        return new JSONRPCResult(JSONRPCResult.CODE_SUCCESS, requestId, methods);
      }
    }

    // Find the specific method
    if ((method = resolveMethod(methodMap, methodName, arguments)) == null)
    {
      return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD, requestId,
          JSONRPCResult.MSG_ERR_NOMETHOD);
    }

    // Call the method
    try
    {
      if (debug)
      {
        log.trace("invoking " + method.getReturnType().getName() + " "
            + method.getName() + "(" + argSignature(method) + ")");
      }

      // Unmarshall arguments
      Object javaArgs[] = unmarshallArgs(context, method, arguments);

      // Call pre invoke callbacks
      if (cbc != null)
      {
        for (int i = 0; i < context.length; i++)
        {
          cbc.preInvokeCallback(context[i], itsThis, method, javaArgs);
        }
      }

      // Invoke the method
      Object returnObj = method.invoke(itsThis, javaArgs);

      // Call post invoke callbacks
      if (cbc != null)
      {
        for (int i = 0; i < context.length; i++)
        {
          cbc.postInvokeCallback(context[i], itsThis, method, returnObj);
        }
      }

      // Marshall the result
      SerializerState serialiserState = new SerializerState();
      result = new JSONRPCResult(JSONRPCResult.CODE_SUCCESS, requestId, ser
          .marshall(serialiserState, returnObj));

      // Handle exceptions creating exception results and
      // calling error callbacks
    }
    catch (UnmarshallException e)
    {
      if (cbc != null)
      {
        for (int i = 0; i < context.length; i++)
        {
          cbc.errorCallback(context[i], itsThis, method, e);
        }
      }
      result = new JSONRPCResult(JSONRPCResult.CODE_ERR_UNMARSHALL, requestId,
          e.getMessage());
    }
    catch (MarshallException e)
    {
      if (cbc != null)
      {
        for (int i = 0; i < context.length; i++)
        {
          cbc.errorCallback(context[i], itsThis, method, e);
        }
      }
      result = new JSONRPCResult(JSONRPCResult.CODE_ERR_MARSHALL, requestId, e
          .getMessage());
    }
    catch (Throwable e)
    {
      if (e instanceof InvocationTargetException)
      {
        e = ((InvocationTargetException) e).getTargetException();
      }
      if (cbc != null)
      {
        for (int i = 0; i < context.length; i++)
        {
          cbc.errorCallback(context[i], itsThis, method, e);
        }
      }
      result = new JSONRPCResult(JSONRPCResult.CODE_REMOTE_EXCEPTION,
          requestId, exceptionTransformer.transform(e));
    }

    // Return the results
    return result;
  }

  /**
   * Get the JSONRPCBridgeState object associated with this bridge.
   * 
   * @return the JSONRPCBridgeState object associated with this bridge.
   */
  public JSONRPCBridgeState getBridgeState()
  {
    return state;
  }

  /**
   * Get the CallbackController object associated with this bridge.
   * 
   * @return the CallbackController object associated with this bridge.
   */
  public CallbackController getCallbackController()
  {
    return cbc;
  }

  /**
   * Gets the map of referenced objects used by this bridge. <p/> The reference
   * map contains objects of classes that have been registered as a Reference or
   * CallableReference.
   * 
   * @return a HashMap with the references currently in use on this bridge
   *         instance.
   */
  public HashMap getReferenceMap()
  {
    return state.getReferenceMap();
  }

  /**
   * Check whether a class is registered as a callable reference type.
   * 
   * @param clazz The class object to check is a callable reference.
   * @return true if it is, false otherwise
   */
  public boolean isCallableReference(Class clazz)
  {
    if (this == globalBridge)
    {
      return false;
    }
    HashSet callableReferenceSet = state.getCallableReferenceSet();
    if (callableReferenceSet == null)
    {
      return false;
    }
    if (callableReferenceSet.contains(clazz))
    {
      return true;
    }
    return globalBridge.isCallableReference(clazz);
  }

  /**
   * Check whether a class is registered as a reference type.
   * 
   * @param clazz The class object to check is a reference.
   * @return true if it is, false otherwise.
   */
  public boolean isReference(Class clazz)
  {
    if (this == globalBridge)
    {
      return false;
    }
    HashSet referenceSet = state.getReferenceSet();
    if (referenceSet == null)
    {
      return false;
    }
    if (referenceSet.contains(clazz))
    {
      return true;
    }
    return globalBridge.isReference(clazz);
  }

  /**
   * Lookup a class that is registered with this bridge.
   * 
   * @param name The registered name of the class to lookup.
   * @return the class for the name
   */
  public Class lookupClass(String name)
  {
    synchronized (state)
    {
      HashMap classMap = state.getClassMap();
      return (Class) classMap.get(name);
    }
  }

  /**
   * Lookup an object that is registered with this bridge.
   * 
   * @param key The registered name of the object to lookup.
   * @return The object desired if it exists, else null.
   */
  public Object lookupObject(Object key)
  {
    synchronized (state)
    {
      HashMap objectMap = state.getObjectMap();
      ObjectInstance oi = (ObjectInstance) objectMap.get(key);
      if (oi != null)
      {
        return oi.o;
      }
    }
    return null;
  }

  /**
   * <p>
   * Registers a class to be returned as a callable reference.
   * </p>
   * <p>
   * The JSONBridge will return a callable reference to the JSON-RPC client for
   * registered classes instead of passing them by value. The JSONBridge will
   * take a references to these objects and the JSON-RPC client will create an
   * invocation proxy for objects of this class for which methods will be called
   * on the instance on the server.
   * </p>
   * <p>
   * <b>Note:</b> A limitation exists in the JSON-RPC client where only the top
   * most object returned from a method can be made into a proxy.
   * </p>
   * <p>
   * A Callable Reference in JSON format looks like this:
   * </p>
   * <code>{ "javaClass":"com.metaparadigm.test.Bar",<br />
   * "objectID":4827452,<br /> "JSONRPCType":"CallableReference" }</code>
   * 
   * @param clazz The class object that should be marshalled as a callable
   *          reference.
   * @throws Exception If the bridge is global, callable references cannot be
   *           added
   */
  public void registerCallableReference(Class clazz) throws Exception
  {
    if (this == globalBridge)
    {
      throw new Exception("Can't register callable reference on global bridge");
    }
    synchronized (state)
    {
      if (state.getReferenceSerializer() == null)
      {
        state.enableReferences();
      }
      state.getCallableReferenceSet().add(clazz);
    }
    if (debug)
    {
      log.info("registered callable reference " + clazz.getName());
    }
  }

  /**
   * Registers a callback to be called before and after method invocation
   * 
   * @param callback The object implementing the InvocationCallback Interface
   * @param contextInterface The type of transport Context interface the
   *          callback is interested in eg. HttpServletRequest.class for the
   *          servlet transport.
   */
  public void registerCallback(InvocationCallback callback,
      Class contextInterface)
  {
    if (cbc == null)
    {
      cbc = new CallbackController();
      cbc.setDebug(isDebug());
    }
    cbc.registerCallback(callback, contextInterface);
  }

  /**
   * Registers a class to export static methods. <p/> The JSONBridge will export
   * all static methods of the class. This is useful for exporting factory
   * classes that may then return CallableReferences to the JSON-RPC client.
   * <p/> Calling registerClass for a clazz again under the same name will have
   * no effect. <p/> To export instance methods you need to use registerObject.
   * 
   * @param name The name to register the class with.
   * @param clazz The class to export static methods from.
   * @throws Exception If a class is already registed with this name
   */
  public void registerClass(String name, Class clazz) throws Exception
  {
    synchronized (state)
    {
      HashMap classMap = state.getClassMap();
      Class exists = (Class) classMap.get(name);
      if (exists != null && exists != clazz)
      {
        throw new Exception("different class registered as " + name);
      }
      if (exists == null)
      {
        classMap.put(name, clazz);
      }
    }
    if (debug)
    {
      log.info("registered class " + clazz.getName() + " as " + name);
    }
  }

  /**
   * Registers an object to export all instance methods and static methods. <p/>
   * The JSONBridge will export all instance methods and static methods of the
   * particular object under the name passed in as a key. <p/> This will make
   * available all methods of the object as
   * <code>&lt;key&gt;.&lt;methodnames&gt;</code> to JSON-RPC clients. <p/>
   * Calling registerObject for a name that already exists will replace the
   * existing entry.
   * 
   * @param key The named prefix to export the object as
   * @param o The object instance to be called upon
   */
  public void registerObject(Object key, Object o)
  {
    ObjectInstance oi = new ObjectInstance(o);
    synchronized (state)
    {
      HashMap objectMap = state.getObjectMap();
      objectMap.put(key, oi);
    }
    if (debug)
    {
      log.info("registered object " + o.hashCode() + " of class "
          + o.getClass().getName() + " as " + key);
    }
  }

  /**
   * Registers an object to export all instance methods defined by
   * interfaceClass. <p/> The JSONBridge will export all instance methods
   * defined by interfaceClass of the particular object under the name passed in
   * as a key. <p/> This will make available these methods of the object as
   * <code>&lt;key&gt;.&lt;methodnames&gt;</code> to JSON-RPC clients.
   * 
   * @param key The named prefix to export the object as
   * @param o The object instance to be called upon
   * @param interfaceClass The type that this object should be registered as.
   *          <p/> This can be used to restrict the exported methods to the
   *          methods defined in a specific superclass or interface.
   */
  public void registerObject(Object key, Object o, Class interfaceClass)
  {
    ObjectInstance oi = new ObjectInstance(o, interfaceClass);
    synchronized (state)
    {
      HashMap objectMap = state.getObjectMap();
      objectMap.put(key, oi);
    }
    if (debug)
    {
      log.info("registered object " + o.hashCode() + " of class "
          + interfaceClass.getName() + " as " + key);
    }
  }

  /**
   * Registers a class to be returned by reference and not by value as is done
   * by default. <p/> The JSONBridge will take a references to these objects and
   * return an opaque object to the JSON-RPC client. When the opaque object is
   * passed back through the bridge in subsequent calls, the original object is
   * substitued in calls to Java methods. This should be used for any objects
   * that contain security information or complex types that are not required in
   * the Javascript client but need to be passed as a reference in methods of
   * exported objects. <p/> A Reference in JSON format looks like this: <p/>
   * <code>{ "javaClass":"com.metaparadigm.test.Foo",<br />
   * "objectID":5535614,<br /> "JSONRPCType":"Reference" }</code>
   * 
   * @param clazz The class object that should be marshalled as a reference.
   * @throws Exception If the bridge is global, callable references cannot be
   *           added
   */
  public void registerReference(Class clazz) throws Exception
  {
    if (this == globalBridge)
    {
      throw new Exception("Can't register reference on global bridge");
    }
    synchronized (state)
    {
      if (state.getReferenceSerializer() == null)
      {
        state.enableReferences();
      }
      state.getReferenceSet().add(clazz);
    }
    if (debug)
    {
      log.info("registered reference " + clazz.getName());
    }
  }

  /**
   * Register a new serializer on this bridge.
   * 
   * @param serializer A class implementing the Serializer interface (usually
   *          derived from AbstractSerializer).
   * @throws Exception If a serialiser has already been registered that
   *           serialises the same class
   */
  public void registerSerializer(Serializer serializer) throws Exception
  {
    ser.registerSerializer(serializer);
  }

  /**
   * Set the JSONRPCBridgeState object for this bridge.
   * 
   * @param state the JSONRPCBridgeState object to be set for this bridge.
   */
  public void setBridgeState(JSONRPCBridgeState state)
  {
    this.state = state;
  }

  /**
   * Set the CallbackController object for this bridge.
   * 
   * @param cbc the CallbackController object to be set for this bridge.
   */
  public void setCallbackController(CallbackController cbc)
  {
    this.cbc = cbc;
  }

  /**
   * Enable or disable debugging message from this bridge instance.
   * 
   * @param debug flag to enable or disable debugging messages
   */
  public void setDebug(boolean debug)
  {
    this.debug = debug;
    ser.setDebug(isDebug());
    if (cbc != null)
    {
      cbc.setDebug(isDebug());
    }
  }

  /**
   * Sets the exception transformer for the bridge.
   * 
   * @param exceptionTransformer The new exception transformer to use.
   */
  public void setExceptionTransformer(ExceptionTransformer exceptionTransformer)
  {
    this.exceptionTransformer = exceptionTransformer;
  }

  /**
   * Unregisters a callback
   * 
   * @param callback The previously registered InvocationCallback object
   * @param contextInterface The previously registered transport Context
   *          interface.
   */
  public void unregisterCallback(InvocationCallback callback,
      Class contextInterface)
  {
    if (cbc == null)
    {
      return;
    }
    cbc.unregisterCallback(callback, contextInterface);
  }

  /**
   * Unregisters a class exported with registerClass. <p/> The JSONBridge will
   * unexport all static methods of the class.
   * 
   * @param name The registered name of the class to unexport static methods
   *          from.
   */
  public void unregisterClass(String name)
  {
    synchronized (state)
    {
      HashMap classMap = state.getClassMap();
      Class clazz = (Class) classMap.get(name);
      if (clazz != null)
      {
        classMap.remove(name);
        if (debug)
        {
          log.info("unregistered class " + clazz.getName() + " from " + name);
        }
      }
    }
  }

  /**
   * Unregisters an object exported with registerObject. <p/> The JSONBridge
   * will unexport all instance methods and static methods of the particular
   * object under the name passed in as a key.
   * 
   * @param key The named prefix of the object to unexport
   */
  public void unregisterObject(Object key)
  {
    synchronized (state)
    {
      HashMap objectMap = state.getObjectMap();
      ObjectInstance oi = (ObjectInstance) objectMap.get(key);
      if (oi.o != null)
      {
        objectMap.remove(key);
        if (debug)
        {
          log.info("unregistered object " + oi.o.hashCode() + " of class "
              + oi.clazz.getName() + " from " + key);
        }
      }
    }
  }

  /**
   * Are debugging messages enabled on this bridge instance.
   * 
   * @return true or false depending on whether debugging messages are enabled.
   */
  protected boolean isDebug()
  {
    return debug || (this != globalBridge && globalBridge.isDebug());
  }

  /**
   * Add all instance methods that can be invoked on this bridge to a HashSet.
   * 
   * @param m HashSet to add all static methods to.
   */
  private void allInstanceMethods(HashSet m)
  {
    synchronized (state)
    {
      HashMap objectMap = state.getObjectMap();
      Iterator i = objectMap.entrySet().iterator();
      while (i.hasNext())
      {
        Map.Entry oientry = (Map.Entry) i.next();
        Object key = oientry.getKey();
        if (!(key instanceof String))
        {
          continue;
        }
        String name = (String) key;
        ObjectInstance oi = (ObjectInstance) oientry.getValue();
        ClassData cd = ClassAnalyzer.getClassData(oi.clazz);
        uniqueMethods(m, name + ".", cd.getMethodMap());
        uniqueMethods(m, name + ".", cd.getStaticMethodMap());
      }
    }
  }

  /**
   * Add all static methods that can be invoked on this bridge to the given
   * HashSet.
   * 
   * @param m HashSet to add all static methods to.
   */
  private void allStaticMethods(HashSet m)
  {
    synchronized (state)
    {
      HashMap classMap = state.getClassMap();
      Iterator i = classMap.entrySet().iterator();
      while (i.hasNext())
      {
        Map.Entry cdentry = (Map.Entry) i.next();
        String name = (String) cdentry.getKey();
        Class clazz = (Class) cdentry.getValue();
        ClassData cd = ClassAnalyzer.getClassData(clazz);
        uniqueMethods(m, name + ".", cd.getStaticMethodMap());
      }
    }
  }

  /**
   * Returns the more fit of the two method candidates
   * 
   * @param methodCandidate One of the methodCandidates to compare
   * @param methodCandidate1 The other of the methodCandidates to compare
   * @return The better of the two candidates
   */
  private MethodCandidate betterSignature(MethodCandidate methodCandidate,
      MethodCandidate methodCandidate1)
  {
    final Method method = methodCandidate.method;
    final Method method1 = methodCandidate1.method;
    final Class[] parameters = method.getParameterTypes();
    final Class[] parameters1 = method1.getParameterTypes();
    int c = 0, c1 = 0;
    for (int i = 0; i < parameters.length; i++)
    {
      final Class parameterClass = parameters[i];
      final Class parameterClass1 = parameters1[i];
      if (parameterClass != parameterClass1)
      {
        if (parameterClass.isAssignableFrom(parameterClass1))
        {
          c1++;
        }
        else
        {
          c++;
        }
      }
    }
    if (c1 > c)
    {
      return methodCandidate1;
    }

    return methodCandidate;

  }

  /**
   * Resolves a string to a class
   * 
   * @param className The name of the class to resolve
   * @return The data associated with the className
   */
  private ClassData resolveClass(String className)
  {
    Class clazz = null;
    ClassData cd = null;

    synchronized (state)
    {
      HashMap classMap = state.getClassMap();
      clazz = (Class) classMap.get(className);
    }

    if (clazz != null)
    {
      cd = ClassAnalyzer.getClassData(clazz);
    }

    if (cd != null)
    {
      if (debug)
      {
        log.trace("found class " + cd.getClazz().getName() + " named "
            + className);
      }
      return cd;
    }

    if (this != globalBridge)
    {
      return globalBridge.resolveClass(className);
    }

    return null;
  }

  /**
   * Resolve which method the caller is requesting <p/> If a method with the
   * requested number of arguments does not exist at all, null will be returned.
   * <p/> If the object or class (for static methods) being invoked contains
   * more than one overloaded methods that match the method key signature, find
   * the closest matching method to invoke according to the JSON arguments being
   * passed in.
   * 
   * @param methodMap Map keyed by MethodKey objects and the values will be
   *          either a Method object, or an array of Method objects, if there is
   *          more than one possible method that can be invoked matching the
   *          MethodKey.
   * @param methodName method name being called.
   * @param arguments JSON arguments to the method, as a JSONArray.
   * @return the Method that most closely matches the call signature, or null if
   *         there is not a match.
   */
  private Method resolveMethod(HashMap methodMap, String methodName,
      JSONArray arguments)
  {
    Method method[] = null;

    // first, match soley by the method name and number of arguments passed in
    // if there is a single match, return the single match
    // if there is no match at all, return null
    // if there are multiple matches, fall through to the second matching phase
    // below
    MethodKey mk = new MethodKey(methodName, arguments.length());
    Object o = methodMap.get(mk);
    if (o instanceof Method)
    {
      Method m = (Method) o;
      if (debug)
      {
        log.trace("found method " + methodName + "(" + argSignature(m) + ")");
      }
      return m;
    }
    else if (o instanceof Method[])
    {
      method = (Method[]) o;
    }
    else
    {
      return null;
    }

    // second matching phase: there were overloaded methods on the object
    // we are invoking so try and find the best match based on the types of
    // the arguments passed in.

    // try and unmarshall the arguments against each candidate method
    // to determine which one matches the best
    ArrayList candidate = new ArrayList();
    if (debug)
    {
      log.trace("looking for method " + methodName + "("
          + argSignature(arguments) + ")");
    }
    for (int i = 0; i < method.length; i++)
    {
      try
      {
        candidate.add(tryUnmarshallArgs(method[i], arguments));
        if (debug)
        {
          log.trace("+++ possible match with method " + methodName + "("
              + argSignature(method[i]) + ")");
        }
      }
      catch (Exception e)
      {
        if (debug)
        {
          log.trace("xxx " + e.getMessage() + " in " + methodName + "("
              + argSignature(method[i]) + ")");
        }
      }
    }

    // now search through all the candidates and find one which matches
    // the json arguments the closest
    MethodCandidate best = null;
    for (int i = 0; i < candidate.size(); i++)
    {
      MethodCandidate c = (MethodCandidate) candidate.get(i);
      if (best == null)
      {
        best = c;
        continue;
      }
      final ObjectMatch bestMatch = best.getMatch();
      final ObjectMatch cMatch = c.getMatch();
      if (bestMatch.getMismatch() > cMatch.getMismatch())
      {
        best = c;
      }
      else if (bestMatch.getMismatch() == cMatch.getMismatch())
      {
        best = betterSignature(best, c);
      }
    }
    if (best != null)
    {
      Method m = best.method;
      if (debug)
      {
        log.trace("found method " + methodName + "(" + argSignature(m) + ")");
      }
      return m;
    }
    return null;
  }

  /**
   * Resolve the key to a specified instance object. If an instance object of
   * the requested key is not found, and this is not the global bridge, then
   * look in the global bridge too. <p/> If the key is not found in this bridge
   * or the global bridge, the requested key may be a class method (static
   * method) or may not exist (not registered under the requested key.)
   * 
   * @param key registered object key being requested by caller.
   * @return ObjectInstance that has been registered under this key, in this
   *         bridge or the global bridge.
   */
  private ObjectInstance resolveObject(Object key)
  {
    ObjectInstance oi = null;
    synchronized (state)
    {
      HashMap objectMap = state.getObjectMap();
      oi = (ObjectInstance) objectMap.get(key);
    }
    if (debug && oi != null)
    {
      log.trace("found object " + oi.o.hashCode() + " of class "
          + oi.clazz.getName() + " with key " + key);
    }
    if (oi == null && this != globalBridge)
    {
      return globalBridge.resolveObject(key);
    }
    return oi;
  }

  /**
   * Tries to unmarshall the arguments to a method
   * 
   * @param method The method to unmarshall the arguements for.
   * @param arguments The arguments to unmarshall
   * @return The methodCandidate that should suit the arguements and method.
   * @throws UnmarshallException If one of the arguments cannot be unmarshalled
   */
  private MethodCandidate tryUnmarshallArgs(Method method, JSONArray arguments)
      throws UnmarshallException
  {
    MethodCandidate candidate = new MethodCandidate(method);
    Class param[] = method.getParameterTypes();
    int i = 0, j = 0;
    try
    {
      for (; i < param.length; i++)
      {
        SerializerState serialiserState = new SerializerState();
        if (LocalArgController.isLocalArg(param[i]))
        {
          candidate.match[i] = ObjectMatch.OKAY;
        }
        else
        {
          candidate.match[i] = ser.tryUnmarshall(serialiserState, param[i], arguments.get(j++));
        }
      }
    }
    catch (JSONException e)
    {
      throw (NoSuchElementException) new NoSuchElementException(e.getMessage()).initCause(e);
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("arg " + (i + 1) + " " + e.getMessage());
    }
    return candidate;
  }

  /**
   * Convert the arguments to a method call from json into java objects to be
   * used for invoking the method, later.
   * 
   * @param context the context of the caller. This will be the servlet request
   *          and response objects in an http servlet call environment. These
   *          are used to insert local arguments (e.g. the request, response or
   *          session,etc.) when found in the java method call argument
   *          signature.
   * @param method the java method that will later be invoked with the given
   *          args.
   * @param arguments the arguments from the caller, in json format.
   * @return the java arguments as unmarshalled from json.
   * @throws UnmarshallException if there is a problem unmarshalling the
   *           arguments.
   */
  private Object[] unmarshallArgs(Object context[], Method method,
      JSONArray arguments) throws UnmarshallException
  {
    Class param[] = method.getParameterTypes();
    Object javaArgs[] = new Object[param.length];
    int i = 0, j = 0;
    try
    {
      for (; i < param.length; i++)
      {
        SerializerState serializerState = new SerializerState();
        if (LocalArgController.isLocalArg(param[i]))
        {
          javaArgs[i] = LocalArgController.resolveLocalArg(context, param[i]);
        }
        else
        {
          javaArgs[i] = ser.unmarshall(serializerState, param[i],
            arguments.get(j++));
        }
      }
    }
    catch (JSONException e)
    {
      throw (NoSuchElementException) new NoSuchElementException(e.getMessage()).initCause(e);
    }
    catch (UnmarshallException f)
    {
      throw new UnmarshallException("arg " + (i + 1) + " " + f.getMessage());
    }

    return javaArgs;
  }
}
