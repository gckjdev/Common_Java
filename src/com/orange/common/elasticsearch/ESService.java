package com.orange.common.elasticsearch;

import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-10-31
 * Time: 上午9:36
 * To change this template use File | Settings | File Templates.
 */
public class ESService {

    public static String CHINESE_ANALYZER = "ik"; // ElasticSearch中文分词插件: ik

    private static ESService ourInstance = new ESService();
    private Logger log = Logger.getLogger(ESService.class.getName());
    private final Client client;

    public static ESService getInstance() {
        return ourInstance;
    }

    private ESService() {
        String address = System.getProperty("es.address");
        if (address == null) {
            address = "127.0.0.1";
        }

        String port = System.getProperty("es.port");
        if (port == null) {
            port = "9300";
        }

        log.info("Start elastic search node client on address "+address+", port "+port);
        client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(address, Integer.parseInt(port)));
    }

    public Client getClient(){
        return client;
    }

}
