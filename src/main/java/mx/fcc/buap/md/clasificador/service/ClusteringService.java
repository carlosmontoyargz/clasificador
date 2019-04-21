package mx.fcc.buap.md.clasificador.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mx.fcc.buap.md.clasificador.domain.AttributeType;
import mx.fcc.buap.md.clasificador.domain.DataSet;
import mx.fcc.buap.md.clasificador.storage.StorageFileNotFoundException;
import mx.fcc.buap.md.clasificador.storage.StorageService;
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
	private final StorageService storageService;
	private final DataSetService dataSetService;

	public void kmeans(String filename)
	{
		try
		{
			DataSet dataSet = dataSetService.read(storageService.load(filename));
			AttributeType types = dataSet.getTypes();

			log.info("------------------- Original ------------------------------");
			log.info(dataSet);

			log.info("------------------- min-max ---------------------------------");
			log.info(dataSet.minMax(BigDecimal.ZERO, BigDecimal.ONE));

			log.info("------------------- zScore ---------------------------------");
			log.info(dataSet.zScore());

			log.info("------------------- Decimal Scaling ---------------------------------");
			log.info(dataSet.decimalScaling());
		}
		catch (IOException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}
}
