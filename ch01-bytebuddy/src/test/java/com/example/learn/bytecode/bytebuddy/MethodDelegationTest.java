package com.example.learn.bytecode.bytebuddy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import org.junit.Test;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static org.junit.Assert.assertEquals;

public class MethodDelegationTest {

    /**
     * let’s delegate all invocations of sayHelloFoo() to sayHelloBar() using ByteBuddy‘s DSL
     */
    @Test
    public void test0() throws IllegalAccessException, InstantiationException {
        String r = new ByteBuddy()
                .subclass(Foo.class)
                .method(named("sayHelloFoo")
                        .and(isDeclaredBy(Foo.class)
                                .and(returns(String.class))))
                .intercept(MethodDelegation.to(Bar.class))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded()
                .newInstance()
                .sayHelloFoo();

        assertEquals(r, new Bar().sayHelloBar());
    }
}
