/*
 * Created on Dec 21, 2004
 */
package org.jabsorb.test;

import java.io.Serializable;

/**
 * @author cytan
 */
public class BeanB implements Serializable {

    private final static long serialVersionUID = 2;

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private BeanA beanA;

    public BeanA getBeanA() {
        return beanA;
    }

    public void setBeanA(BeanA beanA) {
        this.beanA = beanA;
    }
}
