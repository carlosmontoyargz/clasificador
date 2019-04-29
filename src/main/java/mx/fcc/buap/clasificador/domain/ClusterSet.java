package mx.fcc.buap.clasificador.domain;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Log4j2
public class ClusterSet
{
	private final Set<Cluster> clusters;

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
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		clusters.forEach(c -> sb.append(c).append("\n"));
		return "ClusterSet{" +
				"clusters={\n" + sb.toString() + "}" +
				'}';
	}
}
