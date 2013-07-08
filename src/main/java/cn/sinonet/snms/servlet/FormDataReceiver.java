package cn.sinonet.snms.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * 处理表单数据测试服务
 * 
 * @author lwz@2013/07/08
 *
 */
public class FormDataReceiver extends HttpServlet {

	
	public void init() throws ServletException {
		trace("FormDataReceiver inited!");
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		
		trace("FormDataReceiver GET request accepted!");
		PrintWriter out = response.getWriter();		
		
		out.println("ok!\n");
		out.println(request.getParameter("data"));
		System.out.println(">>> data: "+request.getParameter("data"));
		out.flush();
		out.close();
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
				
		trace("POST request accepted !");
		
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");		
		
		PrintWriter out = response.getWriter();
		
		String currentDirPath = getServletContext().getRealPath("FormImages");
		// 判断文件夹是否存在，不存在则创建
		File dirTest = new File(currentDirPath);
		if (!dirTest.exists()) {
			dirTest.mkdirs();
		}
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		//FIXME, 中文文件名时必须设置头编码
		upload.setHeaderEncoding("utf-8");
		
		try {
			List<?> items = upload.parseRequest(request);
			Map<String, Serializable> fields = new HashMap<String, Serializable>();
			Iterator<?> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField())
					//FIXME, 取中文值时必须用UTF-8
					fields.put(item.getFieldName(), item.getString("UTF-8"));
				else
					fields.put(item.getFieldName(), item);
				trace(" Got field: " + item.getFieldName());
			}
			
			//GET OTHER FIELD
			String data = (String)fields.get("data");
			trace("received data is: " + data);
			
			// 获取文件名并做处理
			FileItem uplFile = (FileItem) fields.get("upload");
			if(uplFile != null){
				String fileNameLong = uplFile.getName();
				fileNameLong = fileNameLong.replace('\\', '/');
				String[] pathParts = fileNameLong.split("/");
				String fileName = pathParts[pathParts.length - 1];			

				File pathToSave = new File(currentDirPath.replace('\\', '/'), fileName);
				uplFile.write(pathToSave);
				
				trace("file saved to: " + pathToSave.getAbsolutePath());
				
				out.println("ok");
			}else{
				trace("<<<< upload file missed!");
				out.println("filemissed");
			}			
			
		} catch (Exception ex) {			
				ex.printStackTrace();
				out.println("failed");
		}
		
		out.flush();
		out.close();
	}
	
	private void trace(String msg){
		System.out.println(">>> Msg: "+ msg);
	}
	
}
