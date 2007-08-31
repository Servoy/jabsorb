/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: HashtableSerializer.java,v 1.1.1.1 2004/03/31 14:21:01 mclark Exp $
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

import java.util.Hashtable;
import java.util.Enumeration;
import org.json.JSONObject;

class HashtableSerializer extends Serializer
{
    private static Class[] _serializableClasses = new Class[]
	{ Hashtable.class };

    private static Class[] _JSONClasses = new Class[]
	{ JSONObject.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }


    public ObjectMatch doTryToUnmarshall(Class clazz, Object jso)
	throws UnmarshallException
    {
	return ObjectMatch.OKAY;
    }

    public Object doUnmarshall(Class clazz, Object jso)
	throws UnmarshallException
    {
	return null;
    }

    public Object doMarshall(Object o)
	throws MarshallException
    {
	Hashtable ht = (Hashtable)o;
	JSONObject jso = new JSONObject();
	jso.put("java_class", ht.getClass().getName());
	int i=0;
	try {
	    Enumeration keys = ht.keys();
	    while(keys.hasMoreElements()) {
		// only support String keys
		Object key = keys.nextElement();
		jso.put(key.toString(), marshall(ht.get(key)));
	    }
	} catch (MarshallException e) {
	    throw new MarshallException
		("element " + i + " " + e.getMessage());
	}
	return jso;
    }

}
