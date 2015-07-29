package brooklyn.entity.ldap.openLdap;

import brooklyn.entity.annotation.Effector;
import brooklyn.entity.annotation.EffectorParam;
import brooklyn.entity.basic.*;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.basic.BasicAttributeSensorAndConfigKey;
import brooklyn.event.basic.PortAttributeSensorAndConfigKey;
import brooklyn.location.basic.PortRanges;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(OpenLdapNodeImpl.class)
public interface OpenLdapNode extends SoftwareProcess {
    BasicAttributeSensorAndConfigKey<String> OPENLDAP_ADDRESS = new BasicAttributeSensorAndConfigKey<String>(String.class, "openldap.address");

    //FIXME: configure it correctly in ssh driver
    @SetFromFlag
    PortAttributeSensorAndConfigKey OPENLDAP_PORT = new PortAttributeSensorAndConfigKey("openldap.port", "OpenLDAP port", PortRanges.fromString("389+"));

    BasicAttributeSensorAndConfigKey<Integer> OLCSERVERID = new BasicAttributeSensorAndConfigKey<Integer>(Integer.class , "openldap.olcserverid");

    MethodEffector<Void> LDAP_MODIFY_FROM_STRING = new MethodEffector<Void>(OpenLdapNode.class, "ldapModifyFromString");
    MethodEffector<Void> LDAP_ADD_FROM_STRING = new MethodEffector<Void>(OpenLdapNode.class, "ldapAddFromString");
    MethodEffector<String> GENERATE_SLAPPASSWD = new MethodEffector<String>(OpenLdapNode.class, "generateSlappassword");

    @Effector
    void ldapModifyFromString(@EffectorParam(name = "ldif")String ldif);

    @Effector
    void ldapAddFromString(@EffectorParam(name = "ldif")String ldif);

    @Effector
    String generateSlappassword(@EffectorParam(name= "password")String password);


    }



