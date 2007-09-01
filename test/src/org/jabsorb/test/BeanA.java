/*
 * Created on Dec 21, 2004
 */
package org.jabsorb.test;

import java.io.Serializable;

/**
 * @author cytan
 */
public class BeanA implements Serializable {

    private final static long serialVersionUID = 2;

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private BeanB beanB;

    public BeanB getBeanB() {
        return beanB;
    }

    public void setBeanB(BeanB beanB) {
        this.beanB = beanB;
    }
}
