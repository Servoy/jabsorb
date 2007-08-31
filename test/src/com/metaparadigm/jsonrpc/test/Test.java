package com.metaparadigm.jsonrpc.test;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Test
{

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

    public void voidFunction()
    {

    }

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
	for(int i=0; i<10; i++) {
	    Wiggle w = new Wiggle();
	    w.setFoo("foo " + i);
	    w.setBar(i);
	    ht.put(new Integer(i), w);
	}
	return ht;
    }

    public String[] twice(String string)
    {
	return new String[] { string, string };
    }

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

    public String echo(String msg1, String msg2)
    {
	return msg1 + " and " + msg2;
    }

    public String echo(String message)
    {
	return message;
    }

    public String echo(String strings[])
    {
	StringBuffer buf = new StringBuffer();
	buf.append("appended strings: ");
	for(int i=0; i<strings.length; i++) {
	    if(i > 0) buf.append(",");
	    buf.append("\"" + strings[i] + "\"");
	}
	return buf.toString();
    }

    public int echo(int number)
    {
	return number;
    }

    public String echo(int nums[])
    {
	StringBuffer buf = new StringBuffer();
	buf.append("[");
	for(int i=0; i<nums.length; i++) {
	    if(i > 0) buf.append(",");
	    buf.append("\"" + nums[i] + "\"");
	}
	buf.append("]");
	return buf.toString();
    }


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

	public String toString() { return "waggle " + baz + " and " + bang; }
    }


    public String echo(Wiggle wiggle)
    {
	return wiggle.toString();
    }

    public String echo(Waggle waggle)
    {
	return waggle.toString();
    }


    public Waggle wiggleToWaggle(Wiggle wiggle)
    {
	Waggle waggle = new Waggle();
	waggle.setBaz(wiggle.getBar());
	waggle.setBang(wiggle.getFoo());
	return waggle;
    }

    public Wiggle waggleToWiggle(Waggle waggle)
    {
	Wiggle wiggle = new Wiggle();
	wiggle.setBar(waggle.getBaz());
	wiggle.setFoo(waggle.getBang());
	return wiggle;
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

    public static void fark()
	throws Exception
    {
	throw new Exception("fark!");
    }
}
