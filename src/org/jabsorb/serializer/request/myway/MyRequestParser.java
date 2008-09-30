package org.jabsorb.serializer.request.myway;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.request.RequestParser;
import org.jabsorb.serializer.response.myway.MySerializerState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyRequestParser implements RequestParser
{
  public JSONArray unmarshallArguments(JSONObject jsonReq) throws JSONException
  {
    return new Unwrangler().unwrangleArray(jsonReq
        .getJSONArray(JSONSerializer.PARAMETER_FIELD),
        jsonReq);
  }

  private class Unwrangler
  {
    private final Map<String, JSONObject> parsedObjects;

    public Unwrangler()
    {
      parsedObjects = new HashMap<String, JSONObject>();
    }

    private boolean isObject(Object o)
    {
      if (o instanceof String)
      {
        String s = (String) o;
        if (s.startsWith(MySerializerState.INDEX_PREFIX))
        {
          return true;
        }
      }
      return false;
    }

    public JSONArray unwrangleArray(JSONArray array, JSONObject jsonReq)
        throws JSONException
    {
      for (int i = 0; i < array.length(); i++)
      {
        Object o = array.get(i);
        if (isObject(o))
        {
          array.put(i, getObject((String) o, jsonReq));
        }
        else if (o instanceof JSONArray)
        {
          array.put(i, unwrangleArray((JSONArray) o, jsonReq));
        }
      }
      return array;
    }

    private JSONObject getObject(String key, JSONObject jsonReq)
        throws JSONException
    {
      if (this.parsedObjects.containsKey(key))
      {
        return this.parsedObjects.get(key);
      }
      JSONObject o = jsonReq.getJSONObject(key);
      this.parsedObjects.put(key, o);
      Map<String, Object> newObjects = new TreeMap<String, Object>();
      for (Iterator i = o.keys(); i.hasNext();)
      {
        String k = (String) i.next();
        Object v = o.get(k);
        if (isObject(v))
        {
          Object ob=getObject((String)v, jsonReq);
          newObjects.put(k, ob);
        }
        else if (v instanceof JSONArray)
        {
          newObjects.put(k, unwrangleArray((JSONArray) v, jsonReq));
        }
      }
      for (Entry<String, Object> e : newObjects.entrySet())
      {
        o.put(e.getKey(), e.getValue());
      }
      return o;
    }
  }
}
