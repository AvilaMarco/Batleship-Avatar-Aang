package dev.racnar.shipbender.shared.error;

public class ShipbenderException extends RuntimeException {

    private final ErrorCode code;

    public ShipbenderException ( ErrorCode code, String detail ) {
        super(detail);
        this.code = code;
    }

    public ErrorCode code () {
        return code;
    }
}
