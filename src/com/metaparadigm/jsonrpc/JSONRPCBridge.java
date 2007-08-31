/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: JSONRPCBridge.java,v 1.14 2005/01/21 00:10:50 mclark Exp $
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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * This class implements a bridge that marshalls and unmarshalled JSON-RPC
 * requests recieved by the JSONRPCServlet and invokes methods on exported
 * Java objects.
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
	class="com.metaparadigm.jsonrpc.JSONRPCBridge" /&gt;</code>
 * <p />
 * Then export the object you wish to call methods on. eg.
 * <p />
 * <code>JSONRPCBridge.registerObject("test", testObject);</code>
 * <p />
 * This will make available all methods of the object as <code>test.&lt;methodnames&gt;</code> to JSON-RPC clients. This method should generally be performed
 * after an authentication check to only export specific objects to clients
 * that are authorised to use them.
 * <p />
 * There exists a global bridge singleton object that will allow exporting
 * objects to all HTTP clients. This can be used for registering factory
 * classes although care must be taken with authentication as these objects
 * will be accessible to all clients.
 */

public class JSONRPCBridge
{
    private boolean debug = false;

    public void setDebug(boolean debug) { this.debug = debug; }
    protected boolean isDebug() { return debug; }

    private static JSONRPCBridge globalBridge = new JSONRPCBridge(false);

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


    // key "session exported class name", val ClassData
    private HashMap classMap = new HashMap();
    // key "session exported instance name", val ObjectInstance
    private HashMap objectMap = new HashMap();
    // key Integer hashcode, object held as reference
    protected HashMap referenceMap = new HashMap();
    // key JSONRPCCallback
    private HashSet callbackSet = new HashSet();

    // key clazz, classes that should be returned as References
    protected HashSet referenceSet = new HashSet();
    // key clazz, classes that should be returned as CallableReferences
    protected HashSet callableReferenceSet = new HashSet();
    // Key Serializer
    private HashSet serializerSet = new HashSet();
    // key Class, val Serializer
    private HashMap serializableMap = new HashMap();
    // List for reverse registration order search
    private ArrayList serializerList = new ArrayList();


    public JSONRPCBridge(boolean global) {}

    public JSONRPCBridge()
    {
	try {
	    registerSerializer(new ArraySerializer());
	    registerSerializer(new BeanSerializer());
	    registerSerializer(new ReferenceSerializer());
	    registerSerializer(new DictionarySerializer());
	    registerSerializer(new MapSerializer());
	    registerSerializer(new SetSerializer());
	    registerSerializer(new ListSerializer());
	    registerSerializer(new DateSerializer());
	    registerSerializer(new StringSerializer());
	    registerSerializer(new NumberSerializer());
	    registerSerializer(new BooleanSerializer());
	    registerSerializer(new PrimitiveSerializer());
	} catch (Exception e) {
	    e.printStackTrace();
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


    protected static class ObjectInstance
    {
	private Object o;
	private ClassData cd;

	public ObjectInstance(Object o)
	{
	    this.o = o;
	    cd = getClassData(o.getClass());
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
	System.out.println("JSONRPCBridge.analyzeClass analyzing " +
			   clazz.getName());
	Method methods[] = clazz.getDeclaredMethods();
	ClassData cd = new ClassData();
	cd.clazz = clazz;

	// Create temporary method map
	HashMap staticMethodMap = new HashMap();
	HashMap methodMap = new HashMap();
	for(int i=0; i < methods.length; i++) {
	    Method method = methods[i];
	    int mod = methods[i].getModifiers();
	    Class param[] = method.getParameterTypes();
	    MethodKey mk = new MethodKey(method.getName(), param.length);
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


    private void registerSerializer(Serializer ser)
	throws Exception
    {
	Class classes[] = ser.getSerializableClasses();
	Serializer exists;
	synchronized (serializerSet) {
	    for(int i=0; i < classes.length; i++) {
		exists = (Serializer)serializableMap.get(classes[i]);
		if(exists != null && exists.getClass() != ser.getClass())
		    throw new Exception
			("different serializer already registered for " +
			 classes[i].getName());
	    }
	    if(!serializerSet.contains(ser)) {
		if(debug)
		    System.out.println("JSONRPCBridge.registerSerializer " +
				       ser.getClass().getName());
		for(int i=0; i < classes.length; i++) {
		    serializableMap.put(classes[i], ser);
		}
		ser.setBridge(this);
		serializerSet.add(ser);
		serializerList.add(0, ser);
	    }
	}
    }


    private Serializer getSerializer(Class clazz, Class jsoClazz)
    {
	if(debug)
	    System.out.println
		("JSONRPCBridge.getSerializer java:" +
		 (clazz == null ? "null" : clazz.getName()) +
		 " json:" +
		 (jsoClazz == null ? "null" : jsoClazz.getName()));

	Serializer ser = null;
	synchronized (serializerSet) {
	    ser = (Serializer)serializableMap.get(clazz);
	    if(ser != null && ser.canSerialize(clazz, jsoClazz)) {
		if(debug)
		    System.out.println
			("JSONRPCBridge.getSerializer got " +
			 ser.getClass().getName());
		return ser;
	    }
	    Iterator i = serializerList.iterator();
	    while(i.hasNext()) {
		ser = (Serializer)i.next();
		if(ser.canSerialize(clazz, jsoClazz)) {
		    if(debug)
			System.out.println
			    ("JSONRPCBridge.getSerializer search found " +
			     ser.getClass().getName()); 
		    return ser;
		}
	    }
	}
	return null;
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
	    System.out.println
		("JSONRPCBridge.registerReference registered " +
		 clazz.getName());
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
	    System.out.println
		("JSONRPCBridge.registerCallableReference registered " +
		 clazz.getName());
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
	    ClassData cd = (ClassData)classMap.get(name);
	    if(cd != null && cd.clazz != clazz)
		throw new Exception
		    ("different class registered as " + name);
	    if(cd == null) {
		cd = getClassData(clazz);
		classMap.put(name, cd);
	    }
	}
	if(debug)
	    System.out.println
		("JSONRPCBridge.registerClass registered " +
		 clazz.getName() + " as " + name);
    }


    private ClassData resolveClass(String className)
    {
	ClassData cd = null;
	synchronized (classMap) {
	    cd = (ClassData)classMap.get(className);
	}
	if(debug && cd != null)
	    System.out.println
		("JSONRPCBridge.resolveClass found class " +
		 cd.clazz.getName() + " named " + className);
	if(cd == null && this != globalBridge)
	    return globalBridge.resolveClass(className);
	else
	    return cd;
    }


    /**
     * Registers an object to export all instance methods and static methods.
     *
     * The JSONBridge will export all instance methods and static methods
     * of the particular object under the name passed in as a key.
     * <p />
      * This will make available all methods of the object as <code>&lt;key&gt;.&lt;methodnames&gt;</code> to JSON-RPC clients.
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
	    System.out.println
		("JSONRPCBridge.registerObject object " + o.hashCode() +
		 " of class " + clazz.getName() + " as " + key);
    }


    private ObjectInstance resolveObject(Object key)
    {
	ObjectInstance oi;
	synchronized (objectMap) {
	    oi = (ObjectInstance)objectMap.get(key);
	}
	if(debug && oi != null)
	    System.out.println
		("JSONRPCBridge.resolveObject found object " +
		 oi.o.hashCode() + " of class " + oi.cd.clazz.getName() +
		 " with key " + key);
	if(oi == null && this != globalBridge)
	    return globalBridge.resolveObject(key);
	else
	    return oi;
    }


    /**
     * Registers a callback to be called before and after method invocation
     *
     *
     * @param callback The object implementing the JSONRPCCallback Interface
     */
    public void registerCallback(JSONRPCCallback callback)
    {
	synchronized (callbackSet) {
	    callbackSet.add(callback);
	}
	if(debug)
	    System.out.println
		("JSONRPCBridge.registerCallback " +
		 callback.getClass().getName());
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
		System.out.println
		    ("JSONRPCBridge.resolveMethod found " +
		     methodName + "(" + argSignature(m) + ")");
	    return m;
	}
	else if (o instanceof Method[]) method = (Method[])o;
	else return null;

	ArrayList candidate = new ArrayList();
	if(debug)
	    System.out.println("JSONRPCBridge.resolveMethod looking for " +
			       methodName +
			       "(" + argSignature(arguments) + ")");
	for(int i=0; i < method.length; i++) {
	    try {
		candidate.add(tryToUnmarshallArgs(method[i], arguments));
		if(debug)
		    System.out.println
			("JSONRPCBridge.resolveMethod " +
			 "+++ possible match with " + methodName +
			 "(" + argSignature(method[i]) + ")");
	    } catch (Exception e) {
		if(debug)
		    System.out.println
			("JSONRPCBridge.resolveMethod " +
			 "xxx " + e.getMessage() + " in " + methodName +
			 "(" + argSignature(method[i]) + ")");
	    }
	}
	MethodCandidate best = null;
	for(int i=0; i < candidate.size(); i++) {
	    MethodCandidate c = (MethodCandidate)candidate.get(i);
	    if(best == null || best.getMatch().mismatch >
	       c.getMatch().mismatch) best = c;
	}
	if(best != null) {
	    Method m = best.method;
	    if(debug)
		System.out.println("JSONRPCBridge.resolveMethod found " +
				   methodName + "(" + argSignature(m) + ")");
	    return m;
	}
	return null;
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


    private Class getClassFromHint(Object o)
	throws UnmarshallException
    {
	if(o == null) return null;
	if(o instanceof JSONObject) {
	    try {
		String class_name = ((JSONObject)o).getString("javaClass");
		Class clazz = Class.forName(class_name);
		return clazz;
	    } catch (NoSuchElementException e) {
	    } catch (Exception e) {
		throw new UnmarshallException("class in hint not found");
	    }
	}
	return o.getClass();
    }

    protected ObjectMatch tryToUnmarshall(Class clazz, Object jso)
	throws UnmarshallException
    {
	if(clazz == null)
	    clazz = getClassFromHint(jso);
	if(clazz == null)
	    throw new UnmarshallException("no class hint");
	if(jso == null) {
	    if(!clazz.isPrimitive())
		return ObjectMatch.NULL;
	    else
		throw new UnmarshallException("can't assign null primitive");
	}
	Serializer ser = getSerializer(clazz, jso.getClass());
	if(ser != null) return ser.doTryToUnmarshall(clazz, jso);

	throw new UnmarshallException("no match");
    }


    private MethodCandidate tryToUnmarshallArgs(Method method,
					       JSONArray arguments)
	throws UnmarshallException
    {
	MethodCandidate candidate = new MethodCandidate(method);
	Class param[] = method.getParameterTypes();
	int i = 0;
	try {
	    for(; i < param.length; i++)
		candidate.match[i] =
		    tryToUnmarshall(param[i], arguments.get(i));
	} catch(UnmarshallException e) {
	    throw new UnmarshallException
		("arg " + (i+1) + " " + e.getMessage());
	}
	return candidate;
    }


    protected Object unmarshall(Class clazz, Object jso)
	throws UnmarshallException
    {
	if(clazz == null)
	    clazz = getClassFromHint(jso);
	if(clazz == null)
	    throw new UnmarshallException("no class hint");
	if(jso == null) {
	    if(!clazz.isPrimitive())
		return ObjectMatch.NULL;
	    else
		throw new UnmarshallException("can't assign null primitive");
	}
	Serializer ser = getSerializer(clazz, jso.getClass());
	if(ser != null) return ser.doUnmarshall(clazz, jso);

	throw new UnmarshallException("can't unmarshall");
    }


    private Object[] unmarshallArgs(Method method, JSONArray arguments)
	throws UnmarshallException
    {
	Object javaArgs[] = new Object[arguments.length()];
	Class param[] = method.getParameterTypes();
	int i = 0;
	try {
	    for(; i < param.length; i++)
		javaArgs[i] = unmarshall(param[i], arguments.get(i));
	} catch(UnmarshallException e) {
	    throw new UnmarshallException
		("arg " + (i+1) + " " + e.getMessage());
	}
	return javaArgs;
    }
    
    protected boolean isMarshalledObjectNull(Object o){
        boolean isNull = o == null;
        
        if(debug){
            if(isNull){
                System.out.println("JSONRPCBridge.marshall null");
            } else{
                System.out.println("JSONRPCBridge.marshall class " +
                           o.getClass().getName());
            }
        }
        return isNull;
    }

    protected Object marshall(Object o)
	throws MarshallException
    {
	if (isMarshalledObjectNull(o)) return o;
	Serializer ser = getSerializer(o.getClass(), null);
	if(ser != null) return ser.doMarshall(o);
	throw new MarshallException("can't marshall " +
				    o.getClass().getName());
    }

    private void preInvokeCallback(Object context, Object instance,
				   Method m, Object arguments[])
	throws Exception
    {
	synchronized (callbackSet) {
	    Iterator i = callbackSet.iterator();
	    while(i.hasNext()) {
		JSONRPCCallback callback = (JSONRPCCallback)i.next();
		callback.preInvoke(context, instance, m, arguments);
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
		JSONRPCCallback callback = (JSONRPCCallback)i.next();
		callback.postInvoke(context, instance, m, result);
	    }
	}
    }    

    protected JSONRPCResult call(Object context,
				 int object_id, String classDotMethod,
				 JSONArray arguments)
    {
	// Parse the class and methodName
	StringTokenizer t = new StringTokenizer(classDotMethod, ".");
	String className = null, methodName = null;
	if(t.hasMoreElements()) className = t.nextToken();
	if(t.hasMoreElements()) methodName = t.nextToken();

	ObjectInstance oi = null;
	ClassData cd = null;
	HashMap methodMap = null;
	Method method = null;
	Object itsThis = null;

	if(object_id == 0) {
	    // Handle "system.listMethods"
	    if(classDotMethod.equals("system.listMethods")) {
		HashSet m = new HashSet();
		globalBridge.allStaticMethods(m);
		globalBridge.allInstanceMethods(m);
		allStaticMethods(m);
		allInstanceMethods(m);
		JSONArray methods = new JSONArray();
		Iterator i = m.iterator();
		while(i.hasNext()) methods.put((String)i.next());
		return new JSONRPCResult(JSONRPCResult.CODE_SUCCESS, methods);
	    }
	    // Look up the class, object instance and method objects
	    if(className == null || methodName == null ||
	       ((oi = resolveObject(className)) == null &&
		(cd = resolveClass(className)) == null))
		return JSONRPCResult.ERR_NOMETHOD;	    
	    if(oi != null) {
		itsThis = oi.o;
		methodMap = oi.cd.methodMap;
	    } else {
		methodMap = cd.staticMethodMap;
	    }
	} else {
	    if((oi = resolveObject(new Integer(object_id))) == null)
		return JSONRPCResult.ERR_NOMETHOD;
	    itsThis = oi.o;
	    methodMap = oi.cd.methodMap;
	    // Handle "system.listMethods"
	    if(classDotMethod.equals("system.listMethods")) {
		HashSet m = new HashSet();
		uniqueMethods(m, "", oi.cd.staticMethodMap);
		uniqueMethods(m, "", oi.cd.methodMap);
		JSONArray methods = new JSONArray();
		Iterator i = m.iterator();
		while(i.hasNext()) methods.put((String)i.next());
		return new JSONRPCResult(JSONRPCResult.CODE_SUCCESS, methods);
	    }
	    methodName = className;
	}
	if((method = resolveMethod(methodMap, methodName, arguments)) == null)
	    return JSONRPCResult.ERR_NOMETHOD;

	// Call the method
	JSONRPCResult result = null;
	try {
	    if(debug)
		System.out.println
		    ("JSONRPCBridge.call invoking " +
		     method.getReturnType().getName() + " " +
		     method.getName() + "(" + argSignature(method) + ")");
	    Object javaArgs[] = unmarshallArgs(method, arguments);
	    preInvokeCallback(context, itsThis, method, javaArgs);
	    Object o = method.invoke(itsThis, javaArgs);
	    postInvokeCallback(context, itsThis, method, o);
	    result = new JSONRPCResult(JSONRPCResult.CODE_SUCCESS,
				       marshall(o));
	} catch (UnmarshallException e) {
	    result = new JSONRPCResult(JSONRPCResult.CODE_ERR_UNMARSHALL,
				       e.getMessage());
	} catch (MarshallException e) {
	    result = new JSONRPCResult(JSONRPCResult.CODE_ERR_MARSHALL,
				       e.getMessage());
	} catch (Throwable e) {
	    if(e instanceof InvocationTargetException)
		e = ((InvocationTargetException)e).getTargetException();
	    result = new JSONRPCResult(JSONRPCResult.CODE_EXCEPTION, e);
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
		ClassData cd = (ClassData)cdentry.getValue();
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
		uniqueMethods(m, name + ".", oi.cd.methodMap);
		uniqueMethods(m, name + ".", oi.cd.staticMethodMap);
	    }
	}
    }

}
