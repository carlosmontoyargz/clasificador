package mx.fcc.buap.clasificador.domain;

import java.math.BigDecimal;

/**
 * @author Carlos Montoya
 * @since 24/04/2019
 */
public class Cluster extends DataSet
{
	private Row centroid;

	public Cluster(DataSet dataSet, Row centroid)
	{
		super(dataSet.getAttributeType(), dataSet.getRowSize(), dataSet.getColumnSize());
		this.centroid = centroid;
	}

	public BigDecimal distanceToCentroid(DataRow row)
	{
		return row.compareTo(centroid, getPrecision());
	}

	public Row computeCentroid()
	{
		centroid = new Row(new BigDecimal[0]);
		return centroid;
	}
}
