package brooklyn.entity.ldap.openLdap.model;

import brooklyn.entity.ldap.openLdap.OpenLdapNode;



public class ReplicationProperties {
    private final Iterable<OpenLdapNode> providers;
    private final String bindMethod;
    private final String binddn;
    private final String credentials;
    private final String searchBase;
    private final String scope;
    private final String schemaChecking;
    private final String type;
    private final String retry;
    private final String interval;

    private final Integer currentServerId;
    public ReplicationProperties(Iterable<OpenLdapNode> providers, String bindMethod, String binddn, String credentials, String searchBase, String scope, String schemaChecking, String type, String retry, String interval, Integer currentServerId) {
        this.providers = providers;
        this.bindMethod = bindMethod;
        this.binddn = binddn;
        this.credentials = credentials;
        this.searchBase = searchBase;
        this.scope = scope;
        this.schemaChecking = schemaChecking;
        this.type = type;
        this.retry = retry;
        this.interval = interval;
        this.currentServerId = currentServerId;
    }

    public Integer getCurrentServerId() {
        return currentServerId;
    }

    public Iterable<OpenLdapNode> getProviders() {
        return providers;
    }

    public String getBindMethod() {
        return bindMethod;
    }

    public String getBinddn() {
        return binddn;
    }

    public String getCredentials() {
        return credentials;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public String getScope() {
        return scope;
    }

    public String getSchemaChecking() {
        return schemaChecking;
    }

    public String getType() {
        return type;
    }

    public String getRetry() {
        return retry;
    }

    public String getInterval() {
        return interval;
    }
}
