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
