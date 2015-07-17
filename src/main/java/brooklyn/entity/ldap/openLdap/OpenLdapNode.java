package brooklyn.entity.ldap.openLdap;

import brooklyn.config.ConfigKey;
import brooklyn.entity.Entity;
import brooklyn.entity.annotation.Effector;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.MethodEffector;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.BasicAttributeSensorAndConfigKey;
import brooklyn.event.basic.PortAttributeSensorAndConfigKey;
import brooklyn.event.basic.Sensors;
import brooklyn.location.MachineLocation;
import brooklyn.location.basic.PortRanges;
import brooklyn.util.flags.SetFromFlag;
import com.google.common.net.HostAndPort;

import java.util.List;

@ImplementedBy(OpenLdapNodeImpl.class)
public interface OpenLdapNode extends SoftwareProcess, Entity {
    @SetFromFlag("version")
    public static final ConfigKey<String> SUGGESTED_VERSION = ConfigKeys.newConfigKeyWithDefault(SoftwareProcess.SUGGESTED_VERSION, "");

    //FIXME: put download url here
    @SetFromFlag("downloadUrl")
    public static final BasicAttributeSensorAndConfigKey<String> DOWNLOAD_URL = new BasicAttributeSensorAndConfigKey.StringAttributeSensorAndConfigKey(Attributes.DOWNLOAD_URL, "");

    //FIXME: put port range here
    @SetFromFlag
    public static final PortAttributeSensorAndConfigKey OPENLDAP_PORT = new PortAttributeSensorAndConfigKey("openldap.port", "OpenLDAP port", PortRanges.fromString(""));

    public  String getHost();

    public int getPort();

    @SetFromFlag
    public static final BasicAttributeSensorAndConfigKey<Boolean> OLDMIRRORMODE = new BasicAttributeSensorAndConfigKey<Boolean>(Boolean.class, "openldap.replication.olcmirrormode.enabled");
    @SetFromFlag
    public static final BasicAttributeSensorAndConfigKey<Boolean> OLCOVERLAYCONFIG = new BasicAttributeSensorAndConfigKey<Boolean>(Boolean.class, "openldap.replication.olcoverlayconfig.enabled");
    @SetFromFlag
    public static final BasicAttributeSensorAndConfigKey<Boolean> OLCSYNCPROVCONFIG = new BasicAttributeSensorAndConfigKey<Boolean>(Boolean.class , "openldap.replication.olcsyncprovconfig.enabled");
    @SetFromFlag
    public static final BasicAttributeSensorAndConfigKey<Integer> OLCSERVERID = new BasicAttributeSensorAndConfigKey<Integer>(Integer.class, "openldap.replication.serverid");

    AttributeSensor<Boolean> OPENLDAP_NODE_HAS_JOINED_CLUSTER = Sensors.newBooleanSensor(
            "openldap.node.openLdapNodeHasJoinedCluster", "Flag to indicate whether the OpenLDAP node has joined a cluster member");

    MethodEffector<Void> COMMIT_OPENLDAP_CLUSTER = new MethodEffector<Void>(OpenLdapNode.class, "commitCluster");

    @Effector
    boolean loadLdifFromFile(String filePath);

    @Effector
    boolean loadLdifFromString(String ldif);

    @Effector
     void addMasterProviders(String provider, String bindMethod, String binddn, String credentials, String searchBase, String scope, String schemaChecking, String type, String retry, String interval, List<String> providers);

    @Effector(description = "Commit changes made to the Riak cluster")
    public void commitCluster();


    }
