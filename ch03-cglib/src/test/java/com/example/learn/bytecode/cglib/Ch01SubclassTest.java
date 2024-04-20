package com.example.learn.bytecode.cglib;

import com.example.learn.bytecode.common.service.StudentService;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.FixedValue;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * 可以通过cglib的Enhancer类在runtime临时生成某个类的子类;已继承的方式来实现增强的.
 *
 * cglib底层是通过asm在内存中生成class字节码,然后通过ClassLoader.defineClass来定义子类
 */
public class Ch01SubclassTest {

    /**
     * Enhancer的预设前提是你打算增强父类,
     * 如果不打算对父类增强,则会报异常
     */
    @Test(expected = Exception.class)
    public void test0() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(StudentService.class);
        StudentService proxy = (StudentService) enhancer.create();
    }

    /**
     * 只有通过增强父类,才能正常使用Enhancer
     */
    @Test
    public void test1() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(StudentService.class);
        // 增强,任何方法都返回null
        enhancer.setCallback((FixedValue) () -> null); // 如果方法签名的返回类型的void,cglib框架会自动忽略Callback的返回值
        // 生成增强子类的instance
        StudentService proxyInstance = (StudentService) enhancer.create();

        // 验证是子类
        assertTrue(proxyInstance.getClass().getSuperclass() ==  StudentService.class); // 通过继承的方法来实现增强的

        // 验证方法返回都是null
        String sayHello = proxyInstance.sayHello("张三");
        assertEquals(null, sayHello);
    }

    /**
     * 查看增强子类的类属性和方法
     */
    @Test
    public void test2() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(StudentService.class);
        // 增强,任何方法都返回null
        Callback callback = (FixedValue) () -> null; // 如果方法签名的返回类型的void,cglib框架会自动忽略Callback的返回值
        enhancer.setCallback(callback);
        // 生成增强子类的instance
        StudentService proxyInstance = (StudentService) enhancer.create();

        Class<? extends StudentService> proxyClass = proxyInstance.getClass();

        Arrays.stream(proxyClass.getInterfaces()).forEach(System.out::println);
        System.out.println("------------------------------------------------------------------------------");

        Arrays.stream(proxyClass.getDeclaredFields()).map(Field::getName).forEach(System.out::println);
        System.out.println("------------------------------------------------------------------------------");

        Arrays.stream(proxyClass.getDeclaredMethods()).map(Method::getName).forEach(System.out::println);
        System.out.println("------------------------------------------------------------------------------");
        Arrays.stream(proxyClass.getDeclaredMethods()).forEach(System.out::println);
        System.out.println("------------------------------------------------------------------------------");

        Factory factory = (Factory) proxyInstance;
        Arrays.stream(factory.getCallbacks()).forEach(System.out::println);
        System.out.println("------------------------------------------------------------------------------");
    }
}
