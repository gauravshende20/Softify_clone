package com.harmonia.common.api.error;

public class HarmoniaException extends RuntimeException {
    private final ErrorCode code;
    private final int status;

    public HarmoniaException(ErrorCode code, int status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public HarmoniaException(ErrorCode code, int status, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;
    }

    public ErrorCode getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }

    public static HarmoniaException notFound(ErrorCode code, String message) {
        return new HarmoniaException(code, 404, message);
    }

    public static HarmoniaException conflict(ErrorCode code, String message) {
        return new HarmoniaException(code, 409, message);
    }

    public static HarmoniaException unauthorized(ErrorCode code, String message) {
        return new HarmoniaException(code, 401, message);
    }

    public static HarmoniaException forbidden(ErrorCode code, String message) {
        return new HarmoniaException(code, 403, message);
    }

    public static HarmoniaException badRequest(ErrorCode code, String message) {
        return new HarmoniaException(code, 400, message);
    }

    public static HarmoniaException serviceUnavailable(ErrorCode code, String message) {
        return new HarmoniaException(code, 503, message);
    }

    public static HarmoniaException serviceUnavailable(ErrorCode code, String message, Throwable cause) {
        return new HarmoniaException(code, 503, message, cause);
    }
}
