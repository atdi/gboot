package com.test;


public class TestPlugin {

    public static void main(String[] args) {
        System.out.println(new TestPlugin(2,3).add());
    }
    int a;
    int b;

    public TestPlugin(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int add() {
        return a + b;
    }

    public int diff() {
        return a - b;
    }
}