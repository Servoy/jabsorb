package com.metaparadigm.jsonrpc.test;

import java.util.*;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import com.metaparadigm.jsonrpc.JSONRPCBridge;
import com.metaparadigm.jsonrpc.InvocationCallback;

public class Test
{

    // Void test

    public void voidFunction() {}


    // Exception tests

    public static void throwException()
	throws Exception
    {
	throw new Exception("test exception");
    }


    // Overload tests

    public String[] echo(String strings[])
    {
	return strings;
    }

    public int echo(int i)
    {
	return i;
    }

    public int[] echo(int i[])
    {
	return i;
    }

    public String echo(String message)
    {
	return message;
    }


    // Type tests

    public List echoList(List l) {
        return l;
    }

    public byte[] echoByteArray(byte ba[]) {
	return ba;
    }

    public char[] echoCharArray(char ca[]) {
	return ca;
    }

    public char echoChar(char c) {
	return c;
    }

    public boolean echoBoolean(boolean b) {
	return b;
    }

    public boolean[] echoBooleanArray(boolean ba[]) {
	return ba;
    }

    public Integer[] echoIntegerArray(Integer i[])
    {
	return i;
    }

    public Integer echoIntegerObject(Integer i)
    {
	return i;
    }

    public Long echoLongObject(Long l)
    {
	return l;
    }

    public Float echoFloatObject(Float f)
    {
	return f;
    }

    public Double echoDoubleObject(Double d)
    {
	return d;
    }

    public Date echoDateObject(Date d)
    {
        return d;
    }

    public Object echoObject(Object o)
    {
        return o;
    }

    public Object echoObjectArray(Object[] o)
    {
        return o;
    }

    // Container tests

    public int[] anArray()
    {
	int arr[] = new int[10];
	for(int i=0; i<10; i++) arr[i] = i;
	return arr;
    }

    public ArrayList anArrayList()
    {
	ArrayList al = new ArrayList();
	for(int i=10; i<20; i++) al.add(new Integer(i));
	return al;
    }

    public Vector aVector()
    {
	Vector v = new Vector();
	for(int i=20; i<30; i++) v.add(new Integer(i));
	return v;
    }

    public List aList()
    {
	List l = new Vector();
	for(int i=20; i<30; i++) l.add(new Integer(i));
	return l;
    }

    public Set aSet()
    {
	Set s = new HashSet();
	for(int i=0; i<5; i++) s.add(new Integer(i));
	return s;
    }
    
    public BeanA aBean(){
        BeanA beanA = new BeanA();
        BeanB beanB = new BeanB();
        
        beanB.setBeanA(beanA);
        beanB.setId(beanB.hashCode());
        beanA.setBeanB(beanB);
        beanA.setId(beanA.hashCode());
        
        return beanA;
    }

    public Hashtable aHashtable()
    {
	Hashtable ht = new Hashtable();
	for(int i=0; i<3; i++) {
	    Wiggle w = new Wiggle();
	    w.setFoo("foo " + i);
	    w.setBar(i);
	    ht.put(new Integer(i), w);
	}
	return ht;
    }


    // Misc tests

    public String[] twice(String string)
    {
	return new String[] { string, string };
    }

    public String concat(String msg1, String msg2)
    {
	return msg1 + " and " + msg2;
    }


    // Bean tests

    static public class Wiggle
    {
	private String foo;
	private int bar;

	public Wiggle() {}

	public Wiggle(int i)
	{
	    bar = i;
	    foo = "foo";
	}

	public String getFoo() { return foo; }
	public void setFoo(String foo) { this.foo = foo; }

	public int getBar() { return bar; }
	public void setBar(int bar) { this.bar = bar; }

	public String toString() { return "wiggle " + foo + " and " + bar; }
    }

    static public class Waggle
    {
	private int baz;
	private String bang;
	Integer bork;

	public Waggle() {}

	public Waggle(int i)
	{
	    baz = i;
	    bang = "!";
	}

	public int getBaz() { return baz; }
	public void setBaz(int baz) { this.baz = baz; }

	public String getBang() { return bang; }
	public void setBang(String bang) { this.bang = bang; }

	public Integer getBork() { return bork; }
	public void setBork(Integer bork) { this.bork = bork; }

	public String toString() { return "waggle " + baz + " and " + bang; }
    }


    public Wiggle echo(Wiggle wiggle)
    {
	return wiggle;
    }

    public Waggle echo(Waggle waggle)
    {
	return waggle;
    }

    public ArrayList aWiggleArrayList(int numWiggles)
    {
	ArrayList al = new ArrayList();
	for(int i=0; i < numWiggles; i++)
	    al.add(new Wiggle(i));
	return al;
    }

    public ArrayList aWaggleArrayList(int numWaggles)
    {
	ArrayList al = new ArrayList();
	for(int i=0; i < numWaggles; i++)
	    al.add(new Waggle(i));
	return al;
    }

    public String wigOrWag(ArrayList al)
    {
	Iterator i = al.iterator();
	StringBuffer buf = new StringBuffer();
	while(i.hasNext()) {
	    Object o = i.next();
	    if(o instanceof Wiggle) {
		Wiggle w = (Wiggle)o;
		buf.append(w + " ");
	    } else if(o instanceof Waggle) {
		Waggle w = (Waggle)o;
		buf.append(w + " ");
	    } else {
		buf.append("unknown object ");
	    }
	}
	return buf.toString();
    }


    // Reference Tests

    static public class RefTest
    {
	private String s;

	public RefTest(String s)
	{
	    this.s = s;
	}

	public String toString()
	{
	    return s;
	}
    }

    static public class CallableRefTest
    {
	private static RefTest ref = new RefTest("a secret");

	public String ping()
	{
	    return "ping pong";
	}

	public RefTest getRef()
	{
	    return ref;
	}

	public String whatsInside(RefTest r)
	{
	    return r.toString();
	}
    }

    private static CallableRefTest callableRef = new CallableRefTest();

    public CallableRefTest getCallableRef()
    {
	return callableRef;
    }


    // Debug control

    public void setDebug(JSONRPCBridge bridge, boolean flag)
    {
	bridge.setDebug(flag);
    }

    // Callback tests

    public void setCallback(JSONRPCBridge bridge, boolean flag)
    {
	if(flag) {
	    bridge.registerCallback(cb, HttpServletRequest.class);
	} else {
	    bridge.unregisterCallback(cb, HttpServletRequest.class);
	}
    }

    public static InvocationCallback cb = new InvocationCallback()
	{
	    public void preInvoke(Object context, Object instance,
				  Method m, Object arguments[])
		throws Exception
	    {
		System.out.print("Test.preInvoke");
		if(instance != null)
		    System.out.print(" instance=" + instance);
		System.out.print(" method="+ m.getName());
		for(int i=0; i < arguments.length; i++)
		    System.out.print(" arg[" + i + "]=" + arguments[i]);
		System.out.println("");
	    }

	    public void postInvoke(Object context, Object instance,
				   Method m, Object result)
		throws Exception
	    {}
	};

}
