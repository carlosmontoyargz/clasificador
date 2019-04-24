package mx.fcc.buap.clasificador.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Carlos Montoya
 * @since 24/04/2019
 */
@Data
public class Row
{
	protected final BigDecimal[] attributes;

	/**
	 * Retorna el numero de atributos de este DataRow
	 * @return el numero de atributos de este DataRow
	 */
	public int size() { return attributes.length; }

	public BigDecimal get(int i) { return attributes[i]; }

	public void set(int i, BigDecimal n) { attributes[i] = n; }
}
