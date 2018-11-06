package io.wink.tool.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RpcConsumer {
    //是否异步
    boolean async() default true;

}
