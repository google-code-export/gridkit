package org.gridkit.search.gemfire.benchmark;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.gridkit.search.gemfire.benchmark.model.Commitment;
import org.gridkit.search.gemfire.benchmark.model.Fts;
import org.gridkit.search.gemfire.benchmark.model.JaxbFactory;

import com.gemstone.gemfire.cache.Region;

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
    	int n = 0;
    	Map<String, Commitment> buffer = new HashMap<String, Commitment>();        
    	for (Commitment commitment : commitments) {
    		++n;
            buffer.put(commitment.getPositionKey(), commitment);
            if (buffer.size() > 100) {
            	commitmentRegion.putAll(buffer);
            	buffer.clear();
            }            
            if (n % 10000 == 0) {
            	System.out.println("Loading " + n + "/" + commitments.size());
            }
        }
        commitmentRegion.putAll(buffer);
    }

    public Map<String, Map<String, Integer>> getLineDistribution() {
        Map<String, Map<String, Integer>> result = new HashMap<String, Map<String, Integer>>();
        
        for (Commitment commitment : commitments) {
            String department = commitment.getResponsibleDepartment();
            String budgetLine = commitment.getBudgetLine();
            
            if (!result.containsKey(department))
                result.put(department, new HashMap<String, Integer>());
            
            Map<String, Integer> departmentMap = result.get(department);
            
            if (departmentMap.containsKey(budgetLine))
                departmentMap.put(budgetLine, departmentMap.get(budgetLine) + 1);
            else
                departmentMap.put(budgetLine, 1);
        }
        
        return result;
    }
    
    public List<Commitment> getCommitments() {
        return commitments;
    }
}
