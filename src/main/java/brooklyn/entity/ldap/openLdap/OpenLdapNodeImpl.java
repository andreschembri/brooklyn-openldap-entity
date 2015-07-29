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
        return "ldap://" + this.getAttribute(Attributes.HOSTNAME) + ":" + this.getAttribute(OPENLDAP_PORT);
    }

    @Override
    protected void connectSensors() {
        super.connectSensors();
        connectServiceUpIsRunning();
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
            ldapModifyFromString(ConfigurationGenerator.generateAddSyncReplication(replicationProperties));
        }
    }

    private String generateSlappassword(String password){
       String slappasswd = this.getDriver().ExecuteSlapPasswd(ConfigurationGenerator.generateSlappasswd(password));
        log.error("SLAPPASSWD ::: " + slappasswd);
        return slappasswd;
    }

    @Override
    public Boolean commitCluster(ReplicationProperties replicationProperties) {
        addMasterProviders(replicationProperties);
        this.setAttribute(OPENLDAP_NODE_HAS_JOINED_CLUSTER, true);
        //Fixme: this should return true/false depending on the result code
        return true;
    }

    @Override
    public void loadLdifFromFile(String filePath) {
        getDriver().ExecuteLdifFromFile(getDriver().LDAP_ADD_COMMAND_FROM_FILE, filePath);
        //Fixme: this should return true/false depending on the result code
    }

    @Override
    public void ldapModifyFromString(String ldif) {
        getDriver().ExecuteLDIF(getDriver().LDAP_MODIFY_COMMAND, ldif);
        //Fixme: this should return true/false depending on the result code
    }


    private void ldapAddFromString(String ldif) {
        getDriver().ExecuteLDIF(getDriver().LDAP_ADD_COMMAND, ldif);
        //Fixme: this should return true/false depending on the result code
    }
}
