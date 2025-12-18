package com.mengcraft.util;

public class Bob {

    private void a() {
        System.out.println("a");
    }

    private void a(int i) {
        System.out.println("a(" +
                i +
                ")");
    }

    private void a(String a) {
        System.out.println("a(" +
                a +
                ")");
    }

    private void a(String a, String b) {
        System.out.println("a(" +
                a +
                ", " +
                b +
                ")");
    }

    String b() {
        return "b";
    }

    String b(String b) {
        return b;
    }

    int c() {
        return 111;
    }

    int c(int i) {
        return i;
    }

    String d(String a, String b) {
        return a + b;
    }

    int sum(int i, int i1) {
        return i + i1;
    }

    int sum(int i, int i1, int i2) {
        return i + i1 + i2;
    }
}
