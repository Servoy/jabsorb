/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: AbstractSetSerializer.java,v 1.1 2004/04/01 06:51:29 mclark Exp $
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

import java.util.AbstractSet;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Iterator;
import org.json.JSONObject;

class AbstractSetSerializer extends Serializer
{
    private static Class[] _serializableClasses = new Class[]
	{ AbstractSet.class, HashSet.class, TreeSet.class,
	  LinkedHashSet.class };

    private static Class[] _JSONClasses = new Class[]
	{ JSONObject.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }


    public ObjectMatch doTryToUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String java_class = jso.getString("java_class");
	if(java_class == null)
	    throw new UnmarshallException("no type hint");	
	if(!(java_class.equals("java.util.AbstractSet") ||
	     java_class.equals("java.util.LinkedHashSet") ||
	     java_class.equals("java.util.TreeSet") ||
	     java_class.equals("java.util.HashSet")))
	    throw new UnmarshallException("not an AbstractSet");
	JSONObject jsonset = jso.getJSONObject("set");
	if(jsonset == null)
	    throw new UnmarshallException("set missing");
	return ObjectMatch.OKAY;
    }

    public Object doUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String java_class = jso.getString("java_class");
	if(java_class == null)
	    throw new UnmarshallException("no type hint");	
	AbstractSet abset = null;
	if(java_class.equals("java.util.AbstractSet") ||
	   java_class.equals("java.util.HashSet")) {
	    abset= new HashSet();
	} else if(java_class.equals("java.util.TreeSet")) {
	    abset= new TreeSet();
	} else if(java_class.equals("java.util.LinkedHashSet")) {
	    abset= new LinkedHashSet();
	} else {
	    throw new UnmarshallException("not an AbstractSet");
	}
	JSONObject jsonset = jso.getJSONObject("set");
	Iterator i = jsonset.keys();
	while(i.hasNext()) {
	    abset.add((String)i.next());
	}
	return abset;
    }

    public Object doMarshall(Object o)
	throws MarshallException
    {
	AbstractSet abset = (AbstractSet)o;
	JSONObject obj = new JSONObject();
	JSONObject set = new JSONObject();
	obj.put("java_class", o.getClass().getName());
	obj.put("set", set);
	Object key = null;
	Iterator i = abset.iterator();
	while(i.hasNext()) {
	    key = i.next();
	    // only support String keys
	    set.put(key.toString(), 1);
	}
	return obj;
    }

}
