package org.visualvm.remotevm;

import java.awt.Image;

import javax.swing.JLabel;

import org.openide.util.ImageUtilities;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.DataSourceViewProvider;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;

public class RemoteSshHostOverview extends DataSourceView {

	public RemoteSshHostOverview(DataSource dataSource, String name, Image icon, int preferredPosition, boolean isClosable) {
		super(dataSource, name, icon, preferredPosition, isClosable);
	}

	@Override
	protected DataViewComponent createComponent() {
		DataViewComponent.MasterView master = new DataViewComponent.MasterView("Remote SSH Host", "Remote SSH Host", new JLabel("Remote SSHHost"));
		DataViewComponent.MasterViewConfiguration mvc = new DataViewComponent.MasterViewConfiguration(false);
		DataViewComponent dvc = new DataViewComponent(master, mvc);
		return dvc;
	}

	static class Provider extends DataSourceViewProvider<DataSource> {

		private static Image ICON = ImageUtilities.loadImage("com/sun/tools/visualvm/host/resources/localHost.png", true);
		
		@Override
		protected boolean supportsViewFor(DataSource dataSource) {
			if (dataSource instanceof RemoteSshHost) {
				return true;
			}
			else {
				return false;
			}
		}

		@Override
		protected DataSourceView createView(DataSource dataSource) {
			return new RemoteSshHostOverview(dataSource, ((RemoteSshHost)dataSource).getHostname(), ICON, 0, false);
		}
	}
}
