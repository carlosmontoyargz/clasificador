package mx.fcc.buap.clasificador.service;

import lombok.extern.log4j.Log4j2;
import mx.fcc.buap.clasificador.domain.Cluster;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * @author Carlos Montoya
 * @since 25/04/2019
 */
@Service
@Log4j2
public class ClusterService
{
	public int[] getSortedColumns(Set<Cluster> clusters, int columnSize)
	{
		BigDecimal[] distances = new BigDecimal[columnSize];
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
		log.info("Total sum standard mean distances : {}", Arrays.toString(distances));
		log.info("Sorted column indices : {}", Arrays.toString(sortedIndices));
		return sortedIndices;
	}
}
