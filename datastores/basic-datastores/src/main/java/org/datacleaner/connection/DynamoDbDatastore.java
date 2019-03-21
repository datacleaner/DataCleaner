/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.connection;

import org.apache.metamodel.dynamodb.DynamoDbDataContext;
import org.apache.metamodel.util.SimpleTableDef;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.google.common.base.Strings;

/**
 * Datastore providing access to AWS DynamoDB
 */
public class DynamoDbDatastore extends UsageAwareDatastore<DynamoDbDataContext> implements UpdateableDatastore {

    private static final long serialVersionUID = 1L;

    private final String _region;
    private final String _accessKeyId;
    private final String _secretAccessKey;
    private final SimpleTableDef[] _tableDefs;

    public DynamoDbDatastore(final String name, final String region, final String accessKeyId,
            final String secretAccessKey, final SimpleTableDef[] tableDefs) {
        super(name);
        _region = region;
        _accessKeyId = accessKeyId;
        _secretAccessKey = secretAccessKey;
        _tableDefs = tableDefs;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false, true);
    }

    @Override
    public UpdateableDatastoreConnection openConnection() {
        return (UpdateableDatastoreConnection) super.openConnection();
    }

    @Override
    protected UsageAwareDatastoreConnection<DynamoDbDataContext> createDatastoreConnection() {
        final AmazonDynamoDBClientBuilder clientBuilder = AmazonDynamoDBClientBuilder.standard();
        if (!Strings.isNullOrEmpty(_region)) {
            clientBuilder.setRegion(_region);
        }
        final AWSCredentialsProvider credentialsProvider;
        if (!Strings.isNullOrEmpty(_accessKeyId)) {
            credentialsProvider =
                    new AWSStaticCredentialsProvider(new BasicAWSCredentials(_accessKeyId, _secretAccessKey));
        } else {
            credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
        }
        clientBuilder.setCredentials(credentialsProvider);
        final AmazonDynamoDB client = clientBuilder.build();
        final DynamoDbDataContext dataContext = new DynamoDbDataContext(client, _tableDefs);
        return new UpdateableDatastoreConnectionImpl<>(dataContext, this);
    }

    public String getAccessKeyId() {
        return _accessKeyId;
    }

    public String getSecretAccessKey() {
        return _secretAccessKey;
    }

    public String getRegion() {
        return _region;
    }

    public SimpleTableDef[] getTableDefs() {
        return _tableDefs;
    }

    @Override
    public String toString() {
        return "DynamoDbDatastore[name=" + getName() + "]";
    }

}
