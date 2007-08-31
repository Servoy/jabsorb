/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: Serializer.java,v 1.3 2006/03/06 12:41:32 mclark Exp $
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

package com.metaparadigm.jsonrpc.serializer;

import java.io.Serializable;
import com.metaparadigm.jsonrpc.JSONSerializer;

/**
 * Interface to be implemented by custom serializer objects that convert to and
 * from Java objects and JSON objects.
 */

public interface Serializer extends Serializable {

    public void setOwner(JSONSerializer ser);

    public Class[] getSerializableClasses();

    public Class[] getJSONClasses();

    public boolean canSerialize(Class clazz, Class jsonClazz);

    public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
            Object json) throws UnmarshallException;

    public Object unmarshall(SerializerState state, Class clazz, Object json)
            throws UnmarshallException;

    public Object marshall(SerializerState state, Object o)
            throws MarshallException;
}
