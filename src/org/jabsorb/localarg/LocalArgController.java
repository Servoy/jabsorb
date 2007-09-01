/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: LocalArgController.java,v 1.2 2006/03/06 12:41:33 mclark Exp $
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

package org.jabsorb.localarg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jabsorb.JSONRPCBridge;
import org.jabsorb.localarg.impl.HttpServletRequestArgResolver;
import org.jabsorb.localarg.impl.HttpServletResponseArgResolver;
import org.jabsorb.localarg.impl.HttpSessionArgResolver;
import org.jabsorb.localarg.impl.JSONRPCBridgeServletArgResolver;
import org.jabsorb.reflect.ClassAnalyzer;
import org.jabsorb.serializer.UnmarshallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Controls registration and lookup of LocalArgResolver classes.
 */

public class LocalArgController {

    private final static Logger log = LoggerFactory.getLogger(LocalArgController.class);

    // key argClazz, val HashSet<LocalArgResolverData>
    private static HashMap localArgResolverMap = new HashMap();

    static {
        LocalArgController.registerLocalArgResolver(HttpServletRequest.class,
                HttpServletRequest.class, new HttpServletRequestArgResolver());
        LocalArgController.registerLocalArgResolver(HttpServletResponse.class,
                HttpServletResponse.class, new HttpServletResponseArgResolver());
        LocalArgController.registerLocalArgResolver(HttpSession.class,
                HttpServletRequest.class, new HttpSessionArgResolver());
        LocalArgController.registerLocalArgResolver(JSONRPCBridge.class,
                HttpServletRequest.class, new JSONRPCBridgeServletArgResolver());
    }

    protected static class LocalArgResolverData {

        private LocalArgResolver argResolver;

        private Class argClazz;

        private Class contextInterface;

        public LocalArgResolverData(LocalArgResolver argResolver,
                Class argClazz, Class contextInterface) {
            this.argResolver = argResolver;
            this.argClazz = argClazz;
            this.contextInterface = contextInterface;
        }

        public boolean understands(Object context) {
            return contextInterface.isAssignableFrom(context.getClass());
        }

        public int hashCode() {
            return argResolver.hashCode() * argClazz.hashCode()
                    * contextInterface.hashCode();
        }

        public boolean equals(Object o) {
            LocalArgResolverData cmp = (LocalArgResolverData) o;
            return (argResolver.equals(cmp.argResolver)
                    && argClazz.equals(cmp.argClazz) && contextInterface
                    .equals(cmp.contextInterface));
        }
    }

    /**
     * Registers a Class to be removed from the exported method signatures and
     * instead be resolved locally using context information from the transport.
     * 
     * @param argClazz
     *            The class to be resolved locally
     * @param argResolver
     *            The user defined class that resolves the and returns the
     *            method argument using transport context information
     * @param contextInterface
     *            The type of transport Context object the callback is
     *            interested in eg. HttpServletRequest.class for the servlet
     *            transport
     */
    public static void registerLocalArgResolver(Class argClazz,
            Class contextInterface, LocalArgResolver argResolver) {
        synchronized (localArgResolverMap) {
            HashSet resolverSet = (HashSet) localArgResolverMap.get(argClazz);
            if (resolverSet == null) {
                resolverSet = new HashSet();
                localArgResolverMap.put(argClazz, resolverSet);
            }
            resolverSet.add(new LocalArgResolverData(argResolver, argClazz,
                    contextInterface));
            ClassAnalyzer.invalidateCache();
        }
        log.info("registered local arg resolver "
                + argResolver.getClass().getName() + " for local class "
                + argClazz.getName() + " with context "
                + contextInterface.getName());
    }

    /**
     * Unregisters a LocalArgResolver</b>.
     * 
     * @param argClazz
     *            The previously registered local class
     * @param argResolver
     *            The previously registered LocalArgResolver object
     * @param contextInterface
     *            The previously registered transport Context interface.
     */
    public static void unregisterLocalArgResolver(Class argClazz,
            Class contextInterface, LocalArgResolver argResolver) {
        synchronized (localArgResolverMap) {
            HashSet resolverSet = (HashSet) localArgResolverMap.get(argClazz);
            if (resolverSet == null
                    || !resolverSet.remove(new LocalArgResolverData(
                            argResolver, argClazz, contextInterface))) {
                log.warn("local arg resolver "
                        + argResolver.getClass().getName()
                        + " not registered for local class "
                        + argClazz.getName() + " with context "
                        + contextInterface.getName());
                return;
            }
            if (resolverSet.isEmpty())
                localArgResolverMap.remove(argClazz);
            ClassAnalyzer.invalidateCache();
        }
        log.info("unregistered local arg resolver "
                + argResolver.getClass().getName() + " for local class "
                + argClazz.getName() + " with context "
                + contextInterface.getName());
    }

    /**
     * Determine if an argument of the specified class type
     * can be resolved to a local argument that is filled in
     * on the server prior to being invoked.
     *
     * @param param local argument class.
     * @return true if the class can be resolved to a local argument.
     */
    public static boolean isLocalArg(Class param)
    {
        HashSet resolverSet = null;
        synchronized (localArgResolverMap) {
            resolverSet = (HashSet) localArgResolverMap.get(param);
        }
        return (resolverSet != null ? true : false);
    }

    /**
     * Using the caller's context, resolve a given method call parameter to a local
     * argument.
     *
     * @param context callers context.  In an http servlet environment, this will
     * contain the servlet request and response objects.
     *
     * @param param class type parameter to resolve to a local argument.
     * @return the run time instance that is resolved, to be used when calling the method.
     * 
     * @throws UnmarshallException if there if a failure during resolution.
     */
    public static Object resolveLocalArg(Object context[], Class param)
            throws UnmarshallException {
        HashSet resolverSet = (HashSet) localArgResolverMap.get(param);
        Iterator i = resolverSet.iterator();
        while (i.hasNext()) {
            LocalArgResolverData resolverData = (LocalArgResolverData) i.next();
            for (int j = 0; j < context.length; j++) {
                if (resolverData.understands(context[j])) {
                    try {
                        return resolverData.argResolver.resolveArg(context[j]);
                    } catch (LocalArgResolveException e) {
                        throw new UnmarshallException(
                                "error resolving local argument: " + e);
                    }
                }
            }
        }
        throw new UnmarshallException("couldn't find local arg resolver");
    }
}
