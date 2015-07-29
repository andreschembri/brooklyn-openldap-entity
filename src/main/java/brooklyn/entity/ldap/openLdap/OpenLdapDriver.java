package brooklyn.entity.ldap.openLdap;

import brooklyn.entity.basic.SoftwareProcessDriver;
import brooklyn.util.task.system.ProcessTaskWrapper;

public interface OpenLdapDriver extends SoftwareProcessDriver {
    public static final String LDAP_ADD_COMMAND =  "ldapadd -Y EXTERNAL -H ldapi:///";
    public static final String LDAP_MODIFY_COMMAND ="ldapmodify -Y EXTERNAL -H ldapi:///";
    public static final String LDAP_ADD_COMMAND_FROM_FILE =  "ldapadd -Y EXTERNAL -H ldapi:/// -f";
    public static final String LDAP_MODIFY_COMMAND_FROM_FILE ="ldapmodify -Y EXTERNAL -H ldapi:/// -f";

    public String getStatusCmd();
    public void ExecuteLDIF(String Command, String ldif);
    public void ExecuteLdifFromFile(String Command, String filePath);
    public String ExecuteSlapPasswd(String command);
}
