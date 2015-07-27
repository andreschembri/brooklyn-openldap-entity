package brooklyn.entity.ldap.openLdap;

import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.entity.ldap.openLdap.model.ReplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenLdapNodeImpl extends SoftwareProcessImpl implements OpenLdapNode {
    public static final Logger log = LoggerFactory.getLogger(OpenLdapNodeImpl.class);

    @Override
    public void init(){
        super.init();
        setAttribute(OPENLDAP_NODE_HAS_JOINED_CLUSTER, false);
    }

    private String getAddress(){
        return "ldap://" + this.getHost() + ":" + this.getPort();
    }

    @Override
    protected void connectSensors() {
        super.connectSensors();
        connectServiceUpIsRunning();
        String checkStatusCommand = getDriver().getStatusCmd();
        setAttribute(ADDRESS, getAddress());
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
            ldapModifyFromString(ConfigurationGenerator.generateModifySyncReplication(replicationProperties));
        }

        else if(!getAttribute(OPENLDAP_NODE_HAS_JOINED_CLUSTER)) {
            ldapAddFromString(ConfigurationGenerator.generateAddSyncProvToModuleList());
            ldapModifyFromString(ConfigurationGenerator.generateLoadSyncProv());
            ldapModifyFromString(ConfigurationGenerator.generateSetOlcServerId(this.getAttribute(OLCSERVERID)));
            ldapModifyFromString(ConfigurationGenerator.generateSetPassword(this.generateSlappassword(replicationProperties.getCredentials())));
            ldapModifyFromString(ConfigurationGenerator.generateAddSyncProvider());
            ldapModifyFromString(ConfigurationGenerator.generateModifySyncReplication(replicationProperties));
        }
    }

    private String generateSlappassword(String password){
       String slappasswd = this.getDriver().ExecuteCommand(ConfigurationGenerator.generateSlappasswd(password));
        log.error("SLAPPASSWD ::: " + slappasswd);
        return slappasswd;
    }

    @Override
    public Boolean commitCluster(ReplicationProperties replicationProperties) {
        //FIXME : Ideally this should replicate CONFIG by default, need to default it to something and make it configurable
        addMasterProviders(replicationProperties);
        this.setAttribute(OPENLDAP_NODE_HAS_JOINED_CLUSTER, true);
        return true;
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
