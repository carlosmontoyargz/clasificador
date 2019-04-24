package mx.fcc.buap.clasificador.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Carlos Montoya
 * @since 24/04/2019
 */
@Data
public class Cluster
{
	private final DataRow centroid;
	private final Set<DataRow> rows = new HashSet<>();

	private static int PRECISION = 25;

	public void add(DataRow row)
	{
		rows.add(row);
	}

	public void remove(DataRow row)
	{
		rows.remove(row);
	}

	public BigDecimal distanceToCentroid(DataRow row)
	{
		return row.compareTo(centroid, PRECISION);
	}

	public DataRow computeCentroid()
	{
		return centroid;
	}
}
