package mx.fcc.buap.clasificador.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mx.fcc.buap.clasificador.domain.DataSet;
import mx.fcc.buap.clasificador.storage.StorageFileNotFoundException;
import mx.fcc.buap.clasificador.storage.StorageService;
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
public class KMeansService
{
	private final StorageService storageService;
	private final DataSetService dataSetService;

	public void kmeans(String filename)
	{
		try
		{
			DataSet dataSet = dataSetService.read(storageService.load(filename));

			log.info("------------------- Original ------------------------------");
			log.info(dataSet);

			log.info("------------------- min-max ---------------------------------");
			log.info(dataSet.minMax(BigDecimal.ZERO, BigDecimal.ONE));

			log.info("------------------- zScore ---------------------------------");
			log.info(dataSet.zScore());

			log.info("------------------- Decimal Scaling ---------------------------------");
			log.info(dataSet.decimalScaling());

			log.info("------------------- k-means ---------------------------------");
			dataSet
					.minMax(BigDecimal.ZERO, BigDecimal.ONE)
					.kMeans(5)
					.forEach(log::info);
		}
		catch (IOException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}
}
