/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: BooleanSerializer.java,v 1.4.2.2 2006/03/06 12:39:21 mclark Exp $
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
