package org.gridkit.search.gemfire.benchmark.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "fts")
public class Fts {
    @XmlElement(name="commitment")
    private List<Commitment> commitments = new LinkedList<Commitment>();

    public List<Commitment> getCommitments() {
        return commitments;
    }

    public void setCommitments(List<Commitment> commitments) {
        this.commitments = commitments;
    }
}
