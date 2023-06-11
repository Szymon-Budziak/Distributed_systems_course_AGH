package space;

import java.util.regex.Pattern;

public class JobAck {
    private final String carrierName;
    private final String requestId;

    public JobAck(String carrierName, String requestId) {
        this.carrierName = carrierName;
        this.requestId = requestId;
    }

    public JobAck(String encoded) {
        String[] parts = encoded.split(Pattern.quote("."));

        this.carrierName = parts[0];
        this.requestId = parts[1];
    }

    @Override
    public String toString() {
        return this.carrierName + "." + this.requestId;
    }
}