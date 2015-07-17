package brooklyn.entity.ldap.openLdap;

import brooklyn.entity.Entity;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.event.feed.ssh.SshFeed;
import brooklyn.event.feed.ssh.SshPollConfig;
import brooklyn.location.Location;
import brooklyn.location.basic.Locations;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.guava.Maybe;
import brooklyn.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class OpenLdapNodeImpl extends SoftwareProcessImpl implements OpenLdapNode {



    @Override
    protected void connectSensors() {
        super.connectSensors();

        String checkStatusCommand = getDriver().getStatusCmd();
        Maybe<SshMachineLocation> machine = Locations.findUniqueSshMachineLocation(getLocations());
        SshFeed.builder().entity(this).period(Duration.FIVE_SECONDS).machine(machine.get()).poll(
                new SshPollConfig<Boolean>(SERVICE_PROCESS_IS_RUNNING).command(checkStatusCommand).setOnSuccess(true).setOnFailure(false)
        ).build();

    }
    public void init(){

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
    public int getPort() {
        return this.OPENLDAP_PORT.getAsSensorValue(this);
    }

    @Override
    public String getHost(){
        return this.getAttribute(Attributes.HOSTNAME);
    }


    @Override
    public void addMasterProviders(String provider, String bindMethod, String binddn, String credentials, String searchBase, String scope, String schemaChecking, String type, String retry, String interval, List<String> providers) {
        loadLdifFromString(LdifHelper.generateAddOlcRootDNLdif());
        loadLdifFromString(LdifHelper.generateLoadSyncProvLdif());
        loadLdifFromString(LdifHelper.generateReplicationModification(providers));
        loadLdifFromString(LdifHelper.generateLdapClientBind(providers));
    }

    @Override
    public boolean loadLdifFromFile(String filePath) {
        getDriver().ExecuteLdifFromFile("ldapmodify -Y EXTERNAL -H ldapi:/// -f", filePath);
        return true;
    }
    @Override
    public void commitCluster() {

    }

    @Override
    public boolean loadLdifFromString(String ldif) {
        getDriver().ExecuteLDIF("ldapmodify -Y EXTERNAL -H ldapi:///", ldif);
        return true;
    }
}
