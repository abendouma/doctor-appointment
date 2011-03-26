package com.angelspeech.service.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.angelspeech.service.hibernate.HibernateDAOFactory;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
public abstract class DAOFactory {
    private static Log log = LogFactory.getLog(DAOFactory.class);

    /**
     * Creates a standalone DAOFactory that returns unmanaged DAO
     * beans for use in any environment Hibernate has been configured
     * for. Uses HibernateUtil/SessionFactory and Hibernate context
     * propagation (CurrentSessionContext), thread-bound or transaction-bound,
     * and transaction scoped.
     */
    public static final Class HIBERNATE = HibernateDAOFactory.class;

    /**
     * Factory method for instantiation of concrete factories.
     */
    public static DAOFactory instance(Class factory) {
        try {
            log.debug("Creating concrete DAO factory: " + factory);
            return (DAOFactory)factory.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't create DAOFactory: " + factory);
        }
    }


    // Add your DAO interfaces here
    public abstract DoctorDAO getDoctorDAO();
}
