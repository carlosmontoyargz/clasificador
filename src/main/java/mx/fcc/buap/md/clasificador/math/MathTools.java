package mx.fcc.buap.md.clasificador.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Carlos Montoya
 * @since 19/03/2019
 */
public class MathTools
{
	public static BigDecimal sqrt(BigDecimal A, final int SCALE)
	{
		BigDecimal x0 = BigDecimal.ZERO;
		BigDecimal x1 = new BigDecimal(Math.sqrt(A.doubleValue()));
		BigDecimal TWO = new BigDecimal(2);
		while (!x0.equals(x1))
		{
			x0 = x1;
			x1 = A.divide(x0, SCALE, RoundingMode.HALF_UP);
			x1 = x1.add(x0);
			x1 = x1.divide(TWO, SCALE, RoundingMode.HALF_UP);

		}
		return x1;
	}
}
