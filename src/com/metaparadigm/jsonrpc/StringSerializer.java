/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: StringSerializer.java,v 1.3 2005/06/16 23:26:14 mclark Exp $
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

public class StringSerializer extends AbstractSerializer
{
    private static Class[] _serializableClasses = new Class[]
	{ String.class, char.class, Character.class,
	  byte[].class, char[].class };

    private static Class[] _JSONClasses = new Class[]
	{ String.class, Integer.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }

    public ObjectMatch tryUnmarshall(SerializerState state,
				     Class clazz, Object jso)
	throws UnmarshallException
    {
	return ObjectMatch.OKAY;
    }

    public Object unmarshall(SerializerState state, Class clazz, Object jso)
	throws UnmarshallException
    {
        String val = jso instanceof String?(String)jso:jso.toString();
	if(clazz == char.class) {
	    return new Character(val.charAt(0));
	} else if (clazz == byte[].class) {
	    return val.getBytes();
	} else if (clazz == char[].class) {
	    return val.toCharArray();	    
	} else {
	    return val;
	}
    }

    public Object marshall(SerializerState state, Object o)
	throws MarshallException
    {
	if(o instanceof Character) {
	    return o.toString();
	} else if(o instanceof byte[]) {
	    return new String((byte[])o);
	} else if (o instanceof char[]) {
	    return new String((char[])o);
	} else return o;
    }

}
