/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: AbstractSerializer.java,v 1.1.2.1 2006/03/06 12:39:21 mclark Exp $
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

/**
 * Convenience class for implementing Serializers providing default
 * setOwner and canSerialize implementations.
 */

public abstract class AbstractSerializer implements Serializer
{
    protected JSONSerializer ser;

    public void setOwner(JSONSerializer ser) { this.ser = ser; }

    public boolean canSerialize(Class clazz, Class jsonClazz)
    {
	boolean canJava = false, canJSON = false;

	Class serializableClasses[] = getSerializableClasses();
	for(int i=0; i < serializableClasses.length; i++)
	    if(clazz == serializableClasses[i]) canJava = true;

	if(jsonClazz == null) {
	    canJSON = true;
	} else {
	    Class jsonClasses[] = getJSONClasses();
	    for(int i=0; i < jsonClasses.length; i++)
		if(jsonClazz == jsonClasses[i]) canJSON = true;
	}

	return (canJava && canJSON);
    }
}
