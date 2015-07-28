package brooklyn.entity.ldap.openLdap;

import brooklyn.config.ConfigKey;
import brooklyn.entity.Entity;
import brooklyn.entity.annotation.Effector;
import brooklyn.entity.annotation.EffectorParam;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.EntityLocal;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.ldap.openLdap.model.ReplicationProperties;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.basic.BasicAttributeSensorAndConfigKey;
import brooklyn.event.basic.PortAttributeSensorAndConfigKey;
import brooklyn.location.basic.PortRanges;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(OpenLdapNodeImpl.class)
public interface OpenLdapNode extends SoftwareProcess, Entity, EntityLocal {
    @SetFromFlag("version")
    public static final ConfigKey<String> SUGGESTED_VERSION = ConfigKeys.newConfigKeyWithDefault(SoftwareProcess.SUGGESTED_VERSION, "");

    //FIXME: put download urls properly here
    @SetFromFlag("downloadUrl")
    public static final BasicAttributeSensorAndConfigKey<String> DOWNLOAD_URL = new BasicAttributeSensorAndConfigKey.StringAttributeSensorAndConfigKey(Attributes.DOWNLOAD_URL, "");

    //FIXME: put port properly here
    @SetFromFlag
    public static final PortAttributeSensorAndConfigKey OPENLDAP_PORT = new PortAttributeSensorAndConfigKey("openldap.port", "OpenLDAP port", PortRanges.fromString("389"));

    @SetFromFlag
    public static final BasicAttributeSensorAndConfigKey<Boolean> OLDMIRRORMODE = new BasicAttributeSensorAndConfigKey<Boolean>(Boolean.class, "openldap.replication.olcmirrormode.enabled");

    BasicAttributeSensorAndConfigKey<Integer> OLCSERVERID = new BasicAttributeSensorAndConfigKey<Integer>(Integer.class , "openldap.olcserverid");

    BasicAttributeSensorAndConfigKey<Boolean> OPENLDAP_NODE_HAS_JOINED_CLUSTER = new BasicAttributeSensorAndConfigKey<Boolean>(Boolean.class,
            "openldap.node.openLdapNodeHasJoinedCluster", "Flag to indicate whether the OpenLDAP node has joined a cluster member");

    @Effector
    void loadLdifFromFile(String filePath);

    @Effector
    void ldapModifyFromString(String ldif);

    @Effector
    public Boolean commitCluster(@EffectorParam(name = "replicationProperties")ReplicationProperties replicationProperties);


    }



