package mx.fcc.buap.md.clasificador.domain;

import lombok.Getter;
import mx.fcc.buap.md.clasificador.math.MathTools;

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
public class DataRow
{
	@Getter private int id;
	private final DataSet dataSet;
	private final BigDecimal[] attributes;

	private static final AtomicInteger counter = new AtomicInteger(0);

	public DataRow(DataSet dataSet, BigDecimal[] attributes)
	{
		this.id = counter.incrementAndGet();
		this.dataSet = dataSet;
		this.attributes = attributes;
	}

	public BigDecimal distance(DataRow other, int precision)
	{
		BigDecimal r = ZERO;
		for (int i = 0; i < attributes.length; i++)
			r = r.add( distance(i, other.attributes[i]).pow(2) );
		return MathTools.sqrt(r, precision);
	}

	private BigDecimal distance(int column, BigDecimal other)
	{
		if (other == null) return ONE;

		if (dataSet.isNominal(column))
			return attributes[column].equals(other) ? ZERO : ONE;
		else
			return attributes[column].subtract(other).abs(); // FIXME hay que dividir entre el rango
	}

	/**
	 * Normaliza este DataRow mediante el metodo min-max, a partir de los parametros especificados,
	 * y retorna el resultado en un DataRow nuevo.
	 *
	 * @return Un DataRow nuevo con el resultado de la normalizacion
	 */
	public DataRow minmax(DataRow minRow, DataRow maxRow, BigDecimal newMin, BigDecimal newMax, int precision)
	{
		BigDecimal diffNewMinNewMax = newMax.subtract(newMin);
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
		{
			BigDecimal diffMinMax = maxRow.attributes[i].subtract(minRow.attributes[i]);
			normalized[i] = dataSet.isNominal(i) ? attributes[i] :
					attributes[i]
							.subtract(minRow.attributes[i])
							.divide(diffMinMax, precision, RoundingMode.HALF_UP)
							.multiply(diffNewMinNewMax)
							.add(newMin)
							.stripTrailingZeros();
		}

		return new DataRow(dataSet, normalized);
	}

	/**
	 * Normaliza este DataRow mediante el metodo z-score, a partir de los parametros especificados,
	 * y retorna el resultado en un DataRow nuevo.
	 *
	 * @return Un DataRow nuevo con el resultado de la normalizacion
	 */
	public DataRow zScore(DataRow avg, DataRow stddev, int precision)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = dataSet.isNominal(i) || ZERO.equals(stddev.attributes[i]) ? attributes[i] :
					attributes[i]
							.subtract(avg.attributes[i])
							.divide(stddev.attributes[i], precision, RoundingMode.HALF_UP)
							.stripTrailingZeros();

		return new DataRow(dataSet, normalized);
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

		return new DataRow(dataSet, normalized);
	}

	/**
	 * Retorna el numero de atributos de este DataRow
	 * @return el numero de atributos de este DataRow
	 */
	public int size() { return attributes.length; }

	public BigDecimal get(int i) { return attributes[i]; }

	public void set(int i, BigDecimal n) { attributes[i] = n; }

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DataRow dataRow = (DataRow) o;

		return id == dataRow.id;
	}

	@Override
	public int hashCode() { return id; }

	@Override
	public String toString()
	{
		return "{" +
				"id=" + id +
				", " + Arrays.toString(attributes) +
				'}';
	}
}
