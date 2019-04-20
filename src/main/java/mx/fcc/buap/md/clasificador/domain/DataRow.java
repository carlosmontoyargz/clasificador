package mx.fcc.buap.md.clasificador.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * @author Carlos Montoya
 * @since 14/03/2019
 */
@RequiredArgsConstructor
@Getter
public class DataRow
{
	private final BigDecimal[] atributes;

	public DataRow minMax(BigDecimal[] minA, BigDecimal[] maxA, BigDecimal newMinA, BigDecimal newMaxA)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = atributes[i]
					.subtract(minA[i])
					.divide(maxA[i].subtract(minA[i]), RoundingMode.HALF_UP)
					.multiply(newMaxA.subtract(newMinA))
					.add(newMinA);

		return new DataRow(normalized);
	}

	public DataRow zScore(BigDecimal[] avg, BigDecimal[] stddev)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = atributes[i]
					.subtract(avg[i])
					.divide(stddev[i], RoundingMode.HALF_UP);

		return new DataRow(normalized);
	}

	public DataRow decimalScaling(int[] j)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = atributes[i].movePointLeft(j[i]);

		return new DataRow(normalized);
	}

	public BigDecimal get(int i) { return atributes[i]; }

	public void set(int i, BigDecimal n) { atributes[i] = n; }

	public int size() { return atributes.length; }

	@Override
	public String toString() { return Arrays.toString(atributes); }
}
