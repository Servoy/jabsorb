/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: JSONRPCBridge.java,v 1.1.1.1 2004/03/31 14:20:59 mclark Exp $
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
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * This class implements a bridge that marshalls and unmarshalled JSON-RPC
 * requests recieved by the JSONRPCServlet and invokes methods on exported
 * Java objects.
 * <p />
 * An instance of the JSONRPCBridge object needs to be placed in a
 * HttpSession object registered under the attribute "JSONRPCBridge" to
 * allow the JSONRPCServlet to locate the bridge.
 * <p />
 * The bridge is implemented as session specific to improve the security
 * of applications by allowing exporting of object methods only to
 * specific HttpSessions.
 * <p />
 * To use the bridge to allow calling of Java methods you can easily
 * place a bridge in a HttpSession in JSP using the usebean tag. eg.
 * <p />
 * <code>&lt;jsp:useBean id="JSONRPCBridge" scope="session"
	class="com.metaparadigm.jsonrpc.JSONRPCBridge" /&gt;</code>
 * <p />
 * Then export the object you wish to call methods on. eg.
 * <p />
 * <code>JSONRPCBridge.registerObject("test", testObject);</code>
 * <p />
 * This will make available all methods of the object as <code>test.&lt;methodnames&gt;</code> to JSON-RPC clients.
 * <p />
 * There exists a global bridge singleton object that will allow exporting
 * objects to any HTTP client with no need for a HttpSession although it
 * is not recommended for general usage.
 */

public class JSONRPCBridge
{
    private boolean debug = false;

    public void setDebug(boolean debug) { this.debug = debug; }
    protected boolean isDebug() { return debug; }

    private static JSONRPCBridge globalBridge;

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

    // Refererence Serializer
    protected ReferenceSerializer refSerializer =
	new ReferenceSerializer(this);

    // Key Serializer
    private HashSet serializerSet = new HashSet();
    // key Class, val Serializer
    private HashMap serializableMap = new HashMap();
    // List for reverse registration order search
    private ArrayList serializerList = new ArrayList();


    static {
	globalBridge = new JSONRPCBridge();
	try {
	    globalBridge.registerSerializer(new ArraySerializer());
	    globalBridge.registerSerializer(new BeanSerializer());
	    globalBridge.registerSerializer(new VectorSerializer());
	    globalBridge.registerSerializer(new HashtableSerializer());
	    globalBridge.registerSerializer(new ArrayListSerializer());
	    globalBridge.registerSerializer(new StringSerializer());
	    globalBridge.registerSerializer(new NumberSerializer());
	    globalBridge.registerSerializer(new PrimitiveSerializer());
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
		ser.setBridge(this);
		System.out.println("JSONRPCBridge.registerSerializer " +
				   ser.getClass().getName());
		for(int i=0; i < classes.length; i++) {
		    serializableMap.put(classes[i], ser);
		}
		serializerSet.add(ser);
		serializerList.add(0, ser);
	    }
	}
    }


    private Serializer getSerializer(Class clazz, Class jsoClazz)
    {
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
	if(this != globalBridge)
	    return globalBridge.getSerializer(clazz, jsoClazz);
	else
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
     * <code>{"result": {"java_class":"com.metaparadigm.test.Foo",<br />
     *                   "object_id":5535614,<br />
     *                   "json_type":"Reference"} }</code>
     *
     * @param clazz The class object that should be marshalled as a reference.
     */
    public void registerReference(Class clazz)
	throws Exception
    {
	synchronized (refSerializer.referenceSet) {
	    refSerializer.referenceSet.add(clazz);
	}
	if(debug)
	    System.out.println
		("JSONRPCBridge.registerReference registered " +
		 clazz.getName());
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
     * <code>{"result": {"java_class":"com.metaparadigm.test.Bar",<br />
     *                   "object_id":4827452,<br />
     *                   "json_type":"CallableReference"} }</code>
     *
     * @param clazz The class object that should be marshalled as a callable reference.
     */
    public void registerCallableReference(Class clazz)
	throws Exception
    {
	synchronized (refSerializer.callableReferenceSet) {
	    refSerializer.callableReferenceSet.add(clazz);
	}
	if(debug)
	    System.out.println
		("JSONRPCBridge.registerCallableReference registered " +
		 clazz.getName());
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
	return oi;
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
	if((o instanceof Number) || (o instanceof String))
	    return o.getClass();

	if(o instanceof JSONObject) {
	    String class_name = ((JSONObject)o).getString("java_class");
	    if(class_name != null) {
		Class clazz = null;
		try {
		    clazz = Class.forName(class_name);
		} catch (Exception e) {}
		if(clazz == null)
		    throw new UnmarshallException("class in hint not found");
		return clazz;
	    }
	}

	throw new UnmarshallException("no class hint");
    }

    protected ObjectMatch tryToUnmarshall(Class clazz, Object jso)
	throws UnmarshallException
    {
	if(clazz == null)
	    clazz = getClassFromHint(jso);
	if(clazz.isInterface()) 
	    throw new UnmarshallException("can't marshall interface");
	if(jso == null) {
	    if(!clazz.isPrimitive())
		return ObjectMatch.NULL;
	    else
		throw new UnmarshallException("can't assign null primitive");
	}
	if(refSerializer.canSerialize(clazz, jso.getClass()))
	    return refSerializer.doTryToUnmarshall(clazz, jso);
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
	if(clazz.isInterface()) 
	    throw new UnmarshallException("can't marshall interface");
	if(jso == null) {
	    if(!clazz.isPrimitive())
		return ObjectMatch.NULL;
	    else
		throw new UnmarshallException("can't assign null primitive");
	}
	if(refSerializer.canSerialize(clazz, jso.getClass()))
	    return refSerializer.doUnmarshall(clazz, jso);
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


    protected Object marshall(Object o)
	throws MarshallException
    {
	if(debug)
	    System.out.println("JSONRPCBridge.marshall class " +
			       o.getClass().getName());
	if (o == null) return o;
	if(refSerializer.canSerialize(o.getClass(), null))
	    return refSerializer.doMarshall(o);
	Serializer ser = getSerializer(o.getClass(), null);
	if(ser != null) return ser.doMarshall(o);
	throw new MarshallException("can't marshall " +
				    o.getClass().getName());
    }


    protected JSONRPCResult call(int object_id, String classDotMethod,
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

	if(object_id == -1) {
	    // Handle "system.listMethods"
	    if(classDotMethod.equals("system.listMethods")) {
		return new JSONRPCResult(JSONRPCResult.CODE_SUCCESS,
					 listMethods());
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
		return new JSONRPCResult(JSONRPCResult.CODE_SUCCESS,
					 listMethods(oi.cd.methodMap));
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
	    Object o = method.invoke(itsThis, javaArgs);
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
	    CharArrayWriter caw = new CharArrayWriter();
	    e.printStackTrace(new PrintWriter(caw));
	    result = new JSONRPCResult(JSONRPCResult.CODE_EXCEPTION,
				       "Java Exception: " + caw.toString());
	}

	// Return the results
	return result;
    }


    private Object listMethods(HashMap methodMap)
    {
	HashSet m = new HashSet();
	Iterator i = methodMap.entrySet().iterator();
	while(i.hasNext()) {
	    Map.Entry mentry = (Map.Entry)i.next();
	    MethodKey mk = (MethodKey)mentry.getKey();
	    m.add(mk.methodName);
	}
	Iterator j = m.iterator();
	JSONArray methods = new JSONArray();
	while(j.hasNext()) {
	    String methodName = (String)j.next();
	    methods.put(methodName);
	}
	return methods;
    }


    private Object listMethods()
    {
	HashSet m = new HashSet();
	synchronized (classMap) {
	    Iterator i = classMap.entrySet().iterator();
	    while(i.hasNext()) {
		Map.Entry cdentry = (Map.Entry)i.next();
		String className = (String)cdentry.getKey();
		ClassData cd = (ClassData)cdentry.getValue();
		Iterator j = cd.staticMethodMap.entrySet().iterator();
		while(j.hasNext()) {
		    Map.Entry mentry = (Map.Entry)j.next();
		    MethodKey mk = (MethodKey)mentry.getKey();
		    m.add(className + "." + mk.methodName);
		}
	    }
	}
	synchronized (objectMap) {
	    Iterator i = objectMap.entrySet().iterator();
	    while(i.hasNext()) {
		Map.Entry oientry = (Map.Entry)i.next();
		Object key = oientry.getKey();
		if (!(key instanceof String)) continue;
		String name = (String)key;
		ObjectInstance oi = (ObjectInstance)oientry.getValue();
		Iterator j = oi.cd.methodMap.entrySet().iterator();
		while(j.hasNext()) {
		    Map.Entry mentry = (Map.Entry)j.next();
		    MethodKey mk = (MethodKey)mentry.getKey();
		    m.add(name + "." + mk.methodName);
		}
	    }
	}
	Iterator i = m.iterator();
	JSONArray methods = new JSONArray();
	while(i.hasNext()) {
	    String methodName = (String)i.next();
	    methods.put(methodName);
	}
	return methods;
    }

}
