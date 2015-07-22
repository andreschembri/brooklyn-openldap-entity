package brooklyn.entity.ldap.openLdap;

import java.util.List;

public class ReplicationProperties {
    private final List<String> providers;
    private final String bindMethod;
    private final String binddn;
    private final String credentials;
    private final String searchBase;
    private final String scope;
    private final String schemaChecking;
    private final String type;
    private final String retry;
    private final String interval;

    public ReplicationProperties(List<String> providers, String bindMethod, String binddn, String credentials, String searchBase, String scope, String schemaChecking, String type, String retry, String interval) {
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
    }

    public List<String> getProviders() {
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
