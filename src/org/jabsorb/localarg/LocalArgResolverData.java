package org.jabsorb.localarg;

/**
 * Data holder for this LocalArgResolver.
 */
class LocalArgResolverData
{
  /**
   * The user defined class that resolves the and returns the method argument
   * using transport context information
   */
  private final LocalArgResolver argResolver;

  /**
   * The class to be resolved locally
   */
  private final Class argClazz;

  /**
   * The type of transport Context object the callback is interested in eg.
   * HttpServletRequest.class for the servlet transport
   */
  private final Class contextInterface;

  /**
   * Create a new data holder
   * 
   * @param argResolver The user defined class that resolves the and returns the
   *          method argument using transport context information
   * @param argClazz The class to be resolved locally
   * @param contextInterface The type of transport Context object the callback
   *          is interested in eg. HttpServletRequest.class for the servlet
   *          transport
   */
  public LocalArgResolverData(LocalArgResolver argResolver, Class argClazz,
      Class contextInterface)
  {
    this.argResolver = argResolver;
    this.argClazz = argClazz;
    this.contextInterface = contextInterface;
  }

  public boolean equals(Object o)
  {
    LocalArgResolverData cmp = (LocalArgResolverData) o;
    return (argResolver.equals(cmp.argResolver)
        && argClazz.equals(cmp.argClazz) && contextInterface
        .equals(cmp.contextInterface));
  }

  public int hashCode()
  {
    return argResolver.hashCode() * argClazz.hashCode()
        * contextInterface.hashCode();
  }

  /**
   * Whether this object's context can understand the given object
   * 
   * @param context The object to test
   * @return Whether the contextInterface isAssignableFrom the given object
   */
  public boolean understands(Object context)
  {
    return contextInterface.isAssignableFrom(context.getClass());
  }

  /**
   * Gets the argResolver
   * 
   * @return LocalArgResolver
   */
  LocalArgResolver getArgResolver()
  {
    return argResolver;
  }
}