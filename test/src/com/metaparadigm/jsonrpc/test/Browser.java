package com.metaparadigm.jsonrpc.test;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Random;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Browser
{
    protected static class BrowserStore
    {
	private Set userAgents = new TreeSet();
	private String dataFile;

	protected BrowserStore(String suffix)
	{
	    dataFile = System.getProperty("user.home") +
		"/.json-rpc-java-browsers-" + suffix + ".txt";
	    try {
		load();
	    } catch(IOException e) {
		System.out.println("BrowserStore(): " + e);
	    }
	}

	protected synchronized void load()
	    throws IOException
	{
	    BufferedReader in =
		new BufferedReader(new FileReader(dataFile));
	    String line;
	    while((line = in.readLine()) != null) userAgents.add(line);
	    in.close();
	}

	protected synchronized void save()
	    throws IOException
	{
	    PrintWriter out = new PrintWriter
		(new BufferedWriter(new FileWriter(dataFile)));
	    Iterator i = userAgents.iterator();
	    while(i.hasNext()) out.println(i.next());
	    out.close();
	}

	protected boolean addUserAgent(String userAgent)
	    throws IOException
	{
	    if(!userAgents.contains(userAgent)) {
		userAgents.add(userAgent);
		save();
		return true;
	    }
	    return false;
	}

	protected Set getUserAgents()
	{
	    return userAgents;
	}

    }

    private static BrowserStore passStore = new BrowserStore("pass");
    private static BrowserStore failStore = new BrowserStore("fail");

    public String userAgent;
    public boolean firstRun = true;
    public boolean failed = false;
    public boolean passed = false;
    public boolean addNotify = false;

    private String failKey;

    /*
    private static String makeKey()
    {
    	byte b[] = new byte[8];
    	new Random().nextBytes(b);
	StringBuffer sb = new StringBuffer();
	for(int i=0; i < 8; i++) {
	    sb.append(b[i] & 0x0f + 'a');
	    sb.append((b[i] >> 4) & 0x0f + 'a');
	}
	return sb.toString();
    }
    */

    public synchronized void passUserAgent()
	throws IOException
    {
	if(passed) return;
	System.out.println("Browser.passUserAgent(\"" + userAgent + "\")");
	addNotify = passStore.addUserAgent(userAgent);
	passed = true;
    }

    public synchronized void failUserAgent()
	throws IOException
    {
	if(failed) return;
	System.out.println("Browser.failUserAgent(\"" + userAgent + "\")");
	addNotify = failStore.addUserAgent(userAgent);
	failed = true;
    }

    public synchronized Set getPassedUserAgents()
	throws IOException
    {
	return passStore.getUserAgents();
    }

    public synchronized Set getFailedUserAgents()
	throws IOException
    {
	return failStore.getUserAgents();
    }

}
