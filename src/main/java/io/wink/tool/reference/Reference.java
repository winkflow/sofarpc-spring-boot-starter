package io.wink.tool.reference;

import com.alipay.sofa.rpc.context.RpcInvokeContext;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.invoke.SofaResponseCallback;
import com.alipay.sofa.rpc.core.request.RequestBase;
import io.wink.tool.support.Apply;

import java.util.concurrent.CompletableFuture;

public class Reference {

    public static <T> CompletableFuture<T> callBack(Apply apply) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        apply.apply(RpcInvokeContext.getContext().setResponseCallback(
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
