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
 * 在callback中无法像正常编程中使用super关键字,所以通过特定方法名(invokeSuper)弥补缺少super关键字的功能缺陷.
 */
public class Ch03InvokeOrSuperTest {

    @Test
    public void test0() {
        FactorialCalculatorService factorialCalculatorService = new FactorialCalculatorService();
        assertEquals(6, factorialCalculatorService.factorial(3));
        assertEquals(24, factorialCalculatorService.factorial(4));
    }

    @Test
    public void test1() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(FactorialCalculatorService.class);
        enhancer.setCallback((MethodInterceptor) (thisProxy, method, args, methodProxy) -> methodProxy.invokeSuper(thisProxy, args));
        FactorialCalculatorService proxyInstance = (FactorialCalculatorService) enhancer.create();

        // 验证
        assertEquals(6, proxyInstance.factorial(3));
        System.out.println("----------------------------------------------");
        assertEquals(3628800, proxyInstance.factorial(10));
        System.out.println("----------------------------------------------");
        assertEquals(2432902008176640000L, proxyInstance.factorial(20));
    }


    private static final class FastFactorialCalculator implements MethodInterceptor {

        private static final long[] FAST_DICT = new long[5]; // {1, 1, 2, 6, 24, 120}

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            int i = (int) args[0];

            if (i >= 0 && i < FAST_DICT.length) {
                // cache miss load cache
                if (FAST_DICT[i-1] == 0) {
                    FAST_DICT[i-1] = (long) proxy.invokeSuper(obj, new Object[]{i}); // 在callback中无法像正常编程中使用super关键字,所以通过特定方法名弥补缺少super关键字的缺憾
                }

                FAST_DICT[i] = i * FAST_DICT[i-1];
            }

            return i == 1 ? FAST_DICT[1] : i * (long) proxy.invoke(obj, new Object[]{i - 1});
        }
    }
    @Test
    public void test2() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(FactorialCalculatorService.class);
        enhancer.setCallback(new FastFactorialCalculator());

        FactorialCalculatorService proxyInstance = (FactorialCalculatorService) enhancer.create();

        // 验证
        assertEquals(6, proxyInstance.factorial(3));
        System.out.println("----------------------------------------------");
        assertEquals(3628800, proxyInstance.factorial(10));
        System.out.println("----------------------------------------------");
        assertEquals(2432902008176640000L, proxyInstance.factorial(20));
    }

    @Test(expected = StackOverflowError.class)
    public void test3() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(StudentService.class);
        enhancer.setCallback((MethodInterceptor) (thisProxy, method, args, methodProxy) -> {
            // 如果要调用增强子类的方法,就用invoke(),
            return methodProxy.invoke(thisProxy, args);
        });

        StudentService proxyInstance = (StudentService) enhancer.create();

        // 验证有返回值的方法调用
        assertEquals("Hello Tom", proxyInstance.sayHello("Tom"));
        assertEquals(4, (int) proxyInstance.lengthOfName("Mary"));

        // 验证无返回值的方法调用
        proxyInstance.logout("zhangsan");
    }
}
