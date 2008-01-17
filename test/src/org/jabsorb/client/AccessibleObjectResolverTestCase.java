package org.jabsorb.client;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.jabsorb.JSONSerializer;
import org.jabsorb.reflect.ClassAnalyzer;
import org.jabsorb.serializer.AccessibleObjectResolver;
import org.jabsorb.serializer.Serializer;
import org.jabsorb.test.ConstructorTest;
import org.json.JSONArray;

import junit.framework.TestCase;

public class AccessibleObjectResolverTestCase extends TestCase {
	AccessibleObjectResolver resolver;
	Map methodMap;
	JSONSerializer serializer;
	
	protected void setUp() throws Exception {
		resolver= new AccessibleObjectResolver();
		methodMap = new HashMap();
        methodMap.putAll(ClassAnalyzer.getClassData(ConstructorTest.class).getMethodMap());
        methodMap.putAll(ClassAnalyzer.getClassData(ConstructorTest.class).getConstructorMap());
        serializer = new JSONSerializer();
        serializer.registerDefaultSerializers();
	}

	public void testResolution() {
		JSONArray args= new JSONArray();
		args.put(1);
		Constructor methodInt= (Constructor)resolver.resolveMethod(methodMap, "$constructor", args, serializer);
		Class[] params= methodInt.getParameterTypes();
		assertNotNull(params);
		assertEquals(1, params.length);
		assertEquals(Integer.TYPE, params[0]);
	}
}
