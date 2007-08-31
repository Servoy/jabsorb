/*
 * Created on Dec 21, 2004
 */
package com.metaparadigm.jsonrpc.test;

/**
 * @author cytan
 */
public class BeanB {
    private long id;
    
    public long getId(){
        return id;
    }
    
    public void setId(long id){
        this.id = id;
    }

    private BeanA beanA;
    
    public BeanA getBeanA(){
        return beanA;
    }
    
    public void setBeanA(BeanA beanA){
        this.beanA = beanA;
    }
}
