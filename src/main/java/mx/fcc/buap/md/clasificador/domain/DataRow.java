package mx.fcc.buap.md.clasificador.domain;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import static java.math.BigDecimal.ZERO;

/**
 * @author Carlos Montoya
 * @since 14/03/2019
 */
@RequiredArgsConstructor
public class DataRow
{
	private final AttributeType types;
	private final BigDecimal[] attributes;

	public int size() { return attributes.length; }

	public BigDecimal get(int i) { return attributes[i]; }

	public void set(int i, BigDecimal n) { attributes[i] = n; }

	public DataRow minmax(DataRow minRow, DataRow maxRow, BigDecimal newMin, BigDecimal newMax)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = types.isNominal(i) ? attributes[i] :
					attributes[i]
							.subtract(minRow.attributes[i])
							.divide(
									maxRow.attributes[i].subtract(minRow.attributes[i]),
									//10,
									RoundingMode.HALF_UP)
							.multiply(newMax.subtract(newMin))
							.add(newMin);

		return new DataRow(types, normalized);
	}

	public DataRow zScore(DataRow avg, DataRow stddev)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = types.isNominal(i) || ZERO.equals(stddev.attributes[i]) ? attributes[i] :
					attributes[i]
							.subtract(avg.attributes[i])
							.divide(stddev.attributes[i], RoundingMode.HALF_UP);
		return new DataRow(types, normalized);
	}

	public DataRow decimalScaling(int[] j)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = types.isNominal(i) ? attributes[i] :
					attributes[i].movePointLeft(j[i]);

		return new DataRow(types, normalized);
	}

	@Override
	public String toString() { return Arrays.toString(attributes); }
}
