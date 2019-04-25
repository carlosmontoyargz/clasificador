package mx.fcc.buap.clasificador.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author Carlos Montoya
 * @since 24/04/2019
 */
@Data
public class Row
{
	protected final BigDecimal[] attributes;

	public static Row zeroRow(int size)
	{
		BigDecimal[] at = new BigDecimal[size];
		Arrays.fill(at, BigDecimal.ZERO);
		return new Row(at);
	}

	/**
	 * Retorna el numero de atributos de este DataRow
	 * @return el numero de atributos de este DataRow
	 */
	public int size() { return attributes.length; }

	public BigDecimal get(int i) { return attributes[i]; }

	public void set(int i, BigDecimal n) { attributes[i] = n; }

	@Override
	public boolean equals(Object o) // fixme add test
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Row row = (Row) o;

		return Arrays.equals(attributes, row.attributes);

	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(attributes);
	}
}
