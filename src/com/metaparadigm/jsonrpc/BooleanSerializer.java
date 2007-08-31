/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: BooleanSerializer.java,v 1.4.2.1 2005/12/09 12:31:34 mclark Exp $
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

public class BooleanSerializer extends AbstractSerializer
{
    private final static long serialVersionUID = 1;

    private static Class[] _serializableClasses = new Class[]
	{ boolean.class, Boolean.class };

    private static Class[] _JSONClasses = new Class[]
  { Boolean.class, String.class };

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
	if (jso instanceof String) {
            try {
	        jso = new Boolean((String)jso);
            } catch (Exception e) {
                throw new UnmarshallException("Cannot convert "+ jso+" to Boolean");
            }
	} 
	if(clazz == boolean.class) {
	    return new Boolean(((Boolean)jso).booleanValue());
	} else {
	    return jso;
	}
    }

    public Object marshall(SerializerState state, Object o)
	throws MarshallException
    {
	return o;
    }

}
