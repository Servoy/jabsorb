/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2008 The jabsorb team
 *
 * based on original code from
 * JSON-RPC-Client, a Java client extension to JSON-RPC-Java
 * (C) Copyright CodeBistro 2007, Sasha Ovsankin <sasha at codebistro dot com>
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
package org.jabsorb.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.response.results.FailedResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A factory to create proxies for access to remote Jabsorb services.
 */
public class Client implements InvocationHandler
{
  /**
   * Maps proxy keys to proxies
   */
  private final Map proxyMap;

  /**
   * The serializer instance to use.
   */
  private final JSONSerializer serializer;

  /**
   * The transport session to use for this connection
   */
  private final Session session;

  /**
   * Create a client given a session
   * 
   * @param session transport session to use for this connection
   */
  public Client(Session session)
  {
    try
    {
      this.session = session;
      this.proxyMap = new HashMap();
      this.serializer = new JSONSerializer();
      this.serializer.registerDefaultSerializers();
    }
    catch (Exception e)
    {
      throw new ClientError(e);
    }
  }

  /**
   * Dispose of the proxy that is no longer needed
   * 
   * @param proxy
   */
  public void closeProxy(Object proxy)
  {
    proxyMap.remove(proxy);
  }

  //This method is public because of the inheritance from the InvokationHandler.
  //It should never be called directly.
  public Object invoke(Object proxyObj, Method method, Object[] args)
      throws Exception
  {
    String methodName = method.getName();
    if (methodName.equals("hashCode"))
    {
      return new Integer(System.identityHashCode(proxyObj));
    }
    else if (methodName.equals("equals"))
    {
      return (proxyObj == args[0] ? Boolean.TRUE : Boolean.FALSE);
    }
    else if (methodName.equals("toString"))
    {
      return proxyObj.getClass().getName() + '@'
          + Integer.toHexString(proxyObj.hashCode());
    }
    return invoke((String) proxyMap.get(proxyObj), method.getName(), args,
        method.getReturnType());
  }

  /**
   * Create a proxy for communicating with the remote service.
   * 
   * @param key the remote object key
   * @param klass the class of the interface the remote object should adhere to
   * @return created proxy
   */
  public Object openProxy(String key, Class klass)
  {
    Object result = java.lang.reflect.Proxy.newProxyInstance(klass
        .getClassLoader(), new Class[] { klass }, this);
    proxyMap.put(result, key);
    return result;
  }

  /**
   * Generate and throw exception based on the data in the 'responseMessage'
   * 
   * @param responseMessage The error message
   * @throws JSONException Rethrows the exception in the repsonse.
   */
  protected void processException(JSONObject responseMessage)
      throws JSONException
  {
    JSONObject error = (JSONObject) responseMessage.get("error");
    if (error != null)
    {
      Integer code = new Integer(error.has("code") ? error.getInt("code") : 0);
      String trace = error.has("trace") ? error.getString("trace") : null;
      String msg = error.has("msg") ? error.getString("msg") : null;
      throw new ErrorResponse(code, msg, trace);
    }
    throw new ErrorResponse(new Integer(FailedResult.CODE_ERR_PARSE),
        "Unknown response:" + responseMessage.toString(2), null);
  }

  /**
   * Invokes a method for the ciient.
   * 
   * @param objectTag (optional) the name of the object to invoke the method on.
   *          May be null.
   * @param methodName The name of the method to call.
   * @param args The arguments to the method.
   * @param returnType What should be returned
   * @return The result of the call.
   * @throws Exception JSONObject, UnmarshallExceptions or Exceptions from
   *           invoking the method may be thrown.
   */
  private Object invoke(String objectTag, String methodName, Object[] args,
      Class returnType) throws Exception
  {
    JSONObject message = new JSONObject();
    String methodTag = objectTag == null ? "" : objectTag + ".";
    methodTag += methodName;
    message.put("method", methodTag);

    JSONArray params = new JSONArray();
    if (args != null)
    {
      for (int argNo = 0; argNo < args.length; argNo++)
      {
        Object arg = args[argNo];
        SerializerState state = serializer.createSerializerState();
        params.put(serializer.marshall(state, /* parent */null, arg,
            new Integer(argNo)));
      }
    }
    message.put("params", params);
    message.put("id", 1);

    JSONObject responseMessage = session.sendAndReceive(message);

    if (!responseMessage.has("result"))
      processException(responseMessage);
    Object rawResult = responseMessage.get("result");
    if (rawResult == null)
    {
      processException(responseMessage);
    }
    if (returnType.equals(Void.TYPE))
      return null;
    SerializerState state = serializer.createSerializerState();
    return serializer.unmarshall(state, returnType, rawResult);
  }
}
