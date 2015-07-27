package brooklyn.entity.ldap.openLdap;

import brooklyn.enricher.Enrichers;
import brooklyn.entity.Entity;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.Lifecycle;
import brooklyn.entity.basic.ServiceStateLogic;
import brooklyn.entity.group.AbstractMembershipTrackingPolicy;
import brooklyn.entity.group.DynamicClusterImpl;
import brooklyn.entity.ldap.openLdap.model.ReplicationProperties;
import brooklyn.location.Location;
import brooklyn.policy.EnricherSpec;
import brooklyn.policy.PolicySpec;
import brooklyn.util.time.Time;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ControlledDynamicOpenLdapClusterImpl extends DynamicClusterImpl implements ControlledDynamicOpenLdapCluster {
    public static final Logger log = LoggerFactory.getLogger(ControlledDynamicOpenLdapClusterImpl.class);
    private AtomicInteger serverId = new AtomicInteger(0);

    @Override
    public void init() {
        super.init();
        setAttribute(IS_CLUSTER_INIT, false);
    }

    @Override
    public void start(Collection<? extends Location> locations) {
        log.trace("OPENLDAP start");
        super.start(locations);
        connectSensors();
        log.trace("OPENLDAP start ::Checking if location is empty");
        Time.sleep(6000);
        if (locations.isEmpty()) locations = getLocations();
//        Optional<Entity> anyNode = Iterables.tryFind(getMembers(), Predicates.and(
//                Predicates.instanceOf(OpenLdapNode.class),
//                EntityPredicates.attributeEqualTo(OpenLdapNode.OPENLDAP_NODE_HAS_JOINED_CLUSTER, true),
//                EntityPredicates.attributeEqualTo(OpenLdapNode.SERVICE_UP, true)));
        Optional<Entity> anyNode = Iterables.tryFind(getMembers(), Predicates.and(
                Predicates.instanceOf(OpenLdapNode.class)));
                log.trace("Checking if anyNode is present");
        if (anyNode.isPresent()) {
            clusterizeEntities();
            setAttribute(IS_CLUSTER_INIT, true);
        } else {
            log.warn("No OpenLdap Nodes are found on the cluster: {}. Initialization Failed", getId());
            ServiceStateLogic.setExpectedState(this, Lifecycle.ON_FIRE);
        }
    }

    protected void connectSensors() {
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
    public void onServerPoolMemberChanged(Entity entity) {
        log.debug("Server changed ... " + entity.getLocations().iterator().next());
        clusterizeEntities();
    }

    private void clusterizeEntities(){
        Iterable<OpenLdapNode> targets = Iterables.filter(getChildren(), OpenLdapNode.class);
        setOlcServerIdIfNull(targets);
        for(OpenLdapNode target: targets){
            ReplicationProperties replicationProperties = new ReplicationProperties(targets ,"simple", "cn=admin,cn=config", "password", "cn=config", "sub", "on", "refreshAndPersist", "5 5 300 5", "00:00:05:00",  target.getAttribute(target.OLCSERVERID));
            target.commitCluster(replicationProperties);
        }
    }

    public static class MemberTrackingPolicy extends AbstractMembershipTrackingPolicy {

        @Override
        protected  void onEntityAdded(Entity member){
            if(super.entity instanceof  ControlledDynamicOpenLdapCluster){
                ((ControlledDynamicOpenLdapCluster) super.entity).onServerPoolMemberChanged(member);
            }
        }
    }

    private void setOlcServerIdIfNull(Iterable<OpenLdapNode> openLdapNodes){
        for (OpenLdapNode openLdapNode : openLdapNodes){
            setOlcServerIdIfNull(openLdapNode);
        }
    }

    private void setOlcServerIdIfNull(OpenLdapNode openLdapNode){
        log.error("Checking if OLCServerId is null");
        if(openLdapNode.getAttribute(openLdapNode.OLCSERVERID) == null){
            log.error("OlcServerID was null being set");
            openLdapNode.setAttribute(openLdapNode.OLCSERVERID, serverId.incrementAndGet());
            log.debug("Setting olcServerId to : " + serverId);
        }
    }


}
