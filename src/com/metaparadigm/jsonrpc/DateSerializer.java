/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: DateSerializer.java,v 1.6 2005/07/18 12:27:44 mclark Exp $
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

import java.sql.Timestamp;
import java.util.Date;

import org.json.JSONObject;

public class DateSerializer extends AbstractSerializer
{
    private static Class[] _serializableClasses = new Class[]
	{ Date.class, Timestamp.class, java.sql.Date.class };

    private static Class[] _JSONClasses = new Class[]
	{ JSONObject.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }

    public ObjectMatch tryUnmarshall(SerializerState state,
				     Class clazz, Object o)
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

    public Object unmarshall(SerializerState state, Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	long time = jso.getLong("time");
        if (jso.has("javaClass")) {
            try {
                clazz = Class.forName(jso.getString("javaClass"));
            } catch (ClassNotFoundException cnfe) {
                throw new UnmarshallException(cnfe.getMessage());
            }
        }
	if(Date.class.equals(clazz)) {
            return new Date(time);
        } else if (Timestamp.class.equals(clazz)){
            return new Timestamp(time);
        } else if (java.sql.Date.class.equals(clazz)){
            return new java.sql.Date(time);
	} 
        throw new UnmarshallException("invalid class "+clazz);	
    }

    public Object marshall(SerializerState state, Object o)
	throws MarshallException
    {
	long time;
	if (o instanceof Date) {
	    time = ((Date)o).getTime();
	} else {
	    throw new MarshallException("cannot marshall date using class "+o.getClass());
	}
	JSONObject obj = new JSONObject();	
        if (ser.getMarshallClassHints())
            obj.put("javaClass", o.getClass().getName());
	obj.put("time", time);
	return obj;
    }

}
