package brooklyn.entity.ldap.openLdap;

import java.util.List;

public class LdifHelper {

    public static String generateAddOlcRootDNLdif(){
        return "dn: olcDatabase={0}config,cn=config\n" +
                "add: olcRootDN\n" +
                "olcRootDN: cn=admin,cn=config";
    }

    public static String generateLoadSyncProvLdif(){
        return "dn: cn=module,cn=config\n" +
                "objectClass: olcModuleList\n" +
                "cn: module\n" +
                "olcModulePath: /usr/lib64/openldap\n" +
                "olcModuleLoad: syncprov.la";
    }

    public static String generateOlcOverlayForSyncprovLdif(){
        return "dn: olcOverlay=syncprov,olcDatabase={2}hdb,cn=config\n" +
                "objectClass: olcOverlayConfig\n" +
                "objectClass: olcSyncProvConfig\n" +
                "olcOverlay: syncprov\n" +
                "olcSpSessionLog: 100";
    }


    public static String generateReplicationModification(List<String> providers){
        Integer currentServerID = 0;
        String result = "dn: cn=config\n" +
                "changetype: modify\n" +
                "replace: olcServerID\n" +
                "\n" +
                "# specify uniq ID number on each server\n" +
                "\n" +
                "olcServerID: "+ currentServerID.toString() + "\n" +
                "\n" +
                "dn: olcDatabase={2}hdb,cn=config\n" +
                "changetype: modify\n" +
                "add: olcSyncRepl\n" ;

                for(String provider : providers){
                    result += generateProviderPart(provider);
                }

                result +=
                "add: olcMirrorMode\n" +
                "olcMirrorMode: TRUE\n" +
                "\n" +
                "dn: olcOverlay=syncprov,olcDatabase={2}hdb,cn=config\n" +
                "changetype: add\n" +
                "objectClass: olcOverlayConfig\n" +
                "objectClass: olcSyncProvConfig\n" +
                "olcOverlay: syncprov";

        return result;
    }

    private static String generateProviderPart(String provider){
        return "olcSyncRepl: rid=001\n" +
                "\n" +
                " \n" +
                "# specify another LDAP server's URI\n" +
                "\n" +
                "  provider=ldap://"+ provider +":389/\n" +
                "  bindmethod=simple\n" +
                "  \n" +
                "\n" +
                " \n" +
                "# own domain name\n" +
                "\n" +
                "  binddn=\"cn=Manager,dc=server,dc=world\"\n" +
                "\n" +
                " \n" +
                "# directory manager's password\n" +
                "\n" +
                "  credentials=password\n" +
                "  searchbase=\"dc=server,dc=world\"\n" +
                "\n" +
                " \n" +
                "# includes subtree\n" +
                "\n" +
                "  scope=sub\n" +
                "  schemachecking=on\n" +
                "  type=refreshAndPersist\n" +
                "\n" +
                " \n" +
                "# [retry interval] [retry times] [interval of re-retry] [re-retry times]\n" +
                "\n" +
                "  retry=\"30 5 300 3\"\n" +
                "\n" +
                " \n" +
                "# replication interval\n" +
                "\n" +
                "  interval=00:00:05:00\n" +
                "-\n";
    }

    public static String generateLdapClientBind(List<String> providers){
        String command = "authconfig --ldapserver=";
        for(String provider : providers){
            command += provider + ",";
        }
        command += "--update";
        return command;
    }




}
