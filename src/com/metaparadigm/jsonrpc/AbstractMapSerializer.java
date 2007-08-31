/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: AbstractMapSerializer.java,v 1.2 2004/04/04 16:08:22 mclark Exp $
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

import java.util.AbstractMap;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import org.json.JSONObject;

class AbstractMapSerializer extends Serializer
{
    private static Class[] _serializableClasses = new Class[]
	{ AbstractMap.class, HashMap.class, TreeMap.class,
	  LinkedHashMap.class };

    private static Class[] _JSONClasses = new Class[]
	{ JSONObject.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }


    public ObjectMatch doTryToUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String java_class = jso.getString("javaClass");
	if(java_class == null)
	    throw new UnmarshallException("no type hint");	
	if(!(java_class.equals("java.util.AbstractMap") ||
	     java_class.equals("java.util.LinkedHashMap") ||
	     java_class.equals("java.util.TreeMap") ||
	     java_class.equals("java.util.HashMap")))
	    throw new UnmarshallException("not an AbstractMap");
	JSONObject jsonmap = jso.getJSONObject("map");
	if(jsonmap == null)
	    throw new UnmarshallException("map missing");
	ObjectMatch m = new ObjectMatch(-1);
	Iterator i = jsonmap.keys();
	String key = null;
	try {
	    while(i.hasNext()) {
		key = (String)i.next();
		m = tryToUnmarshall(null, jsonmap.get(key)).max(m);
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
	AbstractMap abmap = null;
	if(java_class.equals("java.util.AbstractMap") ||
	   java_class.equals("java.util.HashMap")) {
	    abmap = new HashMap();
	} else if(java_class.equals("java.util.TreeMap")) {
	    abmap = new TreeMap();
	} else if(java_class.equals("java.util.LinkedHashMap")) {
	    abmap = new LinkedHashMap();
	} else {
	    throw new UnmarshallException("not an AbstractSet");
	}
	JSONObject jsonmap = jso.getJSONObject("map");
	if(jsonmap == null)
	    throw new UnmarshallException("map missing");
	Iterator i = jsonmap.keys();
	String key = null;
	try {
	    while(i.hasNext()) {
		key = (String)i.next();
		abmap.put(key, unmarshall(null, jsonmap.get(key)));
	    }
	} catch (UnmarshallException e) {
	    throw new UnmarshallException
		("key " + key + " " + e.getMessage());
	}
	return abmap;
    }

    public Object doMarshall(Object o)
	throws MarshallException
    {
	AbstractMap abmap = (AbstractMap)o;
	JSONObject obj = new JSONObject();
	JSONObject map = new JSONObject();
	obj.put("javaClass", o.getClass().getName());
	obj.put("map", abmap);
	Object key = null;
	Object val = null;
	try {
	    Iterator i = abmap.entrySet().iterator();
	    while(i.hasNext()) {
		Map.Entry ent = (Map.Entry)i.next();
		key = ent.getKey();
		val = ent.getValue();
		// only support String keys
		abmap.put(key.toString(), marshall(val));
	    }
	} catch (MarshallException e) {
	    throw new MarshallException
		("map key " + key + " " + e.getMessage());
	}
	return obj;
    }

}
