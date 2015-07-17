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
import brooklyn.util.flags.SetFromFlag;

@Catalog(name="Controlled Dynamic OpenLDAP Cluster", description="A cluster of OpenLDAP servers, which can be dynamically resized")
@ImplementedBy(ControlledDynamicOpenLdapClusterImpl.class)
public interface ControlledDynamicOpenLdapCluster extends Entity, DynamicGroup, Resizable, Group, Startable {



    @SetFromFlag("initialSize")
    public static ConfigKey<Integer> INITIAL_SIZE = ConfigKeys.newConfigKeyWithDefault(Cluster.INITIAL_SIZE, 1);

    @SetFromFlag("factory")
    public static BasicAttributeSensorAndConfigKey<ConfigurableEntityFactory<? extends OpenLdapNode>> FACTORY = new BasicAttributeSensorAndConfigKey(
            ConfigurableEntityFactory.class, DynamicCluster.FACTORY.getName(), "factory (or closure) to create the web server");

    @SuppressWarnings({ "unchecked", "rawtypes" })
    /** Spec for web server entiites to be created */
    @SetFromFlag("memberSpec")
    public static BasicAttributeSensorAndConfigKey<EntitySpec<? extends OpenLdapNode>> MEMBER_SPEC = new BasicAttributeSensorAndConfigKey(
            EntitySpec.class, DynamicCluster.MEMBER_SPEC.getName(), "Spec for web server entiites to be created");

    public static AttributeSensor<DynamicCluster> CLUSTER = new BasicAttributeSensor<DynamicCluster>(
            DynamicCluster.class, "controlleddynamiccluster.cluster", "Underlying web-app cluster");

    public static final AttributeSensor<String> HOSTNAME = Attributes.HOSTNAME;

    public static final AttributeSensor<Lifecycle> SERVICE_STATE_ACTUAL = Attributes.SERVICE_STATE_ACTUAL;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @SetFromFlag("clusterSpec")
    public static BasicAttributeSensorAndConfigKey<EntitySpec<? extends DynamicCluster>> CLUSTER_SPEC = new BasicAttributeSensorAndConfigKey(
            EntitySpec.class, "controlleddynamiccluster.clusterSpec", "Spec for creating the cluster");
    public DynamicCluster getCluster();

}
