
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String str1="a,b,c,d|zxc";
		int i=str1.indexOf("|");
		String str2=str1.substring(0,i).toUpperCase();
		String[] strs=str1.split(",");
		System.out.println("");
	}

}
