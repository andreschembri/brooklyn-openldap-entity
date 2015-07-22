package brooklyn.entity.ldap.openLdap;

import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.SoftwareProcessImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OpenLdapNodeImpl extends SoftwareProcessImpl implements OpenLdapNode {
    public static final Logger log = LoggerFactory.getLogger(OpenLdapNodeImpl.class);



    @Override
    protected void connectSensors() {
        super.connectSensors();
        connectServiceUpIsRunning();
        String checkStatusCommand = getDriver().getStatusCmd();
    }

    @Override
    public Class<?> getDriverInterface() {
        return OpenLdapDriver.class;
    }

    @Override
    public OpenLdapDriver getDriver() {
        return (OpenLdapDriver) super.getDriver();
    }


    @Override
    public void disconnectSensors(){
       super.disconnectSensors();
        disconnectServiceUpIsRunning();

    }

    @Override
    public String getHost(){
        return this.getAttribute(Attributes.HOSTNAME);
    }

    @Override
    public int getPort() {
        return this.getAttribute(OPENLDAP_PORT);
    }

    private void addMasterProviders(ReplicationProperties replicationProperties) {
        loadLdifFromString(LdifHelper.generateAddOlcRootDNLdif());
        loadLdifFromString(LdifHelper.generateLoadSyncProvLdif());
        loadLdifFromString(LdifHelper.generateReplicationModification(replicationProperties.getProviders()));
        loadLdifFromString(LdifHelper.generateLdapClientBind(replicationProperties.getProviders()));
    }

    @Override
    public Boolean commitCluster(List<String> currentNodes) {
        log.info("Committing node with host: " + this.getHost());
        ReplicationProperties replicationProperties = new ReplicationProperties(currentNodes,"simple", "cn=Manager,dc=server,dc=world", "password", "dc=server,dc=world", "sub", "on", "refreshAndPersist", "30 5 300 3", "00:00:05:00" );
        addMasterProviders(replicationProperties);
        return true;
    }

    @Override
    public boolean loadLdifFromFile(String filePath) {
        getDriver().ExecuteLdifFromFile("ldapmodify -Y EXTERNAL -H ldapi:/// -f", filePath);
        return true;
    }

    @Override
    public boolean loadLdifFromString(String ldif) {
        getDriver().ExecuteLDIF("ldapmodify -Y EXTERNAL -H ldapi:///", ldif);
        return true;
    }
}
