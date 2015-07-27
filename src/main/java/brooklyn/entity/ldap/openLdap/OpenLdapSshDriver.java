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

import java.io.ByteArrayOutputStream;
import java.util.*;

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
        //TODO: put code here to possibly copy a database ?
        //TODO: add code for other package managers
    }

    @Override
    public void customize() {
        //TODO: Client must be able to provide own DBs to load in non-clustered/first node...
    }

    @Override
    public void launch() {
         newScript(MutableMap.of(USE_PID_FILE, false), LAUNCHING).body.append(sudo("service slapd start")).failOnNonZeroResultCode().execute();
        //TODO put the command to start the service in different linux distributions
    }


    public String getStatusCmd() {
        //TODO: need to check for different OSs
        return "service slapd status";
    }


    @Override
    public ProcessTaskWrapper<Integer> executeScriptAsync(String commands) {
        return DynamicTasks.queue(SshEffectorTasks.ssh(commands));
    }

    @Override
    public void ExecuteLDIF(String command, String ldif) {
        //todo: change the command to an enum (ldapadd, ldapmodify etc) or an if statement

        //Create LDIF file to be executed
        Random rand = new Random();
        String fileName = rand.nextInt(500) + ".ldif";
        log.trace("Going to write file with filename " +fileName + "\n" );

        this.getMachine().execCommands(null, ImmutableList.of("cat  > /tmp/ldap/"  + fileName + "<<EOF\n"
                                                                                        + ldif + "\n" + "EOF"));
        ByteArrayOutputStream sshOutputStream = new ByteArrayOutputStream();
        HashMap<String, Object> streams = new HashMap<String,Object>();
        streams.put(SshMachineLocation.STDOUT.getName(), sshOutputStream);
        streams.put(SshMachineLocation.STDERR.getName(), sshOutputStream);
        //execute ldap
        this.getMachine().execCommands(streams,"ExecuteLDIF with command "+ command , ImmutableList.of(command + " -f" + "/tmp/ldap/" + fileName));
        log.debug("Output After Executing LDIF FILE : " +fileName + "\n" + new String(sshOutputStream.toByteArray()));

        //remove file
        this.getMachine().execCommands(null, ImmutableList.of("rm -f /tmp/ldap/" + fileName ));
    }

    @Override
    public void ExecuteLdifFromFile(String command, String filePath) {
        //todo: change the command to an enum (ldapadd, ldapmodify etc) or an if statement
        //todo: use the -f flag.
        ByteArrayOutputStream sshOutputStream = new ByteArrayOutputStream();
        HashMap<String, Object> streams = new HashMap<String,Object>();
        streams.put(SshMachineLocation.STDOUT.getName(), sshOutputStream);
        streams.put(SshMachineLocation.STDERR.getName(), sshOutputStream);
        this.getMachine().execCommands(streams, "ExecuteLdifFromFile with command "+ command + " and filepath " + filePath, ImmutableList.of(command + " " + filePath));
        log.debug("OUTPUT AFTER EXECUTING " + command + " and " + " filepath: " + filePath + "\n" + new String(sshOutputStream.toByteArray()) );
    }

    public String ExecuteCommand(String command){
        ByteArrayOutputStream sshOutputStream = new ByteArrayOutputStream();
        this.getMachine().execCommands(ImmutableMap.<String, Object>of(SshMachineLocation.STDOUT.getName(), sshOutputStream), "Generating password with Slappasswd", ImmutableList.of(command));
        String result = new String(sshOutputStream.toByteArray());
        return result;
    }



    @Override
    public OpenLdapNodeImpl getEntity() {
        return (OpenLdapNodeImpl) super.getEntity();
    }



}
