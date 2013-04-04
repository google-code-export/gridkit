package org.visualvm.demodescriptorprovider;

import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.DataSourceViewProvider;

public class DemoDataSourceViewProvider extends DataSourceViewProvider<DemoDataSource> {

    @Override
    protected boolean supportsViewFor(DemoDataSource ds) {
        if (DemoDataSource.sharedInstance().equals(ds)) {
            return true;
        }
        return false;
    }

    @Override
    protected DataSourceView createView(DemoDataSource ds) {
        return new DemoDataSourceView(ds);
    }

}
