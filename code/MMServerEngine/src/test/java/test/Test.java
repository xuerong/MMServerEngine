package test;

import com.mm.engine.framework.tool.util.ObjectUtil;

/**
 * Created by a on 2016/8/24.
 */
public class Test {
    public static void main(String[] args){
        aaa("123",new Object[]{""});
    }
    public static void aaa(String a,Object... b){
        System.out.println(b.length);
    }
}
