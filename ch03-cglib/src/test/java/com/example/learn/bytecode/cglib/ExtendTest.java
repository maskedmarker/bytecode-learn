package com.example.learn.bytecode.cglib;

import com.example.learn.bytecode.common.service.PersonService;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.MethodInterceptor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ExtendTest {

    @Test
    public void test0() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(PersonService.class);
        // 所有方法都返回同一个callback的固定值,可能发生类型转换异常
        enhancer.setCallback((FixedValue) () -> "Hello Tom!");
        PersonService proxy = (PersonService) enhancer.create();

        String res = proxy.sayHello(null);
        assertEquals("Hello Tom!", res);

        assertThrows(ClassCastException.class, () -> {
            Integer lengthOfName = proxy.lengthOfName(null);
        });
    }

    @Test
    public void test1() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(PersonService.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            if (method.getDeclaringClass() != Object.class && method.getReturnType() == String.class) {
                return "Hello Tom!";
            } else {
                return proxy.invokeSuper(obj, args);
            }
        });

        PersonService proxy = (PersonService) enhancer.create();

        assertEquals("Hello Tom!", proxy.sayHello(null));
        int lengthOfName = proxy.lengthOfName("Mary");

        assertEquals(4, lengthOfName);
    }
}
