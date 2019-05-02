package mx.fcc.buap.clasificador.domain;

import lombok.Getter;
import mx.fcc.buap.clasificador.tools.MathTools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

/**
 * @author Carlos Montoya
 * @since 14/03/2019
 */
public class DataRow extends Row
{
	@Getter private int indice;
	private final DataSet dataSet;

	private static final AtomicInteger counter = new AtomicInteger(0);

	private DataRow(DataSet dataSet, BigDecimal[] attributes, int indice)
	{
		super(attributes);
		this.indice = indice;
		this.dataSet = dataSet;
	}

	public DataRow(Row r, DataSet dataSet, int indice)
	{
		super(r.attributes);
		this.indice = indice;
		this.dataSet = dataSet;
	}

	/**
	 * Normaliza este DataRow mediante el metodo min-max, a partir de los parametros especificados,
	 * y retorna el resultado en un DataRow nuevo.
	 *
	 * @return Un DataRow nuevo con el resultado de la normalizacion
	 */
	public DataRow minmax(Row minRow, Row maxRow, BigDecimal newMin, BigDecimal newMax)
	{
		BigDecimal diffNewMinNewMax = newMax.subtract(newMin);
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
		{
			BigDecimal diffMinMax = maxRow.attributes[i].subtract(minRow.attributes[i]);
			normalized[i] = dataSet.isNominal(i) ? attributes[i] :
					attributes[i]
							.subtract(minRow.attributes[i])
							.divide(diffMinMax, dataSet.getPrecision(), RoundingMode.HALF_UP)
							.multiply(diffNewMinNewMax)
							.add(newMin)
							.stripTrailingZeros();
		}
		return new DataRow(dataSet, normalized, indice);
	}

	/**
	 * Normaliza este DataRow mediante el metodo z-score, a partir de los parametros especificados,
	 * y retorna el resultado en un DataRow nuevo.
	 *
	 * @return Un DataRow nuevo con el resultado de la normalizacion
	 */
	public DataRow zScore(Row avg, Row stddev)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = dataSet.isNominal(i) || ZERO.equals(stddev.attributes[i]) ? attributes[i] :
					attributes[i]
							.subtract(avg.attributes[i])
							.divide(stddev.attributes[i], dataSet.getPrecision(), RoundingMode.HALF_UP)
							.stripTrailingZeros();

		return new DataRow(dataSet, normalized, indice);
	}

	/**
	 * Normaliza este DataRow mediante el metodo decimal-scaling, a partir de los parametros especificados,
	 * y retorna el resultado en un DataRow nuevo.
	 *
	 * @return Un DataRow nuevo con el resultado de la normalizacion
	 */
	public DataRow decimalScaling(int[] j)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = dataSet.isNominal(i) ? attributes[i] :
					attributes[i]
							.movePointLeft(j[i])
							.stripTrailingZeros();;

		return new DataRow(dataSet, normalized, indice);
	}

	public BigDecimal distance(Row other)
	{
		BigDecimal squaredDistancesSum = ZERO;
		for (int i = 0; i < attributes.length; i++)
			squaredDistancesSum = squaredDistancesSum
					.add(
							distance(i, other.attributes[i])
									.pow(2));
		return MathTools
				.sqrt(squaredDistancesSum, dataSet.getPrecision());
	}

	private BigDecimal distance(int column, BigDecimal other)
	{
		Row rangeRow = dataSet.getRangeRow();

		if (other == null)
			return ONE;
		if (dataSet.isNominal(column))
			return attributes[column]
					.equals(other) ? ZERO : ONE;
		else
			return attributes[column]
					.subtract(other)
					.abs()
					.divide(rangeRow.attributes[column],
							dataSet.getPrecision(),
							RoundingMode.HALF_UP);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DataRow dataRow = (DataRow) o;

		return indice == dataRow.indice;
	}

	@Override
	public int hashCode() { return indice; }

	@Override
	public String toString()
	{
		return "{" +
				"indice=" + indice +
				", " + Arrays.toString(attributes) +
				'}';
	}
}
