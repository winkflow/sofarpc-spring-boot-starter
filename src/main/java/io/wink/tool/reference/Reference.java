package io.wink.tool.reference;

import com.alipay.sofa.rpc.context.RpcInvokeContext;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.invoke.SofaResponseCallback;
import com.alipay.sofa.rpc.core.request.RequestBase;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Reference {

    public static <T> CompletableFuture<T> callBack(Consumer consumer) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        consumer.accept(RpcInvokeContext.getContext().setResponseCallback(
                new SofaResponseCallback() {
                    @Override
                    public void onAppResponse(Object appResponse, String methodName, RequestBase request) {
                        completableFuture.complete((T) appResponse);
                    }

                    @Override
                    public void onAppException(Throwable throwable, String methodName, RequestBase request) {
                        completableFuture.completeExceptionally(throwable);
                    }

                    @Override
                    public void onSofaException(SofaRpcException sofaException, String methodName,
                                                RequestBase request) {
                        completableFuture.completeExceptionally(sofaException);
                    }
                }
        ));
        return completableFuture;
    }
}
