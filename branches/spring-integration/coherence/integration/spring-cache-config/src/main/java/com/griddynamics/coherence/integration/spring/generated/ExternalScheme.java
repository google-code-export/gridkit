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
    "asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager",
    "highUnits",
    "unitCalculator",
    "unitFactor",
    "expiryDelay",
    "listener"
})
@XmlRootElement(name = "external-scheme")
public class ExternalScheme {

    @XmlElement(name = "scheme-name")
    protected SchemeName schemeName;
    @XmlElement(name = "scheme-ref")
    protected String schemeRef;
    @XmlElement(name = "class-name")
    protected ClassName className;
    @XmlElement(name = "init-params")
    protected InitParams initParams;
    @XmlElements({
        @XmlElement(name = "async-store-manager", type = AsyncStoreManager.class),
        @XmlElement(name = "custom-store-manager", type = CustomStoreManager.class),
        @XmlElement(name = "lh-file-manager", type = LhFileManager.class),
        @XmlElement(name = "bdb-store-manager", type = BdbStoreManager.class),
        @XmlElement(name = "nio-file-manager", type = NioFileManager.class),
        @XmlElement(name = "nio-memory-manager", type = NioMemoryManager.class)
    })
    protected List<Object> asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager;
    @XmlElement(name = "high-units")
    protected String highUnits;
    @XmlElement(name = "unit-calculator")
    protected String unitCalculator;
    @XmlElement(name = "unit-factor")
    protected String unitFactor;
    @XmlElement(name = "expiry-delay")
    protected String expiryDelay;
    protected Listener listener;

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
     * Gets the value of the asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAsyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AsyncStoreManager }
     * {@link CustomStoreManager }
     * {@link LhFileManager }
     * {@link BdbStoreManager }
     * {@link NioFileManager }
     * {@link NioMemoryManager }
     * 
     * 
     */
    public List<Object> getAsyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager() {
        if (asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager == null) {
            asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager = new ArrayList<Object>();
        }
        return this.asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager;
    }

    /**
     * Gets the value of the highUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHighUnits() {
        return highUnits;
    }

    /**
     * Sets the value of the highUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHighUnits(String value) {
        this.highUnits = value;
    }

    /**
     * Gets the value of the unitCalculator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnitCalculator() {
        return unitCalculator;
    }

    /**
     * Sets the value of the unitCalculator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnitCalculator(String value) {
        this.unitCalculator = value;
    }

    /**
     * Gets the value of the unitFactor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnitFactor() {
        return unitFactor;
    }

    /**
     * Sets the value of the unitFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnitFactor(String value) {
        this.unitFactor = value;
    }

    /**
     * Gets the value of the expiryDelay property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExpiryDelay() {
        return expiryDelay;
    }

    /**
     * Sets the value of the expiryDelay property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExpiryDelay(String value) {
        this.expiryDelay = value;
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

}
