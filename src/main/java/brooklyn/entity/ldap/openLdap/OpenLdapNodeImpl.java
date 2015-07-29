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
    }

    private String generateOpenLdapAddress(){
        return "ldap://" + this.getAttribute(Attributes.HOSTNAME) + ":" + this.getAttribute(OPENLDAP_PORT);
    }

    @Override
    protected void connectSensors() {
        super.connectSensors();
        connectServiceUpIsRunning();
        setAttribute(OPENLDAP_ADDRESS, generateOpenLdapAddress());
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

    //FIXME: Should go to cluster
    public String generateSlappassword(String password){
       String slappasswd = this.getDriver().ExecuteSlapPasswd(ConfigurationGenerator.generateSlappasswd(password));
        log.error("SLAPPASSWD ::: " + slappasswd);
        return slappasswd;
    }

    @Override
    public void ldapModifyFromString(String ldif) {
        getDriver().ldifModifyFromString(ldif);
        //Fixme: this should return true/false depending on the result code
    }

    @Override
    public void ldapAddFromString(String ldif) {
        getDriver().ldifAddFromString(ldif);
        //Fixme: this should return true/false depending on the result code
    }
}
