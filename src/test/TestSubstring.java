package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestSubstring {
	public static void main(String[] args) {
		String str =  "H:/contarctFile/支出合同+其他合同/2017052506支出合同/12060532_买卖合同一办公耗材/att/12060532.pdf;";
		int a = getCharacterPosition(str,5,"/");
		str = str.substring(0, a);
		System.out.println(str);//H:/contarctFile/支出合同+其他合同/2017052506支出合同/12060532_买卖合同一办公耗材
		
	}
	
	/** 
     * 读取字符串第i次出现特定符号的位置 
     * @param string 
     * @param i 
     * @return 
     */  
    public static int getCharacterPosition(String string ,int i,String character){  
        //这里是获取"/"符号的位置  
       // Matcher slashMatcher = Pattern.compile("/").matcher(string);  
         Matcher slashMatcher = Pattern.compile(character).matcher(string);  
        int mIdx = 0;  
        while(slashMatcher.find()) {  
           mIdx++;  
           //当"/"符号第三次出现的位置  
           if(mIdx == i){  
              break;  
           }  
        }  
        return slashMatcher.start();  
     }  

}
