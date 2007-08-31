package com.metaparadigm.jsonrpc.serializer.impl;

import org.json.JSONObject;
import com.metaparadigm.jsonrpc.serializer.AbstractSerializer;
import com.metaparadigm.jsonrpc.serializer.MarshallException;
import com.metaparadigm.jsonrpc.serializer.ObjectMatch;
import com.metaparadigm.jsonrpc.serializer.SerializerState;
import com.metaparadigm.jsonrpc.serializer.UnmarshallException;

public class RawJSONObjectSerializer extends AbstractSerializer {

    private final static long serialVersionUID = 2;

    private static Class[] _serializableClasses = new Class[] { JSONObject.class  };

    private static Class[] _JSONClasses = new Class[] { JSONObject.class };

    public Class[] getSerializableClasses() {
        return _serializableClasses;
    }

    public Class[] getJSONClasses() {
        return _JSONClasses;
    }

    public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
            Object jso) throws UnmarshallException {
        return ObjectMatch.OKAY;
    }

    public Object unmarshall(SerializerState state, Class clazz, Object jso)
            throws UnmarshallException {
        return jso;
    }

    public Object marshall(SerializerState state, Object o)
            throws MarshallException {
        return o;
    }

}
