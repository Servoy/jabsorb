/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: ListSerializer.java,v 1.2 2004/12/10 08:11:02 mclark Exp $
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

import java.util.List;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;
import org.json.JSONObject;
import org.json.JSONArray;

class ListSerializer extends Serializer
{
    private static Class[] _serializableClasses = new Class[]
	{ List.class, ArrayList.class, LinkedList.class, Vector.class };

    private static Class[] _JSONClasses = new Class[]
	{ JSONObject.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }

    public boolean canSerialize(Class clazz, Class jsonClazz)
    {
	return (super.canSerialize(clazz, jsonClazz) ||
		((jsonClazz == null || jsonClazz == JSONArray.class) &&
		 List.class.isAssignableFrom(clazz)));
    }

    public ObjectMatch doTryToUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String java_class = jso.getString("javaClass");
	if(java_class == null)
	    throw new UnmarshallException("no type hint");
	if(!(java_class.equals("java.util.List") ||
	     java_class.equals("java.util.AbstractList") ||
	     java_class.equals("java.util.LinkedList") ||
	     java_class.equals("java.util.ArrayList") ||
	     java_class.equals("java.util.Vector")))
	    throw new UnmarshallException("not a List");
	JSONArray jsonlist = jso.getJSONArray("list");
	if(jsonlist == null)
	    throw new UnmarshallException("list missing");
	int i = 0;
	ObjectMatch m = new ObjectMatch(-1);
	try {
	    for(; i < jsonlist.length(); i++)
		m = tryToUnmarshall(null, jsonlist.get(i)).max(m);
	} catch (UnmarshallException e) {
	    throw new UnmarshallException
		("element " + i + " " + e.getMessage());
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
	AbstractList al = null;
	if(java_class.equals("java.util.List") ||
	   java_class.equals("java.util.AbstractList") ||
	   java_class.equals("java.util.ArrayList")) {
	    al = new ArrayList();
	} else if(java_class.equals("java.util.LinkedList")) {
	    al = new LinkedList();
	} else if(java_class.equals("java.util.Vector")) {
	    al = new Vector();
	} else {
	    throw new UnmarshallException("not a List");
	}
	JSONArray jsonlist = jso.getJSONArray("list");
	if(jsonlist == null)
	    throw new UnmarshallException("list missing");
	int i = 0;
	try {
	    for(; i < jsonlist.length(); i++)
		al.add(unmarshall(null, jsonlist.get(i)));
	} catch (UnmarshallException e) {
	    throw new UnmarshallException
		("element " + i + " " + e.getMessage());
	}
	return al;
    }

    public Object doMarshall(Object o)
	throws MarshallException
    {
	List list = (List)o;
	JSONObject obj = new JSONObject();
	JSONArray arr = new JSONArray();
	obj.put("javaClass", o.getClass().getName());
	obj.put("list", arr);
	int index=0;
	try {
	    Iterator i = list.iterator();
	    while(i.hasNext()) {
		arr.put(marshall(i.next()));
		index++;
	    }
	} catch (MarshallException e) {
	    throw new MarshallException
		("element " + index + " " + e.getMessage());
	}
	return obj;
    }

}
