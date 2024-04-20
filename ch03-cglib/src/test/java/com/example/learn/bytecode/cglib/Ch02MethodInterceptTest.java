package com.example.learn.bytecode.cglib;

import com.example.learn.bytecode.common.service.StudentService;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.MethodInterceptor;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class Ch02MethodInterceptTest {

    private static final AtomicLong SPAN_ID = new AtomicLong(0);

    /**
     * 方法增强:返回固定值
     */
    @Test
    public void test0() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(StudentService.class);
        // 所有方法都返回同一个callback的固定值,可能发生类型转换异常
        enhancer.setCallback((FixedValue) () -> "Hello Tom!");
        StudentService proxy = (StudentService) enhancer.create();

        String res = proxy.sayHello(null);
        assertEquals("Hello Tom!", res);

        assertThrows(ClassCastException.class, () -> {
            Integer lengthOfName = proxy.lengthOfName(null);
        });
    }

    /**
     * 方法增强:最灵活的
     */
    @Test
    public void test1() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(StudentService.class);
        enhancer.setCallback((MethodInterceptor) (thisProxy, method, args, methodProxy) -> {
            // methodProxy可以让使用者自主选择是调用增强子类instance的同名方法还是被增强父类的方法

            // 添加方法调用追踪
            long spanId = SPAN_ID.addAndGet(1);
            System.out.println(String.format("spanId=%s 调用开始: 类-%s- 方法-%s 参数-%s", spanId, method.getDeclaringClass(), method.getName(), args));

            Object retVal;
            if (method.getReturnType() == Void.class) { // 如果方法签名的返回值为void,则调用被增强父类的方法
                methodProxy.invokeSuper(thisProxy, args);
                retVal = null; // 所有方法都要有返回值,只是方法签名的返回值为void的会被cglib忽略
            } else {
                retVal = methodProxy.invokeSuper(thisProxy, args);
            }

            System.out.println(String.format("spanId=%s 调用结束: 返回值-%s", spanId, retVal));

            return retVal;
        });

        StudentService proxyInstance = (StudentService) enhancer.create();

        // 验证有返回值的方法调用
        assertEquals("Hello Tom", proxyInstance.sayHello("Tom"));
        assertEquals(4, (int) proxyInstance.lengthOfName("Mary"));

        // 验证无返回值的方法调用
        proxyInstance.logout("zhangsan");
    }
}
