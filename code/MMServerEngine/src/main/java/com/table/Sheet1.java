package com.table;
//Auto Generate File, Do NOT Modify!!!!!!!!!!!!!!!
public final class Sheet1{
	public static final Sheet1[] datas={
		new Sheet1("xiaoming",(int)12.0,(long)1.23,(float)3.21,java.sql.Timestamp.valueOf("2016-7-23 10:10:10")),
		new Sheet1("xiaoming",(int)13.0,(long)1.23,(float)3.21,java.sql.Timestamp.valueOf("2016-7-24 10:10:10"))
	};
	private String name;
	private int para1;
	private long para2;
	private float para3;
	private java.sql.Timestamp time;

	public Sheet1(String name,int para1,long para2,float para3,java.sql.Timestamp time){
		this.name=name;
		this.para1=para1;
		this.para2=para2;
		this.para3=para3;
		this.time=time;
	}
	public String getName(){return name;}
	public void setName(String name){this.name=name;}
	public int getPara1(){return para1;}
	public void setPara1(int para1){this.para1=para1;}
	public long getPara2(){return para2;}
	public void setPara2(long para2){this.para2=para2;}
	public float getPara3(){return para3;}
	public void setPara3(float para3){this.para3=para3;}
	public java.sql.Timestamp getTime(){return time;}
	public void setTime(java.sql.Timestamp time){this.time=time;}
}