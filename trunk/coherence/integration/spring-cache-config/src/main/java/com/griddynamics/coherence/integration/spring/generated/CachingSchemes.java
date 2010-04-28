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
    "distributedSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme"
})
@XmlRootElement(name = "caching-schemes")
public class CachingSchemes {

    @XmlElements({
        @XmlElement(name = "distributed-scheme", required = true, type = DistributedScheme.class),
        @XmlElement(name = "replicated-scheme", required = true, type = ReplicatedScheme.class),
        @XmlElement(name = "optimistic-scheme", required = true, type = OptimisticScheme.class),
        @XmlElement(name = "local-scheme", required = true, type = LocalScheme.class),
        @XmlElement(name = "disk-scheme", required = true, type = DiskScheme.class),
        @XmlElement(name = "external-scheme", required = true, type = ExternalScheme.class),
        @XmlElement(name = "paged-external-scheme", required = true, type = PagedExternalScheme.class),
        @XmlElement(name = "overflow-scheme", required = true, type = OverflowScheme.class),
        @XmlElement(name = "class-scheme", required = true, type = ClassScheme.class),
        @XmlElement(name = "near-scheme", required = true, type = NearScheme.class),
        @XmlElement(name = "versioned-near-scheme", required = true, type = VersionedNearScheme.class),
        @XmlElement(name = "invocation-scheme", required = true, type = InvocationScheme.class),
        @XmlElement(name = "read-write-backing-map-scheme", required = true, type = ReadWriteBackingMapScheme.class),
        @XmlElement(name = "versioned-backing-map-scheme", required = true, type = VersionedBackingMapScheme.class),
        @XmlElement(name = "remote-cache-scheme", required = true, type = RemoteCacheScheme.class),
        @XmlElement(name = "remote-invocation-scheme", required = true, type = RemoteInvocationScheme.class),
        @XmlElement(name = "proxy-scheme", required = true, type = ProxyScheme.class)
    })
    protected List<Object> distributedSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme;

    /**
     * Gets the value of the distributedSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the distributedSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDistributedSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DistributedScheme }
     * {@link ReplicatedScheme }
     * {@link OptimisticScheme }
     * {@link LocalScheme }
     * {@link DiskScheme }
     * {@link ExternalScheme }
     * {@link PagedExternalScheme }
     * {@link OverflowScheme }
     * {@link ClassScheme }
     * {@link NearScheme }
     * {@link VersionedNearScheme }
     * {@link InvocationScheme }
     * {@link ReadWriteBackingMapScheme }
     * {@link VersionedBackingMapScheme }
     * {@link RemoteCacheScheme }
     * {@link RemoteInvocationScheme }
     * {@link ProxyScheme }
     * 
     * 
     */
    public List<Object> getDistributedSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme() {
        if (distributedSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme == null) {
            distributedSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme = new ArrayList<Object>();
        }
        return this.distributedSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme;
    }

}
