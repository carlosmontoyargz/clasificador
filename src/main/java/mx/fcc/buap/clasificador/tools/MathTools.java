package mx.fcc.buap.clasificador.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Carlos Montoya
 * @since 19/03/2019
 */
public class MathTools
{
	public static BigDecimal sqrt(BigDecimal A, final int precision)
	{
		BigDecimal x0 = BigDecimal.ZERO;
		BigDecimal x1 = new BigDecimal(Math.sqrt(A.doubleValue()));
		BigDecimal TWO = new BigDecimal(2);
		while (!x0.equals(x1))
		{
			x0 = x1;
			x1 = A.divide(x0, precision, RoundingMode.HALF_UP);
			x1 = x1.add(x0);
			x1 = x1.divide(TWO, precision, RoundingMode.HALF_UP);
		}
		return x1;
	}
}
