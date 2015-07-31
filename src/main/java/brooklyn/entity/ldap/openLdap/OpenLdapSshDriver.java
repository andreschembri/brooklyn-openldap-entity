package brooklyn.entity.ldap.openLdap;

import brooklyn.entity.basic.AbstractSoftwareProcessSshDriver;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.lifecycle.ScriptHelper;
import brooklyn.entity.software.SshEffectorTasks;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.collections.MutableMap;
import brooklyn.util.task.DynamicTasks;
import brooklyn.util.task.ssh.SshTasks;
import brooklyn.util.task.system.ProcessTaskWrapper;
import brooklyn.util.text.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.*;

import static brooklyn.util.ssh.BashCommands.installPackage;
import static brooklyn.util.ssh.BashCommands.sudo;

public class OpenLdapSshDriver extends AbstractSoftwareProcessSshDriver implements OpenLdapDriver {
    final String LDAP_ADD_COMMAND =  "ldapadd -Y EXTERNAL -H ldapi:///";
    final String LDAP_MODIFY_COMMAND ="ldapmodify -Y EXTERNAL -H ldapi:///";

    public static final Logger log = LoggerFactory.getLogger(OpenLdapSshDriver.class);

    public OpenLdapSshDriver(OpenLdapNodeImpl entity, SshMachineLocation machine) {
        super(entity, machine);
    }

    private String generateOpenLdapAddress(){
        return "ldap://" + entity.getAttribute(Attributes.HOSTNAME) + ":" + entity.getAttribute(OpenLdapNode.OPENLDAP_PORT);
    }
    @Override
    public void postLaunch(){
        entity.setAttribute(OpenLdapNode.OPENLDAP_ADDRESS, generateOpenLdapAddress());
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
        //FIXME: stop the node
        newScript(MutableMap.of(USE_PID_FILE, false), STOPPING).execute();
    }

    @Override
    public void install() {
        List<String> commands = new LinkedList<String>();
        commands.add(installPackage(ImmutableMap.of("yum", "openldap-servers openldap-clients openldap-clients nss-pam-ldapd"), null));
        newScript(INSTALLING).body.append(commands).failOnNonZeroResultCode().execute();
        //TODO: put code here to possibly copy a database ?
        //TODO: add code for other package managers
    }

    @Override
    public void customize() {
        //TODO: Client must be able to provide own DBs to load in non-clustered/first node...

        //change port configuration
    }


    private void changePortIfNeeded(){
       if(getEntity().getAttribute(OpenLdapNode.OPENLDAP_PORT) == 389) {
           if (getMachine().getOsDetails().getName().contains("CentOS") || getMachine().getOsDetails().getName().contains("Red Hat")) {
               String semanageCommand = String.format("semanage port -a -t ldap_port_t -p tcp %s", getEntity().getAttribute(OpenLdapNode.OPENLDAP_PORT));
               DynamicTasks.queue(SshTasks.newSshExecTaskFactory(getMachine(), semanageCommand).requiringExitCodeZero()).asTask().getUnchecked();
           }
           String changePortCommand = ConfigurationGenerator.generateChangePortCommand(this.getEntity().getAttribute(OpenLdapNode.OPENLDAP_PORT));
           DynamicTasks.queue(SshTasks.newSshExecTaskFactory(getMachine(), changePortCommand).requiringExitCodeZero()).asTask().getUnchecked();
       }
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
    public void ExecuteLdif(String command, String ldif) {
        //TODO must be changed to return true/false or exception when non-zero is returned

        //Create LDIF file to be executed
        Random rand = new Random();
        String fileName = Strings.makeRandomId(5) + ".ldif";
        log.trace("Going to write file with filename " + fileName + "\n");
        this.copyResource(ldif, fileName);


        String executeLdifCommand = String.format("%s -f %s%s", command, this.getRunDir(), fileName);
        DynamicTasks.queue(SshTasks.newSshExecTaskFactory(getMachine(), executeLdifCommand).requiringExitCodeZero()).asTask().getUnchecked();
        //execute ldap
        this.getMachine().execCommands("ExecuteLDIF with command " + command, ImmutableList.of(command + " -f" + this.getRunDir() + fileName));

        //remove file
        this.getMachine().execCommands("Removing file", ImmutableList.of("rm -f " + this.getRunDir() + fileName ));
    }

    public String ExecuteSlapPasswd(String command){
      return  DynamicTasks.queue(SshTasks.newSshExecTaskFactory(getMachine(), command)
                .requiringZeroAndReturningStdout()).asTask().getUnchecked();
    }

    public void ldifAddFromString(String ldif) {
        ExecuteLdif(LDAP_ADD_COMMAND, ldif);
    }

    public void ldifModifyFromString(String ldif){
        ExecuteLdif(LDAP_MODIFY_COMMAND, ldif);
    }

    @Override
    public OpenLdapNodeImpl getEntity() {
        return (OpenLdapNodeImpl) super.getEntity();
    }



}
