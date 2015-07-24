package brooklyn.entity.ldap.openLdap;


import java.util.Map;

public class LdifHelper {



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
        return "slappaswd -p" + password;
    }

    public static String generateSetPassword(String slappaswdGenertedString) {
        return "dn: cn=config\n" +
                "changeType: modify\n" +
                " \n" +
                "dn: olcDatabase={0}config,cn=config\n" +
                "add: olcRootPW\n" +
                "olcRootPW: " + slappaswdGenertedString;
    }

    public static String generateOlcRootDN() {
        return "dn: olcDatabase={0}config,cn=config\n" +
                "add: olcRootDN\n" +
                "olcRootDN: cn=admin,cn=config";
    }

    public static String generateAddConfigurationReplication(Map<String, Integer> urlsAndIds) {
        //TODO: change this map to pojo
        StringBuilder ldif = new StringBuilder();
        ldif.append("dn: cn=config\n" +
                "changetype: modify\n" +
                "replace: olcServerID\n");
        for (Map.Entry<String, Integer> urlAndId : urlsAndIds.entrySet()) {
            ldif.append(String.format("olcServerID: %d %s %n", urlAndId.getValue(), urlAndId.getKey()));
        }
        return ldif.toString();
    }

    public static String generateAddSyncProv() {
        return "dn: olcOverlay=syncprov,olcDatabase={0}config,cn=config\n" +
                "changetype: add\n" +
                "objectClass: olcOverlayConfig\n" +
                "objectClass: olcSyncProvConfig\n" +
                "olcOverlay: syncprov";
    }

    public static String generateAddSyncRepl(ReplicationProperties replicationProperties) {
        StringBuilder ldif = new StringBuilder();
        ldif.append("dn: olcDatabase={0}config,cn=config\n" +
                "changetype: modify\n" +
                "add: olcSyncRepl\n");
        Map<String, Integer> urlsAndIds = replicationProperties.getProviders();
        for (Map.Entry<String, Integer> urlAndId : urlsAndIds.entrySet()) {
            //TODO: add timeout property
            ldif.append(String.format("olcSyncRepl: rid=%d provider=%s binddn=\"%s\" bindmethod=%s credentials=%s searchbase=\"%s\" type=%s retry=\"%s\" %n",
                    urlAndId.getValue(), urlAndId.getKey(), replicationProperties.getBinddn(), replicationProperties.getBindMethod(),
                    replicationProperties.getCredentials(), replicationProperties.getSearchBase(), replicationProperties.getType(), replicationProperties.getRetry()));
        }
        ldif.append("-\n" +
                "add: olcMirrorMode\n" +
                "olcMirrorMode: TRUE");
        return ldif.toString();
    }


}
