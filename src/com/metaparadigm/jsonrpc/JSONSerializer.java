/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: JSONSerializer.java,v 1.15 2006/03/06 12:41:33 mclark Exp $
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

import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.text.ParseException;
import java.io.Serializable;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

import com.metaparadigm.jsonrpc.serializer.MarshallException;
import com.metaparadigm.jsonrpc.serializer.ObjectMatch;
import com.metaparadigm.jsonrpc.serializer.Serializer;
import com.metaparadigm.jsonrpc.serializer.SerializerState;
import com.metaparadigm.jsonrpc.serializer.UnmarshallException;
import com.metaparadigm.jsonrpc.serializer.impl.ArraySerializer;
import com.metaparadigm.jsonrpc.serializer.impl.BeanSerializer;
import com.metaparadigm.jsonrpc.serializer.impl.BooleanSerializer;
import com.metaparadigm.jsonrpc.serializer.impl.DateSerializer;
import com.metaparadigm.jsonrpc.serializer.impl.DictionarySerializer;
import com.metaparadigm.jsonrpc.serializer.impl.ListSerializer;
import com.metaparadigm.jsonrpc.serializer.impl.MapSerializer;
import com.metaparadigm.jsonrpc.serializer.impl.NumberSerializer;
import com.metaparadigm.jsonrpc.serializer.impl.PrimitiveSerializer;
import com.metaparadigm.jsonrpc.serializer.impl.SetSerializer;
import com.metaparadigm.jsonrpc.serializer.impl.StringSerializer;
import com.metaparadigm.jsonrpc.serializer.impl.RawJSONObjectSerializer;
import com.metaparadigm.jsonrpc.serializer.impl.RawJSONArraySerializer;

/**
 * This class is the public entry point to the serialization code and provides
 * methods for marshalling Java objects into JSON objects and unmarshalling JSON
 * objects into Java objects.
 */

public class JSONSerializer implements Serializable {

    private final static long serialVersionUID = 2;

    private final static Logger log = Logger.getLogger(JSONSerializer.class
            .getName());

    //  Debugging enabled on this serializer.
    private boolean debug = false;

    /**
     * Enable or disable debugging message from this serializer instance.
     * 
     * @param debug flag to enable or disable debugging messages
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Are debugging messages enabled on this serializer instance.
     * 
     * @return true or false depending on whether debugging messages are enabled.
     */
    public boolean isDebug() {
        return debug;
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        serializableMap = new HashMap();
        Iterator i = serializerList.iterator();
        while(i.hasNext()) {
            Serializer s = (Serializer)i.next();
            Class classes[] = s.getSerializableClasses();
            for (int j = 0; j < classes.length; j++) {
                serializableMap.put(classes[j], s);
            }
        }
    }
    
    // Key Serializer
    private HashSet serializerSet = new HashSet();

    // key Class, val Serializer
    private transient HashMap serializableMap = null;

    // List for reverse registration order search
    private ArrayList serializerList = new ArrayList();

    private boolean marshallClassHints = true;
    private boolean marshallNullAttributes = true;

    /**
     * Register all of the provided standard serializers.
     */
    public void registerDefaultSerializers() throws Exception {
        registerSerializer(new RawJSONArraySerializer());
        registerSerializer(new RawJSONObjectSerializer());
        registerSerializer(new BeanSerializer());
        registerSerializer(new ArraySerializer());
        registerSerializer(new DictionarySerializer());
        registerSerializer(new MapSerializer());
        registerSerializer(new SetSerializer());
        registerSerializer(new ListSerializer());
        registerSerializer(new DateSerializer());
        registerSerializer(new StringSerializer());
        registerSerializer(new NumberSerializer());
        registerSerializer(new BooleanSerializer());
        registerSerializer(new PrimitiveSerializer());
    }
    
    /**
     * Register a new type specific serializer.
     * 
     * @param s A class implementing the Serializer interface
     * (usually derived from AbstractSerializer).
     */
    public void registerSerializer(Serializer s) throws Exception {
        Class classes[] = s.getSerializableClasses();
        Serializer exists;
        synchronized (serializerSet) {
            if(serializableMap == null) serializableMap = new HashMap();
            for (int i = 0; i < classes.length; i++) {
                exists = (Serializer) serializableMap.get(classes[i]);
                if (exists != null && exists.getClass() != s.getClass())
                    throw new Exception(
                            "different serializer already registered for "
                                    + classes[i].getName());
            }
            if (!serializerSet.contains(s)) {
                if (isDebug())
                    log.info("registered serializer " + s.getClass().getName());
                s.setOwner(this);
                serializerSet.add(s);
                serializerList.add(0, s);
                for (int j = 0; j < classes.length; j++) {
                    serializableMap.put(classes[j], s);
                }
            }
        }
    }

    /**
     * Find the serializer for the given Java type and/or JSON type.
     * 
     * @param clazz
     *              The Java class to lookup.
     * @param jsoClazz
     *              The JSON class type to lookup (may be null in
     *              the marshalling case in which case only the
     *              class is used to lookup the serializer).
     * @return
     *              The found Serializer for the types specified
     *              or null if none could be found.
     */
    private Serializer getSerializer(Class clazz, Class jsoClazz) {
        if (isDebug())
            log.fine("looking for serializer - java:"
                    + (clazz == null ? "null" : clazz.getName()) + " json:"
                    + (jsoClazz == null ? "null" : jsoClazz.getName()));

        Serializer s = null;
        synchronized (serializerSet) {
            s = (Serializer) serializableMap.get(clazz);
            if (s != null && s.canSerialize(clazz, jsoClazz)) {
                if (isDebug())
                    log.fine("direct match serializer "
                            + s.getClass().getName());
                return s;
            }
            Iterator i = serializerList.iterator();
            while (i.hasNext()) {
                s = (Serializer) i.next();
                if (s.canSerialize(clazz, jsoClazz)) {
                    if (isDebug())
                        log.fine("search found serializer "
                                + s.getClass().getName());
                    return s;
                }
            }
        }
        return null;
    }

    private Class getClassFromHint(Object o) throws UnmarshallException {
        if (o == null)
            return null;
        if (o instanceof JSONObject) {
            try {
                String class_name = ((JSONObject) o).getString("javaClass");
                Class clazz = Class.forName(class_name);
                return clazz;
            } catch (NoSuchElementException e) {
            } catch (Exception e) {
                throw new UnmarshallException("class in hint not found");
            }
        }
        if (o instanceof JSONArray) {
            JSONArray arr = (JSONArray) o;
            if (arr.length() == 0)
                throw new UnmarshallException("no type for empty array");
            // return type of first element
            Class compClazz = getClassFromHint(arr.get(0));
            try {
                if (compClazz.isArray())
                    return Class.forName("[" + compClazz.getName());
                else
                    return Class.forName("[L" + compClazz.getName() + ";");
            } catch (ClassNotFoundException e) {
                throw new UnmarshallException("problem getting array type");
            }
        }
        return o.getClass();
    }

    public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
            Object json) throws UnmarshallException {
        /*
         * If we have a JSON object class hint that is a sub class of the
         * signature 'clazz', then override 'clazz' with the hint class.
         */
        if (clazz != null && json instanceof JSONObject
                && ((JSONObject) json).has("javaClass")
                && clazz.isAssignableFrom(getClassFromHint(json)))
            clazz = getClassFromHint(json);

        if (clazz == null)
            clazz = getClassFromHint(json);
        if (clazz == null)
            throw new UnmarshallException("no class hint");
        if (json == null || json == JSONObject.NULL) {
            if (!clazz.isPrimitive())
                return ObjectMatch.NULL;
            else
                throw new UnmarshallException("can't assign null primitive");
        }
        Serializer s = getSerializer(clazz, json.getClass());
        if (s != null)
            return s.tryUnmarshall(state, clazz, json);

        throw new UnmarshallException("no match");
    }

    public Object unmarshall(SerializerState state, Class clazz, Object json)
            throws UnmarshallException {
        /*
         * If we have a JSON object class hint that is a sub class of the
         * signature 'clazz', then override 'clazz' with the hint class.
         */
        if (clazz != null && json instanceof JSONObject
                && ((JSONObject) json).has("javaClass")
                && clazz.isAssignableFrom(getClassFromHint(json)))
            clazz = getClassFromHint(json);

        if (clazz == null)
            clazz = getClassFromHint(json);
        if (clazz == null)
            throw new UnmarshallException("no class hint");
        if (json == null || json == JSONObject.NULL) {
            if (!clazz.isPrimitive())
                return null;
            else
                throw new UnmarshallException("can't assign null primitive");
        }
        Serializer s = getSerializer(clazz, json.getClass());
        if (s != null)
            return s.unmarshall(state, clazz, json);

        throw new UnmarshallException("can't unmarshall");
    }

    public Object marshall(SerializerState state, Object o)
            throws MarshallException {
        if (o == null) {
            if (isDebug())
                log.fine("marshall null");
            return JSONObject.NULL;
        }
        if (isDebug())
            log.fine("marshall class " + o.getClass().getName());
        Serializer s = getSerializer(o.getClass(), null);
        if (s != null)
            return s.marshall(state, o);
        throw new MarshallException("can't marshall " + o.getClass().getName());
    }

    /**
     * Convert a Java objects (or tree of Java objects) into a string in JSON
     * format
     * 
     * @param obj
     *            the object to be converted to JSON.
     * @return the JSON format string representing the data in the the Java
     *         object.
     */
    public String toJSON(Object obj) throws MarshallException {
        SerializerState state = new SerializerState();
        Object json = marshall(state, obj);
        return json.toString();
    }

    /**
     * Convert a string in JSON format into Java objects.
     * 
     * @param jsonString
     *         the JSON format string.
     * @return an object (or tree of objects) representing the data in the JSON
     *         format string.
     */
    public Object fromJSON(String jsonString) throws UnmarshallException {
        JSONTokener tok = new JSONTokener(jsonString);
        Object json;
        try {
            json = tok.nextValue();
        } catch (ParseException e) {
            throw new UnmarshallException("couldn't parse JSON");
        }
        SerializerState state = new SerializerState();
        return unmarshall(state, null, json);
    }

    /**
     * Should serializers defined in this object include the fully 
     * qualified class name of objects being serialized?  This can 
     * be helpful when unmarshalling, though if not needed can
     * be left out in favor of increased performance and smaller 
     * size of marshalled String.  Default is true.
     *
     * @return whether Java Class hints are included in the serialised
     *         JSON objects
     */
    public boolean getMarshallClassHints() {
        return marshallClassHints;
    }

    /**
     * Should serializers defined in this object include the fully 
     * qualified class name of objects being serialized?  This can 
     * be helpful when unmarshalling, though if not needed can
     * be left out in favor of increased performance and smaller 
     * size of marshalled String.  Default is true.
     *
     * @param marshallClassHints flag to enable/disable inclusion
     *        of Java class hints in the serialized JSON objects
     */
    public void setMarshallClassHints(boolean marshallClassHints) {
        this.marshallClassHints = marshallClassHints;
    }

    /** 
     * Returns true if attributes will null values should still be included
     * in the serialized JSON object.  Defaults to true.  Set to false for 
     * performance gains and small JSON serialized size.  Useful because null and 
     * undefined for JSON object attributes is virtually the same thing.
     *
     * @return boolean value as to whether null attributes will be
     *         in the serialized JSON objects
     */
    public boolean getMarshallNullAttributes() {
        return marshallNullAttributes;
    }

    /** 
     * Returns true if attributes will null values should still be included
     * in the serialized JSON object.  Defaults to true.  Set to false for 
     * performance gains and small JSON serialized size.  Useful because null and 
     * undefined for JSON object attributes is virtually the same thing.
     *
     * @param marshallNullAttributes flag to enable/disable marshalling of
     *        null attributes in the serialized JSON objects
     */
    public void setMarshallNullAttributes(boolean marshallNullAttributes) {
        this.marshallNullAttributes = marshallNullAttributes;
    }
}
