/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: VectorSerializer.java,v 1.1.1.1 2004/03/31 14:21:01 mclark Exp $
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

import java.util.Vector;
import org.json.JSONObject;
import org.json.JSONArray;

class VectorSerializer extends Serializer
{
    private static Class[] _serializableClasses = new Class[]
	{ Vector.class };

    private static Class[] _JSONClasses = new Class[]
	{ JSONObject.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }


    public ObjectMatch doTryToUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String type = jso.getString("json_type");
	if(!type.equals("java.util.Vector"))
	    throw new UnmarshallException("not a Vector");
	JSONArray jsa = jso.getJSONArray("arr");
	int i = 0;
	ObjectMatch m = new ObjectMatch(-1);
	try {
	    for(; i < jsa.length(); i++)
		m = tryToUnmarshall(null, jsa.get(i)).max(m);
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
	String type = jso.getString("json_type");
	if(!type.equals("java.util.Vector"))
	    throw new UnmarshallException("not a Vector");
	JSONArray jsa = jso.getJSONArray("arr");
	int i = 0;
	Vector v = new Vector();
	try {
	    for(; i < jsa.length(); i++)
		v.add(unmarshall(null, jsa.get(i)));
	} catch (UnmarshallException e) {
	    throw new UnmarshallException
		("element " + i + " " + e.getMessage());
	}
	return v;
    }

    public Object doMarshall(Object o)
	throws MarshallException
    {
	Vector v = (Vector)o;
	JSONObject obj = new JSONObject();
	JSONArray arr = new JSONArray();
	obj.put("java_class", v.getClass().getName());
	obj.put("arr", arr);
	int i=0;
	try {
	    for(; i < v.size(); i++) arr.put(marshall(v.elementAt(i)));
	} catch (MarshallException e) {
	    throw new MarshallException
		("element " + i + " " + e.getMessage());
	}
	return obj;
    }

}
