/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: ReferenceSerializer.java,v 1.3 2004/04/04 16:08:22 mclark Exp $
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

    public boolean canSerialize(Class clazz, Class jsonClazz)
    {
	return (!clazz.isArray() &&
		!clazz.isPrimitive() &&
		!clazz.isInterface() &&
		(getBridge().isReference(clazz) ||
		 getBridge().isCallableReference(clazz)) &&
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
	String json_type = jso.getString("JSONRPCType");
	int object_id = jso.getInt("objectID");
	if(json_type != null && json_type.equals("Reference")) {
	    synchronized (getBridge().referenceMap) {
		ref = getBridge().referenceMap.get(new Integer(object_id));
	    }
	}
	return ref;
    }


    public Object doMarshall(Object o)
	throws MarshallException
    {
	Class clazz = o.getClass();
	if(getBridge().isReference(clazz)) {
	    if(getBridge().isDebug())
		System.out.println
		    ("ReferenceSerializer.doMarshall marshalling " +
		     "Reference to object " + o.hashCode() +
		     " of class " + clazz.getName());
	    synchronized (getBridge().referenceMap) {
		getBridge().referenceMap.put(new Integer(o.hashCode()), o);
	    }
	    JSONObject jso = new JSONObject();
	    jso.put("JSONRPCType", "Reference");
	    jso.put("javaClass", clazz.getName());
	    jso.put("objectID", o.hashCode());
	    return jso;
	} else if (getBridge().isCallableReference(clazz)) {
	    if(getBridge().isDebug())
		System.out.println
		    ("ReferenceSerializer.doMarshall marshalling " +
		     "CallableReference to object " + o.hashCode() +
		     " of class " + clazz.getName());
	    getBridge().registerObject(new Integer(o.hashCode()), o);
	    JSONObject jso = new JSONObject();
	    jso.put("JSONRPCType", "CallableReference");
	    jso.put("javaClass", clazz.getName());
	    jso.put("objectID", o.hashCode());
	    return jso;
	}
	return null;
    }

}
