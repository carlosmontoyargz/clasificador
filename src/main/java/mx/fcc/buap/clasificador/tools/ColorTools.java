package mx.fcc.buap.clasificador.tools;

/**
 * @author Carlos Montoya
 * @since 24/04/2019
 */
public class ColorTools
{
	private static final String[] values = new String[]
			{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

	public static String getRandomColorRGB()
	{
		StringBuilder sb = new StringBuilder("#");
		for (int i = 0; i < 6; i++)
			sb.append(values[(int) (Math.random() * 16)]);
		return sb.toString();
	}
}
