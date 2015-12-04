package org.datacleaner.spark;

import java.io.Serializable;

public interface SparkJobLifeCycleListener extends Serializable{
    void onNodeStart();
    void onNodeEnd();
    void onJobStart();
    void onJobEnd();
}
