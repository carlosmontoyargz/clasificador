package mx.fcc.buap.md.clasificador.domain;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mx.fcc.buap.md.clasificador.math.MathTools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

/**
 * @author Carlos Montoya
 * @since 13/03/2019
 */
@Getter
@ToString
@Log4j2
public class DataSet implements Iterable<DataRow>
{
	private final AttributeType types;
	private final List<DataRow> rows;
	private final int columnSize;

	private static final int MAX_SCALE = 20;

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

	public void add(DataRow r)
	{
		if (r.size() == columnSize) rows.add(r);
		else log.error("La instancia {} tiene un numero incorrecto de atributos: {}", r, r.size());
	}

	public DataSet minMax(BigDecimal newMin, BigDecimal newMax)
	{
		DataRow minRow = getMinRow();
		DataRow maxRow = getMaxRow();

		return new DataSet(
				types,
				rows.stream()
						.map(row ->
								row.minmax(minRow, maxRow, newMin, newMax))
						.collect(Collectors.toList()));
	}

	private DataRow getMinRow()
	{
		BigDecimal[] min = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			min[i] = types.isNominal(i) ? ZERO :
					getColumnValues(i)
							.min(BigDecimal::compareTo).orElse(ZERO);
		log.debug("min: {}", Arrays.toString(min));

		return new DataRow(types, min);
	}

	private DataRow getMaxRow()
	{
		BigDecimal[] max = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			max[i] = types.isNominal(i) ? ZERO :
					getColumnValues(i)
							.max(BigDecimal::compareTo).orElse(ZERO);
		log.debug("max: {}", Arrays.toString(max));

		return new DataRow(types, max);
	}

	public DataSet zScore()
	{
		DataRow average = getAverageRow();
		DataRow standardDeviation = getStandardDeviationRow(average);

		return new DataSet(
				types,
				rows.stream()
						.map(row ->
								row.zScore(average, standardDeviation))
						.collect(Collectors.toList()));
	}

	private DataRow getAverageRow()
	{
		BigDecimal[] avg = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			avg[i] = types.isNominal(i) ? ZERO :
					getColumnValues(i)
							.reduce(ZERO, BigDecimal::add)
							.divide(new BigDecimal(rows.size()), RoundingMode.HALF_UP);
		log.debug("average: {}", Arrays.toString(avg));

		return new DataRow(types, avg);
	}

	private DataRow getStandardDeviationRow(DataRow averageRow)
	{
		BigDecimal[] stdv = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
		{
			BigDecimal avgi = averageRow.get(i);
			stdv[i] = types.isNominal(i) ? ZERO :
					MathTools.sqrt(
							getColumnValues(i)
									.map(v -> v.subtract(avgi).pow(2))
									.reduce(ZERO, BigDecimal::add)
									.divide(new BigDecimal(rows.size()), RoundingMode.HALF_UP),
							MAX_SCALE);
		}
		log.debug("standard deviation: {}", Arrays.toString(stdv));

		return new DataRow(types, stdv);
	}

	public DataSet decimalScaling()
	{
		int[] tenPowers = getTenPowers();
		return new DataSet(types,
				rows.stream()
						.map(row ->
								row.decimalScaling(tenPowers))
						.collect(Collectors.toList()));
	}

	private int[] getTenPowers()
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

	private DataRow getAbsoluteMaxRow()
	{
		BigDecimal[] absmax = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			absmax[i] = types.isNominal(i) ? ZERO :
					getColumnValues(i)
							.map(BigDecimal::abs)
							.max(BigDecimal::compareTo).orElse(ZERO);
		log.debug("Absolute maximum: {}", Arrays.toString(absmax));

		return new DataRow(types, absmax);
	}

	private Stream<BigDecimal> getColumnValues(int column)
	{
		return rows.stream().map(row -> row.get(column));
	}

	@Override
	public Iterator<DataRow> iterator() { return rows.iterator(); }
}
