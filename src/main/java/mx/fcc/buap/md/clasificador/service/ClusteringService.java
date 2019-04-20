package mx.fcc.buap.md.clasificador.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mx.fcc.buap.md.clasificador.domain.DataSet;
import mx.fcc.buap.md.clasificador.storage.StorageFileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author Carlos Montoya
 * @since 19/04/2019
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Log4j2
public class ClusteringService
{
	private final CsvReader reader;

	public void kmeans(String filename)
	{
		DataSet dataSet;
		try {
			dataSet = reader.read(filename);
		}
		catch (IOException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}

		log.info("Original");
		dataSet.forEach(log::info);

		log.info("min max");
		dataSet.minMax(BigDecimal.ZERO, BigDecimal.ONE).forEach(log::info);
	}
}
