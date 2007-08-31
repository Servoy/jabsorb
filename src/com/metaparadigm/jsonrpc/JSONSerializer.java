/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: JSONSerializer.java,v 1.9 2005/07/18 12:27:44 mclark Exp $
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

import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.text.ParseException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

/**
 * This class is the public entry point to the serialization code and provides
 * methods for marshalling Java objects into JSON objects and unmarshalling
 * JSON objects into Java objects.
 */

public class JSONSerializer
{
    private final static Logger log =
	Logger.getLogger(JSONSerializer.class.getName());

    private boolean debug = false;

    public void setDebug(boolean debug) { this.debug = debug; }
    public boolean isDebug() { return debug; }

    // Key Serializer
    private HashSet serializerSet = new HashSet();
    // key Class, val Serializer
    private HashMap serializableMap = new HashMap();
    // List for reverse registration order search
    private ArrayList serializerList = new ArrayList();

    private boolean marshallClassHints = true;
    private boolean marshallNullAttributes = true;
    
    public void registerDefaultSerializers()
	throws Exception
    {
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

    public void registerSerializer(Serializer s)
	throws Exception
    {
	Class classes[] = s.getSerializableClasses();
	Serializer exists;
	synchronized (serializerSet) {
	    for(int i=0; i < classes.length; i++) {
		exists = (Serializer)serializableMap.get(classes[i]);
		if(exists != null && exists.getClass() != s.getClass())
		    throw new Exception
			("different serializer already registered for " +
			 classes[i].getName());
	    }
	    if(!serializerSet.contains(s)) {
		if(isDebug())
		    log.info("registered serializer " +
			     s.getClass().getName());
		for(int i=0; i < classes.length; i++) {
		    serializableMap.put(classes[i], s);
		}
		s.setOwner(this);
		serializerSet.add(s);
		serializerList.add(0, s);
	    }
	}
    }

    private Serializer getSerializer(Class clazz, Class jsoClazz)
    {
	if(isDebug())
	    log.fine("looking for serializer - java:" +
		     (clazz == null ? "null" : clazz.getName()) +
		     " json:" +
		     (jsoClazz == null ? "null" : jsoClazz.getName()));

	Serializer s = null;
	synchronized (serializerSet) {
	    s = (Serializer)serializableMap.get(clazz);
	    if(s != null && s.canSerialize(clazz, jsoClazz)) {
		if(isDebug())
		    log.fine("direct match serializer " +
			     s.getClass().getName());
		return s;
	    }
	    Iterator i = serializerList.iterator();
	    while(i.hasNext()) {
		s = (Serializer)i.next();
		if(s.canSerialize(clazz, jsoClazz)) {
		    if(isDebug())
			log.fine("search found serializer " +
				 s.getClass().getName()); 
		    return s;
		}
	    }
	}
	return null;
    }

    private Class getClassFromHint(Object o)
	throws UnmarshallException
    {
	if(o == null) return null;
	if(o instanceof JSONObject) {
	    try {
		String class_name = ((JSONObject)o).getString("javaClass");
		Class clazz = Class.forName(class_name);
		return clazz;
	    } catch (NoSuchElementException e) {
	    } catch (Exception e) {
		throw new UnmarshallException("class in hint not found");
	    }
	}
	if(o instanceof JSONArray) {
	    JSONArray arr = (JSONArray)o;
	    if(arr.length() == 0)
		throw new UnmarshallException("no type for empty array");
	    // return type of first element
	    Class compClazz = getClassFromHint(arr.get(0));
	    try {
		if(compClazz.isArray())
		    return Class.forName("[" + compClazz.getName());
		else
		    return Class.forName("[L" + compClazz.getName() + ";");
	    } catch (ClassNotFoundException e) {
		throw new UnmarshallException("problem getting array type");
	    }
	}
	return o.getClass();
    }

    public ObjectMatch tryUnmarshall(SerializerState state,
				     Class clazz, Object json)
	throws UnmarshallException
    {
	/* If we have a JSON object class hint that is a sub class of the
	   signature 'clazz', then override 'clazz' with the hint class. */
	if(clazz != null &&
	   json instanceof JSONObject &&
	   ((JSONObject)json).has("javaClass") &&
	   clazz.isAssignableFrom(getClassFromHint(json)))
	    clazz = getClassFromHint(json);

	if(clazz == null)
	    clazz = getClassFromHint(json);
	if(clazz == null)
	    throw new UnmarshallException("no class hint");
	if(json == null || json == JSONObject.NULL) {
	    if(!clazz.isPrimitive())
		return ObjectMatch.NULL;
	    else
		throw new UnmarshallException("can't assign null primitive");
	}
	Serializer s = getSerializer(clazz, json.getClass());
	if(s != null) return s.tryUnmarshall(state, clazz, json);

	throw new UnmarshallException("no match");
    }

    public Object unmarshall(SerializerState state, Class clazz, Object json)
	throws UnmarshallException
    {
	/* If we have a JSON object class hint that is a sub class of the
	   signature 'clazz', then override 'clazz' with the hint class. */
	if(clazz != null &&
	   json instanceof JSONObject &&
	   ((JSONObject)json).has("javaClass") &&
	   clazz.isAssignableFrom(getClassFromHint(json)))
	    clazz = getClassFromHint(json);

	if(clazz == null)
	    clazz = getClassFromHint(json);
	if(clazz == null)
	    throw new UnmarshallException("no class hint");
	if(json == null || json == JSONObject.NULL) {
	    if(!clazz.isPrimitive())
		return null;
	    else
		throw new UnmarshallException("can't assign null primitive");
	}
	Serializer s = getSerializer(clazz, json.getClass());
	if(s != null) return s.unmarshall(state, clazz, json);

	throw new UnmarshallException("can't unmarshall");
    }

    public Object marshall(SerializerState state, Object o)
	throws MarshallException
    {
	if(o == null) {
	    if(isDebug()) log.fine("marshall null");
	    return JSONObject.NULL;
	}
	if(isDebug()) log.fine("marshall class " + o.getClass().getName());
	Serializer s = getSerializer(o.getClass(), null);
	if(s != null) return s.marshall(state, o);
	throw new MarshallException("can't marshall " +
				    o.getClass().getName());
    }

    public String toJSON(Object o)
	throws MarshallException
    {
	SerializerState state = new SerializerState();
	Object json = marshall(state, o);
	return json.toString();
    }

    public Object fromJSON(String s)
	throws UnmarshallException
    {
	JSONTokener tok =   new JSONTokener(s);
	Object json;
	try {
	    json = tok.nextValue();
	} catch(ParseException e) {
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
     * @return
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
     * @return
     */
    public void setMarshallClassHints(boolean marshallClassHints) {
        this.marshallClassHints = marshallClassHints;
    }
    
    /** 
     * Returns true if attributes will null values should still be included
     * in the serialized JSON object.  Defaults to true.  Set to false for 
     * performance gains and small JSON serialized size.  Useful because null and 
     * undefined for JSON object attributes is virtually the same thing.
     * @return
     */
    public boolean getMarshallNullAttributes() {
        return marshallNullAttributes;
    }
    /** 
     * Returns true if attributes will null values should still be included
     * in the serialized JSON object.  Defaults to true.  Set to false for 
     * performance gains and small JSON serialized size.  Useful because null and 
     * undefined for JSON object attributes is virtually the same thing.
     * @return
     */
    public void setMarshallNullAttributes(boolean marshallNullAttributes) {
        this.marshallNullAttributes = marshallNullAttributes;
    }
}
