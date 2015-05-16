package main;

enum ApiMethod {

    W3W("string"),
    POSITION("position");

    private String parameter;

    ApiMethod(String parameter) {
        this.parameter = parameter;
    }

    String getPath() {
        return name().toLowerCase();
    }

    String getParameter() {
        return parameter;
    }
}
