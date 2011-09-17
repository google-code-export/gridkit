package org.gridkit.search.gemfire.benchmark;

import com.gemstone.gemfire.cache.Region;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.gridkit.search.gemfire.benchmark.model.Commitment;
import org.gridkit.search.gemfire.benchmark.model.Fts;
import org.gridkit.search.gemfire.benchmark.model.JaxbFactory;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import static org.gridkit.search.gemfire.benchmark.BenchmarkFactory.commitmentRegionName;

public class FtsData {
    private Unmarshaller unmarshaller = JaxbFactory.createUnmarshaller();

    private List<Commitment> commitments = new ArrayList<Commitment>();

    public FtsData(String folderLocation) throws JAXBException {
        File folder = new File(folderLocation);

        for (File file : folder.listFiles((FilenameFilter)new WildcardFileFilter("*.xml"))){
            Fts fts = (Fts) unmarshaller.unmarshal(file);
            commitments.addAll(fts.getCommitments());
        }
    }

    public List<String> getPositionKeys() {
        List<String> keys = new ArrayList<String>(commitments.size());

        for (Commitment commitment : commitments)
            keys.add(commitment.getPositionKey());

        return keys;
    }

    public void fillRegion(Region<String, Commitment> commitmentRegion) {
        for (Commitment commitment : commitments)
            commitmentRegion.put(commitment.getPositionKey(), commitment);
    }

    public List<Commitment> getCommitments() {
        return commitments;
    }
}
