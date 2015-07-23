package brooklyn.entity.ldap.openLdap;

import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.SoftwareProcessImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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
        ldapModifyFromString(LdifHelper.generateAddOlcRootDNLdif());
        ldapAddFromString(LdifHelper.generateLoadSyncProvLdif());
        ldapAddFromString(LdifHelper.generateOlcOverlayForSyncprovLdif());
        ldapModifyFromString(LdifHelper.generateReplicationModification(replicationProperties));
        this.getDriver().ExecuteCommand( LdifHelper.generateLdapClientBind(replicationProperties.getProviders()));
    }

    @Override
    public Boolean commitCluster(Map<String, Integer> currentNodes) {
        log.info("Committing node with host: " + this.getHost());
        Map<String, Integer> applicableNodes = new HashMap<String, Integer>();
        //Remove current node from list
        //FIXME: use cleaner code
        for(Map.Entry<String, Integer> node : currentNodes.entrySet()){
            if (!node.getKey().contains(this.getHost())){
                applicableNodes.put(node.getKey(), node.getValue());
            }
        }
        ReplicationProperties replicationProperties = new ReplicationProperties(applicableNodes,"simple", "cn=Manager,dc=server,dc=world", "password", "dc=server,dc=world", "sub", "on", "refreshAndPersist", "30 5 300 3", "00:00:05:00",  this.getOlcServerId());
        addMasterProviders(replicationProperties);
        return true;
    }

    private Integer olcServerId;

    @Override
    public Integer getOlcServerId() {
        return olcServerId;
    }

    @Override
    public void setOlcServerId(Integer olcServerId) {
        this.olcServerId = olcServerId;
    }

    @Override
    public boolean loadLdifFromFile(String filePath) {
        getDriver().ExecuteLdifFromFile("ldapmodify -Y EXTERNAL -H ldapi:/// -f", filePath);
        return true;
    }

    @Override
    public boolean ldapModifyFromString(String ldif) {
        getDriver().ExecuteLDIF("ldapmodify -Y EXTERNAL -H ldapi:///", ldif);
        return true;
    }


    private boolean ldapAddFromString(String ldif) {
        getDriver().ExecuteLDIF("ldapadd -Y EXTERNAL -H ldapi:///", ldif);
        return true;
    }
}
