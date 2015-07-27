package brooklyn.entity.ldap.openLdap;

import brooklyn.catalog.Catalog;
import brooklyn.config.ConfigKey;
import brooklyn.entity.Entity;
import brooklyn.entity.annotation.Effector;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.ConfigurableEntityFactory;
import brooklyn.entity.basic.Lifecycle;
import brooklyn.entity.group.Cluster;
import brooklyn.entity.group.DynamicCluster;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.BasicAttributeSensor;
import brooklyn.event.basic.BasicAttributeSensorAndConfigKey;
import brooklyn.event.basic.Sensors;
import brooklyn.util.flags.SetFromFlag;
import com.google.common.reflect.TypeToken;

import java.util.Map;

@Catalog(name = "Controlled Dynamic OpenLDAP Cluster", description = "A cluster of OpenLDAP servers, which can be dynamically resized")
@ImplementedBy(ControlledDynamicOpenLdapClusterImpl.class)
public interface ControlledDynamicOpenLdapCluster extends DynamicCluster {


    @SetFromFlag("initialSize")
    public static ConfigKey<Integer> INITIAL_SIZE = ConfigKeys.newConfigKeyWithDefault(Cluster.INITIAL_SIZE, 1);

    @SetFromFlag("factory")
    public static BasicAttributeSensorAndConfigKey<ConfigurableEntityFactory<? extends OpenLdapNode>> FACTORY = new BasicAttributeSensorAndConfigKey(
            ConfigurableEntityFactory.class, DynamicCluster.FACTORY.getName(), "factory (or closure) to create the web server");

    @SuppressWarnings("serial")
    AttributeSensor<Map<Entity, String>> OPENLDAP_CLUSTER_NODES = Sensors.newSensor(
            new TypeToken<Map<Entity, String>>() {
            },
            "openldap.cluster.nodes", "Names of all active OpenLdap nodes in the cluster <Entity,OpenLdap Name>");

    public static AttributeSensor<DynamicCluster> CLUSTER = new BasicAttributeSensor<DynamicCluster>(
            DynamicCluster.class, "controlleddynamiccluster.cluster", "Underlying web-app cluster");

    public static final AttributeSensor<String> HOSTNAME = Attributes.HOSTNAME;

    public static final AttributeSensor<Lifecycle> SERVICE_STATE_ACTUAL = Attributes.SERVICE_STATE_ACTUAL;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @SetFromFlag("clusterSpec")
    public static BasicAttributeSensorAndConfigKey<EntitySpec<? extends DynamicCluster>> CLUSTER_SPEC = new BasicAttributeSensorAndConfigKey(
            EntitySpec.class, "controlleddynamiccluster.clusterSpec", "Spec for creating the cluster");

    ConfigKey<String> BINDMETHOD = ConfigKeys.newConfigKey(String.class, "openldap.replication.bindmethod", "", "simple");
    ConfigKey<String> BINDDN = ConfigKeys.newConfigKey(String.class, "openldap.replication.binddn", "", "cn=admin,cn=config");
    ConfigKey<String> CREDENTIALS = ConfigKeys.newConfigKey(String.class, "openldap.replication.credentials", "", "password");
    ConfigKey<String> SEARCHBASE = ConfigKeys.newConfigKey(String.class, "openldap.replication.searchbase", "", "cn=config");
    ConfigKey<String> SCOPE = ConfigKeys.newConfigKey(String.class, "openldap.replication.scope", "", "sub");
    ConfigKey<Boolean> SCHEMACHECKING = ConfigKeys.newConfigKey(Boolean.class, "openldap.replication.schemachecking", "", true);
    ConfigKey<String> TYPE = ConfigKeys.newConfigKey(String.class, "openldap.replication.type", "", "refreshAndPersist");
    ConfigKey<String> RETRYSTRING = ConfigKeys.newConfigKey(String.class, "openldap.replication.retry", "", "5 5 300 5");
    ConfigKey<String> INTERVALSTRING = ConfigKeys.newConfigKey(String.class, "openldap.replication.interval", "", "00:00:05:00");
    ConfigKey<Integer> TIMEOUT = ConfigKeys.newConfigKey(Integer.class, "openldap.replication.timeout");

    AttributeSensor<Boolean> IS_CLUSTER_INIT = Sensors.newBooleanSensor("openLdap.cluster.isClusterInit", "Flag to determine if the cluster was already initialized");

    void onServerPoolMemberChanged(Entity entity);


}
