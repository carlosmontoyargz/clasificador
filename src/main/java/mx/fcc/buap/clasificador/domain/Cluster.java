package mx.fcc.buap.clasificador.domain;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * @author Carlos Montoya
 * @since 24/04/2019
 */
public class Cluster extends DataSet
{
	@Getter
	private Row centroid;

	public Cluster(DataSet dataSet, Row centroid)
	{
		super(dataSet.getAttributeType(), dataSet.getRowSize(), dataSet.getColumnSize());
		this.centroid = centroid;
	}

	public BigDecimal distanceToCentroid(DataRow row)
	{
		return row.distance(centroid, getPrecision());
	}

	/**
	 * Asigna el centroide de este Cluster con el promedio de los DataRow que contiene, y
	 * retorna un booleano indicando si el nuevo centroide es diferente del anterior.
	 *
	 * @return si el centroide nuevo es diferente del anterior.
	 */
	public boolean recomputeCentroid()
	{
		Row averageRow = getAverageRow();
		if (centroid.equals(averageRow))
			return false;
		else
		{
			centroid = averageRow;
			return true;
		}
	}

	@Override
	public String toString()
	{
		return "Cluster{\n" +
				"centroid=" + centroid + "\n" +
				super.toString() +
				"} ";
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Cluster other = (Cluster) o;
		return centroid.equals(other.centroid);

	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + centroid.hashCode();
		return result;
	}
}
