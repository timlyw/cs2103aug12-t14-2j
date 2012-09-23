
import static org.junit.Assert.assertEquals;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.junit.Before;
import org.junit.Test;


public class CommandProcessorTest {
	
	@Before 
	public void setUpEnvironment(){

	}

	@Test
	public void testcheckTimeFormat() {
		
		assertEquals(TimeExtractor.checkTimeFormat("12pm"), true);
		assertEquals(TimeExtractor.checkTimeFormat("3pm"), true);
		assertEquals(TimeExtractor.checkTimeFormat("4am"), true);
		assertEquals(TimeExtractor.checkTimeFormat("12.40pm"), true);
		assertEquals(TimeExtractor.checkTimeFormat("00:00"), true);
		assertEquals(TimeExtractor.checkTimeFormat("00:12"), true);
		assertEquals(TimeExtractor.checkTimeFormat("15:13"), true);
		
		assertEquals(TimeExtractor.checkTimeFormat("15pm"), false);
		assertEquals(TimeExtractor.checkTimeFormat("12.60pm"), false);
		assertEquals(TimeExtractor.checkTimeFormat("00:70"), false);
		assertEquals(TimeExtractor.checkTimeFormat("400"), false);
		assertEquals(TimeExtractor.checkTimeFormat("1600"), false);
		assertEquals(TimeExtractor.checkTimeFormat("27:00"), false);
	}
	
	@Test
	public void testProcessTime(){
		
		DateTimeFieldType[] types = {
            DateTimeFieldType.hourOfDay(),
            DateTimeFieldType.minuteOfHour(),
		};
		Partial expectedTime = null;
		expectedTime = new Partial(types, new int[] {12,4});
		assertEquals(TimeExtractor.processTime("12:04"),expectedTime );

		expectedTime = new Partial(types, new int[] {12,40});
		assertEquals(TimeExtractor.processTime("12:40"), expectedTime);
		
		expectedTime = new Partial(types, new int[] {0,0});
		assertEquals(TimeExtractor.processTime("00:00"), expectedTime);
		
		expectedTime = new Partial(types, new int[] {0,12});
		assertEquals(TimeExtractor.processTime("00:12"), expectedTime);
		
		expectedTime = new Partial(types, new int[] {15,13});
		assertEquals(TimeExtractor.processTime("15:13"), expectedTime);
		
		expectedTime = new Partial(types, new int[] {15,13});
		assertEquals(TimeExtractor.processTime("3.13pm"), expectedTime);
		
		expectedTime = new Partial(types, new int[] {4,0});
		assertEquals(TimeExtractor.processTime("4am"), expectedTime);
	}

}
