/*
 * File: ProcessingSessionFactory.java
 * 
 * Copyright (c) 2009. All Rights Reserved. Oracle Corporation.
 * 
 * Oracle is a registered trademark of Oracle Corporation and/or its
 * affiliates.
 * 
 * This software is the confidential and proprietary information of Oracle
 * Corporation. You shall not disclose such confidential and proprietary
 * information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Oracle Corporation.
 * 
 * Oracle Corporation makes no representations or warranties about 
 * the suitability of the software, either express or implied, 
 * including but not limited to the implied warranties of 
 * merchantability, fitness for a particular purpose, or 
 * non-infringement.  Oracle Corporation shall not be liable for 
 * any damages suffered by licensee as a result of using, modifying 
 * or distributing this software or its derivatives.
 * 
 * This notice may not be removed or altered.
 */

package com.oracle.coherence.patterns.processing;


import com.oracle.coherence.common.identifiers.Identifier;

import com.tangosol.net.ConfigurableCacheFactory;

import java.lang.reflect.Constructor;


/**
* The {@link ProcessingSessionFactory} allows you to get a {@link ProcessingSession}
* which is the main client interface in to the Processing Pattern Framework. 
* Since the {@link ProcessingSessionFactory} is a singleton, you need to retrieve 
* the instance using the {@code getInstance} method.
* 
* @author Christer Fahlgren 2009.09.29
*/
public abstract class ProcessingSessionFactory
    {
    // ----- Constructors ---------------------------------------------------

    /**
    * Default constructor left empty intentionally.
    */
    protected ProcessingSessionFactory()
        {
        // Left empty intentionally
        }

    
    // ----- Object Methods -------------------------------------------------

    /**
    * This is the main entry point for getting hold of the
    * {@link ProcessingSessionFactory}.
    * 
    * @return the {@link ProcessingSessionFactory} instance.
    */
    public static synchronized ProcessingSessionFactory getInstance()
        {
        if (s_oInstance == null)
            {
            s_oInstance = loadAbstractProcessingSessionFactory();
            s_oInstance.init();
            }
        return s_oInstance;
        }

    /**
    * This is the main entry point for getting hold of the
    * {@link ProcessingSessionFactory}.
    * 
    * @param oCCFactory the {@link ConfigurableCacheFactory} to use.
    */
    public static synchronized ProcessingSessionFactory getInstance(ConfigurableCacheFactory oCCFactory)
        {
        if (s_oInstance == null)
            {
            s_oInstance = loadAbstractProcessingSessionFactory();
            s_oInstance.init(oCCFactory);
            }
        return s_oInstance;
        }

    /**
    * The init method initializes the {@link ProcessingSessionFactory}.
    * 
    * @param oCCFactory the {@link ConfigurableCacheFactory} to use
    */
    public abstract void init(ConfigurableCacheFactory oCCFactory);

    /**
     * The init method initializes the {@link ProcessingSessionFactory}.
     * The ConfigurableCacheFactory used is the default CacheFactory.
     */
    public abstract void init();
    
    /**
     * The shutdown method must be called to exit the {@link ProcessingSessionFactory}.
     * 
     */
     public abstract void shutdown();

    /**
    * Call if you want to change the implementation for the factory.
    * 
    * @param sClassName the class implementing {@link ProcessingSessionFactory}
    */
    public static final void setImplementingClass(String sClassName)
        {
        s_sAbstractProcessingSessionFactoryImplName = sClassName;
        }

    /**
    * Returns a created {@link ProcessingSession}.
    * 
    * @param oIdentifier    the {@link Identifier} to be used for the Session.
    * 
    * @return the {@link ProcessingSession} for this factory
    */
    public abstract ProcessingSession getSession(Identifier oIdentifier);

    // --------------- Private Utility Methods ------------------------------
   
    /**
    * Load the class which implements the {@link ProcessingSessionFactory}.
    * 
    * @return the {@link ProcessingSessionFactory}
    */
    private static final ProcessingSessionFactory loadAbstractProcessingSessionFactory()
        {
        String sClassName = getImplementingClassName();
        try
            {
            Class<ProcessingSessionFactory> clzFactory = 
                    getImplementation(sClassName);
            Constructor<ProcessingSessionFactory> constructor = 
                    clzFactory.getConstructor(new Class[] {});
            return constructor.newInstance();
            }
        catch (ClassNotFoundException oClassNotFoundException)
            {
            throw new RuntimeException(
                "Could not locate the implementing class\"" 
                + sClassName + "\"");
            }
        catch (Exception oException)
            {
            throw new RuntimeException(
                "Could not Load the ProcessingSessionFactory"
                + oException.getMessage());
            }
        }

    /**
    * Gets the class name implementing the {@link ProcessingSessionFactory}.
    * 
    * @return the class name
    */
    private static final String getImplementingClassName()
        {
        String className = DEFAULTPROCESSINGSESSIONFACTORYIMPL;
        if (s_sAbstractProcessingSessionFactoryImplName != null)
            {
            className = s_sAbstractProcessingSessionFactoryImplName;
            }
        return className;
        }

    /**
    * Returns the implementing class.
    * 
    * @param className the name of the class implementing the factory
    * 
    * @return the class implementing the factory
    * 
    * @throws ClassNotFoundException if the class can't be loaded
    */
    @SuppressWarnings("unchecked")
    private static final Class<ProcessingSessionFactory> getImplementation(
        String className) throws ClassNotFoundException
        {
        return (Class<ProcessingSessionFactory>) Class
                .forName(className, true, Thread.currentThread()
                         .getContextClassLoader());
        }

    
    // ----- Member Variables ----------------------------------------------

    /**
    * The implementation to be used. If not provided, the default one will be
    * used.
    */
    private static String s_sAbstractProcessingSessionFactoryImplName;

    /**
    * Singleton Instance for the {@link ProcessingSessionFactory}.
    */
    private static ProcessingSessionFactory s_oInstance;

    // ----- Constants ------------------------------------------------------

    /**
    * The default implementation to be used, in case the user does not
    * provide the implementation to be used.
    */
    private static final String DEFAULTPROCESSINGSESSIONFACTORYIMPL =
            "com.oracle.coherence.patterns.processing.internal.DefaultProcessingSessionFactory";

   

    }
