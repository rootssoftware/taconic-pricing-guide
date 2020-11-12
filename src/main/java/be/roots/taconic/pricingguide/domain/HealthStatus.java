package be.roots.taconic.pricingguide.domain;

public enum HealthStatus {

    MPF("Murine Pathogen Free<sup>TM</sup>"),
    RF("Restricted Flora<sup>TM</sup>"),
    OF("Opportunist Free<sup>TM</sup>"),
    EF("Excluded Flora"),
    DF("Defined Flora"),
    GF("Germ Free"),
    ;

    private final String description;

    HealthStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}