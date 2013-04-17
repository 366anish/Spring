package org.springframework.web.servlet.handler;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.handler.IMethodIntercepterHolder;
import org.springframework.ui.Model;
/**
 * �������߼̳е�������
 * @author lehoon
 *
 */
public class BaseMethodInvokerIntercepter implements IMethodInvokerIntercepter {

	@Override
	public Object invokeHandlerMethod(Method handlerMethod, Object handler,
			HttpServletRequest request, HttpServletResponse response,
			Model model, IMethodIntercepterHolder chain) throws Exception {
		return chain.doChain(handlerMethod, handler, request, response, model);
	}
}
