package org.gridkit.lab.avalanche;

import org.gridkit.vicluster.ViManager;

@Component
public class HBaseComponent {
	
	@Configured
	@LookupHint("shared-resource-strategy")
	private ResourceStrategy connectionRS;
	private ResourceStrategy clusterDetailsRS;
	
	@Manifest
	public void manifest(ManifestBuilder mb) {
		mb.lifecycle("main")
			.path("init", "provision", "configure", "bootstrap", "verify", "run", "shutdown");
		mb.lifecycle("clean")
			.path("clean");
		mb.resourceDependency()
			.name("cloud")
			.resourceType(ViManager.class)
			.requiredForLifecycle("main")
			.done();
		mb.exportService()
		    .name("connection")
		    .resourceType(String.class)
		    .providedAt("main:running")
		    .resourceStrategy(connectionRS)
		    .done();
		mb.exportService()
		    .name("cluster-details")
		    .resourceType(HBaseClusterDetails.class)
		    .providedDuring("configure", "run")
		    .resourceStratefy(clusterDetailsRS)
		    .done();		
	}
	
	@Phase 
	public void init(ActionContext context) {
		
	}
	
	@Phase 
	public void provision(ActionContext context) {
		
	}
	
	@Phase 
	public void configure(ActionContext contex) {
		
	}
	
	@Phase 
	public void bootstrap(ActionContext context) {
		
	}
	
	@Phase
	public void verify(ActionContext context) {
		
	}
	
	@ResourceInject("cloud")
	public void setNanoCloud(ViManager manager) {
		
	}
	
	@ResourceProvider("connection")
	public String getClusterConnectionString() {
	}

	@ResourceProvider("cluster-details")
	public HBaseClusterDetails getClusterConnectionString() {
	}
	

}
