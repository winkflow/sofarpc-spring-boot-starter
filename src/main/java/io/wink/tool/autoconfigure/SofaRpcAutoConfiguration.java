package io.wink.tool.autoconfigure;

import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.config.*;
import io.wink.tool.annotation.RpcConsumer;
import io.wink.tool.annotation.RpcProvider;
import io.wink.tool.property.SofaProperties;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(SofaProperties.class)
public class SofaRpcAutoConfiguration implements ApplicationContextAware {
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    SofaProperties sofaProperties;

    @Value("${spring.application.name}")
    private String appName;

    private Map<String, ConsumerConfig<?>> consumers = new HashMap<>();

    @PostConstruct
    public void init() throws Exception {
        publishRpcService();
        setRpcConsumer();
    }

    private void setRpcConsumer() throws Exception {
        if (this.appName == null || this.appName.equals("")) {
            throw new Exception("empty app name,please check spring.application.name");
        }
        ApplicationConfig applicationConfig = new ApplicationConfig().setAppName(appName);

        RegistryConfig registryConfig = new RegistryConfig()
                .setProtocol("zookeeper")
                .setAddress(sofaProperties.getZkHost() + ":" + sofaProperties.getZkPort());
        final String[] allBeans = applicationContext.getBeanDefinitionNames();
        for (String beanName : allBeans) {
            final Object bean = applicationContext.getBean(beanName);
            ReflectionUtils.doWithFields(bean.getClass(), field -> {
                RpcConsumer rpcConsumer = field.getAnnotation(RpcConsumer.class);
                if (rpcConsumer != null) {
                    Class type = field.getType();
                    String key = type.getName() + ":" + rpcConsumer.async();
                    if (!consumers.containsKey(key)) {
                        ConsumerConfig<?> consumerConfig = new ConsumerConfig<>()
                                .setApplication(applicationConfig)
                                .setInterfaceId(type.getName());
                        if (rpcConsumer.async())
                            consumerConfig.setInvokeType(RpcConstants.INVOKER_TYPE_CALLBACK);
                        consumerConfig.setRegistry(registryConfig)
                                .setTimeout(sofaProperties.getSofaTimeout());
                        consumers.put(key, consumerConfig);
                    }
                    try {
                        field.setAccessible(true);
                        field.set(bean, consumers.get(key).refer());
                        field.setAccessible(false);
                    } catch (Exception e) {
                        throw new BeanCreationException(beanName, e);
                    }
                }
            });
        }
    }

    private void publishRpcService() throws Exception {
        RegistryConfig registryConfig = new RegistryConfig()
                .setProtocol("zookeeper")
                .setAddress(sofaProperties.getZkHost() + ":" + sofaProperties.getZkPort());

        ServerConfig serverConfig = new ServerConfig()
                .setPort(sofaProperties.getSofaPort())
                .setDaemon(false);

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RpcProvider.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            RpcProvider rpcProvider = applicationContext.findAnnotationOnBean(entry.getKey(), RpcProvider.class);

            Class<?> interfaceType;
            if (rpcProvider.interfaceClass() != void.class) {
                interfaceType = rpcProvider.interfaceClass();
            } else if (StringUtils.hasText(rpcProvider.interfaceName())) {
                interfaceType = Class.forName(rpcProvider.interfaceName());
            } else {
                Class<?> targetClass = realTargetClass(bean);
                if (targetClass.getInterfaces().length == 0) {
                    throw new IllegalStateException("Failed to export remote rpcProvider class "
                            + bean.getClass().getName() +
                            ", cause: The @RpcService undefined interfaceClass or interfaceName," +
                            " and the rpcProvider class unimplemented any interfaces.");
                }
                interfaceType = targetClass.getInterfaces()[0];
            }
            ProviderConfig<?> providerConfig = new ProviderConfig<>()
                    .setInterfaceId(interfaceType.getName())
                    .setRef(bean)
                    .setServer(serverConfig)
                    .setRegistry(registryConfig);
            providerConfig.export();
        }
    }

    /**
     * 脱壳帮助方法，bean 有可能被 aop 拦截并代理
     *
     * @param bean rpc provider bean
     */
    private Class<?> realTargetClass(Object bean) {
        if (bean instanceof SpringProxy) {
            return AopUtils.getTargetClass(bean);
        }
        return bean.getClass();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }
}
