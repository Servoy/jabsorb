/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: DateSerializer.java,v 1.4 2006/03/06 12:41:33 mclark Exp $
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

package org.jabsorb.serializer.impl;

import java.sql.Timestamp;
import java.util.Date;

import org.jabsorb.json.JSONObject;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;


public class DateSerializer extends AbstractSerializer {

    private final static long serialVersionUID = 2;

    private static Class[] _serializableClasses = new Class[] { Date.class,
            Timestamp.class, java.sql.Date.class };

    private static Class[] _JSONClasses = new Class[] { JSONObject.class };

    public Class[] getSerializableClasses() {
        return _serializableClasses;
    }

    public Class[] getJSONClasses() {
        return _JSONClasses;
    }

    public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
            Object o) throws UnmarshallException {
        JSONObject jso = (JSONObject) o;
        String java_class = jso.getString("javaClass");
        if (java_class == null)
            throw new UnmarshallException("no type hint");
        if (!(java_class.equals("java.util.Date")))
            throw new UnmarshallException("not a Date");
        return ObjectMatch.OKAY;
    }

    public Object unmarshall(SerializerState state, Class clazz, Object o)
            throws UnmarshallException {
        JSONObject jso = (JSONObject) o;
        long time = jso.getLong("time");
        if (jso.has("javaClass")) {
            try {
                clazz = Class.forName(jso.getString("javaClass"));
            } catch (ClassNotFoundException cnfe) {
                throw new UnmarshallException(cnfe.getMessage());
            }
        }
        if (Date.class.equals(clazz)) {
            return new Date(time);
        } else if (Timestamp.class.equals(clazz)) {
            return new Timestamp(time);
        } else if (java.sql.Date.class.equals(clazz)) {
            return new java.sql.Date(time);
        }
        throw new UnmarshallException("invalid class " + clazz);
    }

    public Object marshall(SerializerState state, Object o)
            throws MarshallException {
        long time;
        if (o instanceof Date) {
            time = ((Date) o).getTime();
        } else {
            throw new MarshallException("cannot marshall date using class "
                    + o.getClass());
        }
        JSONObject obj = new JSONObject();
        if (ser.getMarshallClassHints())
            obj.put("javaClass", o.getClass().getName());
        obj.put("time", time);
        return obj;
    }

}
