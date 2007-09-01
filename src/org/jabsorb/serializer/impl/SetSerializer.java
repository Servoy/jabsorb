/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: SetSerializer.java,v 1.4 2006/03/06 12:41:33 mclark Exp $
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

import java.util.Set;
import java.util.AbstractSet;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Iterator;

import org.jabsorb.json.JSONObject;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;


public class SetSerializer extends AbstractSerializer {

    private final static long serialVersionUID = 2;

    private static Class[] _serializableClasses = new Class[] { Set.class,
            HashSet.class, TreeSet.class, LinkedHashSet.class };

    private static Class[] _JSONClasses = new Class[] { JSONObject.class };

    public Class[] getSerializableClasses() {
        return _serializableClasses;
    }

    public Class[] getJSONClasses() {
        return _JSONClasses;
    }

    public boolean canSerialize(Class clazz, Class jsonClazz) {
        return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && Set.class
                .isAssignableFrom(clazz)));
    }

    public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
            Object o) throws UnmarshallException {
        JSONObject jso = (JSONObject) o;
        String java_class = jso.getString("javaClass");
        if (java_class == null)
            throw new UnmarshallException("no type hint");
        if (!(java_class.equals("java.util.Set")
                || java_class.equals("java.util.AbstractSet")
                || java_class.equals("java.util.LinkedHashSet")
                || java_class.equals("java.util.TreeSet") || java_class
                .equals("java.util.HashSet")))
            throw new UnmarshallException("not a Set");
        JSONObject jsonset = jso.getJSONObject("set");
        if (jsonset == null)
            throw new UnmarshallException("set missing");

        ObjectMatch m = new ObjectMatch(-1);

        Iterator i = jsonset.keys();
        String key = null;

        try {
            while (i.hasNext()) {
                key = (String) i.next();
                m = ser.tryUnmarshall(state, null, jsonset.get(key)).max(m);
            }
        } catch (UnmarshallException e) {
            throw new UnmarshallException("key " + key + " " + e.getMessage());
        }
        return m;
    }

    public Object unmarshall(SerializerState state, Class clazz, Object o)
            throws UnmarshallException {
        JSONObject jso = (JSONObject) o;
        String java_class = jso.getString("javaClass");
        if (java_class == null)
            throw new UnmarshallException("no type hint");
        AbstractSet abset = null;
        if (java_class.equals("java.util.Set")
                || java_class.equals("java.util.AbstractSet")
                || java_class.equals("java.util.HashSet")) {
            abset = new HashSet();
        } else if (java_class.equals("java.util.TreeSet")) {
            abset = new TreeSet();
        } else if (java_class.equals("java.util.LinkedHashSet")) {
            abset = new LinkedHashSet();
        } else {
            throw new UnmarshallException("not a Set");
        }
        JSONObject jsonset = jso.getJSONObject("set");

        if (jsonset == null)
            throw new UnmarshallException("set missing");

        Iterator i = jsonset.keys();
        String key = null;

        try {
            while (i.hasNext()) {
                key = (String) i.next();
                Object setElement = jsonset.get(key);
                abset.add(ser.unmarshall(state, null, setElement));
            }
        } catch (UnmarshallException e) {
            throw new UnmarshallException("key " + i + e.getMessage());
        }
        return abset;
    }

    public Object marshall(SerializerState state, Object o)
            throws MarshallException {
        Set set = (Set) o;

        JSONObject obj = new JSONObject();
        JSONObject setdata = new JSONObject();
        if (ser.getMarshallClassHints())
            obj.put("javaClass", o.getClass().getName());
        obj.put("set", setdata);
        Object key = null;
        Iterator i = set.iterator();

        try {
            while (i.hasNext()) {
                key = i.next();
                // only support String keys
                setdata.put(key.toString(), ser.marshall(state, key));
            }
        } catch (MarshallException e) {
            throw new MarshallException("set key " + key + e.getMessage());
        }
        return obj;
    }

}
