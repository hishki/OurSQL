package parser;
import java.util.HashMap;

import dbTypes.DBTypes;
import dbTypes.VARCHAR;
import table.*;

public class ConditionCalc {
	public static final int TYPE_VARCHAR = 0;
	public static final int TYPE_INT = 1;
	
	private static final int NOT_FUNC=-1;
	private static final int MAX=1;
	private static final int MIN=2;
	private static final int AVG=3;
	private static final int SUM=4;
	private static final int COUNT=5;

	
	private DBObject mydb;
	private DBObject mydb2;
	private boolean tables;	//more than one table then is true
	private HashMap<String, DBTypes> myrow;
	private HashMap<String, DBTypes> myrow2;
	private String table_name1, table_name2;
	public ConditionCalc(DBObject inputdb){
		this.mydb = inputdb;
		tables = false;
		myrow = mydb.getRow();
	}
	public ConditionCalc(DBObject inputdb, DBObject inputdb2, String table_name1, String table_name2){
		this.mydb = inputdb;
		this.mydb2 = inputdb2;
		this.table_name1 = table_name1;
		this.table_name2 = table_name2;
		if(this.mydb2==null)
			tables = false;
		else{
			tables = true;
			
			myrow2 = mydb2.getRow();
		}
		myrow = mydb.getRow();
	}
	
	
	public boolean calculate(String tuple){//Calculate Tuple Condition
		tuple = Clean(tuple);
		//System.err.println("!!"+tuple);
		if(tuple.equals("FALSE"))
			return false;
		if(tuple.equals("TRUE"))
			return true;
		if(tuple.charAt(0)=='('){
			int j = 0;
			int depth = 1;
			while(depth!=0){
				j++;
				if(j>=tuple.length())
					{System.err.println("MisMatch Parenthesis\n <"+tuple+ ">");return false;}
				if(tuple.charAt(j)=='(')
					depth++;
				if(tuple.charAt(j)==')')
					depth--;
			}
			if(j==tuple.length()-1)
					return calculate(tuple.substring(1,j));
			String sub1 = tuple.substring(1,j);
			String sub2 = tuple.substring(j+1,tuple.length());
			return CalcIn(calculate(sub1),sub2);
		}
		if(tuple.startsWith("NOT"))
			return !(calculate(tuple.substring(3,tuple.length())));
		// if we reach this point this means the tuple_condition starts with COL_NAME
		
		
		int func = NOT_FUNC;
		//System.err.println("%%%"+tuple);
		if(tuple.startsWith("MAX"))
			func = MAX;
		if(tuple.startsWith("MIN"))
			func = MIN;
		if(tuple.startsWith("AVG"))
			func = AVG;
		if(tuple.startsWith("SUM"))
			func = SUM;
		if(tuple.startsWith("COUNT"))
			func = COUNT;
		
		if(func!=NOT_FUNC)
		{
			int i = tuple.indexOf("(");
			int j = tuple.indexOf(")");

			tuple = tuple.substring(i+1, j)+tuple.substring(j+1, tuple.length());
			//System.err.print(tuple);
		}
		tuple =Clean(tuple);
		if(is_letter(tuple.charAt(0))){
			int i=0;
			int j=0;
			while(i<tuple.length() && (is_letter(tuple.charAt(i)) ||  is_digit(tuple.charAt(i))))
				i++;
			String table_name=table_name1;
			//System.err.println("@%&^%&^%@&"+tuple+j+i);
			if(i<tuple.length() && tuple.charAt(i)=='.'){
				table_name = tuple.substring(0,i);
				j=i+1;
				i++;
				while(i<tuple.length() && (is_letter(tuple.charAt(i)) ||  is_digit(tuple.charAt(i))))
					i++;
			}
			String ColName = tuple.substring(j,i);
			int type;
			if(tables == false)
				type = getType(ColName, func);
			else
				type = getType(ColName, (table_name.equals(table_name1)?1:2), func);
			String sub = Clean(tuple.substring(i,tuple.length()));
			
			if(type == TYPE_VARCHAR){
				String value = getStrValue(ColName, ((tables==false||table_name.equals(table_name1))?1:2), func);
				
				if(sub.startsWith("<=") || sub.startsWith("=<") ){
					return value.compareTo(StrCompVal(sub.substring(2, sub.length() ) )) <= 0;
				}
				if(sub.startsWith(">=") || sub.startsWith("=>") )
					return value.compareTo(StrCompVal(sub.substring(2, sub.length() ) )) >= 0;
				if(sub.startsWith("<") )
					return value.compareTo(StrCompVal(sub.substring(1, sub.length() ) )) <  0;				
				if(sub.startsWith(">") )
					return value.compareTo(StrCompVal(sub.substring(1, sub.length() ) )) >  0;
				if(sub.startsWith("="))
					return value.compareTo(StrCompVal(sub.substring(1, sub.length() ) )) == 0;
			}
			if(type == TYPE_INT){
				//System.err.println("{}{}{}{}{}"+sub);
				long value = getIntValue(ColName, ((tables==false||table_name.equals(table_name1))?1:2), func);
				sub = Clean(sub);
				if(sub.startsWith("<=") || sub.startsWith("=<"))
					return (value <= IntCompVal(sub.substring(2, sub.length())));
				if(sub.startsWith(">=") || sub.startsWith("=>"))
					return (value >= IntCompVal(sub.substring(2, sub.length())));
				if(sub.startsWith(">") )
					return (value > IntCompVal(sub.substring(1, sub.length())));
				if(sub.startsWith("<"))
					return (value < IntCompVal(sub.substring(1, sub.length())));
				if(sub.startsWith("="))
					return (value == IntCompVal(sub.substring(1, sub.length())));
			}
		}
		if(tuple.charAt(0)=='"'){
			if(tuple.contains("<=")){
				int l = tuple.lastIndexOf("<=");
				return StrCompVal(tuple.substring(0,l)).compareTo(  StrCompVal(tuple.substring(l+2,tuple.length())))<=0;
			}
			if(tuple.contains(">=")){
				int l = tuple.lastIndexOf(">=");
				return StrCompVal(tuple.substring(0,l)).compareTo(  StrCompVal(tuple.substring(l+2,tuple.length())))>=0;
			}
			if(tuple.contains("<")){
				int l = tuple.lastIndexOf("<");
				return StrCompVal(tuple.substring(0,l)).compareTo(  StrCompVal(tuple.substring(l+1,tuple.length())))<0;
			}
			if(tuple.contains(">")){
				int l = tuple.lastIndexOf(">");
				return StrCompVal(tuple.substring(0,l)).compareTo(  StrCompVal(tuple.substring(l+1,tuple.length())))>0;
			}
			if(tuple.contains("=")){
				int l = tuple.lastIndexOf("=");
				return StrCompVal(tuple.substring(0,l)).compareTo(  StrCompVal(tuple.substring(l+1,tuple.length())))==0;
			}

				
		}
		if(is_digit(tuple.charAt(0))){
			if(tuple.contains("<=")){
				int l = tuple.lastIndexOf("<=");
				return IntCompVal(tuple.substring(0,l))<=IntCompVal(tuple.substring(l+2,tuple.length()));
			}
			if(tuple.contains(">=")){
				int l = tuple.lastIndexOf(">=");
				return IntCompVal(tuple.substring(0,l))>=IntCompVal(tuple.substring(l+2,tuple.length()));
			}
			if(tuple.contains("<")){
				int l = tuple.lastIndexOf("<");
				return IntCompVal(tuple.substring(0,l))<IntCompVal(tuple.substring(l+1,tuple.length()));
			}
			if(tuple.contains(">")){
				int l = tuple.lastIndexOf(">");
				return IntCompVal(tuple.substring(0,l))>IntCompVal(tuple.substring(l+1,tuple.length()));
			}
			if(tuple.contains("=")){
				int l = tuple.lastIndexOf("=");
				return IntCompVal(tuple.substring(0,l))==IntCompVal(tuple.substring(l+1,tuple.length()));
			}
		}
		//if we reach this point this means the string isn't a standard tuple condition		
		System.err.println("(Err)Not a standard Tupple Condition\n <"+tuple+">");
		return false;
	}
	
	private  boolean CalcIn(boolean A, String intuple){//Calculate inner Tuple Condition
		intuple = Clean(intuple);
		if(intuple.startsWith("AND"))
			return (A && calculate(intuple.substring(3,intuple.length())));
		if(intuple.startsWith("OR"))
			return (A || calculate(intuple.substring(2,intuple.length())));
		
		System.err.println("(Err)Not a standard inner Tupple Condition:\n"+intuple);
		return false;
	}
	
	public  String StrCompVal(String str){
		str = Clean(str);
		//System.err.println(str+"||\n");
		if(str.charAt(0)== '"'){
			int i = 1;
			while(str.charAt(i)!='"')
				i++;
			if(i==str.length()-1)
				return str.substring(1, i);
			String sub1 = str.substring(1, i);
			String sub2 = str.substring(i+1, str.length());
			//System.err.println(sub1+","+sub2+"|\n");
			return InStrCompVal(sub1, sub2);
		}
		// if we reach this point this means the (int)compare starts with a field
		int func = NOT_FUNC;
		if(str.startsWith("MAX"))
			func = MAX;
		if(str.startsWith("MIN"))
			func = MIN;
		if(str.startsWith("AVG"))
			func = AVG;
		if(str.startsWith("SUM"))
			func = SUM;
		if(str.startsWith("COUNT"))
			func = COUNT;
		
		if(func!=NOT_FUNC)
		{
			int i = str.indexOf("(");
			int j = str.indexOf(")");
			str = str.substring(i+1, j)+str.substring(j+1, str.length());
			//System.err.print(str);
		}
		
		//System.err.println(str+"||\n");
		int i=0;
		int j=0;
		//System.err.print(str);
		while(i<str.length() && (is_letter(str.charAt(i)) ||  is_digit(str.charAt(i) ) ) ){
			//System.err.println(str.length());
			i++;
		}
		String table_name=table_name1;
		if(i<str.length() && str.charAt(i)=='.'){
			table_name = str.substring(0,i);
			j=i+1;
			i++;
			while(i<str.length() && (is_letter(str.charAt(i)) ||  is_digit(str.charAt(i) ) ) )
				i++;
		}
		String value = getStrValue(str.substring(j,i), ((tables==false||table_name.equals(table_name1))?1:2), func);
		if(i==str.length())
			return value;
		if(i!=0)
			return InStrCompVal(value, str.substring(i, str.length()));
		
		
		return "";
	}
	private  String InStrCompVal(String str1, String str2){
		str2 = Clean(str2);
		if(str2.startsWith("+"))
			return str1 + StrCompVal(str2.substring(1,str2.length()));
		
		System.err.println("(Err)Not a standard inner str comp :\n <"+str2+">");
		return "";
	}
	
	public long IntCompVal(String comp){
		comp = Clean(comp);
		int i = 0;
		int sign = 1;
		if(comp.startsWith("-")){
			i++;
		}
		int numb = 0;
		while(i<comp.length()&& is_digit(comp.charAt(i))){
			numb*=10;
			numb+=comp.charAt(i)-'0';
			i++;
		}
		if(i==comp.length())
			return sign * numb;
		if(i!=0)
			return inIntCompVal(sign * numb, comp.substring(i, comp.length()));
		// if we reach this point this means the (int)compare starts with a field
		int func = NOT_FUNC;
		if(comp.startsWith("MAX"))
			func = MAX;
		if(comp.startsWith("MIN"))
			func = MIN;
		if(comp.startsWith("AVG"))
			func = AVG;
		if(comp.startsWith("SUM"))
			func = SUM;
		if(comp.startsWith("COUNT"))
			func = COUNT;
		
		if(func!=NOT_FUNC)
		{
			int s = comp.indexOf("(");
			int j = comp.indexOf(")");
			comp = comp.substring(s+1, j)+comp.substring(j+1, comp.length());
			//System.err.print(comp);
		}
		
		// i == 0
		int j=0;
		while(i<comp.length() && (is_letter(comp.charAt(i)) ||  is_digit(comp.charAt(i) ) ) )
			i++;
		String table_name=table_name1;
		if(i<comp.length() && comp.charAt(i)=='.'){
			table_name = comp.substring(0,i);
			j=i+1;
			i++;
			while(i<comp.length() && (is_letter(comp.charAt(i)) ||  is_digit(comp.charAt(i) ) ) )
				i++;
		}
		//System.err.println(table_name+" "+table_name1+" "+table_name2);
		long value = getIntValue(comp.substring(j,i), ((tables==false||table_name.equals(table_name1))?1:2) , func);
		if(i==comp.length())
			return value;
		if(i!=0)
			return inIntCompVal(value, comp.substring(i, comp.length()));
		
		return 0;
	}
	
	private long inIntCompVal(long numb, String incomp){
		incomp = Clean(incomp);
		if(incomp.startsWith("+"))
			return numb+IntCompVal(incomp.substring(1,incomp.length()));
		if(incomp.startsWith("-"))
			return numb-IntCompVal(incomp.substring(1,incomp.length()));
		if(incomp.startsWith("*"))
			return numb*IntCompVal(incomp.substring(1,incomp.length()));
		if(incomp.startsWith("/"))
			return numb/IntCompVal(incomp.substring(1,incomp.length()));
		
		//Bad in int compVal
		
		return 0;
	}
////////////////////////////////////////////////////////////////////////////	
	private  String Clean(String input){
		int i =0;
		while(i<input.length() && input.charAt(i)==' ')
			i++;
		if(i==input.length())
			{System.err.println("(Err)Empty string to clean!");return "";}
		int j = input.length();
		while(input.charAt(j-1)==' ' || input.charAt(j-1)==';' )
			j--;
			
		
		return input.substring(i,j);
	}
//////////////////////////////////////////////////////////////////////////	
	private  boolean is_letter(char c){
		if('a'<=c && c<='z')
			return true;
		if('A'<=c && c<='Z')
			return true;
		return false;
	}
	private  boolean is_digit(char c){
		if('0'<=c && c<='9')
			return true;
		return false;
	}
//////////////////////////////////////////////////////////////////////////	
	
	private  long getIntValue(String a, int table, int func){
		//System.err.println(a);
		if(func == NOT_FUNC)
			if(table == 1)
				return ((long)myrow.get(a).getValue());
			else
				return ((long)myrow2.get(a).getValue());
		else
			if(table==1)
				if(mydb.getField(func_name(func)+"("+a+")")!=null)
					return (long)mydb.getField(func_name(func)+"("+a+")").getValue();
				else
					return (long)mydb.getField(func_name(func)+"("+table_name1+"."+a+")").getValue();
			else
				if(mydb2.getField(func_name(func)+"("+a+")")!=null)
					return (long)mydb2.getField(func_name(func)+"("+a+")").getValue();
				else
					return (long)mydb2.getField(func_name(func)+"("+table_name2+"."+a+")").getValue();
	}
	
	private  String getStrValue(String a, int table, int func){
		//System.err.println("!!!!!!!!!!"+a+","+table);
		if(func == NOT_FUNC)
			if(table==1)
				return (String)myrow.get(a).getValue();
			else
				return (String)myrow2.get(a).getValue();
		else{
			if(table==1)
				if(mydb.getField(func_name(func)+"("+a+")")!=null)
					return (String)mydb.getField(func_name(func)+"("+a+")").getValue();
				else
					return (String)mydb.getField(func_name(func)+"("+table_name1+"."+a+")").getValue();
			else
				if(mydb2.getField(func_name(func)+"("+a+")")!=null)
					return (String)mydb2.getField(func_name(func)+"("+a+")").getValue();
				else
					return (String)mydb2.getField(func_name(func)+"("+table_name2+"."+a+")").getValue();
		}
	}
	
	private  int getType(String a, int func){
		//System.err.println(a);
		if(func==NOT_FUNC){
			if(!myrow.containsKey(a))
				System.err.println(a+" isn't in hash map");
			if(myrow.get(a).getClass().equals(VARCHAR.class))
				return TYPE_VARCHAR;
			else
				return TYPE_INT;
		}else{
			//System.err.println("@@@"+func_name(func)+"("+a+")"+"@@@");
			if(mydb.getField(func_name(func)+"("+a+")").getClass().equals(VARCHAR.class))
				return TYPE_VARCHAR;
			else
				return TYPE_INT;
		}
			
	}
	
	private  int getType(String a, int table, int func){
		if(func==NOT_FUNC)
			if(table == 1){
				if(myrow.containsKey(a))
					if(myrow.get(a).getClass().equals(VARCHAR.class))
						return TYPE_VARCHAR;
					else
						return TYPE_INT;
				else
					if(myrow.get(table_name1+"."+a).getClass().equals(VARCHAR.class))
						return TYPE_VARCHAR;
					else
						return TYPE_INT;
			}else{
				if(myrow2.containsKey(a))
					if(myrow2.get(a).getClass().equals(VARCHAR.class))
						return TYPE_VARCHAR;
					else
						return TYPE_INT;
				else
					if(myrow2.get(table_name2+"."+a).getClass().equals(VARCHAR.class))
						return TYPE_VARCHAR;
					else
						return TYPE_INT;
			}
		else
			if(table == 1){
				if(mydb.getField(func_name(func)+"("+a+")")!=null)
				//System.err.println("^^^^"+func_name(func)+"("+table_name1+"."+a+")");
					if(mydb.getField(func_name(func)+"("+a+")").getClass().equals(VARCHAR.class))
						return TYPE_VARCHAR;
					else
						return TYPE_INT;
				else{
					System.err.println(func_name(func)+"("+table_name1+"."+a+")");
					if(mydb.getField(func_name(func)+"("+table_name1+"."+a+")").getClass().equals(VARCHAR.class))
						return TYPE_VARCHAR;
					else
						return TYPE_INT;
				}
			}else{
				if(mydb2.getField(func_name(func)+"("+a+")")!=null)
					//System.err.println("^^^^"+func_name(func)+"("+table_name1+"."+a+")");
						if(mydb2.getField(func_name(func)+"("+a+")").getClass().equals(VARCHAR.class))
							return TYPE_VARCHAR;
						else
							return TYPE_INT;
					else
						if(mydb2.getField(func_name(func)+"("+table_name2+"."+a+")").getClass().equals(VARCHAR.class))
							return TYPE_VARCHAR;
						else
							return TYPE_INT;
			}
	}
	
	private String func_name(int func){
		if(func==MAX)
			return "MAX";
		if(func==MIN)
			return "MIN";
		if(func==AVG)
			return "AVG";
		if(func==SUM)
			return "SUM";
		if(func==COUNT)
			return "COUNT";
		
		System.err.println("bad func: "+func);
		return "";
	}
	
}