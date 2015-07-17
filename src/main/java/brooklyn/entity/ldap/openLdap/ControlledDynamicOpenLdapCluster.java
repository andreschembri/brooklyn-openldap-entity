package brooklyn.entity.ldap.openLdap;

import brooklyn.catalog.Catalog;
import brooklyn.config.ConfigKey;
import brooklyn.entity.Entity;
import brooklyn.entity.Group;
import brooklyn.entity.basic.*;
import brooklyn.entity.group.Cluster;
import brooklyn.entity.group.DynamicCluster;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.entity.trait.Resizable;
import brooklyn.entity.trait.Startable;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.BasicAttributeSensor;
import brooklyn.event.basic.BasicAttributeSensorAndConfigKey;
import brooklyn.event.basic.Sensors;
import brooklyn.util.flags.SetFromFlag;
import com.google.common.reflect.TypeToken;

import java.util.Map;

@Catalog(name="Controlled Dynamic OpenLDAP Cluster", description="A cluster of OpenLDAP servers, which can be dynamically resized")
@ImplementedBy(ControlledDynamicOpenLdapClusterImpl.class)
public interface ControlledDynamicOpenLdapCluster extends  DynamicCluster{



    @SetFromFlag("initialSize")
    public static ConfigKey<Integer> INITIAL_SIZE = ConfigKeys.newConfigKeyWithDefault(Cluster.INITIAL_SIZE, 1);

    @SetFromFlag("factory")
    public static BasicAttributeSensorAndConfigKey<ConfigurableEntityFactory<? extends OpenLdapNode>> FACTORY = new BasicAttributeSensorAndConfigKey(
            ConfigurableEntityFactory.class, DynamicCluster.FACTORY.getName(), "factory (or closure) to create the web server");

    @SuppressWarnings("serial")
    AttributeSensor<Map<Entity, String>> OPENLDAP_CLUSTER_NODES = Sensors.newSensor(
            new TypeToken<Map<Entity, String>>() {},
            "openldap.cluster.nodes", "Names of all active OpenLdap nodes in the cluster <Entity,OpenLdap Name>");

    public static AttributeSensor<DynamicCluster> CLUSTER = new BasicAttributeSensor<DynamicCluster>(
            DynamicCluster.class, "controlleddynamiccluster.cluster", "Underlying web-app cluster");

    public static final AttributeSensor<String> HOSTNAME = Attributes.HOSTNAME;

    public static final AttributeSensor<Lifecycle> SERVICE_STATE_ACTUAL = Attributes.SERVICE_STATE_ACTUAL;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @SetFromFlag("clusterSpec")
    public static BasicAttributeSensorAndConfigKey<EntitySpec<? extends DynamicCluster>> CLUSTER_SPEC = new BasicAttributeSensorAndConfigKey(
            EntitySpec.class, "controlleddynamiccluster.clusterSpec", "Spec for creating the cluster");



    AttributeSensor<Boolean> IS_CLUSTER_INIT = Sensors.newBooleanSensor("openLdap.cluster.isClusterInit", "Flag to determine if the cluster was already initialized");

    void onServerPoolMemberChanged(Entity entity);

//    AttributeSensor<String> NODE_LIST = Sensors.newStringSensor("openLdap.cluster.nodeList", "List of nodes (including ports), comma separated");

}
