package org.jabsorb.serializer.response.fixups;

import java.util.List;

import org.jabsorb.serializer.MarshallException;

/**
 * Use this class to make fixups for circular references. No duplicates are
 * fixed up.
 * 
 * @author William Becker
 */
public class FixupCircRefOnly extends UsingFixups
{

  public Object circularReferenceFound(List originalLocation, Object ref,
      Object java) throws MarshallException
  {
    return this.addFixUp(originalLocation, ref);
  }

  public Object duplicateFound(List originalLocation, Object ref, Object java)
      throws MarshallException
  {
    return null;
  }

}
