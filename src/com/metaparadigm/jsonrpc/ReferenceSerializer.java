/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: ReferenceSerializer.java,v 1.1.1.1 2004/03/31 14:21:02 mclark Exp $
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

import java.util.HashSet;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONArray;

class ReferenceSerializer extends Serializer
{
    // key Integer hashcode, object held as reference
    protected HashMap referenceMap = new HashMap();

    // key clazz, classes that should be returned as References
    protected HashSet referenceSet = new HashSet();

    // key clazz, classes that should be returned as CallableReferences
    protected HashSet callableReferenceSet = new HashSet();


    public ReferenceSerializer(JSONRPCBridge bridge) {
	setBridge(bridge);
    }


    public boolean canSerialize(Class clazz, Class jsonClazz)
    {
	return (!clazz.isArray() &&
		!clazz.isPrimitive() &&
		!clazz.isInterface() &&
		(referenceSet.contains(clazz) ||
		 callableReferenceSet.contains(clazz)) &&
		(jsonClazz == null || jsonClazz == JSONObject.class));
    }


    public ObjectMatch doTryToUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	return ObjectMatch.OKAY;
    }

    public Object doUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	Object ref = null;
	String json_type = jso.getString("json_type");
	int object_id = jso.getInt("object_id");
	if(json_type != null && json_type.equals("Reference")) {
	    synchronized (referenceMap) {
		ref = referenceMap.get(new Integer(object_id));
	    }
	}
	return ref;
    }

    public Object doMarshall(Object o)
	throws MarshallException
    {
	Class clazz = o.getClass();
	if(referenceSet.contains(clazz)) {
	    if(getBridge().isDebug())
		System.out.println
		    ("ReferenceSerializer.doMarshall marshalling " +
		     "Reference to object " + o.hashCode() +
		     " of class " + clazz.getName());
	    synchronized (referenceMap) {
		referenceMap.put(new Integer(o.hashCode()), o);
	    }
	    JSONObject jso = new JSONObject();
	    jso.put("json_type", "Reference");
	    jso.put("java_class", clazz.getName());
	    jso.put("object_id", o.hashCode());
	    return jso;
	} else if (callableReferenceSet.contains(clazz)) {
	    if(getBridge().isDebug())
		System.out.println
		    ("ReferenceSerializer.doMarshall marshalling " +
		     "CallableReference to object " + o.hashCode() +
		     " of class " + clazz.getName());
	    getBridge().registerObject(new Integer(o.hashCode()), o);
	    JSONObject jso = new JSONObject();
	    jso.put("json_type", "CallableReference");
	    jso.put("java_class", clazz.getName());
	    jso.put("object_id", o.hashCode());
	    return jso;
	}
	return null;
    }

}
