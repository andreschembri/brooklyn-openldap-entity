package brooklyn.entity.ldap.openLdap;

import brooklyn.enricher.Enrichers;
import brooklyn.entity.Entity;
import brooklyn.entity.basic.*;
import brooklyn.entity.group.AbstractMembershipTrackingPolicy;

import brooklyn.entity.group.DynamicClusterImpl;

import brooklyn.location.Location;
import brooklyn.policy.EnricherSpec;
import brooklyn.policy.PolicySpec;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ControlledDynamicOpenLdapClusterImpl extends DynamicClusterImpl implements ControlledDynamicOpenLdapCluster {
    public static final Logger log = LoggerFactory.getLogger(ControlledDynamicOpenLdapClusterImpl.class);


    @Override
    public void init() {
        log.error("OPENLDAP init");
        super.init();
        setAttribute(IS_CLUSTER_INIT, false);
    }

    @Override
    public void start(Collection<? extends Location> locations) {
        log.error("OPENLDAP start");
        super.start(locations);
        connectSensors();
        log.error("OPENLDAP start ::Checking if location is empty");
        if (locations.isEmpty()) locations = getLocations();
//        log.error("OPENLDAP start:: adding locations");
//        addLocations(locations);
//
//
//        log.error("OPENLDAP start:: creating list of nodes to be started");
//        List<Entity> childrenToStart = MutableList.<Entity>of(getCluster());
//
//        try {
//            log.error("OPENLDAP start:: trying to start each node, current number of nodes: " + childrenToStart.size() + " number of locations: " + locations.size());
////            Entities.invokeEffectorList(this, childrenToStart, Startable.START, ImmutableMap.of("locations", locations)).get();
//           log.error("Current Entity type is " + childrenToStart.toString() );
//            childrenToStart.get(0).invoke(Startable.START, null);
//
//            ServiceStateLogic.setExpectedState(this, Lifecycle.RUNNING);
//        } catch (Exception e) {
//            ServiceStateLogic.setExpectedState(this, Lifecycle.ON_FIRE);
//            throw Exceptions.propagate(e);
//        }finally {
//            connectSensors();
//        }

        Optional<Entity> anyNode = Iterables.tryFind(getMembers(), Predicates.and(
                Predicates.instanceOf(OpenLdapNode.class),
                EntityPredicates.attributeEqualTo(OpenLdapNode.OPENLDAP_NODE_HAS_JOINED_CLUSTER, true),
                EntityPredicates.attributeEqualTo(OpenLdapNode.SERVICE_UP, true)));
        if (anyNode.isPresent()) {
            log.info("Planning and Committing cluster changes on node: {}, cluster: {}", anyNode.get().getId(), getId());
            Entities.invokeEffector(this, anyNode.get(), OpenLdapNode.COMMIT_OPENLDAP_CLUSTER).blockUntilEnded();
            setAttribute(IS_CLUSTER_INIT, true);
        } else {
            log.warn("No Riak Nodes are found on the cluster: {}. Initialization Failed", getId());
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
log.error("SERVER CHANGED WOOOHOOO");
    }

    public static class MemberTrackingPolicy extends AbstractMembershipTrackingPolicy {
        @Override
        protected void onEntityEvent(EventType type, Entity entity) {
            ((ControlledDynamicOpenLdapCluster) super.entity).onServerPoolMemberChanged(entity);
        }
    }


    private List<String> getProviderUrlFromMembers() {
        log.error("OPENLDAP get provider from url members");
        ArrayList<String> providers = new ArrayList<String>();
        Iterable<OpenLdapNode> targets = Iterables.filter(getChildren(), OpenLdapNode.class);
        for (OpenLdapNode node : targets) {
            String providerURL = "ldap://" + node.getHost() + ":" + node.getPort();
            providers.add(providerURL);
        }
        return providers;
    }

    private boolean isNodeAlreadyAdded(OpenLdapNode nodeToBeChecked) {
        //TODO change method name and check if the node is already started as well

        for (Entity curMember : this.getMembers()) {
            if (curMember instanceof OpenLdapNode) {
                //If location is already there NodeWillBeMarked as already existing.
                if (((OpenLdapNode) curMember).getHost() == nodeToBeChecked.getHost()) return true;
            }
        }
        return false;
    }


}
