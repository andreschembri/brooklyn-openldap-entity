package brooklyn.entity.ldap.openLdap;

import brooklyn.enricher.Enrichers;
import brooklyn.entity.Entity;
import brooklyn.entity.basic.*;
import brooklyn.entity.group.DynamicCluster;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.entity.trait.Startable;
import brooklyn.entity.trait.StartableMethods;
import brooklyn.event.SensorEvent;
import brooklyn.event.SensorEventListener;
import brooklyn.event.feed.ConfigToAttributes;
import brooklyn.location.Location;
import brooklyn.util.collections.MutableList;
import brooklyn.util.collections.MutableMap;
import brooklyn.util.exceptions.Exceptions;
import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ControlledDynamicOpenLdapClusterImpl extends DynamicGroupImpl implements ControlledDynamicOpenLdapCluster {
    public static final Logger log = LoggerFactory.getLogger(ControlledDynamicOpenLdapClusterImpl.class);

    public ControlledDynamicOpenLdapClusterImpl() {
        this(MutableMap.of(), null);
    }

    public ControlledDynamicOpenLdapClusterImpl(Map<?, ?> flags) {
        this(flags, null);
    }

    public ControlledDynamicOpenLdapClusterImpl(Entity parent) {
        this(MutableMap.of(), parent);
    }

    @Deprecated
    public ControlledDynamicOpenLdapClusterImpl(Map<?, ?> flags, Entity parent) {
        super(flags, parent);
    }

    @Override
    public void init() {
        log.error("OPENLDAP init");
        super.init();
        ConfigToAttributes.apply(this, FACTORY);
        ConfigToAttributes.apply(this, MEMBER_SPEC);
        ConfigurableEntityFactory<? extends OpenLdapNode> serverFactory = getAttribute(FACTORY);
        EntitySpec<? extends OpenLdapNode> serverSpec = getAttribute(MEMBER_SPEC);
        if (serverFactory == null && serverSpec == null) {
            serverSpec = EntitySpec.create(OpenLdapNode.class);
            setAttribute(MEMBER_SPEC, serverSpec);
        }

        EntitySpec<?extends DynamicCluster> clusterSpec = getAttribute(CLUSTER_SPEC);
Map<String, Object> clusterFlags;
        Map<String,Object> webClusterFlags;
        if (serverSpec != null) {
            webClusterFlags = MutableMap.<String,Object>of("memberSpec", serverSpec);
        } else {
            webClusterFlags = MutableMap.<String,Object>of("factory", serverFactory);
        }

        if (clusterSpec == null) {
            log.debug("creating default web cluster spec for {}", this);
            clusterSpec = EntitySpec.create(DynamicCluster.class);
        }


        doBind();
    }

    @Override
    public void start(Collection<? extends Location> locations) {
        log.error("OPENLDAP start");
        if (isLegacyConstruction()) {
            init();
        }
        log.error("OPENLDAP start ::Checking if location is empty");
        if (locations.isEmpty()) locations = getLocations();
        log.error("OPENLDAP start:: adding locations");
        addLocations(locations);


        log.error("OPENLDAP start:: creating list of nodes to be started");
        List<Entity> childrenToStart = MutableList.<Entity>of(getCluster());

        try {
            log.error("OPENLDAP start:: trying to start each node, current number of nodes: " + childrenToStart.size() + " number of locations: " + locations.size());
//            Entities.invokeEffectorList(this, childrenToStart, Startable.START, ImmutableMap.of("locations", locations)).get();
           log.error("Current Entity type is " + childrenToStart.toString() );
            childrenToStart.get(0).invoke(Startable.START, null);

            ServiceStateLogic.setExpectedState(this, Lifecycle.RUNNING);
        } catch (Exception e) {
            ServiceStateLogic.setExpectedState(this, Lifecycle.ON_FIRE);
            throw Exceptions.propagate(e);
        }finally {
            connectSensors();
        }
    }

    private void connectSensors() {

    }


    @Override
    public void restart() {
        Collection<Location> locations = Lists.newArrayList(getLocations());
        stop();
        start(locations);
    }

    @Override
    public void initEnrichers() {
        log.error("OPENLDAP starting enrichers");
        if (getConfigRaw(UP_QUORUM_CHECK, false).isAbsent()) {
            setConfig(UP_QUORUM_CHECK, QuorumCheck.QuorumChecks.newInstance(2, 1.0, false));
        }
        super.initEnrichers();
        ServiceStateLogic.newEnricherFromChildrenUp().checkChildrenOnly().requireUpChildren(getConfig(UP_QUORUM_CHECK)).addTo(this);

    }

    @Override
    public void rebind() {
        log.error("OPENLDAP rebind");
        super.rebind();
        doBind();
    }

    protected void doBind() {
        log.error("OPENLDAP do rebind");
        //TODO: Logic to subscribe all members of the cluster
        //need to go to each node and add the location of each other node and then start...
        DynamicCluster cluster = getAttribute(CLUSTER);
        if (cluster != null) {
            subscribe(cluster, DynamicCluster.GROUP_MEMBERS, new SensorEventListener<Object>() {
                @Override
                public void onEvent(SensorEvent<Object> event) {
                    //need to check the new nodes...
                    log.error("Hello from do bind");
                }
            });
        }

    }

    @Override
    public void stop() {
        log.error("OPENLDAP stopping");
        ServiceStateLogic.setExpectedState(this, Lifecycle.STOPPING);
        try {
            List<Startable> toStop = Lists.newArrayList();
            StartableMethods.stopSequentially(toStop);
            clearLocations();
            ServiceStateLogic.setExpectedState(this, Lifecycle.STOPPED);
        } catch (Exception e) {
            ServiceStateLogic.setExpectedState(this, Lifecycle.ON_FIRE);
            throw Exceptions.propagate(e);
        }
    }

    @Override
    public boolean addMember(Entity member) {
        log.error("OPENLDAP add member");
        OpenLdapNode newClusterMember = (OpenLdapNode) member;
        if (isNodeAlreadyAdded(newClusterMember)) return false;

        String providerURL = "ldap://" + newClusterMember.getHost() + ":" + newClusterMember.getPort();

        Iterable<OpenLdapNode> targets = Iterables.filter(getChildren(), OpenLdapNode.class);
        for (OpenLdapNode curMember : targets) {
            curMember.addMasterProviders(providerURL, "simple", "cn=Manager,dc=server,dc=world", "password", "dc=server,dc=world", "sub", "on", "refreshAndPersist", "30 5 300 3", "00:00:05:00", getProviderUrlFromMembers());
        }
        return true;
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
        for (Entity curMember : this.getMembers()) {
            if (curMember instanceof OpenLdapNode) {
                //If location is already there NodeWillBeMarked as already existing.
                if (((OpenLdapNode) curMember).getHost() == nodeToBeChecked.getHost()) return true;
            }
        }
        return false;
    }

    @Override
    public Integer resize(Integer desiredSize) {
        log.error("OPENLDAP resizing");
        return getCluster().resize(desiredSize);
    }

    public synchronized DynamicCluster getCluster() {
        return getAttribute(CLUSTER);
    }
}
