package brooklyn.entity.ldap.openLdap;

import brooklyn.enricher.Enrichers;
import brooklyn.entity.Entity;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.EntityLocal;
import brooklyn.entity.group.AbstractMembershipTrackingPolicy;
import brooklyn.entity.group.DynamicClusterImpl;
import brooklyn.entity.ldap.openLdap.model.ReplicationProperties;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.Sensors;
import brooklyn.location.Location;
import brooklyn.policy.EnricherSpec;
import brooklyn.policy.PolicySpec;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ControlledDynamicOpenLdapClusterImpl extends DynamicClusterImpl implements ControlledDynamicOpenLdapCluster {
    public static final Logger log = LoggerFactory.getLogger(ControlledDynamicOpenLdapClusterImpl.class);

    private static AttributeSensor<AtomicInteger> SERVER_ID = Sensors.newSensor(AtomicInteger.class, "SERVER_ID");


    @Override
    public void init() {
        super.init();
        setAttribute(SERVER_ID , new AtomicInteger(0));
        setAttribute(IS_CLUSTER_INIT, false);
        subscribeMembers();
    }

    protected void subscribeMembers() {
        addPolicy(PolicySpec.create(MemberTrackingPolicy.class)
                .displayName("Controller targets tracker")
                .configure("sensorsToTrack", ImmutableSet.of(OpenLdapNode.SERVICE_UP))
                .configure("group", this));

        EnricherSpec<?> first = Enrichers.builder()
                .aggregating(Attributes.ADDRESS)
                .publishing(Attributes.ADDRESS)
                .computing(new Function<Collection<String>, String>() {
                    @Override
                    public String apply(Collection<String> input) {
                        return input.iterator().next();
                    }
                })
                .fromMembers()
                .build();
        addEnricher(first);


    }

    @Override
    public Entity createNode(Location loc, Map<?,?> spec){
        Entity entity =   super.createNode(loc, spec);
        if(entity.getAttribute(OpenLdapNode.OLCSERVERID) == null) {
            ((EntityLocal) entity).setAttribute(OpenLdapNode.OLCSERVERID, getAttribute(SERVER_ID).incrementAndGet());
        }
        return entity;
    }

    private void clusterizeEntities(){
        Iterable<OpenLdapNode> targets = Iterables.filter(getChildren(), OpenLdapNode.class);
        for(OpenLdapNode target: targets) {
            ReplicationProperties replicationProperties = new ReplicationProperties(targets ,getConfig(BINDMETHOD), getConfig(BINDDN), getConfig(CREDENTIALS), getConfig(SEARCHBASE), getConfig(SCOPE), getConfig(SCHEMACHECKING), getConfig(TYPE), getConfig(RETRYSTRING), getConfig(INTERVALSTRING),  target.getAttribute(target.OLCSERVERID));
            clusterNode(replicationProperties, target);
        }
    }

    public static class MemberTrackingPolicy extends AbstractMembershipTrackingPolicy {
        @Override
        protected  void onEntityAdded(Entity member){
                ((ControlledDynamicOpenLdapClusterImpl) entity).clusterizeEntities();
        }
        @Override
        protected void onEntityRemoved(Entity member){
                ((ControlledDynamicOpenLdapClusterImpl) entity).clusterizeEntities();
        }
    }

    private void clusterNode(ReplicationProperties replicationProperties, OpenLdapNode targetNode) {
        if (getAttribute(OPENLDAP_NODE_HAS_JOINED_CLUSTER)){
            targetNode.ldapModifyFromString(ConfigurationGenerator.generateModifySyncReplication(replicationProperties));
        }
        else if(!getAttribute(OPENLDAP_NODE_HAS_JOINED_CLUSTER)) {
            targetNode.ldapAddFromString(ConfigurationGenerator.generateAddSyncProvToModuleList());
            targetNode.ldapModifyFromString(ConfigurationGenerator.generateLoadSyncProv());
            targetNode.ldapModifyFromString(ConfigurationGenerator.generateSetOlcServerId(this.getAttribute(targetNode.OLCSERVERID)));
            targetNode.ldapModifyFromString(ConfigurationGenerator.generateSetPassword(targetNode.generateSlappassword(replicationProperties.getCredentials())));
            targetNode.ldapModifyFromString(ConfigurationGenerator.generateAddSyncProvider());
            targetNode.ldapModifyFromString(ConfigurationGenerator.generateAddSyncReplication(replicationProperties));
        }
    }


}
