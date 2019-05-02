package mx.fcc.buap.clasificador.domain;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import mx.fcc.buap.clasificador.tools.MathTools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

/**
 * @author Carlos Montoya
 * @since 13/03/2019
 */
@Data
@Log4j2
public class DataSet implements Iterable<DataRow>
{
	private final int id = idGenerator.incrementAndGet();

	private final List<DataRow> rows;
	private final AttributeType attributeType;
	private final int columnSize;

	private Row minRow;
	private Row maxRow;
	private Row rangeRow;

	private int precision = 25;
	private final AtomicInteger indiceGenerator = new AtomicInteger(0);
	private static final AtomicInteger idGenerator = new AtomicInteger(0);

	public DataSet(AttributeType type, int rowSize, int columnSize)
	{
		this.attributeType = type;
		this.columnSize = columnSize;
		this.rows = new ArrayList<>(rowSize);
	}

	private DataSet(AttributeType type, List<DataRow> rows)
	{
		this.attributeType = type;
		this.rows = rows;
		this.columnSize = rows.size() == 0 ? 0 : rows.get(0).size();
	}

	/**
	 * Agrega un Row a este DataSet, si el numero de columnas de ese Row
	 * es igual al numero de columnas de este DataSet.
	 *
	 * @param r El DataRow a agregar a este DataSet
	 */
	public void add(Row r)
	{
		if (r.size() == columnSize)
		{
			rows.add(r instanceof DataRow ?
					(DataRow) r :
					new DataRow(r, this, indiceGenerator.incrementAndGet()));
			minRow = null;
			maxRow = null;
			rangeRow = null;
		}
		else
			log.error("La instancia {} tiene un numero incorrecto de atributos: {}", r, r.size());
	}

	public Row get(int i) { return rows.get(i); }

	/**
	 * Clasifica este DataSet mediante el metodo k-means.
	 *
	 * @param k El numero de clusters
	 * @return El conjunto de clusters encontrados.
	 */
	public ClusterSet kMeans(int k) { return kMeans(new ClusterSet(this, k)); }

	public ClusterSet kMeans(Set<Row> centroids) { return kMeans(new ClusterSet(this, centroids)); }

	private ClusterSet kMeans(ClusterSet clusters)
	{
		do
		{
			clusters.clearAll();
			rows.forEach(clusters::assignRowToClosestCluster);
		}
		while (clusters.recomputeCentroids());
		return clusters;
	}

	/**
	 * Normaliza este DataSet mediante el metodo min-max, y retorna el resultado en un
	 * DataSet nuevo.
	 *
	 * @param newMin el nuevo minimo para todas las columnas
	 * @param newMax el nuevo maximo para todas las columnas
	 * @return El resultado de la normalizacion de este DataSet
	 */
	public DataSet minMax(BigDecimal newMin, BigDecimal newMax)
	{
		Row minRow = getMinRow();
		Row maxRow = getMaxRow();

		DataSet resultSet = new DataSet(
				attributeType,
				rows.stream()
						.map(row -> row
								.minmax(minRow, maxRow, newMin, newMax))
						.collect(Collectors.toList()));
		log.info("min-max:\n{}", resultSet);
		return resultSet;
	}

	public Row getMinRow()
	{
		if (minRow == null) minRow = computeMinRow();
		return minRow;
	}

	/**
	 * Calcula el minimo para cada atributo de este DataSet, y retorna el
	 * resultado en un Row nuevo. Si el atributo es de tipo nominal, entonces
	 * el resultado asignado para esa columna es 0
	 *
	 * @return Un Row con el minimo de cada atributo de este DataSet
	 */
	private Row computeMinRow()
	{
		BigDecimal[] min = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			min[i] = isNominal(i) ? ZERO :
					getColumnStream(i)
							.min(BigDecimal::compareTo).orElse(ZERO);
		log.debug("min: {}", Arrays.toString(min));

		return new Row(min);
	}

	public Row getMaxRow()
	{
		if (maxRow == null) maxRow = computeMaxRow();
		return maxRow;
	}

	/**
	 * Calcula el maximo para cada atributo de este DataSet, y retorna el
	 * resultado en un Row nuevo. Si el atributo es de tipo nominal, entonces
	 * el resultado asignado para esa columna es 0
	 *
	 * @return Un Row con el maximo de cada atributo de este DataSet
	 */
	private Row computeMaxRow()
	{
		BigDecimal[] max = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			max[i] = isNominal(i) ? ZERO :
					getColumnStream(i)
							.max(BigDecimal::compareTo).orElse(ZERO);
		log.debug("max: {}", Arrays.toString(max));

		return new Row(max);
	}

	public Row getRangeRow()
	{
		if (rangeRow == null) rangeRow = computeRangeRow();
		return rangeRow;
	}

	private Row computeRangeRow()
	{
		Row max = getMaxRow();
		Row min = getMinRow();

		BigDecimal[] ranges = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			ranges[i] = isNominal(i) ? ZERO:
					max.get(i).subtract(min.get(i));
		log.debug("ranges: {}", Arrays.toString(ranges));

		return new Row(ranges);
	}

	/**
	 * Normaliza este DataSet mediante el metodo z-score, y retorna el resultado en un
	 * DataSet nuevo.
	 *
	 * @return El resultado de la normalizacion de este DataSet
	 */
	public DataSet zScore()
	{
		Row average = computeAverageRow();
		Row standardDeviation = computeStandardDeviationRow(average);

		DataSet resultSet = new DataSet(
				attributeType,
				rows.stream()
						.map(row -> row
								.zScore(average, standardDeviation))
						.collect(Collectors.toList()));
		log.info("z-score:\n{}", resultSet);
		return resultSet;
	}

	/**
	 * Calcula el promedio para cada atributo de este DataSet, y retorna el
	 * resultado en un Row nuevo. Si el atributo es de tipo nominal, entonces
	 * el resultado asignado es la moda de esa columna.
	 *
	 * @return Un Row con el promedio de cada atributo de este DataSet
	 */
	public Row computeAverageRow()
	{
		BigDecimal[] avg = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			avg[i] = isNominal(i) ? mode(i) :
					average(i);

		log.debug("average: {}", Arrays.toString(avg));
		return new Row(avg);
	}

	private BigDecimal average(int column)
	{
		return getColumnStream(column)
				.reduce(ZERO, BigDecimal::add)
				.divide(new BigDecimal(rows.size()),
						precision, RoundingMode.HALF_UP);
	}

	private BigDecimal mode(int column)
	{
		BigDecimal[] columnValues = getColumnStream(column).toArray(BigDecimal[]::new);

		BigDecimal maxValue = ZERO;
		int maxCount = 0, i, j;
		for (i = 0; i < columnValues.length; ++i)
		{
			int count = 0;
			for (j = 0; j < columnValues.length; ++j)
				if (columnValues[j].equals(columnValues[i]))
					++count;

			if (count > maxCount) {
				maxCount = count;
				maxValue = columnValues[i];
			}
		}
		return maxValue;
	}

//	public Row computeStandardDeviationRow()
//	{
//		return computeStandardDeviationRow(computeAverageRow());
//	}

	/**
	 * Calcula la desviacion estandar para cada atributo de este DataSet, y retorna el
	 * resultado en un Row nuevo. Si el atributo es de tipo nominal, entonces
	 * el resultado asignado para esa columna es 0
	 *
	 * @return Un Row con la desviacion estandar de cada atributo de este DataSet
	 */
	private Row computeStandardDeviationRow(Row averageRow)
	{
		BigDecimal[] stdv = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
		{
			BigDecimal avgi = averageRow.get(i);
			stdv[i] = isNominal(i) ? ZERO :
					MathTools.sqrt(
							getColumnStream(i)
									.map(v -> v.subtract(avgi).pow(2))
									.reduce(ZERO, BigDecimal::add)
									.divide(new BigDecimal(rows.size()), RoundingMode.HALF_UP),
							precision);
		}
		log.debug("standard deviation: {}", Arrays.toString(stdv));
		return new Row(stdv);
	}

	/**
	 * Normaliza este DataSet mediante el metodo decimal-scaling, y retorna el resultado en un
	 * DataSet nuevo.
	 *
	 * @return El resultado de la normalizacion de este DataSet
	 */
	public DataSet decimalScaling()
	{
		int[] tenPowers = getMaxOrderMagnitude();
		DataSet resultSet = new DataSet(
				attributeType,
				rows.stream()
						.map(row -> row
								.decimalScaling(tenPowers))
						.collect(Collectors.toList()));
		log.info("decimal-scaling:\n{}", resultSet);
		return resultSet;
	}

	/**
	 * Obtiene el maximo orden de magnitud para cada atributo de este DataSet, y retorna
	 * el resultado en un arreglo de enteros. Si el atributo es de tipo nominal, entonces
	 * el resultado asignado para esa columna es 0
	 *
	 * @return un arreglo de enteros con los maximos ordenes de magnitud
	 */
	private int[] getMaxOrderMagnitude()
	{
		Row absMaxRow = computeAbsoluteMaxRow();
		int[] j = new int[columnSize];
		for (int i = 0; i < columnSize; i++)
		{
			if (isNumerical(i))
			{
				int tenPower = 0;
				BigDecimal n = absMaxRow.get(i);
				while (n.compareTo(ONE) > 0)
				{
					tenPower++;
					n = n.movePointLeft(1);
				}
				j[i] = tenPower;
			}
			else j[i] = 0;
		}
		log.debug("Positions to move: {}", Arrays.toString(j));
		return j;
	}

	/**
	 * Calcula el maximo absoluto para cada atributo de este DataSet, y retorna el
	 * resultado en un Row nuevo. Si el atributo es de tipo nominal, entonces
	 * el resultado asignado para esa columna es 0
	 *
	 * @return Un Row con el maximo absoluto de cada atributo de este DataSet
	 */
	private Row computeAbsoluteMaxRow()
	{
		BigDecimal[] absmax = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			absmax[i] = isNominal(i) ? ZERO :
					getColumnStream(i)
							.map(BigDecimal::abs)
							.max(BigDecimal::compareTo).orElse(ZERO);
		log.debug("Absolute maximum: {}", Arrays.toString(absmax));

		return new Row(absmax);
	}

	/**
	 * Retorna un Stream con los valores de la columna especificada.
	 *
	 * @param column numero de la columna a calcular
	 * @return un Stream con los valores de una columna especifica.
	 */
	private Stream<BigDecimal> getColumnStream(int column)
	{
		return rows.stream().map(row -> row.get(column));
	}

	public int getRowSize() { return rows.size(); }

	public void clear() { rows.removeIf(r -> true); }

	/**
	 * Retorna true si la columna especificada es atributo de tipo numerico.
	 *
	 * @param c El numero de columna
	 * @return Si el tipo de atributo es numerico.
	 */
	public boolean isNumerical(int c) { return attributeType.isNumerical(c); }

	/**
	 * Retorna true si la columna especificada es atributo de tipo nominal.
	 *
	 * @param c El numero de columna
	 * @return Si el tipo de atributo es nominal.
	 */
	public boolean isNominal(int c) { return attributeType.isNominal(c); }

	@Override
	public Iterator<DataRow> iterator() { return rows.iterator(); }

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DataSet dataRows = (DataSet) o;

		return id == dataRows.id;

	}

	@Override
	public int hashCode()
	{
		return id;
	}

	/**
	 * Representa este DataSet en un String
	 * @return la representacion textual de este DataSet
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		rows.forEach(r -> sb
				.append(r)
				.append("\n"));

		return "DataSet{\n" +
				"rowSize=" + rows.size() + "\n" +
				"columnSize=" + columnSize + "\n" +
				"attributeType=" + attributeType + "\n" +
				"rows={\n" + sb.toString() + "}" +
				'}';
	}
}
