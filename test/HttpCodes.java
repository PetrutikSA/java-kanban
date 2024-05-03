public enum HttpCodes {
    Error404 (404),
    Error406 (406),
    Complete200 (200),
    Complete201 (201);

    private final Integer code;

    HttpCodes(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
