package mx.fcc.buap.clasificador.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mx.fcc.buap.clasificador.domain.Cluster;
import mx.fcc.buap.clasificador.domain.DataSet;
import mx.fcc.buap.clasificador.service.DataSetService;
import mx.fcc.buap.clasificador.storage.StorageFileNotFoundException;
import mx.fcc.buap.clasificador.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;

/**
 * @author Carlos Montoya
 * @since 19/04/2019
 */
@Controller
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/kmeans")
@Log4j2
public class KMeansController
{
	private final StorageService storageService;
	private final DataSetService dataSetService;

	@GetMapping("/{filename}")
	public String kMeans(@PathVariable String filename, Model model)
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
			Set<Cluster> clusters = dataSet
					.minMax(BigDecimal.ZERO, BigDecimal.ONE)
					.kMeans(5);

			clusters.forEach(log::info);

			int i = 0;
			Object[][] data = new Object[ dataSet.getRowSize() ][];
			for (Cluster c : clusters)
				for (Object[] o : c.arrayGraficacion())
					data[i++] = o;

			model.addAttribute("filename", filename);
			model.addAttribute("data", data);
		}
		catch (IOException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
		return "kmeans";
	}
}
