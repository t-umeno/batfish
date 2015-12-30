package org.batfish.representation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.main.ConfigurationFormat;
import org.batfish.util.NamedStructure;
import org.batfish.util.SubRange;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Interface extends NamedStructure {

   private static final long serialVersionUID = 1L;

   private static InterfaceType computeCiscoInterfaceType(String name) {
      if (name.startsWith("Async")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("ATM")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Bundle-Ether")) {
         return InterfaceType.AGGREGATED;
      }
      else if (name.startsWith("cmp-mgmt")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Embedded-Service-Engine")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Ethernet")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("FastEthernet")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("GigabitEthernet")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("GMPLS")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("HundredGigE")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Group-Async")) {
         return InterfaceType.AGGREGATED;
      }
      else if (name.startsWith("Loopback")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Management")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("mgmt")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("MgmtEth")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Null")) {
         throw new BatfishException("Don't know what to do with this");
      }
      else if (name.startsWith("Port-channel")) {
         return InterfaceType.AGGREGATED;
      }
      else if (name.startsWith("POS")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Serial")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("TenGigabitEthernet")) {
         return InterfaceType.PHYSICAL;
      }
      else if (name.startsWith("Tunnel")) {
         return InterfaceType.VPN;
      }
      else if (name.startsWith("tunnel-te")) {
         return InterfaceType.VPN;
      }
      else if (name.startsWith("Vlan")) {
         return InterfaceType.VLAN;
      }
      else {
         throw new BatfishException(
               "Missing mapping to interface type for name: \"" + name + "\"");
      }
   }

   public static InterfaceType computeInterfaceType(String name,
         ConfigurationFormat format) {
      switch (format) {
      case ARISTA:
      case CISCO:
      case CISCO_IOS_XR:
         return computeCiscoInterfaceType(name);

      case FLAT_JUNIPER:
      case JUNIPER:
      case JUNIPER_SWITCH:
         return computeJuniperInterfaceType(name);

      case UNKNOWN:
      case VXWORKS:
      default:
         throw new BatfishException(
               "Cannot compute interface type for unsupported configuration format: "
                     + format.toString());
      }
   }

   private static InterfaceType computeJuniperInterfaceType(String name) {
      if (name.startsWith("st")) {
         return InterfaceType.VPN;
      }
      else if (name.startsWith("reth")) {
         return InterfaceType.REDUNDANT;
      }
      else if (name.startsWith("ae")) {
         return InterfaceType.AGGREGATED;
      }
      else {
         return InterfaceType.PHYSICAL;
      }
   }

   private int _accessVlan;

   private boolean _active;

   private ArrayList<SubRange> _allowedVlans;

   private final Set<Prefix> _allPrefixes;

   private Double _bandwidth;

   private String _description;

   private IpAccessList _inboundFilter;

   private IpAccessList _incomingFilter;

   private Integer _isisCost;

   private IsisInterfaceMode _isisL1InterfaceMode;

   private IsisInterfaceMode _isisL2InterfaceMode;

   private int _nativeVlan;

   private OspfArea _ospfArea;

   private Integer _ospfCost;

   private int _ospfDeadInterval;

   private boolean _ospfEnabled;

   private int _ospfHelloMultiplier;

   private boolean _ospfPassive;

   private IpAccessList _outgoingFilter;

   private Configuration _owner;

   private Prefix _prefix;

   private PolicyMap _routingPolicy;

   private final Set<Prefix> _secondaryPrefixes;

   private SwitchportMode _switchportMode;

   private SwitchportEncapsulationType _switchportTrunkEncapsulation;

   private Zone _zone;

   public Interface(String name, Configuration owner) {
      super(name);
      _active = true;
      _allPrefixes = new TreeSet<Prefix>();
      _nativeVlan = 1;
      _owner = owner;
      _switchportMode = SwitchportMode.NONE;
      _allowedVlans = new ArrayList<SubRange>();
      _secondaryPrefixes = new TreeSet<Prefix>();
   }

   public void addAllowedRanges(List<SubRange> ranges) {
      _allowedVlans.addAll(ranges);
   }

   private InterfaceType computeInterfaceType() {
      return computeInterfaceType(_name, _owner.getVendor());
   }

   public int getAccessVlan() {
      return _accessVlan;
   }

   public boolean getActive() {
      return _active;
   }

   public List<SubRange> getAllowedVlans() {
      return _allowedVlans;
   }

   public Set<Prefix> getAllPrefixes() {
      return _allPrefixes;
   }

   public double getBandwidth() {
      return _bandwidth;
   }

   public String getDescription() {
      return _description;
   }

   public IpAccessList getInboundFilter() {
      return _inboundFilter;
   }

   public IpAccessList getIncomingFilter() {
      return _incomingFilter;
   }

   public Integer getIsisCost() {
      return _isisCost;
   }

   public IsisInterfaceMode getIsisL1InterfaceMode() {
      return _isisL1InterfaceMode;
   }

   public IsisInterfaceMode getIsisL2InterfaceMode() {
      return _isisL2InterfaceMode;
   }

   public int getNativeVlan() {
      return _nativeVlan;
   }

   public OspfArea getOspfArea() {
      return _ospfArea;
   }

   public Integer getOspfCost() {
      return _ospfCost;
   }

   public int getOspfDeadInterval() {
      return _ospfDeadInterval;
   }

   public boolean getOspfEnabled() {
      return _ospfEnabled;
   }

   public int getOspfHelloMultiplier() {
      return _ospfHelloMultiplier;
   }

   public boolean getOspfPassive() {
      return _ospfPassive;
   }

   public IpAccessList getOutgoingFilter() {
      return _outgoingFilter;
   }

   public Configuration getOwner() {
      return _owner;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   public PolicyMap getRoutingPolicy() {
      return _routingPolicy;
   }

   public Set<Prefix> getSecondaryPrefixes() {
      return _secondaryPrefixes;
   }

   public SwitchportMode getSwitchportMode() {
      return _switchportMode;
   }

   public SwitchportEncapsulationType getSwitchportTrunkEncapsulation() {
      return _switchportTrunkEncapsulation;
   }

   public Zone getZone() {
      return _zone;
   }

   public boolean isLoopback(ConfigurationFormat vendor) {
      if (vendor == ConfigurationFormat.JUNIPER
            || vendor == ConfigurationFormat.FLAT_JUNIPER) {
         if (!_name.contains(".")) {
            return false;
         }
      }
      return _name.toLowerCase().startsWith("lo");
   }

   public void setAccessVlan(int vlan) {
      _accessVlan = vlan;
   }

   public void setActive(boolean active) {
      _active = active;
   }

   public void setBandwidth(Double bandwidth) {
      _bandwidth = bandwidth;
   }

   public void setDescription(String description) {
      _description = description;
   }

   public void setInboundFilter(IpAccessList inboundFilter) {
      _inboundFilter = inboundFilter;
   }

   public void setIncomingFilter(IpAccessList filter) {
      _incomingFilter = filter;
   }

   public void setIsisCost(Integer isisCost) {
      _isisCost = isisCost;
   }

   public void setIsisL1InterfaceMode(IsisInterfaceMode mode) {
      _isisL1InterfaceMode = mode;
   }

   public void setIsisL2InterfaceMode(IsisInterfaceMode mode) {
      _isisL2InterfaceMode = mode;
   }

   public void setNativeVlan(int vlan) {
      _nativeVlan = vlan;
   }

   public void setOspfArea(OspfArea ospfArea) {
      _ospfArea = ospfArea;
   }

   public void setOspfCost(Integer ospfCost) {
      _ospfCost = ospfCost;
   }

   public void setOspfDeadInterval(int seconds) {
      _ospfDeadInterval = seconds;
   }

   public void setOspfEnabled(boolean b) {
      _ospfEnabled = b;
   }

   public void setOspfHelloMultiplier(int multiplier) {
      _ospfHelloMultiplier = multiplier;
   }

   public void setOspfPassive(boolean passive) {
      _ospfPassive = passive;
   }

   public void setOutgoingFilter(IpAccessList filter) {
      _outgoingFilter = filter;
   }

   public void setPrefix(Prefix prefix) {
      _prefix = prefix;
   }

   public void setRoutingPolicy(PolicyMap policy) {
      _routingPolicy = policy;
   }

   public void setSwitchportMode(SwitchportMode switchportMode) {
      _switchportMode = switchportMode;
   }

   public void setSwitchportTrunkEncapsulation(
         SwitchportEncapsulationType encapsulation) {
      _switchportTrunkEncapsulation = encapsulation;
   }

   public void setZone(Zone zone) {
      _zone = zone;
   }

   public JSONObject toJSONObject() throws JSONException {
      JSONObject iface = new JSONObject();
      iface.put("node", _owner.getName());
      iface.put("name", _name);
      iface.put("prefix", _prefix.toString());
      InterfaceType interfaceType = computeInterfaceType();
      iface.put("type", interfaceType.toString());
      return iface;
   }

}
