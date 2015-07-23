package brooklyn.entity.ldap.openLdap;

import brooklyn.config.ConfigKey;
import brooklyn.entity.Entity;
import brooklyn.entity.annotation.Effector;
import brooklyn.entity.annotation.EffectorParam;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.MethodEffector;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.BasicAttributeSensorAndConfigKey;
import brooklyn.event.basic.PortAttributeSensorAndConfigKey;
import brooklyn.event.basic.Sensors;
import brooklyn.location.basic.PortRanges;
import brooklyn.util.flags.SetFromFlag;

import java.util.List;
import java.util.Map;

@ImplementedBy(OpenLdapNodeImpl.class)
public interface OpenLdapNode extends SoftwareProcess, Entity {
    @SetFromFlag("version")
    public static final ConfigKey<String> SUGGESTED_VERSION = ConfigKeys.newConfigKeyWithDefault(SoftwareProcess.SUGGESTED_VERSION, "");

    //FIXME: put download urls properly here
    @SetFromFlag("downloadUrl")
    public static final BasicAttributeSensorAndConfigKey<String> DOWNLOAD_URL = new BasicAttributeSensorAndConfigKey.StringAttributeSensorAndConfigKey(Attributes.DOWNLOAD_URL, "");

    //FIXME: put port properly here
    @SetFromFlag
    public static final PortAttributeSensorAndConfigKey OPENLDAP_PORT = new PortAttributeSensorAndConfigKey("openldap.port", "OpenLDAP port", PortRanges.fromString("389"));

    public  String getHost();

    public int getPort();

    @SetFromFlag
    public static final BasicAttributeSensorAndConfigKey<Boolean> OLDMIRRORMODE = new BasicAttributeSensorAndConfigKey<Boolean>(Boolean.class, "openldap.replication.olcmirrormode.enabled");
    @SetFromFlag
    public static final BasicAttributeSensorAndConfigKey<Boolean> OLCOVERLAYCONFIG = new BasicAttributeSensorAndConfigKey<Boolean>(Boolean.class, "openldap.replication.olcoverlayconfig.enabled");
    @SetFromFlag
    public static final BasicAttributeSensorAndConfigKey<Boolean> OLCSYNCPROVCONFIG = new BasicAttributeSensorAndConfigKey<Boolean>(Boolean.class , "openldap.replication.olcsyncprovconfig.enabled");


    AttributeSensor<Boolean> OPENLDAP_NODE_HAS_JOINED_CLUSTER = Sensors.newBooleanSensor(
            "openldap.node.openLdapNodeHasJoinedCluster", "Flag to indicate whether the OpenLDAP node has joined a cluster member");

    MethodEffector<Boolean> COMMIT_OPENLDAP_CLUSTER = new MethodEffector<Boolean>(OpenLdapNode.class, "commitCluster");

    @Effector
    boolean loadLdifFromFile(String filePath);

    @Effector
    boolean ldapModifyFromString(String ldif);


    @Effector
    public Boolean commitCluster(@EffectorParam(name = "currentNodes")Map<String, Integer> currentNodes);

    public Integer getOlcServerId();
    public void setOlcServerId(Integer olcServerId);


    }



