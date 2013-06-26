package com.orange.common.cassandra;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-6-26
 * Time: 上午11:02
 * To change this template use File | Settings | File Templates.
 */
public class NewCassendraClient {
    private static NewCassendraClient ourInstance = new NewCassendraClient();

    public static NewCassendraClient getInstance() {
        return ourInstance;
    }

    private NewCassendraClient() {
    }
}
