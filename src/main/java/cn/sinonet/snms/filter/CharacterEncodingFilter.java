package cn.sinonet.snms.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class CharacterEncodingFilter implements Filter{
	
	public void init(FilterConfig arg0) throws ServletException {

	}
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		String encode = request.getCharacterEncoding();
		System.out.println(">>> request before encode : "+encode);
		System.out.println(">>> request before contenttype : "+request.getContentType());
		
		request.setCharacterEncoding("UTF-8");
		chain.doFilter(request, response);
	}
	
	public void destroy() {

	}
	
}
