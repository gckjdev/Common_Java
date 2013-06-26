package com.orange.common.cassandra;


//import com.datastax.driver.core.Cluster;
//import com.datastax.driver.core.Host;
//import com.datastax.driver.core.Metadata;
//import com.datastax.driver.core.Session;
//import com.orange.common.utils.PropertyUtil;
//import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-6-26
 * Time: 上午11:02
 * To change this template use File | Settings | File Templates.
 */

@Deprecated
public class NewCassandraClient {

    /*
    static Logger log = Logger.getLogger(NewCassandraClient.class.getName());

    private static NewCassandraClient ourInstance = new NewCassandraClient();

    public static NewCassandraClient getInstance() {
        return ourInstance;
    }

    private NewCassandraClient() {
        String address = PropertyUtil.getStringProperty("cassandra.address", "127.0.0.1");
        String keyspace = PropertyUtil.getStringProperty("cassandra.address", "game");
        connect(address, keyspace);
    }

    private Cluster cluster;
    private Session session;

    public void connect(String node, String keyspace) {
        cluster = Cluster.builder().addContactPoint(node).build();
        Metadata metadata = cluster.getMetadata();
        log.info("<cassandra> Connected to cluster: " + metadata.getClusterName());
        for ( Host host : metadata.getAllHosts() ) {
            log.info(String.format("<cassandra> Datatacenter: %s; Host: %s; Rack: %s", host.getDatacenter(), host.getAddress(), host.getRack()));
        }

        log.info("<cassandra> create session for keyspace "+keyspace);
        session = cluster.connect(keyspace);

    }

    public void shutdown() {
        cluster.shutdown();
    }

                */

}
