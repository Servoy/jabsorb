package com.metaparadigm.jsonrpc.test;

import java.io.Serializable;

public class Hello implements Serializable
{
    private final static long serialVersionUID = 1;

    public String sayHello(String who)
    {
	return "hello " + who;
    }
}
