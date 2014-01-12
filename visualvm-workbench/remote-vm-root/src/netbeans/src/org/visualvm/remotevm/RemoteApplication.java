package org.visualvm.remotevm;

import java.awt.Image;

import org.openide.util.ImageUtilities;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import com.sun.tools.visualvm.core.model.AbstractModelProvider;

public class RemoteApplication extends Application {

	private String pid;
	
	public RemoteApplication(RemoteSshHost host, String id) {
		super(host.getShadowHost(), "ssh://" + host.getHostname() + "/pid/" + id);
		this.pid = id;
	}

	@Override
	public int getPid() {
		try {
			return Integer.valueOf(pid);
		}
		catch(NumberFormatException e) {
			return 0;
		}
	}
	
	@Override
	public boolean supportsUserRemove() {
		return false;
	}

	@Override
	public boolean checkRemove(DataSource removeRoot) {
		return true;
	}

	@Override
	public String toString() {
		return getId();
	}

	static class Descriptor extends DataSourceDescriptor<RemoteApplication> {

	    private static final Image NODE_ICON = ImageUtilities.loadImage(
	            "com/sun/tools/visualvm/application/resources/application.png", true);   // NOI18N

		
		public Descriptor(RemoteApplication ds) {
			super(ds, ds.toString(), ds.toString(), NODE_ICON, -1, EXPAND_NEVER);
		}
	}
	
	@SuppressWarnings("rawtypes")
	static class DescriptorProvider extends AbstractModelProvider<DataSourceDescriptor, DataSource> {

		@Override
		public DataSourceDescriptor createModelFor(DataSource source) {
			if (source instanceof RemoteApplication) {
				return new Descriptor((RemoteApplication) source);
			}
			else {
				return null;
			}
		}
	}
}
