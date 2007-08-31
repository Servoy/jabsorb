/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: SetSerializer.java,v 1.3 2005/01/21 00:10:50 mclark Exp $
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

import java.util.Set;
import java.util.AbstractSet;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Iterator;
import org.json.JSONObject;

class SetSerializer extends Serializer
{
    private static Class[] _serializableClasses = new Class[]
	{ Set.class, HashSet.class, TreeSet.class, LinkedHashSet.class };

    private static Class[] _JSONClasses = new Class[]
	{ JSONObject.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }

    public boolean canSerialize(Class clazz, Class jsonClazz)
    {
	return (super.canSerialize(clazz, jsonClazz) ||
		((jsonClazz == null || jsonClazz == JSONObject.class) &&
		 Set.class.isAssignableFrom(clazz)));
    }

    public ObjectMatch doTryToUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String java_class = jso.getString("javaClass");
	if(java_class == null)
	    throw new UnmarshallException("no type hint");	
	if(!(java_class.equals("java.util.Set") ||
	     java_class.equals("java.util.AbstractSet") ||
	     java_class.equals("java.util.LinkedHashSet") ||
	     java_class.equals("java.util.TreeSet") ||
	     java_class.equals("java.util.HashSet")))
	    throw new UnmarshallException("not a Set");
	JSONObject jsonset = jso.getJSONObject("set");
	if(jsonset == null)
	    throw new UnmarshallException("set missing");
    
    ObjectMatch m = new ObjectMatch(-1);
    
    Iterator i = jsonset.keys();
    String key = null;
    
    try {
        while(i.hasNext()) {
        key = (String)i.next();
        m = tryToUnmarshall(null, jsonset.get(key)).max(m);
        }
    } catch (UnmarshallException e) {
        throw new UnmarshallException
        ("key " + key + " " + e.getMessage());
    }
    return m;
    }

    public Object doUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String java_class = jso.getString("javaClass");
	if(java_class == null)
	    throw new UnmarshallException("no type hint");	
	AbstractSet abset = null;
	if(java_class.equals("java.util.Set") ||
	   java_class.equals("java.util.AbstractSet") ||
	   java_class.equals("java.util.HashSet")) {
	    abset= new HashSet();
	} else if(java_class.equals("java.util.TreeSet")) {
	    abset= new TreeSet();
	} else if(java_class.equals("java.util.LinkedHashSet")) {
	    abset= new LinkedHashSet();
	} else {
	    throw new UnmarshallException("not a Set");
	}
	JSONObject jsonset = jso.getJSONObject("set");
    
    if(jsonset == null)
        throw new UnmarshallException("set missing");
    
	Iterator i = jsonset.keys();
    String key = null;
    
    try {
    	while(i.hasNext()) {
            key = (String)i.next();
            Object setElement = jsonset.get(key);            
    	    abset.add(unmarshall(null, setElement));
    	}
    }catch (UnmarshallException e) {
        throw new UnmarshallException("key " + i + e.getMessage());
    }
	return abset;
    }

    public Object doMarshall(Object o)
	throws MarshallException
    {
	Set set = (Set)o;

	JSONObject obj = new JSONObject();
	JSONObject setdata = new JSONObject();
	obj.put("javaClass", o.getClass().getName());
	obj.put("set", setdata);
	Object key = null;
	Iterator i = set.iterator();
    
    try{
    	while(i.hasNext()) {
    	    key = i.next();
    	    // only support String keys
    	    setdata.put(key.toString(), marshall(key));
    	}
    }catch (MarshallException e) {
        throw new MarshallException("set key " + key + e.getMessage());
    }
	return obj;
    }

}
