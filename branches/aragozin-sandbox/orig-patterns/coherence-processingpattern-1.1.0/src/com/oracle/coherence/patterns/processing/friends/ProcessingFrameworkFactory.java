/*
 * File: ProcessingFrameworkFactory.java
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

package com.oracle.coherence.patterns.processing.friends;

import com.oracle.coherence.patterns.processing.dispatchers.DispatchController;

import com.oracle.coherence.patterns.processing.task.ExecutorManager;

import com.tangosol.net.ConfigurableCacheFactory;

import java.lang.reflect.Constructor;

/**
* This is the entry point to the {@link ProcessingFrameworkFactory}. This class
* is a singleton per JVM and is retrieved using the {@code getInstance}
* method. 
* 
* @author Christer Fahlgren 2009.09.30
*/
public abstract class ProcessingFrameworkFactory
    {

    // ----- Constructors ----------------------------------------------------

    /**
    * Default constructor left empty intentionally.
    */
    protected ProcessingFrameworkFactory()
        {
        // Left empty intentionally
        }

    // ----- ProcessingFrameworkFactory Methods -----------------------------

    /**
    * This is the main entry point for getting hold of the
    * {@link ProcessingFrameworkFactory}.
    * 
    * @return the AbstractProcessingPatternFactory instance.
    */
    public static synchronized ProcessingFrameworkFactory getInstance()
        {
        if (s_oInstance == null)
            {
            s_oInstance = loadAbstractProcessingFrameworkFactory();
            s_oInstance.init();
            }
        return s_oInstance;
        }

    /**
     * This is the main entry point for getting hold of the
     * {@link ProcessingFrameworkFactory}.
     * 
     * @param oCCFactory the {@link ConfigurableCacheFactory} to use.
     * 
     * @return the AbstractProcessingPatternFactory instance.
     */
     public static synchronized ProcessingFrameworkFactory getInstance(ConfigurableCacheFactory oCCFactory)
         {
         if (s_oInstance == null)
             {
             s_oInstance = loadAbstractProcessingFrameworkFactory();
             s_oInstance.init(oCCFactory);
             }
         return s_oInstance;
         }

    /**
    * The init method must be called before doing anything with the
    * {@link ProcessingFrameworkFactory}.
    *
    * @param oCCFactory the {@link ConfigurableCacheFactory} to use
    */
    public abstract void init(ConfigurableCacheFactory oCCFactory);

    /**
    * The init method must be called before doing anything with the
    * {@link ProcessingFrameworkFactory}. The CacheFactory used is 
    * the default.
    */
    public abstract void init();

    /**
    * The shutdown method must be called to exit the {@link ProcessingFrameworkFactory}.
    */
    public abstract void shutdown();

    /**
    * Call if you want to change the implementation for the factory.
    * 
    * @param className the class implementing {@link ProcessingFrameworkFactory}
    */
    public static final void setImplementingClass(String className)
        {
        s_oAbstractProcessingFrameworkFactoryImpl = className;
        }

    /**
    * Returns the {@link SubmissionManager} for this Factory.
    * 
    * @return the SubmissionManager
    */
    public abstract SubmissionManager getSubmissionManager();

    /**
    * Returns the {@link SubmissionResultManager} for this Factory.
    * 
    * @return the SubmissionResultManager
    */
    public abstract SubmissionResultManager getSubmissionResultManager();

    /**
    * Returns the {@link DispatcherManager} for this Factory.
    * 
    * @return the DispatcherManager
    */
    public abstract DispatcherManager getDispatcherManager();

    /**
    * Returns the {@link DispatchController} for this Factory.
    * 
    * @return the DispatchController
    */
    public abstract DispatchController getDispatchController();


    /**
    * Returns the {@link ExecutorManager} for this Factory.
     * 
     * @return the ExecutorManager
     */
     public abstract ExecutorManager getExecutorManager();

    // --------------- Private Utility Methods ------------------------------
    
    /**
    * Load the class which implements the {@link ProcessingFrameworkFactory}.
    * 
    * @return the {@link ProcessingFrameworkFactory}
    */
    private static final ProcessingFrameworkFactory loadAbstractProcessingFrameworkFactory()
        {
        String sClassName = getImplementingClassName();
        try
            {
            Class<ProcessingFrameworkFactory> clzFactory = getImplementation(sClassName);
            Constructor<ProcessingFrameworkFactory> constructor = clzFactory
                .getConstructor(new Class[] {});
            return constructor.newInstance();
            }

        catch (ClassNotFoundException oClassNotFoundException)
            {
            throw new RuntimeException(
                "Could not locate the implementing class\"" + sClassName
                                + "\"");
            }
        catch (Exception oException)
            {
            throw new RuntimeException(
                "Could not Load the ProcessingFrameworkFactory"
                                + oException.getMessage());
            }
        }

    /**
    * Gets the class name implementing the {@link ProcessingFrameworkFactory}.
    * 
    * @return the class name
    */
    private static final String getImplementingClassName()
        {
        String className = DEFAULTPROCESSINGFRAMEWORKFACTORYIMPL;
        if (s_oAbstractProcessingFrameworkFactoryImpl != null)
            {
            className = s_oAbstractProcessingFrameworkFactoryImpl;
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
    private static final Class<ProcessingFrameworkFactory> getImplementation(
        String className) throws ClassNotFoundException
        {
        return (Class<ProcessingFrameworkFactory>) Class
            .forName(className, true, Thread.currentThread()
                .getContextClassLoader());
        }

    // ----- Static members ----------------------------------------------

    /**
    * The implementation to be used. If not provided, the default one will be
    * used.
    */
    private static String                   s_oAbstractProcessingFrameworkFactoryImpl;

    /**
    * Singleton Instance for the {@link ProcessingFrameworkFactory}.
    */
    private static ProcessingFrameworkFactory s_oInstance;

    // ----- Constants ----------------------------------------------------

    /**
    * The default implementation to be used, in case the user does not
    * provide the implementation to be used.
    */
    private static final String             DEFAULTPROCESSINGFRAMEWORKFACTORYIMPL = "com.oracle.coherence.patterns.processing.internal.DefaultProcessingFrameworkFactory";

    }
