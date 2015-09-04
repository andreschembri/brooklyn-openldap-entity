package brooklyn.entity.ldap.openLdap;

import brooklyn.entity.basic.SoftwareProcessDriver;

public interface OpenLdapDriver extends SoftwareProcessDriver {
    String getStatusCmd();
    void ExecuteLdif(String Command, String ldif);
    String ExecuteSlapPasswd(String command);
    void ldifAddFromString(String ldif);
    void ldifModifyFromString(String ldif);
    Integer getCurrentNumberOfWaiters();
    Integer getCurrentNumberOfConnections();
}
