package com.example.learn.bytecode.cglib;

import com.example.learn.bytecode.common.service.FactorialCalculatorService;
import com.example.learn.bytecode.common.service.StudentService;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

/**
 * 阐述cglib可以做到对self-invocation的到拦截
 *
 * 理解的关键点在于:java的多态
 * 在 Java 中，多态性是在字节码层面通过虚方法表（Virtual Method Table）来实现的.
 * 虚方法表是 Java 虚拟机在加载类的时候创建的一张表，用于存储类的方法信息，包括方法的地址等。
 *
 * 法的地址的确定: 本类虚表中找不到,再找父类的虚表.
 *
 */
public class Ch04SelfInvocationTest {

    private static class ClassA{
        public void method1() {
            System.out.println(this.getClass() + " method1 in ClassA");
        }

        public void method2() {
            System.out.println(this.getClass() + " method2 in ClassA");
        }

        public void method3() {
            System.out.println(this.getClass() + " method3 in ClassA");
            method1();
        }

        public void method4() {
            System.out.println(this.getClass() + " method4 in ClassA");
            method2();
        }

        public void method51() {
            System.out.println(this.getClass() + " method51 in ClassA");
            method1();
        }

        public void method512() {
            System.out.println(this.getClass() + " method512 in ClassA");
            method1();
            method2();
        }

        public void method6512() {
            System.out.println(this.getClass() + " method6512 in ClassA");
            method512();
        }
    }

    /**
     * 虽然单独看字节码文件,方法引用是固定的,
     * 但是在实际执行时,这些方法的引用地址会指向this所属实际类型的虚方法表中对应方法的地址(这些地址在类加载时才确定,并被改写,从而满足多态特性)
     *
     */
    private static class ClassB extends ClassA {
        public void method1() {
            System.out.println(this.getClass() + " method1 in ClassB");
        }

        public void method3() {
            System.out.println(this.getClass() + " method3 in ClassB");
            method1(); // 法引用指向的是 com.example.learn.bytecode.cglib.Ch04SelfInvocationTest.ClassB.method1
        }

        public void method4() {
            System.out.println(this.getClass() + " method4 in ClassB");
            method2(); // 方法引用指向的是 com.example.learn.bytecode.cglib.Ch04SelfInvocationTest.ClassA.method2
        }

        public void method5() {
            System.out.println(this.getClass() + " method5 in ClassB");
            super.method1(); // 方法引用指向的是 com.example.learn.bytecode.cglib.Ch04SelfInvocationTest.ClassA.method1
        }
    }

    private static class ClassC extends ClassA {
        public void method1() {
            System.out.println(this.getClass() + " method1 in ClassC");
        }

        public void method3() {
            System.out.println(this.getClass() + " method3 in ClassC");
            super.method3();
        }

        public void method4() {
            System.out.println(this.getClass() + " method4 in ClassC");
            super.method4();
        }
    }

    /**
     * 展示多态
     */
    @Test
    public void test0() {
        ClassA a = new ClassA();
        a.method3();
        a.method4();
        System.out.println("----------------------------------------------");

        ClassB b = new ClassB();
        b.method3();
        b.method4();
        b.method5();
        System.out.println("----------------------------------------------");

        ClassC c = new ClassC();
        c.method3(); // method3 in ClassC  -> method3 in ClassA -> method1 in ClassC
        c.method4(); // method4 in ClassC -> method4 in ClassA -> method2 in ClassA
    }

    /**
     * 为了简单化, 仅考虑public方法的aop
     */
    private static class ProxyA extends ClassA {
        public void method1() {
            System.out.println(this.getClass() + " method1 in ProxyA begin");
            super.method1();
            System.out.println(this.getClass() + " method1 in ProxyA end");
        }

        public void method2() {
            System.out.println(this.getClass() + " method2 in ProxyA begin");
            super.method2();
            System.out.println(this.getClass() + " method2 in ProxyA end");
        }

        public void methodX51() {
            System.out.println(this.getClass() + " methodX51 in ProxyA begin");
            super.method51();
            System.out.println(this.getClass() + " methodX51 in ProxyA end");
        }

        public void methodX6512() {
            System.out.println(this.getClass() + " methodX6512 in ProxyA begin");
            super.method6512();
            System.out.println(this.getClass() + " methodX6512 in ProxyA end");
        }
    }
    /**
     * 举例说明为什么多态可以满足self-invocation的aop
     *
     */
    @Test
    public void test1() {
        ProxyA proxyA = new ProxyA();

        // 保持真实类型
        proxyA.method1();
        System.out.println("----------------------------------------------");
        proxyA.method2();
        System.out.println("----------------------------------------------");

        System.out.println("***********************************************");
        // 保持目标类型
        ClassA a = proxyA;
        a.method1();
        System.out.println("----------------------------------------------");
        a.method2();
        System.out.println("----------------------------------------------");
        a.method51();
        System.out.println("----------------------------------------------");
        a.method6512();
        System.out.println("----------------------------------------------");
    }
}
