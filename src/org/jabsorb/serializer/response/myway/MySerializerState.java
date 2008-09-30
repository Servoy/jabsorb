package org.jabsorb.serializer.response.myway;

import java.util.HashMap;
import java.util.Map;

import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ProcessedObject;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.jabsorb.serializer.response.results.SuccessfulResult;
import org.json.JSONObject;

public class MySerializerState implements SerializerState
{

  @Override
  public Object checkObject(Object parent, Object currentObject, Object ref)
      throws MarshallException
  {
    MyProcessedObject o = getProcessedObject(currentObject);
    if (o != null)
    {
      return o.getSerialized();
    }
    push(null, currentObject, null);
    return null;
  }

  @Override
  public void setMarshalled(Object marshalledObject, Object java)
  {
    if (marshalledObject instanceof JSONObject)
    {
      MyProcessedObject o = getProcessedObject(java);
      o.setIndexValue(nextIndex());
      final int key = System.identityHashCode(java);
      if(this.nonMarshalledObjects.containsKey(key))
      {
        this.marshalledObjects.put(key, this.nonMarshalledObjects.remove(key));
      }
    }
  }

  @Override
  public SuccessfulResult createResult(Object requestId, Object object,
      Object json)
  {
    return new MyResult(requestId, json,object, this.marshalledObjects);
  }

  private final Map<Integer, MyProcessedObject> marshalledObjects;

  private final Map<Integer, MyProcessedObject> nonMarshalledObjects;

  public MySerializerState()
  {
    this.marshalledObjects = new HashMap<Integer, MyProcessedObject>();
    this.nonMarshalledObjects = new HashMap<Integer, MyProcessedObject>();
    this.index = 1;
  }

  /**
   * Get the JSONObject for the given marshalled object
   * 
   * @param o The java object
   * @return A JSON object for the java object.
   */
  public JSONObject getJSONObject(Object o)
  {
    JSONObject j = (JSONObject) marshalledObjects.get(
        new Integer(System.identityHashCode(o))).getObject();
    return j;
  }

  @Override
  public void pop() throws MarshallException
  {
    //Nothing to do
  }

  private int index;

  private String nextIndex()
  {
    return INDEX_PREFIX + index++;
  }

  @Override
  public Object push(Object parent, Object obj, Object ref)
  {
    final int identity = new Integer(System.identityHashCode(obj));
    final Object toReturn;
    final MyProcessedObject po;
    if (obj instanceof JSONObject)
    {
      JSONObject val = new JSONObject();
      String _index = nextIndex();
      po = new MyProcessedObject(val, _index);

      toReturn = _index;
      if (!this.marshalledObjects.containsKey(identity))
      {
        this.marshalledObjects.put(identity, po);
      }
    }
    else
    {
      po = new MyProcessedObject(obj);
      toReturn = obj;
      if (!this.nonMarshalledObjects.containsKey(identity))
      {
        this.nonMarshalledObjects.put(identity, po);
      }
    }
    return toReturn;
    //throw new MarshallException("Object already marshalled.");
  }

  public static final String INDEX_PREFIX = "_";

  @Override
  public void setSerialized(Object source, Object target)
      throws UnmarshallException
  {
    final ProcessedObject po = this.getProcessedObject(source);
    if (po != null)
    {
      po.setSerialized(target);
    }
  }

  @Override
  public MyProcessedObject getProcessedObject(Object object)
  {
    final int key = System.identityHashCode(object);
    if (this.marshalledObjects.containsKey(key))
    {
      return this.marshalledObjects.get(key);
    }
    return this.nonMarshalledObjects.get(key);
  }

  @Override
  public void store(Object object)
  {
    if (object instanceof JSONObject)
    {
      MyProcessedObject p = new MyProcessedObject(object, nextIndex());
      final int identity = System.identityHashCode(object);
      if (!marshalledObjects.containsKey(identity))
      {
        marshalledObjects.put(identity, p);
      }
    }
  }
}
