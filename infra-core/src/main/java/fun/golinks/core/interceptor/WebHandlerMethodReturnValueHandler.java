package fun.golinks.core.interceptor;

import fun.golinks.core.model.R;
import fun.golinks.core.util.JsonUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletResponse;

public class WebHandlerMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return true;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest) throws Exception {
        if (returnValue instanceof R) {
            return;
        }
        mavContainer.setRequestHandled(true);
        HttpServletResponse httpServletResponse = webRequest.getNativeResponse(HttpServletResponse.class);
        if (httpServletResponse == null) {
            return;
        }
        httpServletResponse.setContentType("application/json; charset=UTF-8");
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.getWriter().write(JsonUtils.toJsonString(R.ok(returnValue)));
    }
}