package io.wink.tool.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RpcProvider {
    /**
     * 代理的接口类型
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 代理的接口名称
     */
    String interfaceName() default "";

    /**
     * 服务的版本号
     */
    String version() default "";
}
