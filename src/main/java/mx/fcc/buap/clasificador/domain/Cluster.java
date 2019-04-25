package mx.fcc.buap.clasificador.domain;

import lombok.Getter;
import lombok.Setter;
import mx.fcc.buap.clasificador.tools.ColorTools;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Carlos Montoya
 * @since 24/04/2019
 */
@Getter @Setter
public class Cluster extends DataSet
{
	private Row centroid;
	private String rgbColor = ColorTools.getRandomColorRGB();

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

	public Map<String, Object> getGraphMap()
	{
		Map<String, Object> map = new HashMap<>();
		map.put("data", arrayGraficacion());
		map.put("color", rgbColor);
		return map;
	}

	private Object[][] arrayGraficacion()
	{
		//String style = "point {size: 3; fill-color: " + rgbColor;
		Object[][] data = new Object[getRowSize()][];
		List<DataRow> rows = getRows();
		for (int i = 0; i < getRowSize(); i++)
			data[i] = new Object[] {
					rows.get(i).get(1),
					rows.get(i).get(2),
					rows.get(i).get(3),
					//style
			};
		return data;
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
