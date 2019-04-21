package mx.fcc.buap.md.clasificador.domain;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * @author Carlos Montoya
 * @since 20/04/2019
 */
@RequiredArgsConstructor
public class AttributeType
{
	private final int[] attributes;

	public boolean isNumerical(int column)
	{
		return attributes[column] == 0;
	}

	public boolean isNominal(int column)
	{
		return attributes[column] != 0;
	}

	public String toString()
	{
		return Arrays.toString(attributes);
	}

	public String header()
	{
		StringBuilder header = new StringBuilder();
		for (int att : attributes) header.append(att).append(" ");
		return header.toString();
	}
}
