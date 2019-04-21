package mx.fcc.buap.md.clasificador.domain;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import mx.fcc.buap.md.clasificador.math.MathTools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
	@Getter private final AttributeType types;
	@Getter private final int columnSize;
	private final List<DataRow> rows;

	private static final int MAX_SCALE = 25;

	public DataSet(AttributeType types, int rowSize, int columnSize)
	{
		this.types = types;
		this.columnSize = columnSize;
		this.rows = new ArrayList<>(rowSize);
	}

	private DataSet(AttributeType types, List<DataRow> rows)
	{
		this.types = types;
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
				types,
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
			min[i] = types.isNominal(i) ? ZERO :
					getColumnStream(i)
							.min(BigDecimal::compareTo).orElse(ZERO);
		log.debug("min: {}", Arrays.toString(min));

		return new DataRow(types, min);
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
			max[i] = types.isNominal(i) ? ZERO :
					getColumnStream(i)
							.max(BigDecimal::compareTo).orElse(ZERO);
		log.debug("max: {}", Arrays.toString(max));

		return new DataRow(types, max);
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
				types,
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
			avg[i] = types.isNominal(i) ? ZERO :
					getColumnStream(i)
							.reduce(ZERO, BigDecimal::add)
							.divide(new BigDecimal(rows.size()), RoundingMode.HALF_UP);
		log.debug("average: {}", Arrays.toString(avg));

		return new DataRow(types, avg);
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
			stdv[i] = types.isNominal(i) ? ZERO :
					MathTools.sqrt(
							getColumnStream(i)
									.map(v -> v.subtract(avgi).pow(2))
									.reduce(ZERO, BigDecimal::add)
									.divide(new BigDecimal(rows.size()), RoundingMode.HALF_UP),
							MAX_SCALE);
		}
		log.debug("standard deviation: {}", Arrays.toString(stdv));

		return new DataRow(types, stdv);
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
				types,
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
			if (types.isNumerical(i))
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
			absmax[i] = types.isNominal(i) ? ZERO :
					getColumnStream(i)
							.map(BigDecimal::abs)
							.max(BigDecimal::compareTo).orElse(ZERO);
		log.debug("Absolute maximum: {}", Arrays.toString(absmax));

		return new DataRow(types, absmax);
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
				"types=" + types +
				", columnSize=" + columnSize +
				", rows={\n" + sb.toString() + "}" +
				'}';
	}
}
