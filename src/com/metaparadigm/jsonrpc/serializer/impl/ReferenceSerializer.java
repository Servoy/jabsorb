/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: ReferenceSerializer.java,v 1.5 2006/03/06 12:41:32 mclark Exp $
 *
 * Copyright Metaparadigm Pte. Ltd. 2004, 2005.
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

package com.metaparadigm.jsonrpc.serializer.impl;

import java.util.logging.Logger;
import org.json.JSONObject;

import com.metaparadigm.jsonrpc.JSONRPCBridge;
import com.metaparadigm.jsonrpc.serializer.AbstractSerializer;
import com.metaparadigm.jsonrpc.serializer.MarshallException;
import com.metaparadigm.jsonrpc.serializer.ObjectMatch;
import com.metaparadigm.jsonrpc.serializer.SerializerState;
import com.metaparadigm.jsonrpc.serializer.UnmarshallException;

public class ReferenceSerializer extends AbstractSerializer {

    private final static long serialVersionUID = 2;

    private final static Logger log = Logger
            .getLogger(ReferenceSerializer.class.getName());

    private JSONRPCBridge bridge;

    private static Class[] _serializableClasses = new Class[] {};

    private static Class[] _JSONClasses = new Class[] {};

    public Class[] getSerializableClasses() {
        return _serializableClasses;
    }

    public Class[] getJSONClasses() {
        return _JSONClasses;
    }

    public ReferenceSerializer(JSONRPCBridge bridge) {
        this.bridge = bridge;
    }

    public boolean canSerialize(Class clazz, Class jsonClazz) {
        return (!clazz.isArray()
                && !clazz.isPrimitive()
                && !clazz.isInterface()
                && (bridge.isReference(clazz) || bridge
                        .isCallableReference(clazz)) && (jsonClazz == null || jsonClazz == JSONObject.class));
    }

    public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
            Object o) throws UnmarshallException {
        return ObjectMatch.OKAY;
    }

    public Object unmarshall(SerializerState state, Class clazz, Object o)
            throws UnmarshallException {
        JSONObject jso = (JSONObject) o;
        Object ref = null;
        String json_type = jso.getString("JSONRPCType");
        int object_id = jso.getInt("objectID");
        if (json_type != null && json_type.equals("Reference")) {
            synchronized (bridge.getBridgeState()) {
                ref = bridge.getReferenceMap().get(new Integer(object_id));
            }
        }
        return ref;
    }

    public Object marshall(SerializerState state, Object o)
            throws MarshallException {
        Class clazz = o.getClass();
        Integer identity = new Integer(System.identityHashCode(o));
        if (bridge.isReference(clazz)) {
            if (ser.isDebug())
                log.fine("marshalling reference to object " + identity
                        + " of class " + clazz.getName());
            synchronized (bridge.getBridgeState()) {
                bridge.getReferenceMap().put(identity, o);
            }
            JSONObject jso = new JSONObject();
            jso.put("JSONRPCType", "Reference");
            jso.put("javaClass", clazz.getName());
            jso.put("objectID", identity);
            return jso;
        } else if (bridge.isCallableReference(clazz)) {
            if (ser.isDebug())
                log.fine("marshalling callable reference to object " + identity
                        + " of class " + clazz.getName());
            bridge.registerObject(identity, o);
            JSONObject jso = new JSONObject();
            jso.put("JSONRPCType", "CallableReference");
            jso.put("javaClass", clazz.getName());
            jso.put("objectID", identity);
            return jso;
        }
        return null;
    }

}
