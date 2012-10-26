package analytic;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Propery {
	/**
	 * 
	 * @param str  ����properites�ļ���
	 * @return ���������ݴ����Map��
	 */
	public Map<String, String> analytic(String str){
		Map<String, String> map=new HashMap<String, String>();
		Properties proper =new Properties();
		InputStream in=this.getClass().getClassLoader().getResourceAsStream(str);
		try {
			proper.load(in);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("��ȡ"+str+"ʧ��");
			e.printStackTrace();
		}
		Enumeration<Object> enumeration=proper.keys();
		while(enumeration.hasMoreElements()){
			String key =enumeration.nextElement().toString();
			String value = proper.getProperty(key);
			try {
				key=new String(key.getBytes("ISO8859-1"),"UTF-8");
				value=new String(value.getBytes("ISO8859-1"),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				System.out.println("ת��ʧ��");
				e.printStackTrace();
			}
			map.put(key, value);
		}
		return map;
	}
}
