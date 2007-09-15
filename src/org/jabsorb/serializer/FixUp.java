package org.jabsorb.serializer;

import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

/**
 * Encapsulates a single fix up entry for a circular
 * reference or duplicate detected during processing.
 */
public class FixUp
{
  /**
   * Create a FixUp for a duplicate or circular reference.
   *
   * @param fixupLocation a List of String|Integer's representing references to locate the place where
   *         this fixup entry needs to be created.
   *
   * @param originalLocation List of String|Integer's representing references to locate the original
   *         location of the circular reference or duplicate that this FixUp applies to.
   */
  public FixUp(List fixupLocation, List originalLocation)
  {
    jsExpression = makeJsReferencePath(fixupLocation) + "=" + makeJsReferencePath(originalLocation);
  }

  /**
   * JavaScript expression representing the assignment statement needed to apply this
   * fixup to the JSON that accompanies it.
   */
  private String jsExpression;

  /**
   * Generate a String representation of this FixUp entry, which is suitable
   * for use as an assignment statement in a JavaScript expression.
   * @return a String representation of this FixUp entry.
   */
  public String toString()
  {
    return jsExpression;
  }

  /**
   * Return a javascript "path" expression to the object reference for the given list of
   * references.
   *
   * @param parts List of Integer and String objects representing the reference to an object.
   * @return equivalent JavaScript expression to find that object.
   */
  private String makeJsReferencePath(List parts)
  {
    if (parts==null || parts.size()==0)
    {
      // this is not expected... safety check
      throw new IllegalArgumentException("invalid location");
    }

    StringBuffer out;
    Iterator i=parts.iterator();
    out = new StringBuffer(i.next().toString());

    while (i.hasNext())
    {
      out.append("[");
      Object next = i.next();
      if (next instanceof Integer)
      {
        out.append((Integer)next);
      }
      else
      {
        out.append(JSONObject.quote((String)next));
      }
      out.append("]");
    }
    return out.toString();
  }

}
