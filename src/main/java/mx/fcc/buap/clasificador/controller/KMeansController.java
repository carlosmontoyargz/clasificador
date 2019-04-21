package mx.fcc.buap.clasificador.controller;

import lombok.RequiredArgsConstructor;
import mx.fcc.buap.clasificador.service.KMeansService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Carlos Montoya
 * @since 19/04/2019
 */
@Controller
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping("/kmeans")
public class KMeansController
{
	private final KMeansService service;

	@GetMapping("/{filename}")
	public String kMeans(@PathVariable String filename, Model model)
	{
		service.kmeans(filename);

		model.addAttribute("filename", filename);
		return "kmeans";
	}
}
