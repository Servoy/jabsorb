/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: JSONRPCBridge.java,v 1.35 2005/07/22 13:41:06 mclark Exp $
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public (LGPL)
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details: http://www.gnu.org/
 *
 */

package com.metaparadigm.jsonrpc;

import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * This class implements a bridge that unmarshalls JSON objects in JSON-RPC
 * request format, invokes a method on the exported object, and then marshalls
 * the resulting Java objects to JSON objects in JSON-RPC result format.
 * <p />
 * An instance of the JSONRPCBridge object is automatically placed in the
 * HttpSession object registered under the attribute "JSONRPCBridge" by
 * the JSONRPCServlet.
 * <p />
 * The bridge is implemented as session specific to improve the security
 * of applications by allowing exporting of object methods only to
 * specific HttpSessions.
 * <p />
 * To use the bridge to allow calling of Java methods you can easily
 * access the HttpSession specific bridge in JSP using the usebean tag. eg.
 * <p />
 * <code>&lt;jsp:useBean id="JSONRPCBridge" scope="session"
 *   class="com.metaparadigm.jsonrpc.JSONRPCBridge" /&gt;</code>
 * <p />
 * Then export the object you wish to call methods on. eg.
 * <p />
 * <code>JSONRPCBridge.registerObject("test", testObject);</code>
 * <p />
 * This will make available all methods of the object as
 * <code>test.&lt;methodnames&gt;</code> to JSON-RPC clients. This method
 * should generally be performed after an authentication check to only
 * export specific objects to clients that are authorised to use them.
 * <p />
 * There exists a global bridge singleton object that will allow exporting
 * objects to all HTTP clients. This can be used for registering factory
 * classes although care must be taken with authentication as these objects
 * will be accessible to all clients.
 */

public class JSONRPCBridge
{
    private final static Logger log =
	Logger.getLogger(JSONRPCBridge.class.getName());

    private boolean debug = false;

    public void setDebug(boolean debug) {
	this.debug = debug;
	ser.setDebug(debug);
    }
    protected boolean isDebug() { return debug; }

    private static JSONRPCBridge globalBridge = new JSONRPCBridge();

    /**
     * This method retreieves the global bridge singleton.
     *
     * It should be used with care as objects should generally be
     * registered within session specific bridges for security reasons.
     *
     * @return returns the global bridge object.
     */
    public static JSONRPCBridge getGlobalBridge() { return globalBridge; }

    // key clazz, val ClassData
    private static HashMap classCache = new HashMap();
    // key argClazz, val HashSet<LocalArgResolverData>
    private static HashMap localArgResolverMap = new HashMap();

    // JSONSerializer instance for this bridge
    private JSONSerializer ser = new JSONSerializer();

    // key CallbackData
    private HashSet callbackSet = new HashSet();
    // key "session exported class name", val Class
    private HashMap classMap = new HashMap();
    // key "session exported instance name", val ObjectInstance
    private HashMap objectMap = new HashMap();
    // key Integer hashcode, object held as reference
    protected HashMap referenceMap = new HashMap();

    // ReferenceSerializer if enabled
    Serializer referenceSer = null;
    // key clazz, classes that should be returned as References
    protected HashSet referenceSet = new HashSet();
    // key clazz, classes that should be returned as CallableReferences
    protected HashSet callableReferenceSet = new HashSet();


    private static HttpServletRequestArgResolver requestResolver =
	new HttpServletRequestArgResolver();
    private static HttpSessionArgResolver sessionResolver =
	new HttpSessionArgResolver();
    private static JSONRPCBridgeServletArgResolver bridgeResolver =
	new JSONRPCBridgeServletArgResolver();

    static
    {
	registerLocalArgResolver(javax.servlet.http.HttpServletRequest.class,
				 javax.servlet.http.HttpServletRequest.class,
				 requestResolver);
	registerLocalArgResolver(javax.servlet.http.HttpSession.class,
				 javax.servlet.http.HttpServletRequest.class,
				 sessionResolver);
	registerLocalArgResolver(JSONRPCBridge.class,
				 javax.servlet.http.HttpServletRequest.class,
				 bridgeResolver);
    }


    public JSONRPCBridge()
    {
        this(true);
    }

    public JSONRPCBridge(boolean useDefaultSerializers)
    {
        if (useDefaultSerializers) {
            try {
                ser.registerDefaultSerializers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    protected static class ClassData
    {
	private Class clazz;
	// key methodKey, val (Method || Method[])
	private HashMap methodMap;
	// key methodKey, val (Method || Method[])
	private HashMap staticMethodMap;
    }


    protected static class CallbackData
    {
	private InvocationCallback cb;
	private Class contextInterface;

	public CallbackData(InvocationCallback cb, Class contextInterface)
	{
	    this.cb = cb;
	    this.contextInterface = contextInterface;
	}

	public boolean understands(Object context)
	{
	    return contextInterface.isAssignableFrom(context.getClass());
	}

	public int hashCode()
	{
	    return cb.hashCode() * contextInterface.hashCode();
	}

	public boolean equals(Object o)
	{
	    CallbackData cmp = (CallbackData)o;
	    return (cb.equals(cmp.cb) &&
		    contextInterface.equals(cmp.contextInterface));
	}
    }


    protected static class LocalArgResolverData
    {
	private LocalArgResolver argResolver;
	private Class argClazz;
	private Class contextInterface;

	public LocalArgResolverData(LocalArgResolver argResolver,
				    Class argClazz,
				    Class contextInterface)
	{
	    this.argResolver = argResolver;
	    this.argClazz = argClazz;
	    this.contextInterface = contextInterface;
	}

	public boolean understands(Object context)
	{
	    return contextInterface.isAssignableFrom(context.getClass());
	}

	public int hashCode()
	{
	    return argResolver.hashCode() * argClazz.hashCode() *
		contextInterface.hashCode();
	}

	public boolean equals(Object o)
	{
	    LocalArgResolverData cmp = (LocalArgResolverData)o;
	    return (argResolver.equals(cmp.argResolver) &&
		    argClazz.equals(cmp.argClazz) &&
		    contextInterface.equals(cmp.contextInterface));
	}
    }


    protected static class ObjectInstance
    {
	private Object o;
	private Class clazz;

	public ObjectInstance(Object o)
	{
	    this.o = o;
	    clazz = o.getClass();
	}

	public ClassData classData()
	{
	    return getClassData(clazz);
	}
    }


    protected static class MethodKey
    {
	private String methodName;
	private int numArgs;

	public MethodKey(String methodName, int numArgs)
	{
	    this.methodName = methodName;
	    this.numArgs = numArgs;
	}

	public int hashCode()
	{
	    return methodName.hashCode() * numArgs;
	}

	public boolean equals(Object o)
	{
	    if(!(o instanceof MethodKey)) return false;
	    return (methodName.equals(((MethodKey)o).methodName) &&
		    numArgs == ((MethodKey)o).numArgs);
	}
    }


    protected static class MethodCandidate
    {
	private Method method;
	private ObjectMatch match[];

	public MethodCandidate(Method method)
	{
	    this.method = method;
	    match = new ObjectMatch[method.getParameterTypes().length];
	}

	public ObjectMatch getMatch()
	{
	    int mismatch = -1;
	    for(int i=0; i < match.length; i++) {
		mismatch = Math.max(mismatch, match[i].mismatch);
	    }
	    if(mismatch == -1) return ObjectMatch.OKAY;
	    else return new ObjectMatch(mismatch);
	}
    }


    private static ClassData analyzeClass(Class clazz)
    {
	log.info("analyzing " + clazz.getName());
	Method methods[] = clazz.getMethods();
	ClassData cd = new ClassData();
	cd.clazz = clazz;

	// Create temporary method map
	HashMap staticMethodMap = new HashMap();
	HashMap methodMap = new HashMap();
	for(int i=0; i < methods.length; i++) {
	    Method method = methods[i];
	    if(method.getDeclaringClass() == Object.class) continue;
	    int mod = methods[i].getModifiers();
	    if(!Modifier.isPublic(mod)) continue;
	    Class param[] = method.getParameterTypes();

	    // don't count locally resolved args
	    int argCount=0;
	    synchronized(localArgResolverMap) {
		for(int n=0; n<param.length; n++) {
		    HashSet resolvers = (HashSet)
			localArgResolverMap.get(param[n]);
		    if(resolvers != null) continue;
		    argCount++;
		}
	    }

	    MethodKey mk = new MethodKey(method.getName(), argCount);
	    ArrayList marr = (ArrayList)methodMap.get(mk);
	    if(marr == null) {
		marr = new ArrayList();
		methodMap.put(mk, marr);
	    }
	    marr.add(method);
	    if(Modifier.isStatic(mod)) {
		marr = (ArrayList)staticMethodMap.get(mk);
		if(marr == null) {
		    marr = new ArrayList();
		    staticMethodMap.put(mk, marr);
		}
		marr.add(method);
	    }
	}
	cd.methodMap = new HashMap();
	cd.staticMethodMap = new HashMap();
	// Convert ArrayLists to arrays
	Iterator i = methodMap.entrySet().iterator();
	while(i.hasNext()) {
	    Map.Entry entry = (Map.Entry)i.next();
	    MethodKey mk = (MethodKey)entry.getKey();
	    ArrayList marr = (ArrayList)entry.getValue();
	    if(marr.size() == 1) {
		cd.methodMap.put(mk, marr.get(0));
	    } else {
		cd.methodMap.put(mk, marr.toArray(new Method[0]));
	    }
	}
	i = staticMethodMap.entrySet().iterator();
	while(i.hasNext()) {
	    Map.Entry entry = (Map.Entry)i.next();
	    MethodKey mk = (MethodKey)entry.getKey();
	    ArrayList marr = (ArrayList)entry.getValue();
	    if(marr.size() == 1) {
		cd.staticMethodMap.put(mk, marr.get(0));
	    } else {
		cd.staticMethodMap.put(mk, marr.toArray(new Method[0]));
	    }
	}
	return cd;
    }


    private static ClassData getClassData(Class clazz)
    {
	ClassData cd;
	synchronized (classCache) {
	    cd = (ClassData)classCache.get(clazz);
	    if(cd == null) {
		cd = analyzeClass(clazz);
		classCache.put(clazz, cd);
	    }
	}
	return cd;
    }


    public void registerSerializer(Serializer s)
	throws Exception
    {
	ser.registerSerializer(s);
    }

    public void enableReferences()
	throws Exception
    {
	if(referenceSer == null) {
	    referenceSer = new ReferenceSerializer(this);
	    ser.registerSerializer(referenceSer);
	}
    }

    /**
     * Registers a class to be returned by reference and not by value
     * as is done by default.
     * <p />
     * The JSONBridge will take a references to these objects and return
     * an opaque object to the JSON-RPC client. When the opaque object
     * is passed back through the bridge in subsequent calls, the
     * original object is substitued in calls to Java methods. This
     * should be uses for any objects that contain security information
     * or complex types that are not required in the Javascript client
     * but need to be passed as a reference in methods of exported objects.
     * <p />
     * A Reference in JSON format looks like this:
     * <p />
     * <code>{"result": {"javaClass":"com.metaparadigm.test.Foo",<br />
     *                   "objectID":5535614,<br />
     *                   "JSONRPCType":"Reference"} }</code>
     *
     * @param clazz The class object that should be marshalled as a reference.
     */
    public void registerReference(Class clazz)
	throws Exception
    {
	synchronized (referenceSet) {
	    referenceSet.add(clazz);
	}
	if(debug)
	    log.info("registered reference " + clazz.getName());
    }

    protected boolean isReference(Class clazz)
    {
	if(referenceSet.contains(clazz)) return true;
	if(this == globalBridge) return false;
	return globalBridge.isReference(clazz);
    }


    /**
     * Registers a class to be returned as a callable reference.
     *
     * The JSONBridge will return a callable reference to the JSON-RPC client.
     * Callable references are not explicitly supported by the JSON-RPC.
     * The implementation in JSON-RPC-Java requires the JSON-RPC client
     * to connect to a different URL including the returned object_id to
     * call methods on the reference. The format of the URL is:
     * <p />
     * <code>/JSON-RPC?object_id=&lt;object_id&gt;</code>
     * <p />
     * A Callable Reference in JSON format looks like this:
     * <p />
     * <code>{"result": {"javaClass":"com.metaparadigm.test.Bar",<br />
     *                   "objectID":4827452,<br />
     *                   "JSONRPCType":"CallableReference"} }</code>
     *
     * @param clazz The class object that should be marshalled as a callable reference.
     */
    public void registerCallableReference(Class clazz)
	throws Exception
    {
	synchronized (callableReferenceSet) {
	    callableReferenceSet.add(clazz);
	}
	if(debug)
	    log.info("registered callable reference " + clazz.getName());
    }

    protected boolean isCallableReference(Class clazz)
    {
	if(callableReferenceSet.contains(clazz)) return true;
	if(this == globalBridge) return false;
	return globalBridge.isCallableReference(clazz);
    }


    /**
     * Registers a class to export static methods.
     *
     * The JSONBridge will export all static methods of the class.
     * This is useful for exporting factory classes that may then 
     * return CallableReferences to the JSON-RPC client.
     * <p />
     * To export instance methods you need to use registerObject.
     *
     * @param clazz The class to export static methods from.
     */
    public void registerClass(String name, Class clazz)
	throws Exception
    {
	synchronized (classMap) {
	    Class exists = (Class)classMap.get(name);
	    if(exists != null && exists != clazz)
		throw new Exception
		    ("different class registered as " + name);
	    if(exists == null)
		classMap.put(name, clazz);
	}
	if(debug)
	    log.info("registered class " + clazz.getName() + " as " + name);
    }


    private ClassData resolveClass(String className)
    {
	Class clazz = null;
	ClassData cd = null;

	synchronized (classMap) {
	    clazz = (Class)classMap.get(className);
	}

	if (clazz != null) cd = getClassData(clazz);

	if (cd != null) {
	    if(debug) log.fine("found class " + cd.clazz.getName() +
			       " named " + className);
	    return cd;
	}

	if(this != globalBridge)
	    return globalBridge.resolveClass(className);

	return null;
    }


    /**
     * Registers an object to export all instance methods and static methods.
     *
     * The JSONBridge will export all instance methods and static methods
     * of the particular object under the name passed in as a key.
     * <p />
     * This will make available all methods of the object as
     * <code>&lt;key&gt;.&lt;methodnames&gt;</code> to JSON-RPC clients.
     *
     * @param key The named prefix to export the object as
     * @param o The object instance to be called upon
     */
    public void registerObject(Object key, Object o)
    {
	Class clazz = o.getClass();
	ObjectInstance inst = new ObjectInstance(o);
	synchronized (objectMap) {
	    objectMap.put(key, inst);
	}
	if(debug)
	    log.info("registered object " + o.hashCode() +
		     " of class " + clazz.getName() + " as " + key);
    }


    private ObjectInstance resolveObject(Object key)
    {
	ObjectInstance oi;
	synchronized (objectMap) {
	    oi = (ObjectInstance)objectMap.get(key);
	}
	if(debug && oi != null)
	    log.fine("found object " + oi.o.hashCode() +
		     " of class " + oi.classData().clazz.getName() +
		     " with key " + key);
	if(oi == null && this != globalBridge)
	    return globalBridge.resolveObject(key);
	else
	    return oi;
    }


    /**
     * Registers a callback to be called before and after method invocation
     *
     * @param callback The object implementing the InvocationCallback Interface
     * @param contextInterface The type of transport Context interface the
       callback is interested in eg. HttpServletRequest.class for the servlet
       transport. */
    public void registerCallback(InvocationCallback callback,
				 Class contextInterface)
    {

	synchronized (callbackSet) {
	    callbackSet.add(new CallbackData(callback, contextInterface));
	}
	if(debug)
	    log.info("registered callback " + callback.getClass().getName() +
		     " with context interface " + contextInterface.getName());
    }

    /**
     * Unregisters a callback
     *
     * @param callback The previously registered InvocationCallback object
     * @param contextInterface The previously registered transport Context
     * interface. */
    public void unregisterCallback(InvocationCallback callback,
				   Class contextInterface)
    {

	synchronized (callbackSet) {
	    callbackSet.remove(new CallbackData(callback, contextInterface));
	}
	if(debug)
	    log.info("unregistered callback " + callback.getClass().getName() +
		     " with context " + contextInterface.getName());
    }


    /**
     * Registers a Class to be removed from the exported method signatures
     * and instead be resolved locally using context information
     * from the transport.
     *
     * @param argClazz The class to be resolved locally
     * @param argResolver The user defined class that resolves the
     * and returns the method argument using transport context information
     * @param contextInterface The type of transport Context object the
     * callback is interested in eg. HttpServletRequest.class for the
     * servlet transport */
    public static void registerLocalArgResolver(Class argClazz,
						Class contextInterface,
						LocalArgResolver argResolver)
    {
	synchronized (localArgResolverMap) {
	    HashSet resolverSet = (HashSet)localArgResolverMap.get(argClazz);
	    if(resolverSet == null) {
		resolverSet = new HashSet();
		localArgResolverMap.put(argClazz, resolverSet);
	    }
	    resolverSet.add(new LocalArgResolverData(argResolver,
						     argClazz,
						     contextInterface));
	    classCache = new HashMap(); // invalidate class cache
	}
	log.info("registered local arg resolver " +
		 argResolver.getClass().getName() +
		 " for local class " + argClazz.getName() +
		 " with context " + contextInterface.getName());
    }

    /**
     * Unregisters a LocalArgResolver</b>.
     *
     * @param argClazz The previously registered local class
     * @param argResolver The previously registered LocalArgResolver object
     * @param contextInterface The previously registered transport Context
     * interface. */
    public static void unregisterLocalArgResolver(Class argClazz,
						  Class contextInterface,
						  LocalArgResolver argResolver)
    {
	synchronized (localArgResolverMap) {
	    HashSet resolverSet = (HashSet)localArgResolverMap.get(argClazz);
	    if(resolverSet == null ||
	       !resolverSet.remove(new LocalArgResolverData(argResolver,
							    argClazz,
							    contextInterface)))
		{
		    log.warning("local arg resolver " +
				argResolver.getClass().getName() +
				" not registered for local class " +
				argClazz.getName() + " with context " +
				contextInterface.getName());
		    return;
		}
	    if(resolverSet.isEmpty()) localArgResolverMap.remove(argClazz);
	    classCache = new HashMap(); // invalidate class cache
	}
	log.info("unregistered local arg resolver " +
		 argResolver.getClass().getName() +
		 " for local class " + argClazz.getName() +
		 " with context " + contextInterface.getName());
    }


    private Method resolveMethod(HashMap methodMap, String methodName,
				JSONArray arguments)
    {
	Method method[] = null;
	MethodKey mk = new MethodKey(methodName, arguments.length());
	Object o = methodMap.get(mk);
	if(o instanceof Method) {
	    Method m = (Method)o;
	    if(debug)
		log.fine("found method " + methodName +
			 "(" + argSignature(m) + ")");
	    return m;
	}
	else if (o instanceof Method[]) method = (Method[])o;
	else return null;

	ArrayList candidate = new ArrayList();
	if(debug)
	    log.fine("looking for method " + methodName +
		     "(" + argSignature(arguments) + ")");
	for(int i=0; i < method.length; i++) {
	    try {
		candidate.add(tryUnmarshallArgs(method[i], arguments));
		if(debug)
		    log.fine("+++ possible match with method " + methodName +
			     "(" + argSignature(method[i]) + ")");
	    } catch (Exception e) {
		if(debug)
		    log.fine("xxx " + e.getMessage() + " in " + methodName +
			     "(" + argSignature(method[i]) + ")");
	    }
	}
	MethodCandidate best = null;
	for(int i=0; i < candidate.size(); i++) {
	    MethodCandidate c = (MethodCandidate)candidate.get(i);
        if (best == null)
        {
            best = c;
            continue;
        }
        final ObjectMatch bestMatch = best.getMatch();
        final ObjectMatch cMatch = c.getMatch();
	    if(bestMatch.mismatch > cMatch.mismatch)
            best = c;
        else if (bestMatch.mismatch == cMatch.mismatch)
            best = betterSignature(best, c);
	}
	if(best != null) {
	    Method m = best.method;
	    if(debug)
		log.fine("found method " +
			 methodName + "(" + argSignature(m) + ")");
	    return m;
	}
	return null;
    }

    private MethodCandidate betterSignature(MethodCandidate methodCandidate, MethodCandidate methodCandidate1)
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
                    c1++;
                else
                    c++;
            }
        }
        if (c1 > c)
            return methodCandidate1;
        else
            return methodCandidate;
    }

    private static String argSignature(Method method)
    {
	Class param[] = method.getParameterTypes();
	StringBuffer buf = new StringBuffer();
	for(int i=0; i < param.length; i++) {
	    if(i > 0) buf.append(",");
	    buf.append(param[i].getName());
	}
	return buf.toString();
    }


    private static String argSignature(JSONArray arguments)
    {
        StringBuffer buf = new StringBuffer();
        for(int i=0; i < arguments.length(); i += 1) {
	    if(i > 0) buf.append(",");
            Object jso = arguments.get(i);
            if (jso == null) {
                buf.append("java.lang.Object");
            } else if (jso instanceof String) {
                buf.append("java.lang.String");
            } else if (jso instanceof Number) {
                buf.append("java.lang.Number");
            } else if (jso instanceof JSONArray) {
                buf.append("java.lang.Object[]");
            } else {
                buf.append("java.lang.Object");
            }
        }
        return buf.toString();
    }

    private MethodCandidate tryUnmarshallArgs(Method method,
					      JSONArray arguments)
	throws UnmarshallException
    {
	MethodCandidate candidate = new MethodCandidate(method);
	Class param[] = method.getParameterTypes();
	int i = 0, j = 0;
	HashSet resolverSet;
	try {
	    for(; i < param.length; i++) {
		SerializerState state = new SerializerState();
		synchronized(localArgResolverMap) {
		    resolverSet = (HashSet)localArgResolverMap.get(param[i]);
		}
		if(resolverSet != null)
		    candidate.match[i] = ObjectMatch.OKAY;
		else
		    candidate.match[i] =
			ser.tryUnmarshall(state, param[i], arguments.get(j++));
	    }
	} catch(UnmarshallException e) {
	    throw new UnmarshallException
		("arg " + (i+1) + " " + e.getMessage());
	}
	return candidate;
    }

    private Object resolveLocalArg(Object context[], HashSet resolverSet)
	throws UnmarshallException
    {

	Iterator i = resolverSet.iterator();
	while(i.hasNext()) {
	    LocalArgResolverData resolverData =
		(LocalArgResolverData)i.next();
	    for(int j=0; j< context.length; j++) {
		if(resolverData.understands(context[j])) {
		    try {
			return resolverData.argResolver.resolveArg(context[j]);
		    } catch (LocalArgResolveException e) {
			throw new UnmarshallException
			    ("error resolving local argument: " + e);
		    }
		}
	    }
	}
	throw new UnmarshallException("couldn't find local arg resolver");
    }

    private Object[] unmarshallArgs(Object context[],
				    Method method, JSONArray arguments)
	throws UnmarshallException
    {
	Class param[] = method.getParameterTypes();
	Object javaArgs[] = new Object[param.length];
	int i = 0, j = 0;
	HashSet resolverSet;
	try {
	    for(; i < param.length; i++) {
		SerializerState state = new SerializerState();
		synchronized(localArgResolverMap) {
		    resolverSet = (HashSet)localArgResolverMap.get(param[i]);
		}
		if(resolverSet != null)
		    javaArgs[i] = resolveLocalArg(context, resolverSet);
		else
		    javaArgs[i] = ser.unmarshall(state,
						 param[i], arguments.get(j++));
	    }
	} catch(UnmarshallException e) {
	    throw new UnmarshallException
		("arg " + (i+1) + " " + e.getMessage());
	}
	return javaArgs;
    }

    private void preInvokeCallback(Object context, Object instance,
				   Method m, Object arguments[])
	throws Exception
    {
	synchronized (callbackSet) {
	    Iterator i = callbackSet.iterator();
	    while(i.hasNext()) {
		CallbackData cbdata = (CallbackData)i.next();
		if(cbdata.understands(context))
		    cbdata.cb.preInvoke(context, instance, m, arguments);
	    }
	}
    }

    private void postInvokeCallback(Object context, Object instance,
				    Method m, Object result)
	throws Exception
    {
	synchronized (callbackSet) {
	    Iterator i = callbackSet.iterator();
	    while(i.hasNext()) {
		CallbackData cbdata = (CallbackData)i.next();
		if(cbdata.understands(context))
		    cbdata.cb.postInvoke(context, instance, m, result);
	    }
	}
    }    

    public JSONRPCResult call(Object context[], JSONObject jsonReq)
    {
	JSONRPCResult result = null;
	String encodedMethod = null;
	Object requestId = null;
	JSONArray arguments = null;

	try {
	    // Get method name, arguments and request id
	    encodedMethod = jsonReq.getString("method");
	    arguments = jsonReq.getJSONArray("params");
	    requestId = jsonReq.opt("id");
	} catch (NoSuchElementException e) {
	    log.severe("no method or parameters in request");
	    return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD, null,
				     JSONRPCResult.MSG_ERR_NOMETHOD);
	}

	if(isDebug())
	    log.fine("call " + encodedMethod + "(" + arguments + ")" +
		     ", requestId=" + requestId);

	String className = null;
	String methodName = null;
	int objectID = 0;

	// Parse the class and methodName
	StringTokenizer t = new StringTokenizer(encodedMethod, ".");
	if(t.hasMoreElements()) className = t.nextToken();
	if(t.hasMoreElements()) methodName = t.nextToken();

	// See if we have an object method in the format ".obj#<objectID>"
	if(encodedMethod.startsWith(".obj#")) {
	    t = new StringTokenizer(className, "#");
	    t.nextToken();
	    objectID = Integer.parseInt(t.nextToken());
	}

	ObjectInstance oi = null;
	ClassData cd = null;
	HashMap methodMap = null;
	Method method = null;
	Object itsThis = null;

	if(objectID == 0) {
	    // Handle "system.listMethods"
	    if(encodedMethod.equals("system.listMethods")) {
		HashSet m = new HashSet();
		globalBridge.allInstanceMethods(m);
		if (globalBridge != this) {
		    globalBridge.allStaticMethods(m);
		    globalBridge.allInstanceMethods(m);
		}
		allStaticMethods(m);
		allInstanceMethods(m);
		JSONArray methods = new JSONArray();
		Iterator i = m.iterator();
		while(i.hasNext()) methods.put((String)i.next());
		return new JSONRPCResult
		    (JSONRPCResult.CODE_SUCCESS, requestId, methods);
	    }
	    // Look up the class, object instance and method objects
	    if(className == null || methodName == null ||
	       ((oi = resolveObject(className)) == null &&
		(cd = resolveClass(className)) == null))
		return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD,
					 requestId,
					 JSONRPCResult.MSG_ERR_NOMETHOD);
	    if(oi != null) {
		itsThis = oi.o;
		methodMap = oi.classData().methodMap;
	    } else {
		methodMap = cd.staticMethodMap;
	    }
	} else {
	    if((oi = resolveObject(new Integer(objectID))) == null)
		return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD,
					 requestId,
					 JSONRPCResult.MSG_ERR_NOMETHOD);
	    itsThis = oi.o;
	    methodMap = oi.classData().methodMap;
	    // Handle "system.listMethods"
	    if(methodName.equals("listMethods")) {
		HashSet m = new HashSet();
		uniqueMethods(m, "", oi.classData().staticMethodMap);
		uniqueMethods(m, "", oi.classData().methodMap);
		JSONArray methods = new JSONArray();
		Iterator i = m.iterator();
		while(i.hasNext()) methods.put((String)i.next());
		return new JSONRPCResult(JSONRPCResult.CODE_SUCCESS,
					 requestId, methods);
	    }
	}

	if((method = resolveMethod(methodMap, methodName, arguments)) == null)
	    return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD,
				     requestId,
				     JSONRPCResult.MSG_ERR_NOMETHOD);
	// Call the method
	try {
	    if(debug)
		log.fine("invoking " + method.getReturnType().getName() +
			 " " + method.getName() +
			 "(" + argSignature(method) + ")");
	    Object javaArgs[] = unmarshallArgs(context, method, arguments);
	    for(int i=0; i< context.length; i++)
		preInvokeCallback(context[i], itsThis, method, javaArgs);
	    Object o = method.invoke(itsThis, javaArgs);
	    for(int i=0; i< context.length; i++)
		postInvokeCallback(context[i], itsThis, method, o);
	    SerializerState state = new SerializerState();
	    result = new JSONRPCResult(JSONRPCResult.CODE_SUCCESS,
				       requestId, ser.marshall(state, o));
	} catch (UnmarshallException e) {
	    result = new JSONRPCResult(JSONRPCResult.CODE_ERR_UNMARSHALL,
				       requestId, e.getMessage());
	} catch (MarshallException e) {
	    result = new JSONRPCResult(JSONRPCResult.CODE_ERR_MARSHALL,
				       requestId, e.getMessage());
	} catch (Throwable e) {
	    if(e instanceof InvocationTargetException)
		e = ((InvocationTargetException)e).getTargetException();
	    result = new JSONRPCResult(JSONRPCResult.CODE_REMOTE_EXCEPTION,
				       requestId, e);
	}

	// Return the results
	return result;
    }


    private void uniqueMethods(HashSet m, String prefix, HashMap methodMap)
    {
	Iterator i = methodMap.entrySet().iterator();
	while(i.hasNext()) {
	    Map.Entry mentry = (Map.Entry)i.next();
	    MethodKey mk = (MethodKey)mentry.getKey();
	    m.add(prefix + mk.methodName);
	}
    }


    private void allStaticMethods(HashSet m)
    {
	synchronized (classMap) {
	    Iterator i = classMap.entrySet().iterator();
	    while(i.hasNext()) {
		Map.Entry cdentry = (Map.Entry)i.next();
		String name = (String)cdentry.getKey();
		Class clazz = (Class)cdentry.getValue();
		ClassData cd = getClassData(clazz);
		uniqueMethods(m, name + ".", cd.staticMethodMap);
	    }
	}
    }

    private void allInstanceMethods(HashSet m)
    {
	synchronized (objectMap) {
	    Iterator i = objectMap.entrySet().iterator();
	    while(i.hasNext()) {
		Map.Entry oientry = (Map.Entry)i.next();
		Object key = oientry.getKey();
		if (!(key instanceof String)) continue;
		String name = (String)key;
		ObjectInstance oi = (ObjectInstance)oientry.getValue();
		uniqueMethods(m, name + ".", oi.classData().methodMap);
		uniqueMethods(m, name + ".", oi.classData().staticMethodMap);
	    }
	}
    }

    public JSONSerializer getSerializer() {
        return ser;
    }
    public void setSerializer(JSONSerializer ser) {
        this.ser = ser;
    }
}
