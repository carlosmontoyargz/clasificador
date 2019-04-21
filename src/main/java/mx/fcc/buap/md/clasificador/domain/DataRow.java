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

	public DataRow minmax(DataRow minRow, DataRow maxRow, BigDecimal newMin, BigDecimal newMax, int scale)
	{
		BigDecimal diffNewMinNewMax = newMax.subtract(newMin);
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
		{
			BigDecimal diffMinMax = maxRow.attributes[i].subtract(minRow.attributes[i]);
			normalized[i] = types.isNominal(i) ? attributes[i] :
					attributes[i]
							.subtract(minRow.attributes[i])
							.divide(diffMinMax, scale, RoundingMode.HALF_UP)
							.multiply(diffNewMinNewMax)
							.add(newMin)
							.stripTrailingZeros();
		}

		return new DataRow(types, normalized);
	}

	public DataRow zScore(DataRow avg, DataRow stddev, int scale)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = types.isNominal(i) || ZERO.equals(stddev.attributes[i]) ? attributes[i] :
					attributes[i]
							.subtract(avg.attributes[i])
							.divide(stddev.attributes[i], scale, RoundingMode.HALF_UP)
							.stripTrailingZeros();
		return new DataRow(types, normalized);
	}

	public DataRow decimalScaling(int[] j)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = types.isNominal(i) ? attributes[i] :
					attributes[i]
							.movePointLeft(j[i])
							.stripTrailingZeros();;

		return new DataRow(types, normalized);
	}

	@Override
	public String toString() { return Arrays.toString(attributes); }
}
