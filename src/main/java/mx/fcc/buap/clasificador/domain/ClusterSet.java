package mx.fcc.buap.clasificador.domain;

import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Carlos Montoya
 * @since 25/04/2019
 */
@Log4j2
public class ClusterSet implements Iterable<Cluster>
{
	private final DataSet dataSet;
	private final Set<Cluster> clusters;

	public ClusterSet(DataSet dataSet, Set<Row> centroids)
	{
		this.dataSet = dataSet;
		this.clusters = createEmptyClusters(centroids);
	}

	public ClusterSet(DataSet dataSet, int k)
	{
		this.dataSet = dataSet;
		this.clusters = createEmptyClusters(getRandomCentroids(k));
	}

	private Set<Cluster> createEmptyClusters(Set<Row> centroids)
	{
		if (centroids == null || centroids.size() == 0)
			return Collections.emptySet();
		else
			return centroids
					.stream()
					.map(r -> new Cluster(dataSet, r))
					.collect(Collectors.toSet());
	}

	private Set<Row> getRandomCentroids(int k)
	{
		int dataSetSize = dataSet.getRowSize();
		if (dataSetSize < k) return Collections.emptySet();

		Set<Row> centroids = new HashSet<>(k);
		while (centroids.size() < k)
			centroids
					.add(dataSet
							.get( (int) (Math.random() * dataSetSize)) );
		return centroids;
	}

	public void assignRowToClosestCluster(DataRow row)
	{
		Cluster nearest = null;
		BigDecimal min = BigDecimal.valueOf(Long.MAX_VALUE);
		for (Cluster cluster : clusters)
		{
			BigDecimal distance = cluster.distanceToCentroid(row);
			if (distance.compareTo(min) < 0)
			{
				nearest = cluster;
				min = distance;
			}
		}
		if (nearest != null) nearest.add(row);
	}

	/**
	 * Recalcula los centroides de los clusters y retorna true si alguno de ellos
	 * cambio.
	 *
	 * @return si alguno de los centroides cambio
	 */
	public boolean recomputeCentroids()
	{
		return clusters.stream()
				.map(Cluster::recomputeCentroid)
				.filter(b -> b)
				.findFirst()
				.orElse(false);
	}

	public void clearAll() { clusters.forEach(DataSet::clear); }

	public List<Map<String, Object>> toJson()
	{
		if (clusters.isEmpty())
			return Collections.emptyList();

		int[] sortedColumns = getSortedColumns();
		return clusters.stream()
				.map(c -> c
						.toJson(sortedColumns[0],
								sortedColumns[1],
								sortedColumns[2]))
				.collect(Collectors.toList());
	}

	private int[] getSortedColumns()
	{
		AtomicInteger columnSize = new AtomicInteger(0);
		clusters.stream().findFirst().ifPresent(c -> columnSize.set(c.getColumnSize()));

		BigDecimal[] distances = new BigDecimal[columnSize.get()];
		Arrays.fill(distances, BigDecimal.ZERO);
		for (Cluster c : clusters)
			for (int i = 0; i < distances.length; i++)
				distances[i] = distances[i]
						.add(c.meanDistanceToCentroid());

		int[] sortedIndices = IntStream
				.range(0, distances.length)
				.boxed()
				.sorted(Comparator.comparing(i -> distances[i]))
				.mapToInt(ele -> ele)
				.toArray();
		log.info("Total sum mean distances : {}", Arrays.toString(distances));
		log.info("Sorted column indices : {}", Arrays.toString(sortedIndices));

		return sortedIndices;
		//return new int[]{2, 3, 21};
	}

	@Override
	public Iterator<Cluster> iterator() { return clusters.iterator(); }

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		clusters.forEach(c -> sb.append(c).append("\n"));
		return "ClusterSet{" +
				"clusters={\n" + sb.toString() + "}" +
				'}';
	}
}
