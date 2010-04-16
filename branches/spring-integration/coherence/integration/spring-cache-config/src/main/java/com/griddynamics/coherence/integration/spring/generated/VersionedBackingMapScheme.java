//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.04.16 at 09:47:17 PM MSD 
//


package com.griddynamics.coherence.integration.spring.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "schemeName",
    "schemeRef",
    "className",
    "initParams",
    "internalCacheScheme",
    "missCacheScheme",
    "cachestoreScheme",
    "readOnly",
    "writeDelayOrWriteDelaySeconds",
    "writeBatchFactor",
    "writeRequeueThreshold",
    "refreshAheadFactor",
    "rollbackCachestoreFailures",
    "versionPersistentScheme",
    "versionTransientScheme",
    "manageTransient",
    "listener",
    "autostart"
})
@XmlRootElement(name = "versioned-backing-map-scheme")
public class VersionedBackingMapScheme {

    @XmlElement(name = "scheme-name")
    protected SchemeName schemeName;
    @XmlElement(name = "scheme-ref")
    protected String schemeRef;
    @XmlElement(name = "class-name")
    protected ClassName className;
    @XmlElement(name = "init-params")
    protected InitParams initParams;
    @XmlElement(name = "internal-cache-scheme")
    protected InternalCacheScheme internalCacheScheme;
    @XmlElement(name = "miss-cache-scheme")
    protected MissCacheScheme missCacheScheme;
    @XmlElement(name = "cachestore-scheme")
    protected CachestoreScheme cachestoreScheme;
    @XmlElement(name = "read-only")
    protected String readOnly;
    @XmlElements({
        @XmlElement(name = "write-delay", type = WriteDelay.class),
        @XmlElement(name = "write-delay-seconds", type = WriteDelaySeconds.class)
    })
    protected List<Object> writeDelayOrWriteDelaySeconds;
    @XmlElement(name = "write-batch-factor")
    protected String writeBatchFactor;
    @XmlElement(name = "write-requeue-threshold")
    protected String writeRequeueThreshold;
    @XmlElement(name = "refresh-ahead-factor")
    protected String refreshAheadFactor;
    @XmlElement(name = "rollback-cachestore-failures")
    protected String rollbackCachestoreFailures;
    @XmlElement(name = "version-persistent-scheme")
    protected VersionPersistentScheme versionPersistentScheme;
    @XmlElement(name = "version-transient-scheme")
    protected VersionTransientScheme versionTransientScheme;
    @XmlElement(name = "manage-transient")
    protected String manageTransient;
    protected Listener listener;
    protected Autostart autostart;

    /**
     * Gets the value of the schemeName property.
     * 
     * @return
     *     possible object is
     *     {@link SchemeName }
     *     
     */
    public SchemeName getSchemeName() {
        return schemeName;
    }

    /**
     * Sets the value of the schemeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link SchemeName }
     *     
     */
    public void setSchemeName(SchemeName value) {
        this.schemeName = value;
    }

    /**
     * Gets the value of the schemeRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchemeRef() {
        return schemeRef;
    }

    /**
     * Sets the value of the schemeRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchemeRef(String value) {
        this.schemeRef = value;
    }

    /**
     * Gets the value of the className property.
     * 
     * @return
     *     possible object is
     *     {@link ClassName }
     *     
     */
    public ClassName getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClassName }
     *     
     */
    public void setClassName(ClassName value) {
        this.className = value;
    }

    /**
     * Gets the value of the initParams property.
     * 
     * @return
     *     possible object is
     *     {@link InitParams }
     *     
     */
    public InitParams getInitParams() {
        return initParams;
    }

    /**
     * Sets the value of the initParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link InitParams }
     *     
     */
    public void setInitParams(InitParams value) {
        this.initParams = value;
    }

    /**
     * Gets the value of the internalCacheScheme property.
     * 
     * @return
     *     possible object is
     *     {@link InternalCacheScheme }
     *     
     */
    public InternalCacheScheme getInternalCacheScheme() {
        return internalCacheScheme;
    }

    /**
     * Sets the value of the internalCacheScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link InternalCacheScheme }
     *     
     */
    public void setInternalCacheScheme(InternalCacheScheme value) {
        this.internalCacheScheme = value;
    }

    /**
     * Gets the value of the missCacheScheme property.
     * 
     * @return
     *     possible object is
     *     {@link MissCacheScheme }
     *     
     */
    public MissCacheScheme getMissCacheScheme() {
        return missCacheScheme;
    }

    /**
     * Sets the value of the missCacheScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link MissCacheScheme }
     *     
     */
    public void setMissCacheScheme(MissCacheScheme value) {
        this.missCacheScheme = value;
    }

    /**
     * Gets the value of the cachestoreScheme property.
     * 
     * @return
     *     possible object is
     *     {@link CachestoreScheme }
     *     
     */
    public CachestoreScheme getCachestoreScheme() {
        return cachestoreScheme;
    }

    /**
     * Sets the value of the cachestoreScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link CachestoreScheme }
     *     
     */
    public void setCachestoreScheme(CachestoreScheme value) {
        this.cachestoreScheme = value;
    }

    /**
     * Gets the value of the readOnly property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReadOnly() {
        return readOnly;
    }

    /**
     * Sets the value of the readOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReadOnly(String value) {
        this.readOnly = value;
    }

    /**
     * Gets the value of the writeDelayOrWriteDelaySeconds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the writeDelayOrWriteDelaySeconds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWriteDelayOrWriteDelaySeconds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WriteDelay }
     * {@link WriteDelaySeconds }
     * 
     * 
     */
    public List<Object> getWriteDelayOrWriteDelaySeconds() {
        if (writeDelayOrWriteDelaySeconds == null) {
            writeDelayOrWriteDelaySeconds = new ArrayList<Object>();
        }
        return this.writeDelayOrWriteDelaySeconds;
    }

    /**
     * Gets the value of the writeBatchFactor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWriteBatchFactor() {
        return writeBatchFactor;
    }

    /**
     * Sets the value of the writeBatchFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWriteBatchFactor(String value) {
        this.writeBatchFactor = value;
    }

    /**
     * Gets the value of the writeRequeueThreshold property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWriteRequeueThreshold() {
        return writeRequeueThreshold;
    }

    /**
     * Sets the value of the writeRequeueThreshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWriteRequeueThreshold(String value) {
        this.writeRequeueThreshold = value;
    }

    /**
     * Gets the value of the refreshAheadFactor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefreshAheadFactor() {
        return refreshAheadFactor;
    }

    /**
     * Sets the value of the refreshAheadFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefreshAheadFactor(String value) {
        this.refreshAheadFactor = value;
    }

    /**
     * Gets the value of the rollbackCachestoreFailures property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRollbackCachestoreFailures() {
        return rollbackCachestoreFailures;
    }

    /**
     * Sets the value of the rollbackCachestoreFailures property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRollbackCachestoreFailures(String value) {
        this.rollbackCachestoreFailures = value;
    }

    /**
     * Gets the value of the versionPersistentScheme property.
     * 
     * @return
     *     possible object is
     *     {@link VersionPersistentScheme }
     *     
     */
    public VersionPersistentScheme getVersionPersistentScheme() {
        return versionPersistentScheme;
    }

    /**
     * Sets the value of the versionPersistentScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link VersionPersistentScheme }
     *     
     */
    public void setVersionPersistentScheme(VersionPersistentScheme value) {
        this.versionPersistentScheme = value;
    }

    /**
     * Gets the value of the versionTransientScheme property.
     * 
     * @return
     *     possible object is
     *     {@link VersionTransientScheme }
     *     
     */
    public VersionTransientScheme getVersionTransientScheme() {
        return versionTransientScheme;
    }

    /**
     * Sets the value of the versionTransientScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link VersionTransientScheme }
     *     
     */
    public void setVersionTransientScheme(VersionTransientScheme value) {
        this.versionTransientScheme = value;
    }

    /**
     * Gets the value of the manageTransient property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManageTransient() {
        return manageTransient;
    }

    /**
     * Sets the value of the manageTransient property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManageTransient(String value) {
        this.manageTransient = value;
    }

    /**
     * Gets the value of the listener property.
     * 
     * @return
     *     possible object is
     *     {@link Listener }
     *     
     */
    public Listener getListener() {
        return listener;
    }

    /**
     * Sets the value of the listener property.
     * 
     * @param value
     *     allowed object is
     *     {@link Listener }
     *     
     */
    public void setListener(Listener value) {
        this.listener = value;
    }

    /**
     * Gets the value of the autostart property.
     * 
     * @return
     *     possible object is
     *     {@link Autostart }
     *     
     */
    public Autostart getAutostart() {
        return autostart;
    }

    /**
     * Sets the value of the autostart property.
     * 
     * @param value
     *     allowed object is
     *     {@link Autostart }
     *     
     */
    public void setAutostart(Autostart value) {
        this.autostart = value;
    }

}
