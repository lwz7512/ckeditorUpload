package ckeditor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.json.JSONArray;

import cn.sinonet.common.beans.Folder;

public class CKEditorUploadServlet extends HttpServlet {

	private static Log log = LogFactory.getLog(CKEditorUploadServlet.class);
	
	private static final long serialVersionUID = 1L;
	private static String baseDir;// CKEditor的根目录
	private static boolean debug = false;// 是否debug模式

	private static Hashtable<String, ArrayList<String>> allowedExtensions;// 允许的上传文件扩展名
	private static Hashtable<String, ArrayList<String>> deniedExtensions;// 阻止的上传文件扩展名
	private static SimpleDateFormat dirFormatter;// 目录命名格式:yyyyMM
//	private static SimpleDateFormat fileFormatter;// 文件命名格式:yyyyMMddHHmmssSSS
	
	private String contextPath;

	/**
	 * Servlet初始化方法
	 */
	public void init() throws ServletException {
				
		// 格式化目录和文件命名方式
		dirFormatter = new SimpleDateFormat("yyyyMM");
//		fileFormatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		// 从web.xml中获取根目录名称
		baseDir = getInitParameter("baseDir");
		
		if (baseDir == null)
			baseDir = "/UserFiles/";
		String realBaseDir = getServletContext().getRealPath(baseDir);
		File baseFile = new File(realBaseDir);
		if (!baseFile.exists()) {
			baseFile.mkdirs();
		}
		// 实例化允许的扩展名和阻止的扩展名
		allowedExtensions = new Hashtable<String, ArrayList<String>>(3);
		deniedExtensions = new Hashtable<String, ArrayList<String>>(3);
		// 从web.xml中读取配置信息
		allowedExtensions.put("File",
				stringToArrayList(getInitParameter("AllowedExtensionsFile")));
		deniedExtensions.put("File",
				stringToArrayList(getInitParameter("DeniedExtensionsFile")));

		allowedExtensions.put("Image",
				stringToArrayList(getInitParameter("AllowedExtensionsImage")));
		deniedExtensions.put("Image",
				stringToArrayList(getInitParameter("DeniedExtensionsImage")));
		
		if (debug)
			System.out
					.println("---- SimpleUploaderServlet initialization completed ----\r\n");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//save path for file url compose;		
		if(contextPath == null) contextPath = request.getContextPath();
		String typeStr = request.getParameter("Type");
		System.out.println("*** compose tree for: " + typeStr);			
				
		String currentDirPath = getServletContext().getRealPath(baseDir);		
		File repository = new File(currentDirPath);
		if (!repository.exists()) {
			repository.mkdirs();
		}
		
		Folder root = new Folder();				
		walk(repository, root);//compose tree...
		
		Folder result = null;
		for(Folder f : root.getChildren()){
			if(f.getId().equals(typeStr)){
				result = f;
				break;
			}
		}
		if(result == null){
			result = new Folder();
			System.out.println("*** Warning, type not found: " + typeStr);
		}
		JSONArray folders = JSONArray.fromObject(result);
		String fileStr = folders.toString();
		
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();
		out.println(fileStr);
		out.flush();
		out.close();
		
	}
	
	private void walk(File root, Folder folder) {

        File[] list = root.listFiles();

        for ( File f : list ) {
        	Folder child = new Folder();
        	child.setId(f.getName());
            child.setText(f.getName());
            String relativePath = createRelativePath(f.getAbsolutePath());
            child.setRelativePath(relativePath);
            
            folder.addChild(child);//add to parent
            
            if ( f.isDirectory() ) {
            	child.setCls("folder");//not workable
                child.setLeaf(false);
                walk(f, child);                
            }
            else {
            	child.setCls("leaf");//not workable
                child.setLeaf(true);
            }
        }
    }
	
	private String createRelativePath(String absPath){
		String[] files = absPath.split(baseDir);
		String relativePath = contextPath + baseDir + files[1];		
		return relativePath;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (debug)
			System.out.println("--- BEGIN DOPOST ---");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();
		// 从请求参数中获取上传文件的类型：File/Image/Flash
		String typeStr = request.getParameter("Type");
		if (typeStr == null) {
			typeStr = "File";
		}
		if (debug)
			System.out.println(typeStr);
		// 实例化dNow对象，获取当前时间
		Date dNow = new Date();
		// 设定上传文件路径
		String currentPath = baseDir + typeStr + "/"
				+ dirFormatter.format(dNow);
		// 获得web应用的上传路径
		String currentDirPath = getServletContext().getRealPath(currentPath);
		// 判断文件夹是否存在，不存在则创建
		File dirTest = new File(currentDirPath);
		if (!dirTest.exists()) {
			dirTest.mkdirs();
		}
		// 将路径前加上web应用名
		currentPath = request.getContextPath() + currentPath;
		if (debug)
			System.out.println(currentDirPath);
		// 文件名和文件真实路径
		String fileUrl = "";
		
		// 使用Apache Common组件中的fileupload进行文件上传
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List<?> items = upload.parseRequest(request);
			Map<String, Serializable> fields = new HashMap<String, Serializable>();
			Iterator<?> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField())
					fields.put(item.getFieldName(), item.getString());
				else
					fields.put(item.getFieldName(), item);
			}
			// CEKditor中file域的name值是upload
			FileItem uplFile = (FileItem) fields.get("upload");
			// 获取文件名并做处理
			String fileNameLong = uplFile.getName();
			fileNameLong = fileNameLong.replace('\\', '/');
			String[] pathParts = fileNameLong.split("/");
			String fileName = pathParts[pathParts.length - 1];
			// 获取文件扩展名
			String ext = getExtension(fileName);
			// 设置上传文件名
			//fileName = fileFormatter.format(dNow) + "." + ext;
			// 获取文件名(无扩展名)
			//String nameWithoutExt = getNameWithoutExtension(fileName);

			File pathToSave = new File(currentDirPath, fileName);
			fileUrl = currentPath + "/" + fileName;
			if (extIsAllowed(typeStr, ext)) {
				uplFile.write(pathToSave);
			} else {
				if (debug)
					System.out.println("无效的文件类型： " + ext);
			}
		} catch (Exception ex) {
			if (debug)
				ex.printStackTrace();
		}
		
		// CKEditorFuncNum是回调时显示的位置，这个参数必须有
		String callback = request.getParameter("CKEditorFuncNum");
		if(callback != null){
			out.println("<script type=\"text/javascript\">");
			out.println("window.parent.CKEDITOR.tools.callFunction(" + callback
					+ ",'" + fileUrl + "',''" + ")");
			out.println("</script>");
		}
		
		out.println("上传成功！<a href='javascript:window.history.back()'>返回</a>");
		out.flush();
		out.close();
		if (debug)
			System.out.println("--- END DOPOST ---");
	}

	/**
	 * 获取文件名的方法
	 */
//	private static String getNameWithoutExtension(String fileName) {
//		return fileName.substring(0, fileName.lastIndexOf("."));
//	}

	/**
	 * 获取扩展名的方法
	 */
	private String getExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".") + 1);
	}

	/**
	 * 字符串像ArrayList转化的方法
	 */

	private ArrayList<String> stringToArrayList(String str) {
		if (debug)
			System.out.println(str);
		String[] strArr = str.split("\\|");
		ArrayList<String> tmp = new ArrayList<String>();
		if (str.length() > 0) {
			for (int i = 0; i < strArr.length; ++i) {
				if (debug)
					System.out.println(i + " - " + strArr[i]);
				tmp.add(strArr[i].toLowerCase());
			}
		}
		return tmp;
	}

	/**
	 * 判断扩展名是否允许的方法
	 */
	private boolean extIsAllowed(String fileType, String ext) {
		ext = ext.toLowerCase();
		ArrayList<?> allowList = (ArrayList<?>) allowedExtensions.get(fileType);
		ArrayList<?> denyList = (ArrayList<?>) deniedExtensions.get(fileType);
		if (allowList.size() == 0) {
			if (denyList.contains(ext)) {
				return false;
			} else {
				return true;
			}
		}
		if (denyList.size() == 0) {
			if (allowList.contains(ext)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

}
