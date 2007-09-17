package org.jabsorb.serializer;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents an object that has been or is in the process of being processed by the JSONSerializer
 * this is stored in the SerializerState in order to detect circular references and duplicates.
 */
public class ProcessedObject
{
  /**
   * The parent object of this object.  It will be null if this is the root object being processed.
   */
  private ProcessedObject parent;

  /**
   * The processed object.
   */
  private Object object;

  /**
   * The "json" reference key such that [the json representation of] parent[ref] = object.
   * this will either be a String for an object reference or an Integer for an array reference.
   */
  private Object ref;

  /**
   * Get the parent ProcessedObject of this ProcessedObject.  It can be null if this is the root of
   * the JSON Object being processed.
   *
   * @return the parent ProcessedObject of this ProcessedObject or null if this is the root of
   *        the Object hierarchy being processed.
   */
  public ProcessedObject getParent()
  {
    return parent;
  }

  /**
   * Set the parent ProcessedObject of this ProcessedObject.  It can be null if this is the root of
   * the JSON Object being processed.
   *
   * @param parent the parent ProcessedObject of this ProcessedObject, or null if this is the root of
   *        the Object hierarchy being processed.
   */
  public void setParent(ProcessedObject parent)
  {
    this.parent = parent;
  }

  /**
   * Get the actual Object that this ProcessedObject wraps.
   *
   * @return the actual Object that this ProcessedObject wraps.
   */
  public Object getObject()
  {
    return object;
  }

  /**
   * Get the unique id for this ProcessedObject.
   * This is the basis for determining if the ProcessedObject has already been processed or not, and therefore
   * for detecting if it is a circular reference and/or duplicate object.
   *
   * @return the unique id for this ProcessedObject.
   */
  public Integer getUniqueId()
  {
    return new Integer(System.identityHashCode(object));
  }

  public void setObject(Object object)
  {
    this.object = object;
  }

  /**
   * Get the reference name String | Integer of this ProcessedObject.  This is the reference
   * such that parent[ref]  refers to this object.
   *
   * @return the reference Integer|String identifying this Object in its parent.
   */
  public Object getRef()
  {
    return ref;
  }

  /**
   * Set the reference name String | Integer of this ProcessedObject.  This is the reference
   * such that parent[ref]  refers to this object.
   *
   * @param ref the reference Integer|String identifying this Object in its parent.
   */
  public void setRef(Object ref)
  {
    this.ref = ref;
  }

  /**
   * Get the reference "path" location for where this object was originally located.
   *
   * @return a List of Integer | String objects representing the path to the location of this
   *         object in the JSON hierarchy.
   */
  public List getLocation()
  {
    ProcessedObject link = this;

    // todo: could possibly make use of the existing linked list structure
    // todo: already here and instead return a List view of that structure
    // todo: is that easy to do??

    List path = new LinkedList();

    while (link!=null)
    {
      path.add(0, link.ref);
      link = link.getParent();
    }

    //todo: this original location could potentially be cached here for performance.
    //todo: (but only do if it becomes necessary)
    //todo: It should always be generated on demand at least the first time around, because
    //todo: in many cases it's not needed! (it's only needed to cover circular reference and duplicate objects)

    return path;
  }
}
