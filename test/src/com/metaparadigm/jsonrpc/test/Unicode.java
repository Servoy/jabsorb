package com.metaparadigm.jsonrpc.test;

import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class Unicode
{

    private static InputStream getResourceStream(String rsrcName)
	throws IOException
    {
	ClassLoader loader =
	    Thread.currentThread().getContextClassLoader();
	return loader.getResourceAsStream("unicode/" + rsrcName);
    }

    public static class UnicodeTest
    {
	private String desc;
	private String charset;
	private String rsrc;
	private String data;
	private boolean compares = false;

	public void setDescription(String desc) { this.desc = desc; }
	public void setCharset(String charset) { this.charset = charset; }
	public void setResource(String rsrc) { this.rsrc = rsrc; }
	public void setData(String data) { this.data = data; }
	private void setCompares(boolean b) { compares = b; }

	public String getDescription() { return desc; }
	public String getCharSet() { return charset; }
	public boolean getCompares() { return compares; }

	public synchronized String getData()
	    throws IOException
	{
	    if(data == null) loadData();
	    return data;
	}

	protected void loadData()
	    throws IOException
	{
	    BufferedReader in = new BufferedReader
		(new InputStreamReader(getResourceStream(rsrc), charset));
	    StringBuffer sb = new StringBuffer();
	    String line;
	    while((line = in.readLine()) != null) sb.append(line);
	    in.close();
	    data = sb.toString();
	}
    }

    protected static class UnicodeTestStore
    {
	private HashMap tests = new HashMap();
	private Properties testProps = new Properties();

	protected UnicodeTestStore(String indexName)
	{
	    try {
		InputStream in = getResourceStream(indexName);
		testProps.load(in);
		in.close();
		Iterator i = testProps.entrySet().iterator();
		while(i.hasNext()) {
		    Map.Entry m = (Map.Entry)i.next();
		    String key = (String)m.getKey();
		    String value = (String)m.getValue();
		    StringTokenizer tok = new StringTokenizer(key, ".");
		    String testName = tok.nextToken();
		    if(!tok.hasMoreElements())
			throw new Exception("invalid syntax: " + key);
		    String testAttr = tok.nextToken();
		    UnicodeTest test = (UnicodeTest)tests.get(testName);
		    if(test == null) {
			test = new UnicodeTest();
			tests.put(testName, test);
		    }
		    if(testAttr.equals("description")) {
			test.setDescription(value);
		    } else if(testAttr.equals("charset")) {
			test.setCharset(value);
		    } else if(testAttr.equals("resource")) {
			test.setResource(value);
		    } else {
			throw new Exception("invalid attribute: " + key);
		    }
		}
	    } catch(Exception e) {
		System.out.println("UnicodeTestStore(): " + e);
	    }
	}

	public HashMap getTests() { return tests; }
    }

    private UnicodeTestStore store =
	new UnicodeTestStore("00index.properties");

    public HashMap getTests()
    {
	return store.getTests();
    }

    public HashMap compareTests(HashMap remoteTests)
	throws Exception
    {
	Iterator i = remoteTests.entrySet().iterator();
	while(i.hasNext()) {
	    Map.Entry m = (Map.Entry)i.next();
	    String testName = (String)m.getKey();
	    UnicodeTest remoteTest =
		(UnicodeTest)m.getValue();
	    UnicodeTest localTest =
		(UnicodeTest)store.getTests().get(testName);
	    if(localTest == null)
		throw new Exception("test not found");
	    remoteTest.setCompares
		(localTest.getData().equals(remoteTest.getData()));
	}
	return remoteTests;
    }
}
