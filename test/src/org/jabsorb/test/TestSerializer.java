/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2009 The jabsorb team
 *
 * based on original code from
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
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

package org.jabsorb.test;

import java.util.HashMap;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.test.ITest.Waggle;
import org.json.JSONObject;

import junit.framework.TestCase;

public class TestSerializer extends TestCase
{
  static class TestMap1 extends HashMap /* <Integer, String> */
  {
  }

  static final TestMap1 TEST_MAP1         = new TestMap1();
  static
  {
    TEST_MAP1.put(new Integer(1), "1");
    TEST_MAP1.put(new Integer(2), "2");
  }

  JSONSerializer        ser;

  SerializerState       marshallerState   = new SerializerState();

  SerializerState       unmarshallerState = new SerializerState();

  protected void setUp() throws Exception
  {
    ser = new JSONSerializer();
    ser.registerDefaultSerializers();
    ser.setMarshallClassHints(true);
  }

  public void dontTestExtendedMaps() throws Exception
  {
    JSONObject json = (JSONObject) ser.marshall(marshallerState, null,
        TEST_MAP1, "testMap1");
    System.out.println("Serialized: ");
    System.out.println(json.toString(2));
    TestMap1 unmarshalled = (TestMap1) ser.unmarshall(unmarshallerState,
        TestMap1.class, json);
    assertEquals(TEST_MAP1, unmarshalled);
  }

  static final HashMap /* <Integer, String> */TEST_MAP2 = new HashMap/*
                                                                     * <Integer,
                                                                     * String>
                                                                     */();
  static
  {
    TEST_MAP2.put(new Integer(1), "1");
    TEST_MAP2.put(new Integer(2), "2");
  }

  public void dontTestMaps() throws Exception
  {
    JSONObject json = (JSONObject) ser.marshall(marshallerState, null,
        TEST_MAP2, "testMap2");
    System.out.println("Serialized: ");
    System.out.println(json.toString(2));
    HashMap/* <Integer, String> */unmarshalled = (HashMap/* <Integer, String> */) ser
        .unmarshall(unmarshallerState, HashMap.class, json);
    assertEquals(TEST_MAP2, unmarshalled);
  }

  public void testWaggle() throws Exception
  {
    SerializerState marshallerState = new SerializerState();
    SerializerState unmarshallerState = new SerializerState();
    ITest.Waggle waggle = new ITest.Waggle(1);
    JSONObject json1 = (JSONObject) ser.marshall(marshallerState, null, waggle,
        "waggle");
    ITest.Waggle unmarshalled = (ITest.Waggle) ser.unmarshall(unmarshallerState,
        ITest.Waggle.class, json1);
    assertEquals(waggle.toString(), unmarshalled.toString());
    marshallerState = new SerializerState();
    JSONObject json2 = (JSONObject) ser.marshall(marshallerState, null,
        unmarshalled, "waggle");
    assertEquals(json1.toString(), json2.toString());
  }
}
