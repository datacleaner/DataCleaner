package org.eobjects.datacleaner.monitor.jobwizard.movedata
import org.eobjects.analyzer.connection.UpdateableDatastore
import org.eobjects.analyzer.connection.Datastore
import org.eobjects.analyzer.connection.UpdateableDatastoreConnection
import org.apache.metamodel.UpdateableDataContext

/**
 * An implementation of the UpdateableDatastore interface.
 * 
 * It simply decorates a (non-updatable) Datastore and assumes
 * it's DataContext does have update capabilities.
 */
class MockUpdateableDatastore(datastore: Datastore) extends UpdateableDatastore with UpdateableDatastoreConnection {

  override def getUpdateableDataContext: UpdateableDataContext = {
    var con = datastore.openConnection()
    return con.getDataContext().asInstanceOf[UpdateableDataContext]
  }

  override def getDataContext = datastore.openConnection().getDataContext()

  override def getSchemaNavigator = datastore.openConnection().getSchemaNavigator()

  override def getDatastore = this

  override def close = {}

  override def openConnection: UpdateableDatastoreConnection = this

  override def getName = datastore.getName()

  override def getDescription = datastore.getDescription()

  override def getPerformanceCharacteristics = datastore.getPerformanceCharacteristics()

  override def setDescription(description: String) = {
    datastore.setDescription(description)
  }
}