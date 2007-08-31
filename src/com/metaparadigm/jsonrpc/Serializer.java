/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: Serializer.java,v 1.3.2.1 2005/12/09 12:31:34 mclark Exp $
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

import java.io.Serializable;

/**
 * Interface to be implemented by custom serializer objects that convert
 * to and from Java objects and JSON objects.
 */

public interface Serializer extends Serializable
{
    public void setOwner(JSONSerializer ser);

    public Class[] getSerializableClasses();
    public Class[] getJSONClasses();

    public boolean canSerialize(Class clazz, Class jsonClazz);

    public ObjectMatch tryUnmarshall(SerializerState state,
				     Class clazz, Object json)
	throws UnmarshallException;

    public Object unmarshall(SerializerState state, Class clazz, Object json)
	throws UnmarshallException;

    public Object marshall(SerializerState state, Object o)
	throws MarshallException;
}
