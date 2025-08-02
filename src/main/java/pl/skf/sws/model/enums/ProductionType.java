package pl.skf.sws.model.enums;

import lombok.Getter;

@Getter
public enum ProductionType {

    PISF_POLISH(0),
    POLISH(1),
    FOREIGN(2),
    UNKNOWN(999);

    private final int code;

    ProductionType(int code) {
        this.code = code;
    }

    public static ProductionType fromCode(int code) {
        for (ProductionType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown production code: " + code);
    }
}
