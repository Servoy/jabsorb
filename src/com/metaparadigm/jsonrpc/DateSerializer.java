/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: DateSerializer.java,v 1.2 2004/12/10 08:11:02 mclark Exp $
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

import java.util.Date;
import org.json.JSONObject;

class DateSerializer extends Serializer
{
    private static Class[] _serializableClasses = new Class[]
	{ Date.class };

    private static Class[] _JSONClasses = new Class[]
	{ JSONObject.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }

    public boolean canSerialize(Class clazz, Class jsonClazz)
    {
	return (super.canSerialize(clazz, jsonClazz) ||
		((jsonClazz == null || jsonClazz == JSONObject.class) &&
		 Date.class.isAssignableFrom(clazz)));
    }

    public ObjectMatch doTryToUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String java_class = jso.getString("javaClass");
	if(java_class == null)
	    throw new UnmarshallException("no type hint");	
	if(!(java_class.equals("java.util.Date")))
	    throw new UnmarshallException("not a Date");
	int time = jso.getInt("time");
	return ObjectMatch.OKAY;
    }

    public Object doUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String java_class = jso.getString("javaClass");
	if(java_class == null)
	    throw new UnmarshallException("no type hint");	
	if(!(java_class.equals("java.util.Date")))
	    throw new UnmarshallException("not a Date");
	int time = jso.getInt("time");
	return new Date((long)time * (long)1000);
    }

    public Object doMarshall(Object o)
	throws MarshallException
    {
	Date date = (Date)o;
	JSONObject obj = new JSONObject();
	int time = (int)((long)date.getTime() / (long)1000);
	obj.put("javaClass", o.getClass().getName());
	obj.put("time", time);
	return obj;
    }

}
