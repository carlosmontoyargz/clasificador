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
		redirectAttributes.addAttribute("method", form.getMethod());
		redirectAttributes.addAttribute("numberOfClusters", form.getNumberOfClusters());
		return "redirect:/clasificador/" + filename;
	}

	@GetMapping("/{filename}")
	public String clasificar(@PathVariable String filename,
	                         @RequestParam String method,
	                         @RequestParam int numberOfClusters, Model model)
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
		return "resultado-clasificacion";
	}

	@ModelAttribute("normalizationMethods")
	public String[] getNormalizationMethods() { return normalizationMethods; }
}
