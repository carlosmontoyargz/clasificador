package mx.fcc.buap.clasificador.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Montoya
 * @since 25/04/2019
 */
@Data
public class ClusterForm
{
	private List<String> methods = new ArrayList<>();
	private String choosen;
	private int numberOfClusters;
}
