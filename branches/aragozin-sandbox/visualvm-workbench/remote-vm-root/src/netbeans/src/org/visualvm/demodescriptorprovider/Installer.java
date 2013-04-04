package org.visualvm.demodescriptorprovider;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import com.sun.tools.visualvm.core.ui.DataSourceViewsManager;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    DemoDataSource ds = DemoDataSource.sharedInstance();
    private static DemoDataSourceViewProvider INSTANCE = new DemoDataSourceViewProvider();

    @Override
    public void restored() {
        DataSource.ROOT.getRepository().addDataSource(ds);
        DataSourceViewsManager.sharedInstance().addViewProvider(INSTANCE, DemoDataSource.class);
        DataSourceDescriptorFactory.getDefault().registerProvider(new DemoDataSourceDescriptorProvider());
    }

    @Override
    public void uninstalled() {
        DataSource.ROOT.getRepository().removeDataSource(ds);
        DataSourceViewsManager.sharedInstance().removeViewProvider(INSTANCE);
        DataSourceDescriptorFactory.getDefault().unregisterProvider(new DemoDataSourceDescriptorProvider());
    }
    
}
