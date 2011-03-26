package com.angelspeed.service.util;

import org.hibernate.*;
import org.hibernate.cfg.*;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
public class HibernateUtil {
   public static final SessionFactory sessionFactory;

   static {
      try {
         // Create the SessionFactory from hibernate.cfg.xml
         sessionFactory = new AnnotationConfiguration()
         		.configure().buildSessionFactory();
      }
      catch (Throwable ex) {
         // Make sure you log the exception, as it might be swallowed
         System.err.println("Initial SessionFactory creation failed." + ex);
         throw new ExceptionInInitializerError(ex);
      }
   }

   public static SessionFactory getSessionFactory(){
	   return sessionFactory;
   }
   
}
