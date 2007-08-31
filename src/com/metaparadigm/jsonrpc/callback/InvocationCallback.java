/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: InvocationCallback.java,v 1.2 2006/03/06 12:41:33 mclark Exp $
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

package com.metaparadigm.jsonrpc.callback;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Interface to be implemented by objects registered for invocation
 * callbacks with the JSONRPCBridge.
 */

public interface InvocationCallback extends Serializable {

    /**
     * Callback before invocation of an RPC method.
     * @param context   The transport context (the HttpServletRequest
     object in the case of the HTTP transport).
     * @param instance  The object instance or null if it is a static method.
     * @param method    Method that failed the invocation.
     * @param arguments The arguments passed to the method
     */
    public void preInvoke(Object context, Object instance, Method method,
            Object arguments[]) throws Exception;

    /**
     * Callback after invocation of an RPC method.
     * @param context   The transport context (the HttpServletRequest
     object in the case of the HTTP transport).
     * @param instance  The object instance or null if it is a static method.
     * @param method    Method that failed the invocation.
     * @param result    The returned result from the method
     */
    public void postInvoke(Object context, Object instance, Method method,
            Object result) throws Exception;

}
