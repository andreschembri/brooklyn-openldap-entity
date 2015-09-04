package brooklyn.entity.ldap.openLdap;

import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.entity.ldap.openLdap.model.ReplicationProperties;
import brooklyn.event.feed.function.FunctionFeed;
import brooklyn.event.feed.function.FunctionPollConfig;
import brooklyn.util.time.Duration;
import com.google.common.base.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class OpenLdapNodeImpl extends SoftwareProcessImpl implements OpenLdapNode {
    public static final Logger log = LoggerFactory.getLogger(OpenLdapNodeImpl.class);

    @Override
    public void init(){
        super.init();
    }

    @Override
    protected void connectSensors() {
        super.connectSensors();
        connectServiceUpIsRunning();
        connectCustomSensors();
    }

    private void connectCustomSensors() {
        FunctionFeed.builder().entity(this).period(Duration.FIVE_SECONDS).poll(((FunctionPollConfig) (new FunctionPollConfig(CONNECTION_COUNTER)).callable(new Callable() {
            public Integer call() {
                return Integer.valueOf(OpenLdapNodeImpl.this.getDriver().getCurrentNumberOfConnections());
            }
        }))).build();
        FunctionFeed.builder().entity(this).period(Duration.FIVE_SECONDS).poll((FunctionPollConfig) (new FunctionPollConfig(READ_WAITERS_COUNTER)).callable(new Callable() {
            @Override
            public Integer call() throws Exception {
                return Integer.valueOf(OpenLdapNodeImpl.this.getDriver().getCurrentNumberOfWaiters());
            }
        }));
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
