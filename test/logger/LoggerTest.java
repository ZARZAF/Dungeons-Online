package logger;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoggerTest {

    @Test
    public void testLogError() throws IOException {
        FileHandler fileHandler = new FileHandler("logs/test/test_error.log");
        Logger.logError(fileHandler, Level.SEVERE, "Test error message");

        assertTrue(new File("logs/test/test_error.log").exists());
    }

    @Test
    public void testLogErrorWithException() throws IOException {
        FileHandler fileHandler = new FileHandler("logs/test/test_error_with_exception.log");
        Exception exception = new Exception("Test exception");
        Logger.logError(fileHandler, Level.SEVERE, "Test error message with exception", exception);

        assertTrue(new File("logs/test/test_error_with_exception.log").exists());
    }

    @Test
    public void testErrorAndGameHandlers() {
        assertNotNull(Logger.error());
        assertNotNull(Logger.game());

        assertTrue(new File("logs/error.log").exists());
        assertTrue(new File("logs/game.log").exists());
    }

}
