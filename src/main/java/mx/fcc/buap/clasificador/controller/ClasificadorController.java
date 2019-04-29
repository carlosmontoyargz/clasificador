package mx.fcc.buap.clasificador.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mx.fcc.buap.clasificador.domain.ClusterSet;
import mx.fcc.buap.clasificador.domain.DataSet;
import mx.fcc.buap.clasificador.dto.ClusterForm;
import mx.fcc.buap.clasificador.service.DataSetService;
import mx.fcc.buap.clasificador.storage.StorageFileNotFoundException;
import mx.fcc.buap.clasificador.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Carlos Montoya
 * @since 19/04/2019
 */
@Controller
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/clasificador")
@Log4j2
public class ClasificadorController
{
	private final StorageService storageService;
	private final DataSetService dataSetService;

	@GetMapping("")
	public String getOptions(@RequestParam String filename, Model model)
	{
		ClusterForm clusterForm = new ClusterForm();
		List<String> methods = clusterForm.getMethods();
		methods.add("min-max");
		methods.add("z-score");
		methods.add("decimal-scaling");

		model.addAttribute("methods", methods);
		model.addAttribute("filename", filename);

		return "normalization";
	}

	@GetMapping("/{method}/{filename}")
	public String kMeans(@PathVariable String method,
	                     @PathVariable String filename,
	                     @RequestParam int numberOfClusters,
	                     Model model)
	{
		try
		{
			DataSet dataSet = dataSetService.read(storageService.load(filename));
			log.info("Original:\n{}", dataSet);

			ClusterSet clusters = null;
			if (method.equals("min-max"))
				clusters = dataSet
						.minMax(BigDecimal.ZERO, BigDecimal.ONE)
						.kMeans(numberOfClusters);
			else if (method.equals("z-score"))
				clusters = dataSet
						.zScore()
						.kMeans(numberOfClusters);
			else if (method.equals("decimal-scaling"))
				clusters = dataSet
						.decimalScaling()
						.kMeans(numberOfClusters);
			if (clusters == null) return "";

			log.info("------------------- k-means ---------------------------------");
			log.info(clusters);

			storageService.store(clusters.toString(), filename + "-clusters.txt");

			model.addAttribute("filename", filename);
			model.addAttribute("resultFilename", filename + "-clusters.txt");
			model.addAttribute("clusters", clusters.toJson());
		}
		catch (IOException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
		return "kmeans";
	}

	/*@PostMapping("")
	public String receiveClusterForm(@ModelAttribute ClusterForm form, @RequestParam String filename)
	{
		log.info(form);
		return "redirect:/" +
				form.getMethods().get(0) +
				"/" +
				filename +
				"?numberOfClusters=" + form.getNumberOfClusters();
	}*/
}
