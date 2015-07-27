package brooklyn.entity.ldap.openLdap;

import brooklyn.enricher.Enrichers;
import brooklyn.entity.Entity;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.basic.Lifecycle;
import brooklyn.entity.basic.ServiceStateLogic;
import brooklyn.entity.group.AbstractMembershipTrackingPolicy;
import brooklyn.entity.group.DynamicClusterImpl;
import brooklyn.location.Location;
import brooklyn.policy.EnricherSpec;
import brooklyn.policy.PolicySpec;
import brooklyn.util.time.Time;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ControlledDynamicOpenLdapClusterImpl extends DynamicClusterImpl implements ControlledDynamicOpenLdapCluster {
    public static final Logger log = LoggerFactory.getLogger(ControlledDynamicOpenLdapClusterImpl.class);


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
            log.info("Planning and Committing cluster changes on node: {}, cluster: {}", anyNode.get().getId(), getId());
           //Fixme: fix this name
            Map<String,Integer> arrayListTest = getProviderUrlAndIdFromMembers();



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


    private AtomicInteger serverId = new AtomicInteger(0);

    @Override
    public void onServerPoolMemberChanged(Entity entity) {
        log.debug("Server changed ... " + entity.getLocations().iterator().next());

    if (entity instanceof OpenLdapNode){
        OpenLdapNode openLdapNode = (OpenLdapNode) entity;


        Map<String, Integer> currentMembers = new HashMap<String, Integer>();
        for(Map.Entry<String, Integer> member : getProviderUrlAndIdFromMembers().entrySet()){
            currentMembers.put(member.getKey(), member.getValue());
        }
        Iterable<OpenLdapNode> targets = Iterables.filter(getChildren(), OpenLdapNode.class);
        for(OpenLdapNode target: targets){
            target.commitCluster(currentMembers);
        }}
    }

    public static class MemberTrackingPolicy extends AbstractMembershipTrackingPolicy {

        @Override
        protected  void onEntityAdded(Entity member){
            if(super.entity instanceof  ControlledDynamicOpenLdapCluster){
                ((ControlledDynamicOpenLdapCluster) super.entity).onServerPoolMemberChanged(member);
            }
        }
    }

    private void setOlcServerIdIfNull(OpenLdapNode openLdapNode){
        if(openLdapNode.getOlcServerId() == null){
            openLdapNode.setOlcServerId(serverId.incrementAndGet());
            log.debug("Setting olcServerId to : " + serverId);
        }
    }


    private Map<String, Integer> getProviderUrlAndIdFromMembers() {
        log.debug("OPENLDAP get provider from url members");
        Map<String, Integer> providers = new HashMap<String, Integer>();
        Iterable<OpenLdapNode> targets = Iterables.filter(getChildren(), OpenLdapNode.class);
        for (OpenLdapNode node : targets) {
            String providerURL = "ldap://" + node.getHost() + ":" + node.getPort();
            setOlcServerIdIfNull(node);
            Integer id = node.getOlcServerId();
            log.debug("Node placed in map has the values of " + providerURL + " " + id);
            providers.put(providerURL, id);
        }
        return providers;
    }


}
