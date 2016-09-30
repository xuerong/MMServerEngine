package com.mm.engine.framework.control.gm;

import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.tool.helper.BeanHelper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONStringer;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by a on 2016/9/29.
 */
public class GmSevlet extends HttpServlet {
    private GmService gmService;
    private String gmJsonStr;
    public void init() throws ServletException{
        gmService = BeanHelper.getServiceBean(GmService.class);
        Map<String, GmSegment> gmSegmentMap = gmService.getGmSegments();
        JSONObject jsonObject = new JSONObject();
        // 加个排序
        List<GmSegment> gmSegmentList = new ArrayList<>(gmSegmentMap.values());
        gmSegmentList.sort(new Comparator<GmSegment>() {
            @Override
            public int compare(GmSegment o1, GmSegment o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        //
        for(GmSegment gmSegment : gmSegmentList){
            JSONObject item = new JSONObject();
            item.put("id",gmSegment.getId());
            item.put("describe",gmSegment.getDescribe());
            JSONObject type = new JSONObject();
            int i=0;
            int nameLength = gmSegment.getParamsName().length;
            for(Class cls : gmSegment.getParamsType()){
                String name = "param";
                if(nameLength>i){
                    name=gmSegment.getParamsName()[i];
                }
                type.put("param"+i,name+"("+cls.getSimpleName()+")");
                i++;
            }
            item.put("type",type);
            jsonObject.put(gmSegment.getId(),item);
        }
        gmJsonStr = jsonObject.toString();
        System.out.println("init gmServlet");
    }
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            doGm(req, resp);
        }catch (Throwable e){
            resp.getWriter().write("error:"+e.getMessage());
        }
    }

    private void doGm(HttpServletRequest req, HttpServletResponse resp) throws Throwable{
        String oper = req.getParameter("oper");
        if(oper==null){
            throw new MMException("oper==null");
        }
        if(oper.equals("begin")){
            resp.getWriter().write(gmJsonStr);
        }else if(oper.equals("gmSubmit")){
            String gm = req.getParameter("gm");
            GmSegment gmSegment = gmService.getGmSegments().get(gm);
            if(gmSegment == null){
                throw new MMException("gmSegment is not exist,gm = "+gm);
            }
            // 参数转换
            Class[] paramTypes = gmSegment.getParamsType();
            Object[] param = new Object[paramTypes.length];
            int i=0;
            for(Class paramType:paramTypes){
                String p = req.getParameter("param"+i);
                if(p==null){
                    throw new MMException("param error, param"+i+" is not exist");
                }
                param[i] = stringToObject(p,paramType);
                i++;
            }
            Object ret = gmService.handle(gm,param);
            if(ret== null || ret.getClass() == Void.class || ret.getClass()==void.class){
                resp.getWriter().write("void");
            }else if(ret.getClass()==Map.class){
                resp.getWriter().write(mapToString((Map)ret));
            }else if(ret.getClass()==String.class){
                resp.getWriter().write(ret.toString());
            }else{
                resp.getWriter().write(ret.toString());
            }
        }else{
            throw new MMException("oper is error,oper="+oper);
        }
    }

    public Object stringToObject(String str,Class cls){
        if(str.length()==0 && cls != String.class){
            if(cls == boolean.class || cls == Boolean.class){
                str="false";
            }else{
                str="0";
            }
        }
        if(cls == int.class || cls == Integer.class){
            return Integer.parseInt(str);
        }else if(cls == long.class || cls == Long.class){
            return Long.parseLong(str);
        }else if(cls == float.class || cls == Float.class){
            return Float.parseFloat(str);
        }else if(cls == double.class || cls == Double.class){
            return Double.parseDouble(str);
        }else if(cls == char.class || cls == Character.class){
            return str.charAt(0);
        }else if(cls == byte.class || cls == Byte.class){
            return Byte.parseByte(str);
        }else if(cls == boolean.class || cls == Boolean.class){
            return Boolean.parseBoolean(str);
        }else if(cls == short.class || cls == Short.class){
            return Short.parseShort(str);
        }else if(cls == String.class){
            return str;
        }

        return str;
    }

    public String mapToString(Map map){
        JSONObject jsonObject = JSONObject.fromObject(map);
        return jsonObject.toString();
    }
}
