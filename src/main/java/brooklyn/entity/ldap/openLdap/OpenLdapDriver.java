package brooklyn.entity.ldap.openLdap;

import brooklyn.entity.basic.SoftwareProcessDriver;
import brooklyn.util.task.system.ProcessTaskWrapper;

public interface OpenLdapDriver extends SoftwareProcessDriver {
    public String getStatusCmd();
    public ProcessTaskWrapper<Integer> executeScriptAsync(String commands);
    public void ExecuteLDIF(String Command, String ldif);
    public void ExecuteLdifFromFile(String Command, String filePath);
}
