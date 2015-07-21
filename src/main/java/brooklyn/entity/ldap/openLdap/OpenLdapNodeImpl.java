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
        log.error("Getting Status by invoking " + checkStatusCommand);

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


    @Override
    public void addMasterProviders(String provider, String bindMethod, String binddn, String credentials, String searchBase, String scope, String schemaChecking, String type, String retry, String interval, List<String> providers) {
        loadLdifFromString(LdifHelper.generateAddOlcRootDNLdif());
        loadLdifFromString(LdifHelper.generateLoadSyncProvLdif());
        loadLdifFromString(LdifHelper.generateReplicationModification(providers));
        loadLdifFromString(LdifHelper.generateLdapClientBind(providers));
    }

//    @Override
//    public void commitCluster() {
//        log.error("well this worked");
//    }

    @Override
    public Boolean commitCluster(List<String> currentNodes) {
        log.error("YEY YEY");
        log.error("Current node is " + this.getHost());
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
