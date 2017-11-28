package logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import static ch.qos.logback.classic.Level.DEBUG_INT;
import static ch.qos.logback.classic.Level.ERROR_INT;
import static ch.qos.logback.classic.Level.INFO_INT;
import static ch.qos.logback.classic.Level.TRACE_INT;
import static ch.qos.logback.classic.Level.WARN_INT;

/**
 * @author im.
 */
public class GCPJsonLayout extends PatternLayout {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String doLayout(ILoggingEvent event) {
        String formattedMessage = super.doLayout(event);
        return doLayoutInternal(formattedMessage, event);
    }

    /**
     * For testing without having to deal wth the complexity of super.doLayout()
     * Uses formattedMessage instead of event.getMessage()
     */
    private String doLayoutInternal(String formattedMessage, ILoggingEvent event) {
        GCPLoggingEvent logEvent =
                new GCPLoggingEvent(formattedMessage, convertTimestamp(event.getTimeStamp()),
                        convertLevel(event.getLevel()), event.getThreadName());

        try {
            // Add a newline so that each JSON log entry is on its own line.
            // Note that it is also important that the JSON log entry does not span multiple lines.
            return objectMapper.writeValueAsString(logEvent) + "\n";
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    private static GCPLoggingTimestamp convertTimestamp(
            long millisSinceEpoch) {
        int nanos = ((int) (millisSinceEpoch % 1000)) * 1_000_000; // strip out just the milliseconds and convert to nanoseconds
        long seconds = millisSinceEpoch / 1000L; // remove the milliseconds
        return new GCPLoggingTimestamp(seconds, nanos);
    }

    private static String convertLevel(Level level) {
        switch (level.toInt()) {
            case TRACE_INT:
                return "TRACE";
            case DEBUG_INT:
                return "DEBUG";
            case INFO_INT:
                return "INFO";
            case WARN_INT:
                return "WARN";
            case ERROR_INT:
                return "ERROR";
            default:
                return null; /* This should map to no level in GCP Cloud Logging */
        }
    }

    /* Must be public for Jackson JSON conversion */
    public static class GCPLoggingEvent {
        private String message;
        private GCPLoggingTimestamp timestamp;
        private String thread;
        private String severity;

        public GCPLoggingEvent(String message, GCPLoggingTimestamp timestamp, String severity,
                               String thread) {
            super();
            this.message = message;
            this.timestamp = timestamp;
            this.thread = thread;
            this.severity = severity;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public GCPLoggingTimestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(GCPLoggingTimestamp timestamp) {
            this.timestamp = timestamp;
        }

        public String getThread() {
            return thread;
        }

        public void setThread(String thread) {
            this.thread = thread;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }
    }

    /* Must be public for JSON marshalling logic */
    public static class GCPLoggingTimestamp {
        private long seconds;
        private int nanos;

        public GCPLoggingTimestamp(long seconds, int nanos) {
            super();
            this.seconds = seconds;
            this.nanos = nanos;
        }

        public long getSeconds() {
            return seconds;
        }

        public void setSeconds(long seconds) {
            this.seconds = seconds;
        }

        public int getNanos() {
            return nanos;
        }

        public void setNanos(int nanos) {
            this.nanos = nanos;
        }

    }

    @Override
    public Map<String, String> getDefaultConverterMap() {
        return PatternLayout.defaultConverterMap;
    }
}