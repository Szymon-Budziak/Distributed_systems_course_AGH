package space;

import java.util.regex.Pattern;

public class Job {
    private final String agencyName;
    private final String capability;
    private final String requestId;

    public Job(String agencyName, String capability, String requestId) {
        this.agencyName = agencyName;
        this.capability = capability;
        this.requestId = requestId;
    }

    public Job(String encoded) {
        String[] parts = encoded.split(Pattern.quote("."));

        this.agencyName = parts[0];
        this.capability = parts[1];
        this.requestId = parts[2];
    }

    public String getAgencyName() {
        return this.agencyName;
    }

    public String getCapability() {
        return this.capability;
    }

    public String getRequestId() {
        return this.requestId;
    }

    @Override
    public String toString() {
        return this.agencyName + '.' + this.capability + '.' + this.requestId;
    }
}