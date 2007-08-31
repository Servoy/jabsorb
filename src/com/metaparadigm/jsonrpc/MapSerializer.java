/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: MapSerializer.java,v 1.4 2005/06/16 23:26:14 mclark Exp $
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
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import org.json.JSONObject;

public class MapSerializer extends AbstractSerializer
{
    private static Class[] _serializableClasses = new Class[]
	{ Map.class, HashMap.class, TreeMap.class, LinkedHashMap.class };

    private static Class[] _JSONClasses = new Class[]
	{ JSONObject.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }

    public boolean canSerialize(Class clazz, Class jsonClazz)
    {
	return (super.canSerialize(clazz, jsonClazz) ||
		((jsonClazz == null || jsonClazz == JSONObject.class) &&
		 Map.class.isAssignableFrom(clazz)));
    }

    public ObjectMatch tryUnmarshall(SerializerState state,
				     Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String java_class = jso.getString("javaClass");
	if(java_class == null)
	    throw new UnmarshallException("no type hint");	
	if(!(java_class.equals("java.util.Map") ||
	     java_class.equals("java.util.AbstractMap") ||
	     java_class.equals("java.util.LinkedHashMap") ||
	     java_class.equals("java.util.TreeMap") ||
	     java_class.equals("java.util.HashMap")))
	    throw new UnmarshallException("not a Map");
	JSONObject jsonmap = jso.getJSONObject("map");
	if(jsonmap == null)
	    throw new UnmarshallException("map missing");
	ObjectMatch m = new ObjectMatch(-1);
	Iterator i = jsonmap.keys();
	String key = null;
	try {
	    while(i.hasNext()) {
		key = (String)i.next();
		m = ser.tryUnmarshall(state, null, jsonmap.get(key)).max(m);
	    }
	} catch (UnmarshallException e) {
	    throw new UnmarshallException
		("key " + key + " " + e.getMessage());
	}
	return m;
    }

    public Object unmarshall(SerializerState state, Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String java_class = jso.getString("javaClass");
	if(java_class == null)
	    throw new UnmarshallException("no type hint");	
	AbstractMap abmap = null;
	if(java_class.equals("java.util.Map") ||
	   java_class.equals("java.util.AbstractMap") ||
	   java_class.equals("java.util.HashMap")) {
	    abmap = new HashMap();
	} else if(java_class.equals("java.util.TreeMap")) {
	    abmap = new TreeMap();
	} else if(java_class.equals("java.util.LinkedHashMap")) {
	    abmap = new LinkedHashMap();
	} else {
	    throw new UnmarshallException("not a Map");
	}
	JSONObject jsonmap = jso.getJSONObject("map");
	if(jsonmap == null)
	    throw new UnmarshallException("map missing");
	Iterator i = jsonmap.keys();
	String key = null;
	try {
	    while(i.hasNext()) {
		key = (String)i.next();
		abmap.put(key, ser.unmarshall(state, null, jsonmap.get(key)));
	    }
	} catch (UnmarshallException e) {
	    throw new UnmarshallException
		("key " + key + " " + e.getMessage());
	}
	return abmap;
    }

    public Object marshall(SerializerState state, Object o)
	throws MarshallException
    {
	Map map = (Map)o;
	JSONObject obj = new JSONObject();
	JSONObject mapdata = new JSONObject();
        if (ser.getMarshallClassHints())
            obj.put("javaClass", o.getClass().getName());
	obj.put("map", mapdata);
	Object key = null;
	Object val = null;
	try {
	    Iterator i = map.entrySet().iterator();
	    while(i.hasNext()) {
		Map.Entry ent = (Map.Entry)i.next();
		key = ent.getKey();
		val = ent.getValue();
		// only support String keys
		mapdata.put(key.toString(), ser.marshall(state, val));
	    }
	} catch (MarshallException e) {
	    throw new MarshallException
		("map key " + key + " " + e.getMessage());
	}
	return obj;
    }

}
