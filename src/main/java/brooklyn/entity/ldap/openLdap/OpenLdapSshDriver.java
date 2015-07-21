package brooklyn.entity.ldap.openLdap;

import brooklyn.entity.basic.AbstractSoftwareProcessSshDriver;
import brooklyn.entity.basic.lifecycle.ScriptHelper;
import brooklyn.entity.software.SshEffectorTasks;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.collections.MutableMap;
import brooklyn.util.task.DynamicTasks;
import brooklyn.util.task.system.ProcessTaskWrapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static brooklyn.util.ssh.BashCommands.installPackage;
import static brooklyn.util.ssh.BashCommands.sudo;

public class OpenLdapSshDriver extends AbstractSoftwareProcessSshDriver implements OpenLdapDriver {

    public static final Logger log = LoggerFactory.getLogger(OpenLdapSshDriver.class);

    public OpenLdapSshDriver(OpenLdapNodeImpl entity, SshMachineLocation machine) {
        super(entity, machine);
    }

    @Override
    public boolean isRunning() {

        ScriptHelper checkRunningScript = newScript(CHECK_RUNNING)
                .body.append(sudo(getStatusCmd()));
        int returnScriptValue = checkRunningScript.execute();
        log.error("CheckRunningScript is returning " +  returnScriptValue);
        return (returnScriptValue == 0);
    }

    @Override
    public void stop() {
        newScript(MutableMap.of(USE_PID_FILE, false), STOPPING).execute();
    }

    @Override
    public void install() {
        List<String> commands = new LinkedList<String>();
        commands.add(installPackage(ImmutableMap.of("yum", "openldap-servers openldap-clients openldap-clients nss-pam-ldapd"), null));
        newScript(INSTALLING).body.append(commands).execute();
        //TODO: add code for other package managers
    }

    @Override
    public void customize() {
    }

    @Override
    public void launch() {
         newScript(MutableMap.of(USE_PID_FILE, false), LAUNCHING).body.append(sudo("service slapd start")).failOnNonZeroResultCode().execute();
        //TODO put the command to start the service in different linux distributions
    }


    public String getStatusCmd() {
        //TODO: put commands to check status of process here
        return "service slapd status";
    }


    @Override
    public ProcessTaskWrapper<Integer> executeScriptAsync(String commands) {
        return DynamicTasks.queue(SshEffectorTasks.ssh(commands));
    }

    @Override
    public void ExecuteLDIF(String command, String ldif) {
        //todo: change the command to an enum or an if statement
        this.getMachine().execCommands(null, ImmutableList.of(command + " " + ldif));
    }

    @Override
    public void ExecuteLdifFromFile(String command, String filePath) {
        //todo: change the command to an enum or an if statement
        //todo: use the -f flag.
        this.getMachine().execCommands(null, ImmutableList.of(command + " " + filePath));
    }



    @Override
    public OpenLdapNodeImpl getEntity() {
        return (OpenLdapNodeImpl) super.getEntity();
    }

//    public int getPort() {
//        return getEntity().getPort();
//    }


}
