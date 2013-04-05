package org.visualvm.remotevm;

import org.openide.modules.ModuleInstall;

import com.sun.tools.visualvm.core.datasource.DataSource;

public class Installer extends ModuleInstall {

    RemoteSshHostsContainer ds = RemoteSshHostsContainer.sharedInstance();
//    private static DemoDataSourceViewProvider INSTANCE = new DemoDataSourceViewProvider();

    @Override
    public void restored() {
//        DataSource.ROOT.getRepository().addDataSource(ds);
        SshHostsSupport.getInstance();
//        DataSourceViewsManager.sharedInstance().addViewProvider(INSTANCE, DemoDataSource.class);
//        DataSourceDescriptorFactory.getDefault().registerProvider(new DemoDataSourceDescriptorProvider());
    }

    @Override
    public void uninstalled() {
        DataSource.ROOT.getRepository().removeDataSource(ds);
//        DataSourceViewsManager.sharedInstance().removeViewProvider(INSTANCE);
//        DataSourceDescriptorFactory.getDefault().unregisterProvider(new DemoDataSourceDescriptorProvider());
    }
    
}
