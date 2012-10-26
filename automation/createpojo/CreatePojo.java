package createpojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreatePojo {

	/**
	 * 
	 * @param map
	 *            ��ŵ�table��Ϣ
	 * @param templatemap
	 *            ���ģ���ļ���
	 */
	public void createPOJO(Map<String, String> map,
			Map<String, String> templatemap) {
		Iterator<String> iterator = map.keySet().iterator();
		// ���private���
		List<String> filedtypeList = new LinkedList<String>();
		Set<String> set = new HashSet<String>();
		String createtemplate = templatemap.get("jdbc.createTemplate");
		String service = templatemap.get("jdbc.Service");
		String serviceimpl = templatemap.get("jdbc.ServiceImpl");
		String selectpackage = templatemap.get("jdbc.selectPackage");
		while (iterator.hasNext()) {
			String tablename = iterator.next();
			String str = map.get(tablename);
			String privatetype = "";
			String strs[] = str.split(",");
			// �������ͱ��滻֮ǰ����Ϣ
			String strs2[] = str.split(",");
			for (int i = 0; i < strs.length; i++) {
				if (strs[i].contains("CHAR") || strs[i].contains("TEXT")
						|| strs[i].equals("SYSNAME")) {
					privatetype = "private String";
					strs[i] = "String";
				} else if (strs[i].contains("INT")) {
					if (strs[i].equals("INTEGER")) {
						privatetype = "private Integer";
						strs[i] = "Integer";
					} else {
						privatetype = "private int";
						strs[i] = "int";
					}
				} else if (strs[i].equals("DECIMAL")
						|| strs[i].equals("NUMERIC")
						|| strs[i].equals("NUMBER")
						|| strs[i].contains("DOUBLE")) {
					privatetype = "private Double";
					strs[i] = "Double";
				} else if (strs[i].contains("DATE")
						|| strs[i].equals("TIMESTAMP")
						|| strs[i].equals("TIME")) {
					privatetype = "private Date";
					strs[i] = "Date";
				} else if (strs[i].contains("BINARY")||strs[i].equals("BLOB")) {
					privatetype = "private byte[]";
					strs[i] = "byte[]";
				} else if (strs[i].contains("BIT")) {
					privatetype = "private byte";
					strs[i] = "byte";
				} else if (strs[i].contains("FLOAT")) {
					privatetype = "private float";
					strs[i] = "float";
				} else {
					filedtypeList.add(privatetype + " " + strs[i]);
				}
			}
			this.setAndGet(strs, tablename, filedtypeList, selectpackage);
			set.add(tablename);
			try {
				// ����Ibatis��ģ��
				if (createtemplate != null && !createtemplate.equals("")) {
					Map<String, String> tempmap = new HashMap<String, String>();
					for (int i = 0; i < strs2.length; i = i + 2) {
						tempmap.put(strs2[i + 1], strs2[i]);
					}
					this.createXML(tablename, this.getClass().getClassLoader()
							.getResource(createtemplate).toURI(),
							selectpackage, tempmap, templatemap
									.get("jdbc.createType"));
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			filedtypeList.clear();
		}
		if((service!=null&&!service.equals(""))||(serviceimpl!=null&&!serviceimpl.equals("")))
		this.createDAO(set, service, serviceimpl, selectpackage);
	}

	/**
	 * 
	 * @param strs
	 *            ����ֶ�������
	 * @param tablename
	 *            ����
	 * @param list
	 *            ��ŵ�private���� ����set��get������java��
	 * @param selectpackage
	 *            ѡ�����ɵı�������ѡ�񸳿�ֵ
	 */
	private void setAndGet(String strs[], String tablename, List<String> list,
			String selectpackage) {
		String javaPath = "";
		// ����selectpackage����Ϊ�������ɵ��ļ������ִ���
		if (selectpackage != null && !selectpackage.equals("")) {
			selectpackage=selectpackage.toLowerCase();
			int i = selectpackage.indexOf("|");
			if (i != -1) {
				javaPath = new File("").getAbsolutePath() + "\\src\\"
						+ selectpackage.substring(i + 1).replace(".", "\\");
			} else {
					javaPath = new File("").getAbsolutePath() + "\\src\\com\\model";	
			}
		} else {
			javaPath = new File("").getAbsolutePath() + "\\src\\com\\model";
		}
		String fileJava = javaPath + "\\" + tablename + ".java";
		// x1�ж��Ƿ���Ҫ����java.util.Date��
		int x1 = 0;
		// File createXML=new File(fileXML);
		try {
			File createDirectory = new File(javaPath);
			File createFile = new File(fileJava);
			createDirectory.mkdirs();
			createFile.createNewFile();
			Writer javaW = new OutputStreamWriter(
					new FileOutputStream(fileJava));
			String packagename = new File(fileJava).getPath();
			int indexstart = packagename.indexOf("src");
			int indexend = packagename.lastIndexOf("\\");
			// ������ȡ�ַ���ʱ�����src\
			String tempname = packagename.substring(indexstart + 4, indexend);
			packagename = tempname.replace("\\", ".");
			javaW.write("package " + packagename + ";\n\n");
			for (String str : strs) {
				if (str.equals("Date"))
					x1++;
			}
			if (x1 != 0) {
				javaW.write("import java.util.Date;\n");
			}
			javaW.write("\npublic class " + tablename + "{\n\n");
			Iterator<String> iterator = list.iterator();
			while (iterator.hasNext()) {
				javaW.write("\t" + iterator.next() + ";\n");
			}
			javaW.write("\n");
			javaW.flush();
			// ѭ��������ֶ������͵�����
			for (int i = 0; i < strs.length; i = i + 2) {
				javaW.write("\tpublic void set"
						+ strs[i + 1].substring(0, 1).toUpperCase()
						+ strs[i + 1].substring(1, strs[i + 1].length()) + "("
						+ strs[i] + " " + strs[i + 1] + "){\n");
				javaW.write("\t\tthis." + strs[i + 1] + "=" + strs[i + 1]
						+ ";\n\t}\n\n");
				javaW.write("\tpublic " + strs[i] + " get"
						+ strs[i + 1].substring(0, 1).toUpperCase()
						+ strs[i + 1].substring(1, strs[i + 1].length())
						+ "(){\n" + "\t\treturn " + strs[i + 1] + ";\n\t}\n\n");
			}
			javaW.write("}");
			javaW.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param tablexml
	 *            ������xml����
	 * @param xmltemplate
	 *            ��ȡXMLģ���URI��ַ
	 * @param selectpackage
	 *            ���ɵ�xml��λ��
	 * @param map
	 *            ��ŵ��ֶ�������Ӧ������
	 * @param createtype
	 *            �Ƿ��Ibatisģ������:TYPE
	 */
	public void createXML(String tablexml, URI xmltemplate,
			String selectpackage, Map<String, String> map, String createtype) {
		File file = new File(xmltemplate);
		try {
			// ��ȡXMLģ��
			BufferedReader fileRead = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			// �����Ҫ���ɵ�ģ����Ϣ
			StringBuilder xmlSava = new StringBuilder();
			String xmlPath = "";
			// ����selectpackage����Ϊ��������ɵ��ļ������ִ���
			if (selectpackage != null && !selectpackage.equals("")) {
				selectpackage=selectpackage.toLowerCase();
				int i = selectpackage.indexOf("|");
				if (i != -1) {
					xmlPath = new File("").getAbsolutePath() + "\\src\\"
							+ selectpackage.substring(i + 1).replace(".", "\\");
				} else {
						xmlPath = new File("").getAbsolutePath() + "\\src\\com\\model";		
				}
			} else {
				xmlPath = new File("").getAbsolutePath() + "\\src\\com\\model";
			}
			String fileXML = xmlPath + "\\" + tablexml + ".xml";
			File createDirectory = new File(xmlPath);
			File createFile = new File(fileXML);
			createDirectory.mkdirs();
			createFile.createNewFile();
			// ���xml��Ϣ
			Writer javaW = new OutputStreamWriter(new FileOutputStream(fileXML));
			// ��Ŷ�ȡ��һ���ı���Ϣ
			String strLine = null;
			String strSave = "";
			while ((strLine = fileRead.readLine()) != null) {
				xmlSava.append(strLine + "\n");
			}
			strSave = xmlSava.toString();
			Map<String, String> replaceMap = new HashMap<String, String>();
			Iterator<String> iterator = map.keySet().iterator();
			if (createtype.equals("true")) {
				// �����Ҫ�滻�Ĺؼ��ַ�
				int x=xmlPath.indexOf("com");
				String temp=xmlPath.substring(x);
				replaceMap.put("##package##", temp.replace("\\", "."));
				replaceMap.put("##TT##", tablexml);
				replaceMap.put("##CLASS##", tablexml);
				replaceMap.put("##ALIAS##", tablexml.toLowerCase());
				replaceMap.put("##CachModel##", tablexml + "Cache");
				replaceMap.put("##SQLSELECTALL##", "SELECT * FROM "
						+ tablexml.toUpperCase() + " WHERE 1=1");
				replaceMap.put("##SQLCOUNT##", "SELECT COUNT(*) FROM "
						+ tablexml.toUpperCase() + " WHERE 1=1");
				String tempsql = "", insertrow = "", updaterow = "";
				int i = 1;
				while (iterator.hasNext()) {
					String str = iterator.next();
					tempsql += "#" + str + ":" + map.get(str) + "#,";
					insertrow += str + ",";
					updaterow += "," + str.toUpperCase() + " = #" + str + ":"
							+ map.get(str) + "# ";
					i++;
					if (i == 8) {
						i = 0;
						tempsql += "\n\t";
						insertrow += "\n\t";
						updaterow += "\n\t";
					}
				}	
				/*//�ж��Ƿ����\n\t�ַ��������ȡʱ�ǽ�ȡ��\n\t
				int insertnt=insertrow.lastIndexOf("\n\t");
				int tempnt=tempsql.lastIndexOf("\n\t");
				if(insertnt!=-1&&tempnt!=-1){
					replaceMap.put("##SQLINSERT##", "INSERT INTO "
							+ tablexml.toUpperCase() + "( "
							+ insertrow.toUpperCase()+ " )" +"\n\t"+"VALUES( "
							+ tempsql.substring(0) + " )");
				}else{
					replaceMap.put("##SQLINSERT##", "INSERT INTO "
							+ tablexml.toUpperCase() + "( "
							+ insertrow.toUpperCase() + " ) VALUES( "
							+ tempsql.substring(0, tempsql.length() - 1) + " )");
				}	*/
				replaceMap.put("##SQLINSERT##", "INSERT INTO "
						+ tablexml.toUpperCase() + "( "
						+ insertrow.toUpperCase().substring(0,insertrow.length()-1) + " ) VALUES( "
						+ tempsql.substring(0, tempsql.length() - 1) + " )");
				
				replaceMap.put("##SQLUPDATE##", "UPDATE "
						+ tablexml.toUpperCase() + " SET "
						+ updaterow.substring(1) + " WHERE 1=1");
				replaceMap.put("##SQLDEL##", "DELETE FROM "
						+ tablexml.toUpperCase());
				// ������Ҫ�滻������
				Iterator<String> replacekey = replaceMap.keySet().iterator();
				while (replacekey.hasNext()) {
					String str = replacekey.next();
					strSave = strSave.replace(str, replaceMap.get(str));
				}
				javaW.write(strSave);
				javaW.flush();
				javaW.close();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createDAO(Set<String> set, String service, String serviceimpl,
			String packagepath) {
		try {
			File[] namefiles = {
					new File(this.getClass().getClassLoader().getResource(
							service).toURI()),
					new File(this.getClass().getClassLoader().getResource(
							serviceimpl).toURI()) };
			String ServicePath = "", ServiceimplPath = "";
			// ����packagepath����Ϊ��������ɵ��ļ������ִ���
			if (packagepath != null && !packagepath.equals("")) {
				int i = packagepath.indexOf("|");
				if (i != -1) {
					ServicePath = new File("").getAbsolutePath() + "\\src\\"
							+ packagepath.substring(i + 1).replace(".", "\\")
							+ "\\Service";
					ServiceimplPath = new File("").getAbsolutePath()
							+ "\\src\\"
							+ packagepath.substring(i + 1).replace(".", "\\")
							+ "\\Service\\impl";
				} else {
					ServicePath = new File("").getAbsolutePath()
							+ "\\src\\" + packagepath.substring(i + 1).replace(".", "\\") + "\\Service";
					ServiceimplPath = new File("").getAbsolutePath()
							+ "\\src\\" + packagepath.substring(i + 1).replace(".", "\\") + "\\Service\\impl";
				}
			} else {
				ServicePath = new File("").getAbsolutePath()
						+ "\\src\\com\\Service";
				ServiceimplPath = new File("").getAbsolutePath()
						+ "\\src\\com\\Service\\impl";
			}
			// ���ڱ���Service��Serviceimpl�ļ�������һ��
			int i = 1;
			for (File file : namefiles) {
				BufferedReader fileRead = new BufferedReader(
						new InputStreamReader(new FileInputStream(file)));
				// ��Ŷ�ȡ��һ���ı���Ϣ
				String strLine = null;
				String strSave = "";
				StringBuilder LineSava = new StringBuilder();
				while ((strLine = fileRead.readLine()) != null) {
						LineSava.append(strLine + "\n");
				}
				strSave = LineSava.toString();
				Iterator<String> alltable = set.iterator();
				File createDirectory;
				File createFile;
				String filename = "", filepath = "";
				while (alltable.hasNext()) {
					String tablename = alltable.next();
					// �滻ģ���е�##CLASS##
					String strTT = strSave.replace("##CLASS##", tablename);
					// ʹͬ��tablename������Service��ServiceImpl
					if (i == 1) {
						filename = ServicePath + "\\" + tablename
								+ "Service.java";
						filepath = ServicePath;
					} else {
						filename = ServiceimplPath + "\\" + tablename
								+ "ServiceImpl.java";
						filepath = ServiceimplPath;
					}
					createDirectory = new File(filepath);
					createFile = new File(filename);
					createDirectory.mkdirs();
					createFile.createNewFile();
					String packagename = new File(filename).getPath();
					int indexstart = packagename.indexOf("src");
					int indexend = packagename.lastIndexOf("\\");
					// ������ȡ�ַ���ʱ�����src\
					String tempname = packagename.substring(indexstart + 4,
							indexend);
					packagename = tempname.replace("\\", ".");
					// ���xml��Ϣ
					Writer javaW = new OutputStreamWriter(new FileOutputStream(
							filename));
					javaW.write("package " + packagename + ";\n\n");
					javaW.write(strTT);
					javaW.flush();
					javaW.close();
				}
				i++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
