package mx.fcc.buap.md.clasificador.domain;

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

/**
 * @author Carlos Montoya
 * @since 13/03/2019
 */
@ToString
@Log4j2
public class DataSet implements Iterable<DataRow>
{
	private List<DataRow> rows;
	private int[] types;
	private int columnSize;

	public DataSet(int rowSize, int columnSize, int[] types)
	{
		this.rows = new ArrayList<>(rowSize);
		this.types = types;
		this.columnSize = columnSize;
	}

	private DataSet(List<DataRow> rows, int[] types)
	{
		this.rows = rows;
		this.types = types;
		if (rows.size() > 0) columnSize = rows.get(0).size();
	}

	public void add(DataRow r)
	{
		if (r.size() == columnSize)
			rows.add(r);
		else
			log.error("La instancia {} tiene un numero incorrecto de atributos: {}", r, r.size());
	}

	public DataSet minMax(BigDecimal newMin, BigDecimal newMax)
	{
		DataRow minRow = getMinRow();
		log.debug("minA: {}", minRow);

		DataRow maxRow = getMaxRow();
		log.debug("maxA: {}", maxRow);

		return new DataSet(rows.stream()
				.map(row -> row.minmax(minRow, maxRow, newMin, newMax))
				.collect(Collectors.toList()), types);
	}

	private DataRow getMinRow()
	{
		BigDecimal[] min = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			min[i] = getColumnValues(i)
					.min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
		return new DataRow(min);
	}

	private DataRow getMaxRow()
	{
		BigDecimal[] max = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			max[i] = getColumnValues(i)
					.max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
		return new DataRow(max);
	}

	public DataSet zScore()
	{
		DataRow averageRow = getAverageRow();
		log.debug("average: {}", averageRow);

		DataRow stddvtRow = getStandardDeviationRow(averageRow, 10);
		log.debug("standard deviation: {}", stddvtRow);

		return new DataSet(rows.stream()
				.map(row -> row.zScore(averageRow, stddvtRow))
				.collect(Collectors.toList()), types);
	}

	private DataRow getAverageRow()
	{
		BigDecimal[] avg = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			avg[i] = getColumnValues(i)
					.reduce(BigDecimal.ZERO, BigDecimal::add)
					.divide(new BigDecimal(rows.size()), RoundingMode.HALF_UP);
		return new DataRow(avg);
	}

	private DataRow getStandardDeviationRow(DataRow averageRow, int scale)
	{
		BigDecimal[] stdv = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
		{
			BigDecimal avgi = averageRow.get(i);
			stdv[i] = MathTools.sqrt(
					getColumnValues(i)
							.map(v -> v.subtract(avgi).pow(2))
							.reduce(BigDecimal.ZERO, BigDecimal::add)
							.divide(new BigDecimal(rows.size()), RoundingMode.HALF_UP),
					scale);
		}
		return new DataRow(stdv);
	}

	public DataSet decimalScaling()
	{
		DataRow absMaxRow = getAbsoluteMaxRow();
		log.debug("Absolute maximum: {}", absMaxRow);

		int[] tenPowers = getTenPowers(absMaxRow);
		log.debug("Positions to move: {}", Arrays.toString(tenPowers));

		return new DataSet(rows.stream()
				.map(row -> row.decimalScaling(tenPowers))
				.collect(Collectors.toList()), types);
	}

	private DataRow getAbsoluteMaxRow()
	{
		BigDecimal[] absmax = new BigDecimal[columnSize];
		for (int i = 0; i < columnSize; i++)
			absmax[i] = getColumnValues(i)
					.map(BigDecimal::abs)
					.max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
		return new DataRow(absmax);
	}

	private int[] getTenPowers(DataRow absMaxRow)
	{
		int[] j = new int[columnSize];
		for (int i = 0; i < columnSize; i++)
		{
			int tenPower = 0;
			BigDecimal n = absMaxRow.get(i);
			while (n.compareTo(BigDecimal.ONE) > 0)
			{
				tenPower++;
				n = n.movePointLeft(1);
			}
			j[i] = tenPower;
		}
		return j;
	}

	private Stream<BigDecimal> getColumnValues(int column)
	{
		return rows.stream().map(dataRow -> dataRow.get(column));
	}

	@Override
	public Iterator<DataRow> iterator() { return rows.iterator(); }
}
