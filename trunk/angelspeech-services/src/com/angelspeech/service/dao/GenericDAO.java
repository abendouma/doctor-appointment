package com.angelspeech.service.dao;

import java.io.Serializable;
import java.util.List;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
public interface GenericDAO<T, ID extends Serializable> {
	T findById(ID id, boolean lock);
    List<T> findAll();
    List<T> findByExample(T exampleInstance, String... excludeProperty);
    T makePersistent(T entity);
    void makeTransient(T entity);
}
