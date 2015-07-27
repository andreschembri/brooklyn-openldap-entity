package brooklyn.entity.ldap.openLdap;


import brooklyn.entity.ldap.openLdap.model.ReplicationProperties;


public class ConfigurationGenerator {

public static String generateAddSyncProvToModuleList(){
    return "dn: cn=module,cn=config\n" +
            "objectClass: olcModuleList\n" +
            "cn: module\n" +
            "olcModulePath: /usr/lib64/openldap\n" +
            "olcModuleLoad: syncprov.la";
}

    public static String generateLoadSyncProv() {
        return "dn: cn=module{0},cn=config\n" +
                "changetype: modify\n" +
                "add: olcModuleLoad\n" +
                "olcModuleLoad: syncprov";
    }

    public static String generateSetOlcServerId(Integer curServerId) {
        return "dn: cn=config\n" +
                "changeType: modify\n" +
                "add: olcServerID\n" +
                "olcServerID: " + curServerId;
    }

    public static String generateSlappasswd(String password) {
        return "slappasswd -s" + password;
    }

    public static String generateSetPassword(String slappaswdGenertedString) {
        return  "dn: olcDatabase={0}config,cn=config\n" +
                "add: olcRootPW\n" +
                "olcRootPW: " + slappaswdGenertedString;
    }

    public static String generateAddSyncProvider() {
        return "dn: olcOverlay=syncprov,olcDatabase={0}config,cn=config\n" +
                "changetype: add\n" +
                "objectClass: olcOverlayConfig\n" +
                "objectClass: olcSyncProvConfig\n" +
                "olcOverlay: syncprov";
    }

    public static String generateAddSyncReplication(ReplicationProperties replicationProperties) {
        StringBuilder ldif = new StringBuilder();
        ldif.append("dn: olcDatabase={0}config,cn=config\n" +
                "changetype: modify\n" +
                "add: olcSyncRepl\n");
        Iterable<OpenLdapNode> providers = replicationProperties.getProviders();
        for (OpenLdapNode provider : providers) {
            //TODO: add timeout property
            ldif.append(String.format("olcSyncRepl: rid=%d provider=%s binddn=\"%s\" bindmethod=%s credentials=%s searchbase=\"%s\" type=%s retry=\"%s\" %n",
                    provider.getAttribute(provider.OLCSERVERID), provider.getAttribute(provider.ADDRESS), replicationProperties.getBinddn(), replicationProperties.getBindMethod(),
                    replicationProperties.getCredentials(), replicationProperties.getSearchBase(), replicationProperties.getType(), replicationProperties.getRetry()));
        }
        ldif.append("-\n" +
                "add: olcMirrorMode\n" +
                "olcMirrorMode: TRUE");
        return ldif.toString();
    }

    public static String generateModifySyncReplication(ReplicationProperties replicationProperties) {
        StringBuilder ldif = new StringBuilder();
        ldif.append("dn: olcDatabase={0}config,cn=config\n" +
                "changetype: modify\n" +
                "replace: olcSyncRepl\n");
        Iterable<OpenLdapNode> providers = replicationProperties.getProviders();
        for (OpenLdapNode provider : providers) {
            //TODO: add timeout property
            ldif.append(String.format("olcSyncRepl: rid=%d provider=%s binddn=\"%s\" bindmethod=%s credentials=%s searchbase=\"%s\" type=%s retry=\"%s\" %n",
                    provider.getAttribute(provider.OLCSERVERID), provider.getAttribute(provider.ADDRESS), replicationProperties.getBinddn(), replicationProperties.getBindMethod(),
                    replicationProperties.getCredentials(), replicationProperties.getSearchBase(), replicationProperties.getType(), replicationProperties.getRetry()));
        }
        ldif.append("-\n" +
                "replace: olcMirrorMode\n" +
                "olcMirrorMode: TRUE");
        return ldif.toString();
    }


}
