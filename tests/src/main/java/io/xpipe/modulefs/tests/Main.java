package io.xpipe.modulefs.tests;

public class Main {

    public static void main(String[] args) throws Exception {
        var ct = new CommonTests();
        for (var method : CommonTests.class.getDeclaredMethods()) {
            if (method.isSynthetic()) {
                continue;
            }

            System.out.println("Running " + method.getName());
            method.invoke(ct);
        }
    }
}
