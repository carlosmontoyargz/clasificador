package mx.fcc.buap.clasificador.service;

import lombok.extern.log4j.Log4j2;
import mx.fcc.buap.clasificador.domain.AttributeType;
import mx.fcc.buap.clasificador.domain.DataSet;
import mx.fcc.buap.clasificador.domain.Row;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Carlos Montoya
 * @since 19/04/2019
 */
@Service
@Log4j2
public class DataSetService
{
	private static final String separator = ",";

	/**
	 * Crea una instancia de DataSet a partir del archivo CSV pasado como parametro.
	 *
	 * @param file El path del archivo csv a cargar
	 * @return El DataSet creado
	 * @throws IOException Si ocurre un error durante la lectura del archivo CSV
	 */
	public DataSet read(Path file) throws IOException
	{
		int rows = 0, columns = 0;
		AttributeType types = null;

		Iterator<String> lineIterator = Files.readAllLines(file).iterator();
		if (lineIterator.hasNext()) rows =
				Integer.valueOf(lineIterator.next().split(separator)[0]);
		if (lineIterator.hasNext())
			columns = Integer.valueOf(lineIterator.next().split(separator)[0]);
		if (lineIterator.hasNext())
			types = new AttributeType(Arrays
					.stream(lineIterator.next().split(separator))
					.mapToInt(Integer::valueOf)
					.toArray());

		DataSet dataSet = new DataSet(types, rows, columns);
		while (lineIterator.hasNext())
			dataSet.add(new Row(
					Arrays.stream(lineIterator.next().split(separator))
							.map(this::convert)
							.toArray(BigDecimal[]::new)));
		return dataSet;
	}

	public Set<Row> convertToRow(Path path) throws IOException
	{
		Set<Row> result = new HashSet<>();
		for (String s : Files.readAllLines(path))
			result.add(new Row(
					Arrays.stream(s.split(separator))
							.map(this::convert)
							.toArray(BigDecimal[]::new)));
		log.info("Centroides leidos:\n{}", result);
		return result;
	}


	private BigDecimal convert(String s)
	{
		try { return new BigDecimal(s); }
		catch (NumberFormatException e) { return BigDecimal.ZERO; }
	}
}
