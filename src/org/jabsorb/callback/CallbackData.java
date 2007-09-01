/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: CallbackData.java,v 1.2 2006/03/06 12:41:33 mclark Exp $
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

package org.jabsorb.callback;

import java.io.Serializable;

/**
 * Container class for information about callbacks and the transport
 * they are associated with.
 */

public class CallbackData implements Serializable {
    private final static long serialVersionUID = 2;

    private InvocationCallback cb;

    private Class contextInterface;

    public InvocationCallback getCallback()
    {
        return cb;
    }

    public CallbackData(InvocationCallback cb, Class contextInterface) {
        this.cb = cb;
        this.contextInterface = contextInterface;
    }

    public boolean understands(Object context) {
        return contextInterface.isAssignableFrom(context.getClass());
    }

    public int hashCode() {
        return cb.hashCode() * contextInterface.hashCode();
    }

    public boolean equals(Object o) {
        CallbackData cmp = (CallbackData) o;
        return (cb.equals(cmp.cb) && contextInterface
                .equals(cmp.contextInterface));
    }
}
