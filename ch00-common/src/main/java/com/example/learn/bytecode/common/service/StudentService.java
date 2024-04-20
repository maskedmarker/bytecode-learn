package com.example.learn.bytecode.common.service;

public class StudentService {

    public String sayHello(String name) {
        System.out.println(this.getClass() + " sayHello");
        return "Hello " + name;
    }

    public Integer lengthOfName(String name) {
        System.out.println(this.getClass() + " lengthOfName");
        return name.length();
    }

    public void logout(String name) {
        System.out.println(this.getClass() + " logout");
    }

    private void initService() {
        System.out.println(this.getClass() + " initService");
    }
}
