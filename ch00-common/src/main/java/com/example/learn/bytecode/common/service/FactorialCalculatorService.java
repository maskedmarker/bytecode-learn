package com.example.learn.bytecode.common.service;

public class FactorialCalculatorService {

    // 阶乘
    public long factorial (int i) {
        System.out.println(this.getClass() + " factorial " + i);

        if (i == 1) {
            return 1;
        } else {
            return i * factorial(i - 1);
        }
    }
}
