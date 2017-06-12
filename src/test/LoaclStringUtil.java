package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoaclStringUtil {
	public static void main(String[] args) {
		String str =  "H:\\contarctFile\\支出合同+其他合同\\2017052506支出合同\\12060532_买卖合同一办公耗材\\att\\12060532.pdf";
		System.out.println(getCharacterPosition(str, 5, "\\\\"));
		
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
//        	System.out.println(slashMatcher.start()); 
           mIdx++;  
           //当"/"符号第三次出现的位置  
           if(mIdx == i){  
              break;  
           }  
        }  
        return slashMatcher.start();  
     }  
    public static String getBeforString(String string ,String character){
    	int i = getIndex(string);
		int a = getCharacterPosition(string,i,character);
		string = string.substring(0, a);
		return string;
    }
    
    /**
     * 获得倒数第二个斜扛的顺数为第几个
     * @param str
     * @return
     */
    private static int getIndex(String str){
    	return getCountSubString(str, "\\")-1;
    }
    
    
    private static int counter = 0;  
    
    public static int getCountSubString(String str,String subString)  
    {  
        if (str.indexOf(subString)==-1)  
        {  
            return 0;  
        }  
        else if(str.indexOf(subString) != -1)  
        {  
            counter++;  
            getCountSubString(str.substring(str.indexOf(subString)+subString.length()),subString);  
            return counter;  
        }  
        return 0;  
    }  

}
