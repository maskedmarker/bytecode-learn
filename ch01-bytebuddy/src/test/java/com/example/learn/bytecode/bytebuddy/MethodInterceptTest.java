package com.example.learn.bytecode.bytebuddy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MethodInterceptTest {

    @Test
    public void test0() throws IllegalAccessException, InstantiationException {
        DynamicType.Unloaded unloadedType = new ByteBuddy()
                .subclass(Object.class)
                .method(ElementMatchers.isToString())
                .intercept(FixedValue.value("Hello World ByteBuddy!"))
                .make();
        // At this point, our class is already created but not loaded into the JVM yet.
        // It is represented by an instance of DynamicType.Unloaded, which is a binary form of the generated type.

        // we need to load the generated class into the JVM before we can use it
        Class<?> dynamicType = unloadedType.load(getClass().getClassLoader()).getLoaded();

        assertEquals(dynamicType.newInstance().toString(), "Hello World ByteBuddy!");
    }
}
