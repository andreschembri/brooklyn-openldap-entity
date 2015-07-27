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
public void init(){
   super.init();
    setAttribute(OPENLDAP_NODE_HAS_JOINED_CLUSTER, false);
}

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
        if (getAttribute(OPENLDAP_NODE_HAS_JOINED_CLUSTER)){
            ldapModifyFromString(LdifHelper.generateModifySyncReplication(replicationProperties));
        }

        else if(!getAttribute(OPENLDAP_NODE_HAS_JOINED_CLUSTER)){
        this.ldapAddFromString(LdifHelper.generateAddSyncProvToModuleList());
        ldapModifyFromString(LdifHelper.generateLoadSyncProv());
        ldapModifyFromString(LdifHelper.generateSetOlcServerId(this.getOlcServerId()));
        ldapModifyFromString(LdifHelper.generateSetPassword(this.generateSlappassword(replicationProperties.getCredentials())));
        ldapModifyFromString(LdifHelper.generateAddSyncProvider());
        ldapModifyFromString(LdifHelper.generateModifySyncReplication(replicationProperties));
        }
    }

    private String generateSlappassword(String password){
       String slappasswd = this.getDriver().ExecuteCommand(LdifHelper.generateSlappasswd(password));
        log.error("SLAPPASSWD ::: " + slappasswd);
        return slappasswd;
    }

    @Override
    public Boolean commitCluster(Map<String, Integer> currentNodes) {
        //FIXME : Ideally this should replicate CONFIG by default, need to default it to something and make it configurable
        ReplicationProperties replicationProperties = new ReplicationProperties(currentNodes,"simple", "cn=admin,cn=config", "password", "cn=config", "sub", "on", "refreshAndPersist", "5 5 300 5", "00:00:05:00",  this.getOlcServerId());
        addMasterProviders(replicationProperties);
        this.setAttribute(OPENLDAP_NODE_HAS_JOINED_CLUSTER, true);
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
