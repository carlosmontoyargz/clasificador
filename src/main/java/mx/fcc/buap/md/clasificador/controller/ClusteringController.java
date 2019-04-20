package mx.fcc.buap.md.clasificador.controller;

import lombok.RequiredArgsConstructor;
import mx.fcc.buap.md.clasificador.service.ClusteringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Carlos Montoya
 * @since 19/04/2019
 */
@Controller
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ClusteringController
{
	private final ClusteringService service;

	@GetMapping("/kmeans/{filename}")
	public String kMeans(@PathVariable String filename, Model model)
	{
		service.kmeans(filename);

		model.addAttribute("filename", filename);
		return "kmeans";
	}
}
