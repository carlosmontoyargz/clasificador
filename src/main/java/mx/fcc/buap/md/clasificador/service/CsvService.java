package mx.fcc.buap.md.clasificador.service;

import lombok.RequiredArgsConstructor;
import mx.fcc.buap.md.clasificador.domain.DataRow;
import mx.fcc.buap.md.clasificador.domain.DataSet;
import mx.fcc.buap.md.clasificador.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author Carlos Montoya
 * @since 19/04/2019
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CsvService
{
	private final StorageService storageService;
	private static final String separator = ",";

	public DataSet read(String filename) throws IOException
	{
		DataSet dataSet = new DataSet();
		try (BufferedReader br = new BufferedReader
				(new FileReader(storageService.load(filename).toFile())))
		{
			String line;
			while ((line = br.readLine()) != null)
				dataSet.add
				(
					new DataRow
					(
						Arrays
								.stream(line.split(separator))
								.map(this::convert)
								.toArray(BigDecimal[]::new)
					)
				);
		}
		return dataSet;
	}

	private BigDecimal convert(String s)
	{
		try { return new BigDecimal(s); }
		catch (NumberFormatException e) { return BigDecimal.ZERO; }
	}
}
