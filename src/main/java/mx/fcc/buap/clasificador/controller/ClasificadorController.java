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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author Carlos Montoya
 * @since 19/04/2019
 */
@Controller
@RequestMapping("/clasificador")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Log4j2
public class ClasificadorController
{
	private final StorageService storageService;
	private final DataSetService dataSetService;

	private static final String[] normalizationMethods = new String[]
			{ "min-max", "z-score", "decimal-scaling" };

	@GetMapping("")
	public String parametrosClasificacion(@RequestParam String filename, Model model)
	{
		model.addAttribute("filename", filename);
		model.addAttribute("form", new ClusterForm());
		return "parametros-clasificacion";
	}

	@PostMapping("")
	public String parametrosClasificacionPost(@ModelAttribute("form") ClusterForm form,
	                                          @RequestParam("filename") String filename,
	                                          RedirectAttributes redirectAttributes)
	{
		MultipartFile centroids = form.getCentroids();
		if (centroids != null && !centroids.isEmpty())
		{
			storageService.store(centroids);
			redirectAttributes.addAttribute("centroids", centroids.getOriginalFilename());
		}
		else
		{
			redirectAttributes.addAttribute("numberOfClusters", form.getNumberOfClusters());
		}
		redirectAttributes.addAttribute("method", form.getMethod());
		return "redirect:/clasificador/" + filename;
	}

	@GetMapping("/{filename}")
	public String clasificar(@PathVariable String filename,
	                         @RequestParam String method,
	                         @RequestParam(required = false) Integer numberOfClusters,
	                         @RequestParam(required = false) String centroids,
	                         Model model)
	{
		try
		{
			DataSet dataSet = dataSetService.read(storageService.load(filename));
			log.info("Original:\n{}", dataSet);

			DataSet normalized;
			if (method.equals("min-max"))
				normalized = dataSet
						.minMax(BigDecimal.ZERO, BigDecimal.ONE);
			else if (method.equals("z-score"))
				normalized = dataSet
						.zScore();
			else if (method.equals("decimal-scaling"))
				normalized = dataSet
						.decimalScaling();
			else return "";

			ClusterSet clusters;
			if (centroids != null)
			{;
				clusters = normalized
						.kMeans(dataSetService
								.convertToRow(storageService
										.loadAsResource(centroids)
										.getFile().toPath()));
			}
			else
				clusters = normalized
						.kMeans(numberOfClusters);

			log.info("------------------- k-means ---------------------------------");
			log.info(clusters);

			storageService.store(clusters.toString(), filename + "-clusters.txt");

			model.addAttribute("filename", filename);
			model.addAttribute("resultFilename", filename + "-clusters.txt");
			model.addAttribute("clusters", clusters.toJson());
		}
		catch (IOException e) {
			throw new StorageFileNotFoundException("Could not read files: " + filename, e);
		}
		return "resultado-clasificacion";
	}

	@ModelAttribute("normalizationMethods")
	public String[] getNormalizationMethods() { return normalizationMethods; }
}
