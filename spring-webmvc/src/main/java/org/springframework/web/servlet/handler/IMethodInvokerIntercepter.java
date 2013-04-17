package org.springframework.web.servlet.handler;

/**
 * ���ڷ�����������
 * 
 * @author lehoon
 * 
 */

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;


public interface IMethodInvokerIntercepter {
	public Object invokeHandlerMethod(Method handlerMethod, Object handler,
			HttpServletRequest request, HttpServletResponse response,
			Model model, IMethodIntercepterHolder chain) throws Exception;

}
