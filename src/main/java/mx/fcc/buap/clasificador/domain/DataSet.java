package mx.fcc.buap.clasificador.domain;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import mx.fcc.buap.clasificador.math.MathTools;

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
@Log4j2
public class DataSet implements Iterable<DataRow>
{
	@Getter private final AttributeType attributeType;
	@Getter private final int columnSize;
	private final List<DataRow> rows;

	private static final int MAX_SCALE = 25;

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
	 * Agrega un DataRow a este DataSet, si el numero de columnas de ese DataRow
	 * es igual al numero de columnas de este DataSet.
	 *
	 * @param r El DataRow a agregar a este DataSet
	 */
	public void add(DataRow r)
	{
		if (r.size() == columnSize) rows.add(r);
		else log.error("La instancia {} tiene un numero incorrecto de atributos: {}", r, r.size());
	}

	/**
	 * Clasifica este DataSet mediante el metodo k-means.
	 *
	 * @param k El numero de clusters
	 * @return El conjunto de clusters encontrados.
	 */
	public Set<Set<DataRow>> kMeans(int k)
	{
		return null;
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
		DataRow minRow = getMinRow();
		DataRow maxRow = getMaxRow();

		return new DataSet(
				attributeType,
				rows.stream()
						.map(row -> row
								.minmax(minRow, maxRow, newMin, newMax, MAX_SCALE))
						.collect(Collectors.toList()));
	}

	/**
	 * Calcula el minimo para cada atributo de este DataSet, y retorna el
	 * resultado en un DataRow nuevo. Si el atributo es de tipo nominal, entonces
	 * el resultado asignado para esa columna es 0
	 *
	 * @return Un DataRow con el minimo de cada atributo de este DataSet
	 */
	private DataRow getMinRow()
	{
		BigDecimal[] min = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			min[i] = isNominal(i) ? ZERO :
					getColumnStream(i)
							.min(BigDecimal::compareTo).orElse(ZERO);
		log.debug("min: {}", Arrays.toString(min));

		return new DataRow(this, min);
	}

	/**
	 * Calcula el maximo para cada atributo de este DataSet, y retorna el
	 * resultado en un DataRow nuevo. Si el atributo es de tipo nominal, entonces
	 * el resultado asignado para esa columna es 0
	 *
	 * @return Un DataRow con el maximo de cada atributo de este DataSet
	 */
	private DataRow getMaxRow()
	{
		BigDecimal[] max = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			max[i] = isNominal(i) ? ZERO :
					getColumnStream(i)
							.max(BigDecimal::compareTo).orElse(ZERO);
		log.debug("max: {}", Arrays.toString(max));

		return new DataRow(this, max);
	}

	/**
	 * Normaliza este DataSet mediante el metodo z-score, y retorna el resultado en un
	 * DataSet nuevo.
	 *
	 * @return El resultado de la normalizacion de este DataSet
	 */
	public DataSet zScore()
	{
		DataRow average = getAverageRow();
		DataRow standardDeviation = getStandardDeviationRow(average);

		return new DataSet(
				attributeType,
				rows.stream()
						.map(row -> row
								.zScore(average, standardDeviation, MAX_SCALE))
						.collect(Collectors.toList()));
	}

	/**
	 * Calcula el promedio para cada atributo de este DataSet, y retorna el
	 * resultado en un DataRow nuevo. Si el atributo es de tipo nominal, entonces
	 * el resultado asignado para esa columna es 0
	 *
	 * @return Un DataRow con el promedio de cada atributo de este DataSet
	 */
	private DataRow getAverageRow()
	{
		BigDecimal[] avg = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			avg[i] = isNominal(i) ? ZERO :
					getColumnStream(i)
							.reduce(ZERO, BigDecimal::add)
							.divide(new BigDecimal(rows.size()), RoundingMode.HALF_UP);
		log.debug("average: {}", Arrays.toString(avg));

		return new DataRow(this, avg);
	}

	/**
	 * Calcula la desviacion estandar para cada atributo de este DataSet, y retorna el
	 * resultado en un DataRow nuevo. Si el atributo es de tipo nominal, entonces
	 * el resultado asignado para esa columna es 0
	 *
	 * @return Un DataRow con la desviacion estandar de cada atributo de este DataSet
	 */
	private DataRow getStandardDeviationRow(DataRow averageRow)
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
							MAX_SCALE);
		}
		log.debug("standard deviation: {}", Arrays.toString(stdv));

		return new DataRow(this, stdv);
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
		return new DataSet(
				attributeType,
				rows.stream()
						.map(row -> row
								.decimalScaling(tenPowers))
						.collect(Collectors.toList()));
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
		DataRow absMaxRow = getAbsoluteMaxRow();
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
	 * resultado en un DataRow nuevo. Si el atributo es de tipo nominal, entonces
	 * el resultado asignado para esa columna es 0
	 *
	 * @return Un DataRow con el maximo absoluto de cada atributo de este DataSet
	 */
	private DataRow getAbsoluteMaxRow()
	{
		BigDecimal[] absmax = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			absmax[i] = isNominal(i) ? ZERO :
					getColumnStream(i)
							.map(BigDecimal::abs)
							.max(BigDecimal::compareTo).orElse(ZERO);
		log.debug("Absolute maximum: {}", Arrays.toString(absmax));

		return new DataRow(this, absmax);
	}

	/**
	 * Retorna un Stream de objetos BigDecimal con los valores de la columna especificada.
	 *
	 * @param column numero de la columna a calcular
	 * @return un Stream de los valores de una columna
	 */
	private Stream<BigDecimal> getColumnStream(int column)
	{
		return rows.stream().map(row -> row.get(column));
	}

	/**
	 * Retorna true si la columna especificada es atributo de tipo numerico.
	 *
	 * @param c El numero de columna
	 * @return Si el tipo de atributo es numerico.
	 */
	public boolean isNumerical(int c)
	{
		return attributeType.isNumerical(c);
	}

	/**
	 * Retorna true si la columna especificada es atributo de tipo nominal.
	 *
	 * @param c El numero de columna
	 * @return Si el tipo de atributo es nominal.
	 */
	public boolean isNominal(int c)
	{
		return attributeType.isNominal(c);
	}

	@Override
	public Iterator<DataRow> iterator() { return rows.iterator(); }

	/**
	 * Representa este DataSet en un String
	 * @return la representacion textual de este DataSet
	 */
	@Override
	public String toString()
	{
		AtomicInteger i = new AtomicInteger(0);
		StringBuilder sb = new StringBuilder();
		rows.forEach(r -> sb
				.append(i.incrementAndGet())
				.append(" - ")
				.append(r)
				.append("\n"));

		return "DataSet{" +
				"attributeType=" + attributeType +
				", columnSize=" + columnSize +
				", rows={\n" + sb.toString() + "}" +
				'}';
	}
}