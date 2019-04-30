package mx.fcc.buap.clasificador.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Carlos Montoya
 * @since 25/04/2019
 */
@Data
public class ClusterForm
{
	private String method;
	private int numberOfClusters;
	private MultipartFile file;
}
