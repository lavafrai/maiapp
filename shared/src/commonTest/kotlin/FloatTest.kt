import ru.lavafrai.maiapp.utils.toString
import kotlin.test.Test
import kotlin.test.assertEquals

class FloatTest {
    @Test
    fun padsShortFraction() {
        // Used to crash: "4.5" has a one-digit fraction
        assertEquals("4.50", 4.5.toString(2))
        assertEquals("4.00", 4.0.toString(2))
        assertEquals("5.00", 5.0.toString(2))
    }

    @Test
    fun roundsLongFraction() {
        assertEquals("4.57", 4.5678.toString(2))
        assertEquals("3.33", (10.0 / 3).toString(2))
        assertEquals("4.67", (14.0 / 3).toString(2))
        assertEquals("5.00", 4.999.toString(2))
    }

    @Test
    fun otherPrecisions() {
        assertEquals("4.6", 4.56.toString(1))
        assertEquals("4", 4.56.toString(0))
        assertEquals("0.05", 0.05.toString(2))
        assertEquals("-1.25", (-1.25).toString(2))
    }
}
