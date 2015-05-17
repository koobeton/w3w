package main;

enum W3WApiMethod {

    W3W("string"),
    POSITION("position");

    private String parameter;

    W3WApiMethod(String parameter) {
        this.parameter = parameter;
    }

    String getPath() {
        return name().toLowerCase();
    }

    String getParameter() {
        return parameter;
    }
}
