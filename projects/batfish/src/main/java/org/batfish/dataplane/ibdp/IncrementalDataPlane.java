package org.batfish.dataplane.ibdp;

import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.common.util.CollectionUtil.toImmutableSortedMap;
import static org.batfish.specifier.LocationInfoUtils.computeLocationInfo;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.ForwardingAnalysisImpl;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.vxlan.Layer2Vni;

public final class IncrementalDataPlane implements Serializable, DataPlane {

  public static class Builder {

    private Map<String, Node> _nodes;
    private Topology _layer3Topology;

    public Builder setNodes(Map<String, Node> nodes) {
      _nodes = ImmutableMap.copyOf(nodes);
      return this;
    }

    public Builder setLayer3Topology(Topology layer3Topology) {
      _layer3Topology = layer3Topology;
      return this;
    }

    public IncrementalDataPlane build() {
      return new IncrementalDataPlane(this);
    }
  }

  private final class ConfigurationsSupplier
      implements Serializable, Supplier<Map<String, Configuration>> {

    @Override
    public Map<String, Configuration> get() {
      return computeConfigurations();
    }
  }

  private final class FibsSupplier
      implements Serializable, Supplier<Map<String, Map<String, Fib>>> {

    @Override
    public Map<String, Map<String, Fib>> get() {
      return computeFibs();
    }
  }

  private final class ForwardingAnalysisSupplier
      implements Serializable, Supplier<ForwardingAnalysis> {

    @Override
    public ForwardingAnalysis get() {
      return computeForwardingAnalysis();
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final Supplier<Map<String, Configuration>> _configurations =
      Suppliers.memoize(new ConfigurationsSupplier());

  private final Supplier<Map<String, Map<String, Fib>>> _fibs =
      Suppliers.memoize(new FibsSupplier());

  private final Supplier<ForwardingAnalysis> _forwardingAnalysis =
      Suppliers.memoize(new ForwardingAnalysisSupplier());

  private final Map<String, Node> _nodes;

  private final Topology _layer3Topology;

  private transient SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>>
      _ribs;

  @Nonnull private final Table<String, String, Set<Bgpv4Route>> _bgpRoutes;
  @Nonnull private final Table<String, String, Set<EvpnRoute<?, ?>>> _evpnRoutes;
  @Nonnull private final Table<String, String, Set<Layer2Vni>> _vniSettings;

  private IncrementalDataPlane(Builder builder) {
    _nodes = builder._nodes;
    _layer3Topology = builder._layer3Topology;
    _bgpRoutes = computeBgpRoutes();
    _evpnRoutes = computeEvpnRoutes();
    _vniSettings = computeVniSettings();
  }

  private Map<String, Configuration> computeConfigurations() {
    return _nodes.entrySet().stream()
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, e -> e.getValue().getConfiguration()));
  }

  private Map<String, Map<String, Fib>> computeFibs() {
    return toImmutableMap(
        _nodes,
        Entry::getKey,
        nodeEntry ->
            toImmutableMap(
                nodeEntry.getValue().getVirtualRouters(),
                Entry::getKey,
                vrfEntry -> vrfEntry.getValue().getFib()));
  }

  private ForwardingAnalysis computeForwardingAnalysis() {
    Map<String, Configuration> configs = getConfigurations();
    return new ForwardingAnalysisImpl(
        configs, getFibs(), _layer3Topology, computeLocationInfo(configs));
  }

  private SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>>
      computeRibs() {
    return toImmutableSortedMap(
        _nodes,
        Entry::getKey,
        nodeEntry ->
            toImmutableSortedMap(
                nodeEntry.getValue().getVirtualRouters(),
                Entry::getKey,
                vrfEntry -> vrfEntry.getValue().getMainRib()));
  }

  @Nonnull
  @Override
  public Table<String, String, Set<Bgpv4Route>> getBgpRoutes() {
    return _bgpRoutes;
  }

  @Override
  @Nonnull
  public Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnRoutes() {
    return _evpnRoutes;
  }

  @Nonnull
  private Table<String, String, Set<Bgpv4Route>> computeBgpRoutes() {
    Table<String, String, Set<Bgpv4Route>> table = HashBasedTable.create();

    _nodes.forEach(
        (hostname, node) ->
            node.getVirtualRouters()
                .forEach(
                    (vrfName, vr) -> {
                      table.put(hostname, vrfName, vr.getBgpRoutes());
                    }));
    return table;
  }

  @Nonnull
  private Table<String, String, Set<EvpnRoute<?, ?>>> computeEvpnRoutes() {
    Table<String, String, Set<EvpnRoute<?, ?>>> table = HashBasedTable.create();
    _nodes.forEach(
        (hostname, node) ->
            node.getVirtualRouters()
                .forEach(
                    (vrfName, vr) -> {
                      table.put(hostname, vrfName, vr.getEvpnRoutes());
                    }));
    return table;
  }

  @Nonnull
  private Table<String, String, Set<Layer2Vni>> computeVniSettings() {
    Table<String, String, Set<Layer2Vni>> result = HashBasedTable.create();
    for (Node node : _nodes.values()) {
      for (Entry<String, VirtualRouter> vr : node.getVirtualRouters().entrySet()) {
        result.put(
            node.getConfiguration().getHostname(), vr.getKey(), vr.getValue().getLayer2Vnis());
      }
    }
    return result;
  }

  @Override
  public Map<String, Configuration> getConfigurations() {
    return _configurations.get();
  }

  @Override
  public Map<String, Map<String, Fib>> getFibs() {
    return _fibs.get();
  }

  @Override
  public ForwardingAnalysis getForwardingAnalysis() {
    return _forwardingAnalysis.get();
  }

  @Nonnull
  @Override
  public Table<String, String, Set<Layer2Vni>> getLayer2Vnis() {
    return _vniSettings;
  }

  public Map<String, Node> getNodes() {
    return _nodes;
  }

  /**
   * Retrieve the {@link PrefixTracer} for each {@link VirtualRouter} after dataplane computation.
   * Map structure: Hostname -&gt; VRF name -&gt; prefix tracer.
   */
  public SortedMap<String, SortedMap<String, PrefixTracer>> getPrefixTracingInfo() {
    /*
     * Iterate over nodes, then virtual routers, and extract prefix tracer from each.
     * Sort hostnames and VRF names
     */
    return toImmutableSortedMap(
        _nodes,
        Entry::getKey,
        nodeEntry ->
            toImmutableSortedMap(
                nodeEntry.getValue().getVirtualRouters(),
                Entry::getKey,
                vrfEntry -> vrfEntry.getValue().getPrefixTracer()));
  }

  @Override
  public SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary() {
    /*
     * Iterate over nodes, then virtual routers, and extract prefix tracer from each.
     * Sort hostnames and VRF names
     */
    return toImmutableSortedMap(
        _nodes,
        Entry::getKey,
        nodeEntry ->
            toImmutableSortedMap(
                nodeEntry.getValue().getVirtualRouters(),
                Entry::getKey,
                vrfEntry -> vrfEntry.getValue().getPrefixTracer().summarize()));
  }

  @Override
  public SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> getRibs() {
    if (_ribs == null) {
      _ribs = computeRibs();
    }
    return _ribs;
  }
}
