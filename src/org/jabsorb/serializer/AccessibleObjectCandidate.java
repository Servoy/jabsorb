package org.jabsorb.serializer;

import java.lang.reflect.AccessibleObject;

/**
 * Used to determine whether two methods match
 */
public class AccessibleObjectCandidate
{
  /**
   * The method/constructor
   */
  final private AccessibleObject accessibleObject;

  /**
   * The match data for each parameter of the method.
   */
  final private ObjectMatch match[];

  /**
   * The parameters of the accessibleObject
   */
  final private Class[] parameterTypes;

  /**
   * Creatse a new MethodCandidate
   * 
   * @param accessibleObject The method/constructor for this candidate
   * @param parameterTypes The parameters of the accessibleObject
   * @param matches How well this matches the requested method/constructor
   */
  public AccessibleObjectCandidate(AccessibleObject accessibleObject,
      Class[] parameterTypes, ObjectMatch[] matches)
  {
    if (parameterTypes.length != matches.length)
    {
      throw new ArrayIndexOutOfBoundsException(
          "parameter types and matches need to be of the same size");
    }
    this.accessibleObject = accessibleObject;
    this.parameterTypes = parameterTypes;
    this.match = matches;
  }

  /**
   * Gets an object Match for the method.
   * 
   * @return An object match with the amount of mismatches
   */
  public ObjectMatch getMatch()
  {
    // TODO: Why this hard coded value?? Wouldn't it be better to say OKAY?
    int mismatch = ObjectMatch.OKAY.getMismatch();
    for (int i = 0; i < match.length; i++)
    {
      mismatch = Math.max(mismatch, match[i].getMismatch());
    }
    // TODO: Comparing like this is quite dodgy!
    if (mismatch == ObjectMatch.OKAY.getMismatch())
    {
      return ObjectMatch.OKAY;
    }
    if (mismatch == ObjectMatch.SIMILAR.getMismatch())
    {
      return ObjectMatch.SIMILAR;
    }
    if (mismatch == ObjectMatch.ROUGHLY_SIMILAR.getMismatch())
    {
      return ObjectMatch.ROUGHLY_SIMILAR;
    }
    return new ObjectMatch(mismatch);
  }

  /**
   * Gets the parameter types for the method/constructor
   * @return The parameter types
   */
  public Class[] getParameterTypes()
  {
    return parameterTypes;
  }

  /**
   * Gets the method/constructor
   * @return Method or Constructor
   */
  public AccessibleObject getAccessibleObject()
  {
    return accessibleObject;
  }
}
