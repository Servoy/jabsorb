package org.jabsorb.serializer.response.myway;

import java.util.Map;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.response.results.SuccessfulResult;
import org.json.JSONException;
import org.json.JSONObject;

public class MyResult extends SuccessfulResult
{
  private final Map<Integer, MyProcessedObject> map;

  public MyResult(Object id, Object jsonResult, Object javaResult,Map<Integer, MyProcessedObject> map)
  {
    super(id, jsonResult);
    this.javaResult=javaResult;
    this.map = map;
  }
private final Object javaResult;
  @Override
  public JSONObject createOutput() throws JSONException
  {
    JSONObject o = this._createOutput();
    Object result=getResult();
    if (result != null)
    {
      o.put(JSONSerializer.RESULT_FIELD, getResult());
      for (MyProcessedObject p : this.map.values())
      {
        o.put(p.getIndex().getIndex(), p.getRealSerialized());
      }
    }
    return o;
  }

}
