package mx.fcc.buap.clasificador.tools;

import lombok.extern.log4j.Log4j2;
import org.junit.Test;

@Log4j2
public class ColorToolsTest
{
	@Test
	public void getRandomColorRGB()
	{
		for (int i = 0; i < 20; i++)
			log.info(ColorTools.getRandomColorRGB());
	}
}