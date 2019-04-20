package mx.fcc.buap.md.clasificador.domain;

import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mx.fcc.buap.md.clasificador.service.MathTools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Carlos Montoya
 * @since 13/03/2019
 */
@ToString
@Log4j2
public class DataSet implements Iterable<DataRow>
{
	private int columns;
	private List<DataRow> dataRows;

	public DataSet() { dataRows = new ArrayList<>(); }

	private DataSet(List<DataRow> rows)
	{
		dataRows = rows;
		if (rows.size() > 0) columns = rows.get(0).size();
	}

	public void add(DataRow i)
	{
		if (dataRows.size() > 0)
		{
			if (i.size() == columns) dataRows.add(i);

			else log.error("La instancia tiene un numero incorrecto de atributos {}", i);
		}
		else
		{
			columns = i.size();
			dataRows.add(i);
		}
	}

	public DataSet minMax(BigDecimal newMin, BigDecimal newMax)
	{
		BigDecimal[] minA = new BigDecimal[columns];
		BigDecimal[] maxA = new BigDecimal[columns];
		for (int i = 0; i < columns; i++)
		{
			minA[i] = min(i);
			maxA[i] = max(i);
		}
		log.debug("minA: {}", Arrays.toString(minA));
		log.debug("maxA: {}", Arrays.toString(maxA));

		return new DataSet(dataRows.stream()
				.map(dataRow -> dataRow.minMax(minA, maxA, newMin, newMax))
				.collect(Collectors.toList()));
	}

	public DataSet zScore()
	{
		BigDecimal[] avg = new BigDecimal[columns];
		BigDecimal[] stddev = new BigDecimal[columns];
		for (int i = 0; i < columns; i++)
		{
			avg[i] = avg(i);
			stddev[i] = stddev(i, avg[i], 10);
		}
		log.debug("average: {}", Arrays.toString(avg));
		log.debug("standard deviation: {}", Arrays.toString(stddev));

		return new DataSet(dataRows.stream()
				.map(dataRow -> dataRow.zScore(avg, stddev))
				.collect(Collectors.toList()));
	}

	public DataSet decimalScaling()
	{
		int[] j = new int[columns];
		for (int i = 0; i < columns; i++)
			j[i] = MathTools.tenPower(absoluteMax(i));
		log.debug("j: {}", Arrays.toString(j));


		return new DataSet(dataRows.stream()
				.map(dataRow -> dataRow.decimalScaling(j))
				.collect(Collectors.toList()));
	}

	private BigDecimal min(int i)
	{
		return dataRows.stream()
				.map(dataRow -> dataRow.get(i))
				.min(BigDecimal::compareTo)
				.orElse(BigDecimal.ZERO);
	}

	private BigDecimal max(int i)
	{
		return dataRows.stream()
				.map(dataRow -> dataRow.get(i))
				.max(BigDecimal::compareTo)
				.orElse(BigDecimal.ZERO);
	}

	private BigDecimal absoluteMax(int i)
	{
		return dataRows.stream()
				.map(dataRow -> dataRow.get(i).abs())
				.max(BigDecimal::compareTo)
				.orElse(BigDecimal.ZERO);
	}

	private BigDecimal avg(int i)
	{
		return dataRows.stream()
				.map(dataRow -> dataRow.get(i))
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.divide(new BigDecimal(dataRows.size()), RoundingMode.HALF_UP);
	}

	private BigDecimal stddev(int i, BigDecimal avg, int scale)
	{
		return MathTools.sqrt(dataRows.stream()
				.map(dataRow -> dataRow
						.get(i)
						.subtract(avg)
						.pow(2))
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.divide(new BigDecimal(dataRows.size()), RoundingMode.HALF_UP), scale);
	}

	@Override
	public Iterator<DataRow> iterator()
	{
		return dataRows.iterator();
	}
}
