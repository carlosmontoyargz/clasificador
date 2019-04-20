package mx.fcc.buap.md.clasificador.domain;

import lombok.Getter;
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
@Getter
public class DataRow
{
	private final BigDecimal[] columns;

	public int size() { return columns.length; }

	public BigDecimal get(int i) { return columns[i]; }

	public void set(int i, BigDecimal n) { columns[i] = n; }

	public DataRow minmax(DataRow minRow, DataRow maxRow, BigDecimal newMin, BigDecimal newMax)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = columns[i]
					.subtract(minRow.columns[i])
					.divide
					(
						maxRow.columns[i]
								.subtract(minRow.columns[i]),
						RoundingMode.HALF_UP
					)
					.multiply(newMax.subtract(newMin))
					.add(newMin);

		return new DataRow(normalized);
	}

	public DataRow zScore(DataRow avg, DataRow stddev)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = ZERO.equals(stddev.columns[i]) ? columns[i] :
					columns[i]
							.subtract(avg.columns[i])
							.divide(stddev.columns[i], RoundingMode.HALF_UP);
		return new DataRow(normalized);
	}

	public DataRow decimalScaling(int[] j)
	{
		BigDecimal[] normalized = new BigDecimal[size()];
		for (int i = 0; i < normalized.length; i++)
			normalized[i] = columns[i].movePointLeft(j[i]);

		return new DataRow(normalized);
	}

	@Override
	public String toString() { return Arrays.toString(columns); }
}
