import syntaxtree.*;
import visitor.*;

import java.util.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.FileWriter;

public class Main {
	public static void main(String[] args) throws Exception {
		if(args.length < 1){
			System.err.println("Usage: java Main <inputFile>");
			System.exit(1);
		}

		FileInputStream fis = null;
		try{
			for (int file = 0; file < args.length; file++){
				System.out.println("\n\nFILE: " + args[file]);

				
				fis = new FileInputStream(args[file]);
				MiniJavaParser parser = new MiniJavaParser(fis);

				WriteToFile write = new WriteToFile(args[file]);
				Goal root = parser.Goal();
				
				System.err.println("Program parsed successfully.");
				
				MyVisitor eval = new MyVisitor();
				root.accept(eval, "table");
				root.accept(eval, "check");
				eval.DeleteList();
				write.closefile();
			}
		}
		catch(ParseException ex){
			System.out.println(ex.getMessage());
		}
		catch(FileNotFoundException ex){
			System.err.println(ex.getMessage());
		}
		finally{
			try{
				if(fis != null) fis.close();
			}
			catch(IOException ex){
				System.err.println(ex.getMessage());
			}
		}
	}
}

class WriteToFile {
	public static FileWriter outputfile;

	public WriteToFile(String str){
		try {
			outputfile = new FileWriter(str.split("\\.")[0]+".ll");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public void closefile(){
		try {
			outputfile.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public static void writefile(String str){
		try {
			outputfile.write(str);
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
}

class Ext_class{
	String name;
	String ext_name;
	public Ext_class(String name,String ext_name){
		this.name = name;
		this.ext_name = ext_name;
	}
}

class Method{
	String name;
	String type;
	Map<String,String> param_map;
	Map<String,String> var_map;
	public Method(String name,String type){
		this.name = name;
		this.type = type;
		this.param_map = new LinkedHashMap<String,String>();
		this.var_map = new LinkedHashMap<String,String>();
	}
}

class Symbol_table{
	String name;
	Map<String,String> vardecl_map;
	List <Method> method_list;
	List<Symbol_table> ext_classes;
	int ext_index;
	public Symbol_table(String name){
		this.name = name;
		this.vardecl_map = new LinkedHashMap<String,String>();
		this.method_list = new ArrayList<Method>();
		this.ext_classes = new ArrayList<Symbol_table>();
		this.ext_index = -1;
	}
	public void put_var(String name,String type){
		this.vardecl_map.put(name,type);
	}
	public void put_method(String name,String type){
		this.method_list.add(new Method(name,type));
	}
	public void  put_var_in_method(String method_name,String name,String type){
		for(Method method : this.method_list){
			if(method.name.equals(method_name)){
				method.var_map.put(name, type);
			}
		}
	}
	public void put_param_in_method(String method_name,String name,String type){
		for(Method method : this.method_list){
			if(method.name.equals(method_name)){
				method.param_map.put(name, type);
			}
		}
	}
	public void put_ext_class(String name){
		this.ext_classes.add(new Symbol_table(name));
		this.ext_index++;
	}
	public static boolean isNumeric(String str) { 
		try {
		  Integer.parseInt(str);  
		  return true;
		} catch(NumberFormatException e){  
		  return false;  
		}  
	}
	public String Id_check(String var_name,String method_name,String ext_name,List<Ext_class> ext_class_list,List<Symbol_table> s_table){ 
		String var_type = "";
		for(Method method : this.method_list){
			if(isNumeric(var_name)){
				var_type = "int";
				break;
			}
			else if(var_name.equals("false") ||var_name.equals("true")){
				var_type = "boolean";
				break;
			}
			if(method.name.equals(method_name)){
				if(!method.var_map.containsKey(var_name)){
					if(!method.param_map.containsKey(var_name)){
						if(!this.vardecl_map.containsKey(var_name)){
							if(!ext_name.equals("")){
								for(Symbol_table table : s_table){
									if(table.name.equals(ext_name)){
										if(!table.vardecl_map.containsKey(var_name)){
											System.out.println("An error occured! No variable found!");
											System.exit(0);
										}
										else{
											var_type = table.vardecl_map.get(var_name);
										}
									}
								}
							}
							else{
								System.out.println("An error occured! No variable found!");
								System.exit(0);
							}
						}
						else{
							var_type = this.vardecl_map.get(var_name);
						}
					}
					else{
						var_type = method.param_map.get(var_name);
					}
				}
				else{
					var_type = method.var_map.get(var_name);
				}
			}
		}
		return var_type;
	}
	public boolean In_Method(String name){
		for(Method method : method_list){
			if(method.name.equals(name)){
				return true;
			}
		}
		return false;
	}

	//Checkarei an h methodos anoikei sto paidi toy
	public boolean Ext_method_check(String method_name,String child_name){
		for(Method method : method_list){
			if(method.name.equals(method_name)){
				for(Symbol_table table : ext_classes){
					if(table.name.equals(child_name)){
						for(Method ext_method : table.method_list){
							if(method.name.equals(ext_method.name)){
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
}

class V_table{
	String name;
	String global;
	Map<String,Integer> offset_map;
	Map<String,Integer> offset_var_map;
	public V_table(String name,String global){
		this.offset_map = new LinkedHashMap<String,Integer>();
		this.offset_var_map = new LinkedHashMap<String,Integer>();
		this.name = name;
		this.global = global;
	}
	public void add_offset(String method_name,Integer offset){
		this.offset_map.put(method_name, offset);
	}
	public void add_var_offset(String var_name,Integer offset){
		this.offset_var_map.put(var_name, offset);
	}
}

class MyVisitor extends GJDepthFirst<String, String>{

	static List <Symbol_table> s_table = new ArrayList<Symbol_table>();
	static List <V_table> v_table = new ArrayList<V_table>();
	static List <Ext_class> ext_class_list = new ArrayList<Ext_class>();
	int table_index = 0;
	int var_c = 0;
	int exp_res = 0;
	int if_num = 0;
	int while_num = 0;
	int nsz_num = 0;
	int oob_num = 0;
	String operation="";
	boolean check = true;
	public Integer get_var_offset(String class_name,String method_name,String var_name){
		for(Symbol_table table : s_table){
			if(table.name.equals(class_name)){
				for(Method method : table.method_list){
					if(method.name.equals(method_name)){
						if(method.var_map.containsKey(var_name)){
							return -1;
						}
						else if(method.param_map.containsKey(var_name)){
							return -1;
						}
					}
				}
				if(table.vardecl_map.containsKey(var_name)){
					for(V_table vv : v_table){
						if(vv.name.equals(table.name)){
							return vv.offset_var_map.get(var_name)+8;
						}
					}
				}
			}
			for(Symbol_table table2 : table.ext_classes){
				if(table2.name.equals(class_name)){
					for(Method method : table2.method_list){
						if(method.name.equals(method_name)){
							if(method.var_map.containsKey(var_name)){
								return -1;
							}
							else if(method.param_map.containsKey(var_name)){
								return -1;
							}
						}
					}
					if(table2.vardecl_map.containsKey(var_name)){
						for(V_table vv : v_table){
							if(vv.name.equals(table2.name)){
								return vv.offset_var_map.get(var_name)+8;
							}
						}
					}
					if(table.vardecl_map.containsKey(var_name)){
						for(V_table vv : v_table){
							if(vv.name.equals(table.name)){
								return vv.offset_var_map.get(var_name)+8;
							}
						}
					}
				}
			}
		}
		return -1;
	}
	public String get_type(String class_name,String method_name,String var_name){
		for(Symbol_table table : s_table){
			if(table.name.equals(class_name)){
				for(Method method : table.method_list){
					if(method.name.equals(method_name)){
						if(method.var_map.containsKey(var_name)){
							return method.var_map.get(var_name);
						}
						else if(method.param_map.containsKey(var_name)){
							return method.param_map.get(var_name);
						}
					}
				}
				if(table.vardecl_map.containsKey(var_name)){
					return table.vardecl_map.get(var_name);
				}
			}
			for(Symbol_table table2 : table.ext_classes){
				if(table2.name.equals(class_name)){
					for(Method method : table2.method_list){
						if(method.name.equals(method_name)){
							if(method.var_map.containsKey(var_name)){
								return method.var_map.get(var_name);
							}
							else if(method.param_map.containsKey(var_name)){
								return method.param_map.get(var_name);
							}
						}
					}
					if(table2.vardecl_map.containsKey(var_name)){
						return table2.vardecl_map.get(var_name);
					}
					for(Method method : table.method_list){
						if(method.name.equals(method_name)){
							if(method.var_map.containsKey(var_name)){
								return method.var_map.get(var_name);
							}
							else if(method.param_map.containsKey(var_name)){
								return method.param_map.get(var_name);
							}
						}
					}
					if(table.vardecl_map.containsKey(var_name)){
						return table.vardecl_map.get(var_name);
					}
				}
			}
		}
		return var_name;
	}
	public String get_signature(String class_name,String method_name){
		String str3 = "";
		String str2 = "";
		for(Symbol_table table : s_table){
			if(table.name.equals(class_name)){
				for(Method method : table.method_list){
					if(method.name.equals(method_name)){
						str3 = "";
						for (String key: method.param_map.keySet()) {
							if(key!=""){
								str3 += ","+type_to_bit(method.param_map.get(key));
							}
						}
						str2 = type_to_bit(method.type) + " (i8*" + str3 + ")*";
						return str2;
					}
				}
			}
			for(Symbol_table table2 : table.ext_classes){
				if(table2.name.equals(class_name)){
					for(Method method : table.method_list){
						if(method.name.equals(method_name)){
							str3 = "";
							for (String key: method.param_map.keySet()) {
								if(key!=""){
									str3 += ","+type_to_bit(method.param_map.get(key));
								}
							}
							str2 = type_to_bit(method.type) + " (i8*" + str3 + ")*";
							return str2;
						}
					}
					for(Method method : table2.method_list){
						if(method.name.equals(method_name)){
							str3 = "";
							for (String key: method.param_map.keySet()) {
								if(key!=""){
									str3 += ","+type_to_bit(method.param_map.get(key));
								}
							}
							str2 = type_to_bit(method.type) + " (i8*" + str3 + ")*";
							return str2;
						}
					}
				}
			}
		}
		return str2;
	}
	public String type_to_bit(String type){
		if(type.equals("int")){
			return "i32";
		}
		else if(type.equals("boolean")){
			return "i1";
		}
		else if(type.equals("int[]")){
			return "i32*";
		}
		return "i8*";
	}
	public int type_to_int(String type){
		if(type.equals("int")){
			return 4;
		}
		else if(type.equals("boolean")){
			return 1;
		}
		return 8;
	}
	public String next_var(){
		var_c++;
		return "%_"+Integer.toString(var_c-1);
	}
	public String next_exp(){
		exp_res++;
		return "exp_res_"+Integer.toString(exp_res-1)+":\n";
	}
	public void writev_table(){
		for(Symbol_table table : s_table){
			int num=0;
			String str2="";
			String str3 = "";
			for(Method method : table.method_list){
				if(!method.name.equals("main")){
					num++;
					str3 = "";
					for (String key: method.param_map.keySet()) {
						if(key!=""){
							str3 += ","+type_to_bit(method.param_map.get(key));
						}
					}
					str2 += "\ti8* bitcast ("+type_to_bit(method.type) + " (i8*" + str3 + ")*" + " @"+table.name+"."+method.name + " to i8*),\n";
				}
			}
			v_table.add(new V_table(table.name,"["+Integer.toString(num)+" x i8*]"));
			String str = "@."+table.name+"_vtable = global ["+Integer.toString(num)+" x i8*] [\n";
			if(!str2.equals("")){
				str += str2.substring(0, str2.length() - 2) +"\n]\n\n";
			}
			else{
				str = str.substring(0, str.length() - 1);
				str += "]\n\n";
			}
			WriteToFile.writefile(str);

			for(Symbol_table ext_table : table.ext_classes){
				int num_2=0;
				String str2_2="";
				String str3_2 = "";
				for(Method method : table.method_list){
					if(!method.name.equals("main")&&!table.Ext_method_check(method.name,ext_table.name)){
						num_2++;
						str3_2 = "";
						for (String key: method.param_map.keySet()) {
							if(key!=""){
								str3_2 += ","+type_to_bit(method.param_map.get(key));
							}
						}
						str2_2 += "\ti8* bitcast ("+type_to_bit(method.type) + " (i8*" + str3_2 + ")*" + " @"+table.name+"."+method.name + " to i8*),\n";
					}
				}
				for(Method method : ext_table.method_list){
					if(!method.name.equals("main")){
						num_2++;
						str3_2 = "";
						for (String key: method.param_map.keySet()) {
							if(key!="")
								str3_2 += ","+type_to_bit(method.param_map.get(key));
						}
						str2_2 += "\ti8* bitcast ("+type_to_bit(method.type) + " (i8*" + str3_2 + ")*" + " @"+ext_table.name+"."+method.name + " to i8*),\n";
					}
				}
				v_table.add(new V_table(ext_table.name,"["+Integer.toString(num_2)+" x i8*]"));
				String str_2 = "@."+ext_table.name+"_vtable = global ["+Integer.toString(num_2)+" x i8*] [\n";
				if(!str2_2.equals("")){
					str2_2 = str2_2.substring(0, str2_2.length() - 2);
					str_2 += str2_2 +"\n]\n\n";
				}
				else{
					str_2 = str_2.substring(0, str_2.length() - 1);
					str_2 += "]\n\n";
				}
				WriteToFile.writefile(str_2);
			}
		}
	}
	public void DeleteList(){
		s_table.clear();
		v_table.clear();
		ext_class_list.clear();
	}
	public void Offset_check(){
		for(Symbol_table table : s_table){
			int offset = 0;
			int offset2 = 0;
			System.out.println("-----------Class "+table.name + "-----------");
			System.out.println("--Variables---");
			for (String key: table.vardecl_map.keySet()) {
				System.out.println(table.name + "." + key + " : " + offset);
				String type = table.vardecl_map.get(key);
				for(V_table vv : v_table){
					if(vv.name.equals(table.name)){
						vv.add_var_offset(key, offset);
					}
				}
				if(type.equals("int")){
					offset += 4;
				}
				else if(type.equals("boolean")){
					offset += 1;
				}
				else{
					offset += 8;
				}
			}
			System.out.println("---Methods---");
			for(Method method : table.method_list){
				System.out.println(table.name + "." + method.name + " : " + offset2);
				for(V_table vv : v_table){
					if(vv.name.equals(table.name)){
						vv.add_offset(method.name, offset2);
					}
				}
				offset2 += 8;
			}
			for(Symbol_table table2 : table.ext_classes){
				System.out.println("-----------Class "+table2.name + "-----------");
				System.out.println("--Variables---");


				for (String key: table.vardecl_map.keySet()) {
					for(V_table vv : v_table){
						if(vv.name.equals(table.name)){
							for(V_table vv2 : v_table){
								if(vv2.name.equals(table2.name)){
									vv2.add_var_offset(key, vv.offset_var_map.get(key));
								}
							}
						}
					}
				}

				for (String key: table2.vardecl_map.keySet()) {
					System.out.println(table2.name + "." + key + " : " + offset);
					String type = table2.vardecl_map.get(key);
					for(V_table vv : v_table){
						if(vv.name.equals(table2.name)){
							vv.add_var_offset(key, offset);
						}
					}

					if(type.equals("int")){
						offset += 4;
					}
					else if(type.equals("boolean")){
						offset += 1;
					}
					else{
						offset += 8;
					}
				}
				for(Method method2 : table.method_list){
					for(V_table vv : v_table){
						if(vv.name.equals(table.name)){
							for(V_table vv2 : v_table){
								if(vv2.name.equals(table2.name)){
									vv2.add_offset(method2.name, vv.offset_map.get(method2.name));
								}
							}
						}
					}
				}
				System.out.println("---Methods---");
				for(Method method : table2.method_list){
					boolean flg = false;
					for(Method method2 : table.method_list){
						if(method2.name.equals(method.name)){
							flg = true;
						}
					}
					if(!flg){
						System.out.println(table2.name + "." + method.name + " : " + offset2);
						for(V_table vv : v_table){
							if(vv.name.equals(table2.name)){
								vv.add_offset(method.name, offset2);
							}
						}
						offset2 += 8;
					}
				}
			}
		}
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> "public"
	 * f4 -> "static"
	 * f5 -> "Void"
	 * f6 -> "main"
	 * f7 -> "("
	 * f8 -> "String"
	 * f9 -> "["
	 * f10 -> "]"
	 * f11 -> Identifier()
	 * f12 -> ")"
	 * f13 -> "{"
	 * f14 -> ( VarDeclaration() )*
	 * f15 -> ( Statement() )*
	 * f16 -> "}"
	 * f17 -> "}"
	 */
	@Override
	public String visit(MainClass n, String argu) throws Exception{
		if(argu == "table"){
			check = false;
			String classname = n.f1.accept(this, null);
			
			s_table.add(new Symbol_table(classname));
			s_table.get(table_index).put_method("main","void");
			NodeListOptional varDecls = n.f14;                                    
			for (int i = 0; i < varDecls.size(); ++i) {
				VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
				String varname = varDecl.f1.accept(this,null);
				String varType = varDecl.f0.accept(this,null);
	
				s_table.get(table_index).put_var_in_method("main", varname, varType); 
			}
			return null;
		}
		else{
			writev_table();
			Offset_check();
			WriteToFile.writefile(input_str);
			check = true;
			table_index = 0;
			
			WriteToFile.writefile("\ndefine i32 @main() {\n");
			n.f14.accept(this,argu);
			n.f15.accept(this,n.f1.accept(this, null)+" main");
			WriteToFile.writefile("\tret i32 0\n");
			WriteToFile.writefile("}\n");
		}
		return null;
	}

	/**
	* f0 -> ClassDeclaration()
	*       | ClassExtendsDeclaration()
	*/
	@Override
	public String visit(TypeDeclaration n, String argu) throws Exception{
		return n.f0.accept(this, argu);
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> ( VarDeclaration() )*
	 * f4 -> ( MethodDeclaration() )*
	 * f5 -> "}"
	 */
	@Override
	public String visit(ClassDeclaration n, String argu) throws Exception {
		if(!check){
			String classname = n.f1.accept(this, null);

			//Check class duplicate
			for(Symbol_table table : s_table){
				if(table.name == classname){
					System.out.println("An error occured! Duplicate class name");
					System.exit(0);
				}
			}
			for(Ext_class ext_class : ext_class_list){
				if(ext_class.name == classname){
					System.out.println("An error occured! Duplicate class name");
					System.exit(0);
				}
			}
			table_index++;  
			s_table.add(new Symbol_table(classname));
			NodeListOptional varDecls = n.f3;                                   
			for (int i = 0; i < varDecls.size(); ++i) {
				VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
				String varname = varDecl.f1.accept(this,null);

				//Check variable duplicate
				if(s_table.get(table_index).vardecl_map.containsKey(varname)){
					System.out.println("An error occured! Duplicate variable name");
					System.exit(0);
				}

				String varType = varDecl.f0.accept(this,null);
				s_table.get(table_index).put_var(varname, varType);
			}
			NodeListOptional methDecls = n.f4;                               
			for (int i = 0; i < methDecls.size(); ++i) {
				MethodDeclaration methDecl = (MethodDeclaration) methDecls.elementAt(i);
				String argumentList = methDecl.f4.present() ? methDecl.f4.accept(this, null) : "";
				String type = methDecl.f1.accept(this, null);
				String name = methDecl.f2.accept(this, null);

				//Check duplicate method
				for(Method method : s_table.get(table_index).method_list){
					if(method.name == name){
						System.out.println("An error occured! Duplicate method name");
						System.exit(0);
					}
				}
				
				s_table.get(table_index).put_method(name, type);

				String[] temp = argumentList.split(",");
				String par_type="";
				String par_name="";
				for(String str : temp){
					String[] temp_string = str.trim().split(" ");
					int counter = 0;
					for(String f_temp : temp_string){
						if(f_temp == ""){
							break;
						}
						if(counter==0){
							par_type = f_temp;
						}
						else{
							par_name = f_temp;
						}
						counter++;
					}

					//Check duplicate param
					for(Method method : s_table.get(table_index).method_list){
						if(method.name == name){
							if(method.param_map.containsKey(par_name)){
								System.out.println("An error occured! Duplicate param name");
								System.exit(0);
							}
						}
					}

					s_table.get(table_index).put_param_in_method(name, par_name, par_type);
				}

				NodeListOptional varDes = methDecl.f7;         
				for (int j = 0; j < varDes.size(); ++j) {
					VarDeclaration varDe = (VarDeclaration) varDes.elementAt(j);
					String var_name = varDe.f1.accept(this,null);

					//Check duplicate variable
					for(Method method : s_table.get(table_index).method_list){
						if(method.name == name){
							if(method.var_map.containsKey(var_name)||method.param_map.containsKey(var_name)){
								System.out.println("An error occured! Duplicate variable in method name");
								System.exit(0);
							}
						}
					}

					String var_type = varDe.f0.accept(this,null);
					s_table.get(table_index).put_var_in_method(name, var_name, var_type);    
				}
			}
			super.visit(n, argu);
			return null;
		}
		else{
			table_index++;
			var_c = 0;
			n.f4.accept(this,n.f1.accept(this, null));
		}
		return null;
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "extends"
	 * f3 -> Identifier()
	 * f4 -> "{"
	 * f5 -> ( VarDeclaration() )*
	 * f6 -> ( MethodDeclaration() )*
	 * f7 -> "}"
	 */
	@Override
	public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
		if(!check){
			boolean flg=false;
			for(Symbol_table table : s_table){
				if(table.name.equals(n.f3.accept(this,null))){
					flg=true;
					String classname = n.f1.accept(this, null);
					String classname_ext = n.f3.accept(this, null);
					//Check class duplicate
					for(Symbol_table table2 : s_table){
						if(table2.name == classname){
							System.out.println("An error occured! Duplicate class name");
							System.exit(0);
						}
					}
					for(Ext_class ext_class : ext_class_list){
						if(ext_class.name == classname){
							System.out.println("An error occured! Duplicate class name");
							System.exit(0);
						}
					}

					table.put_ext_class(n.f1.accept(this,null));
					Ext_class eclass = new Ext_class(classname, classname_ext);
					ext_class_list.add(eclass);
					NodeListOptional varDecls = n.f5;                                       /* Print the variable inside this class */
					for (int i = 0; i < varDecls.size(); ++i) {
						VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
						String varname = varDecl.f1.accept(this,null);

						//Check variable duplicate
						if(table.ext_classes.get(table.ext_index).vardecl_map.containsKey(varname)){
							System.out.println("An error occured! Duplicate variable name");
							System.exit(0);
						}

						String varType = varDecl.f0.accept(this,null);
						table.ext_classes.get(table.ext_index).put_var(varname, varType);
					}
					NodeListOptional methDecls = n.f6;                                       /* Print the variable inside this class */
					for (int i = 0; i < methDecls.size(); ++i) {
						MethodDeclaration methDecl = (MethodDeclaration) methDecls.elementAt(i);
						String argumentList = methDecl.f4.present() ? methDecl.f4.accept(this, null) : "";
						String type = methDecl.f1.accept(this, null);
						String name = methDecl.f2.accept(this, null);

						//Check duplicate method
						for(Method method : table.ext_classes.get(table.ext_index).method_list){
							if(method.name == name){
								System.out.println("An error occured! Duplicate method name");
								System.exit(0);
							}
						}

						table.ext_classes.get(table.ext_index).put_method(name, type);
						String[] temp = argumentList.split(",");
						String par_type="";
						String par_name="";
						for(String str : temp){
							String[] temp_string = str.trim().split(" ");
							int counter = 0;
							for(String f_temp : temp_string){
								if(f_temp == ""){
									break;
								}
								if(counter==0){
									par_type = f_temp;
								}
								else{
									par_name = f_temp;
								}
								counter++;
							}

							//Check duplicate param
							for(Method method : table.ext_classes.get(table.ext_index).method_list){
								if(method.name == name){
									if(method.param_map.containsKey(par_name)){
										System.out.println("An error occured! Duplicate param name");
										System.exit(0);
									}
								}
							}

							table.ext_classes.get(table.ext_index).put_param_in_method(name, par_name, par_type);
						}

						NodeListOptional varDes = methDecl.f7;         
						for (int j = 0; j < varDes.size(); ++j) {
							VarDeclaration varDe = (VarDeclaration) varDes.elementAt(j);
							String var_name = varDe.f1.accept(this,null);

							//Check duplicate variable
							for(Method method : table.ext_classes.get(table.ext_index).method_list){
								if(method.name == name){
									if(method.var_map.containsKey(var_name)||method.param_map.containsKey(var_name)){
										System.out.println("An error occured! Duplicate variable in method name");
										System.exit(0);
									}
								}
							}

							String var_type = varDe.f0.accept(this,null);
							table.ext_classes.get(table.ext_index).put_var_in_method(name, var_name, var_type);    
						}
					}
				}
			}
			if(!flg){
				System.out.println("An error occured! No extend class found");
				System.exit(0);
			}
			super.visit(n, argu);
			return null;
		}
		else{
			n.f6.accept(this,n.f1.accept(this, null));
		}
		return null;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	* f2 -> ";"
	*/
	@Override
	public String visit(VarDeclaration n, String argu) throws Exception{
		String Type = n.f0.accept(this, null);
		String Name = n.f1.accept(this, null);
		if(check){
			WriteToFile.writefile("\t%"+Name+" = alloca "+type_to_bit(Type)+"\n");
		}
		return Type + " " + Name;
	}

	/**
	 * f0 -> "public"
	 * f1 -> Type()
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( FormalParameterList() )?
	 * f5 -> ")"
	 * f6 -> "{"
	 * f7 -> ( VarDeclaration() )*
	 * f8 -> ( Statement() )*
	 * f9 -> "return"
	 * f10 -> Expression()
	 * f11 -> ";"
	 * f12 -> "}"
	 */
	@Override
	public String visit(MethodDeclaration n, String argu) throws Exception {
		if(check){
			var_c = 0;
			String id = n.f2.accept(this,argu);
			WriteToFile.writefile("\ndefine "+ type_to_bit(n.f1.accept(this, argu))+" @"+argu+"."+id+"(i8* %this");
			String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";
			String[] temp = argumentList.split(",");
			String par_type="";
			String par_name="";
			String str2 = "";
			for(String str : temp){
				String[] temp_string = str.trim().split(" ");
				int counter = 0;
				for(String f_temp : temp_string){
					if(f_temp == ""){
						break;
					}
					if(counter==0){
						par_type = f_temp;
					}
					else{
						par_name = f_temp;
					}
					counter++;
				}
				if(par_name!=""){
					WriteToFile.writefile(", "+type_to_bit(par_type) + " %."+par_name);
					str2+="\t%"+par_name+" = alloca "+type_to_bit(par_type)+"\n";
					str2+="\tstore "+type_to_bit(par_type)+" %."+par_name+", "+type_to_bit(par_type)+"* %"+par_name+"\n";
				}
			}
			WriteToFile.writefile(") {\n");
			WriteToFile.writefile(str2);
			n.f7.accept(this, argu+" "+id);
			n.f8.accept(this, argu+" "+id);
			operation = "return";
			String str = n.f10.accept(this, argu+" "+id);
			operation = " ";
			if(Symbol_table.isNumeric(str)){
				WriteToFile.writefile("\tret i32 "+str+"\n}\n");
			}
			else
				WriteToFile.writefile("\tret i32 %_"+Integer.toString(var_c-1)+"\n}\n");
		}
		return null;
	}
	/**
	 * f0 -> FormalParameter()
	 * f1 -> FormalParameterTail()
	 */
	@Override
	public String visit(FormalParameterList n, String argu) throws Exception {
		String ret = n.f0.accept(this, null);

		if (n.f1 != null) {
			ret += n.f1.accept(this, null);
		}

		return ret;
	}

	/**
	 * f0 -> FormalParameter()
	 * f1 -> FormalParameterTail()
	 */
	@Override
	public String visit(FormalParameterTerm n, String argu) throws Exception {
		return n.f1.accept(this, argu);
	}

	/**
	 * f0 -> ","
	 * f1 -> FormalParameter()
	 */
	@Override
	public String visit(FormalParameterTail n, String argu) throws Exception {
		String ret = "";
		for ( Node node: n.f0.nodes) {
			ret += ", " + node.accept(this, null);
		}

		return ret;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 */
	@Override
	public String visit(FormalParameter n, String argu) throws Exception{
		String type = n.f0.accept(this, null);
		String name = n.f1.accept(this, null);
		return type + " " + name;
	}

	/**
	* f0 -> ArrayType()
	*       | BooleanType()
	*       | IntegerType()
	*       | Identifier()
	*/
	@Override
	public String visit(Type n, String argu) throws Exception {
		return n.f0.accept(this, null);
	}

	/**
	* f0 -> "int"
	* f1 -> "["
	* f2 -> "]"
	*/
	@Override
	public String visit(ArrayType n, String argu) {
		return "int[]";
	}

	/**
	* f0 -> "boolean"
	*/
	@Override
	public String visit(BooleanType n, String argu) {
		return "boolean";
	}

	/**
	* f0 -> "int"
	*/
	@Override
	public String visit(IntegerType n, String argu){
		return "int";
	}

	/**
	* f0 -> Block()
	*       | AssignmentStatement()
	*       | ArrayAssignmentStatement()
	*       | IfStatement()
	*       | WhileStatement()
	*       | PrintStatement()
	*/
	@Override
	public String visit(Statement n, String argu) throws Exception {
		return n.f0.accept(this, argu);
	}

	/**
	* f0 -> "{"
	* f1 -> ( Statement() )*
	* f2 -> "}"
	*/
	@Override
	public String visit(Block n, String argu) throws Exception {
		return n.f1.accept(this,argu);
	}

	/**
	 * f0 -> Identifier()
	* f1 -> "="
	* f2 -> Expression()
	* f3 -> ";"
	*/
	@Override
	public String visit(AssignmentStatement n, String argu) throws Exception {
		if(check){
			String id = n.f0.accept(this, argu);
			operation = "=";
			n.f2.accept(this,argu+" "+id);
			operation = "";
		}
		return " ";
	}

	/**
	 * f0 -> Identifier()
	* f1 -> "["
	* f2 -> Expression()
	* f3 -> "]"
	* f4 -> "="
	* f5 -> Expression()
	* f6 -> ";"
	*/
	@Override
	public String visit(ArrayAssignmentStatement n, String argu) throws Exception{
		if(check && argu!=null){
			operation = "";
			String id = n.f0.accept(this,argu);
			String num = n.f2.accept(this,argu);
			operation = "array";
			n.f5.accept(this,argu+" "+id+" "+num);
			operation = "";
		}
		return " ";
	}

	/**
	 * f0 -> "if"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> Statement()
	* f5 -> "else"
	* f6 -> Statement()
	*/
	@Override
	public String visit(IfStatement n, String argu) throws Exception{
		n.f2.accept(this, argu);
		WriteToFile.writefile("\tbr i1 %_"+Integer.toString(var_c-1)+", label %if_then_"+Integer.toString(if_num)+", label %if_else_"+Integer.toString(if_num)+"\n");
		WriteToFile.writefile("\tif_else_"+Integer.toString(if_num)+":\n");
		n.f6.accept(this,argu);
		WriteToFile.writefile("\tbr label %if_end_"+Integer.toString(if_num)+"\n");
		WriteToFile.writefile("\tif_then_"+Integer.toString(if_num)+":\n");
		n.f4.accept(this,argu);
		WriteToFile.writefile("\tbr label %if_end_"+Integer.toString(if_num)+"\n");
		WriteToFile.writefile("\tif_end_"+Integer.toString(if_num)+":\n");
		if_num++;
		return " ";
	}

	/**
	 * f0 -> "while"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> Statement()
	*/
	@Override
	public String visit(WhileStatement n, String argu) throws Exception{
		WriteToFile.writefile("\tbr label %loop"+Integer.toString(while_num)+"\n");
		WriteToFile.writefile("\tloop"+Integer.toString(while_num)+":\n");
		while_num++;
		n.f2.accept(this,argu);
		WriteToFile.writefile("\tbr i1 %_"+Integer.toString(var_c-1)+", label %loop"+Integer.toString(while_num)+", label %loop"+Integer.toString(while_num+1)+"\n");
		WriteToFile.writefile("\tloop"+Integer.toString(while_num)+":\n");
		while_num++;
		n.f4.accept(this,argu);
		WriteToFile.writefile("\tbr label %loop"+Integer.toString(while_num-2)+"\n");
		WriteToFile.writefile("\tloop"+Integer.toString(while_num)+":\n");
		while_num++;
		return " ";
	}

	/**
	 * f0 -> "System.out.println"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> ";"
	*/
	@Override
	public String visit(PrintStatement n, String argu) throws Exception {
		operation = "load";
		String str = n.f2.accept(this,argu);
		operation = "";
		if(Symbol_table.isNumeric(str)){
			WriteToFile.writefile("\tcall void (i32) @print_int(i32 "+str+")\n");
		}
		else
			WriteToFile.writefile("\tcall void (i32) @print_int(i32 %_"+Integer.toString(var_c-1)+")\n");
		// return n.f2.accept(this,argu);
		return null;
	}

	/**
	 * f0 -> AndExpression()
	*       | CompareExpression()
	*       | PlusExpression()
	*       | MinusExpression()
	*       | TimesExpression()
	*       | ArrayLookup()
	*       | ArrayLength()
	*       | MessageSend()
	*       | PrimaryExpression()
	*/
	@Override
	public String visit(Expression n, String argu) throws Exception {
		return n.f0.accept(this, argu);
	}

	/**
	 * f0 -> PrimaryExpression()
	* f1 -> "&&"
	* f2 -> PrimaryExpression()
	*/
	@Override
	public String visit(AndExpression n, String argu) throws Exception {
		operation = "load";
		n.f0.accept(this, argu);
		operation = "";
		WriteToFile.writefile("\tbr i1 %_"+Integer.toString(var_c-1)+", label %exp_res_"+Integer.toString(exp_res+1)+", label %exp_res_"+Integer.toString(exp_res)+"\n");
		WriteToFile.writefile("\t"+next_exp());
		WriteToFile.writefile("\tbr label %exp_res_"+Integer.toString(exp_res+2)+"\n");
		WriteToFile.writefile("\t"+next_exp());
		operation = "load";
		n.f2.accept(this, argu);
		operation = "";
		WriteToFile.writefile("\tbr label %exp_res_"+Integer.toString(exp_res)+"\n");
		WriteToFile.writefile("\t"+next_exp());
		WriteToFile.writefile("\tbr label %exp_res_"+Integer.toString(exp_res)+"\n");
		WriteToFile.writefile("\t"+next_exp());
		WriteToFile.writefile("\t"+next_var() + " = phi i1  [ 0, %exp_res_"+Integer.toString(exp_res-4)+" ], [ %_"+Integer.toString(var_c-2)+", %exp_res_"+Integer.toString(exp_res-2)+" ]\n");
		return n.f0.accept(this,argu) + "&&" + n.f2.accept(this,argu);
	}

	/**
	 * f0 -> PrimaryExpression()
	* f1 -> "<"
	* f2 -> PrimaryExpression()
	*/
	@Override
	public String visit(CompareExpression n, String argu) throws Exception {
		if(check){
			operation = "load";
			String str1 = n.f0.accept(this,argu);
			int var_num = var_c-1;
			String str2 = n.f2.accept(this,argu);
			if(!Symbol_table.isNumeric(str2)){
				str2 = "%_"+Integer.toString(var_c-1);
			}
			if(!Symbol_table.isNumeric(str1)){
				str1 = "%_"+Integer.toString(var_num);
			}
			WriteToFile.writefile("\t"+next_var()+" = icmp slt i32 "+str1+", "+str2+"\n");
			operation = "";
		}
		// return n.f0.accept(this,argu) + "<" + n.f2.accept(this,argu);
		return null;
	}

	/**
	 * f0 -> PrimaryExpression()
	* f1 -> "+"
	* f2 -> PrimaryExpression()
	*/
	@Override
	public String visit(PlusExpression n, String argu) throws Exception {
		String op = operation;
		int num_var = var_c-1;
		operation = "load";
		String str1 = n.f0.accept(this,argu);
		int var_num = var_c-1;
		operation = "load";
		String str2 = n.f2.accept(this,argu);
		if(!Symbol_table.isNumeric(str2)){
			str2 = "%_"+Integer.toString(var_c-1);
		}
		if(!Symbol_table.isNumeric(str1)){
			str1 = "%_"+Integer.toString(var_num);
		}
		WriteToFile.writefile("\t"+next_var()+" = add i32 "+str1+", "+str2+"\n");
		var_num = var_c-1;
		operation = "";
		if(op.equals("=")){
			String[] temp = argu.trim().split(" ");
			int num = get_var_offset(temp[0],temp[1],temp[2]);
			String type = get_type(temp[0],temp[1],temp[2]);
			if(num == -1){
				WriteToFile.writefile("\tstore "+type_to_bit(type)+" %_"+Integer.toString(var_c-1)+", "+type_to_bit(type)+"* %"+temp[2]+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32*\n");
				WriteToFile.writefile("\tstore i32 %_"+Integer.toString(var_c-3)+", i32* %_"+Integer.toString(var_c-1)+"\n");
			}
			WriteToFile.writefile("\tstore i32 %_"+Integer.toString(var_c-1)+", i32* %"+temp[2]+"\n");
		}
		else if(op.equals("array")){
			String[] temp = argu.trim().split(" ");
			// WriteToFile.writefile("\tstore i32 %_"+Integer.toString(var_c-1)+", i32* %"+temp[2]+"\n");
			int num = get_var_offset(temp[0],temp[1],temp[2]);
			String type = get_type(temp[0],temp[1],temp[2]);
			if(num == -1){
				WriteToFile.writefile("\t"+next_var()+" = load "+type_to_bit(type)+"*, "+type_to_bit(type)+"** %"+temp[2]+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32**\n");
				WriteToFile.writefile("\t"+next_var()+" = load i32*, i32** %_"+Integer.toString(var_c-2)+"\n");
			}
			int tempvar = var_c-1;
			WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
			int var_temp = var_c-1;
			if(!Symbol_table.isNumeric(temp[3])){
				int num2 = get_var_offset(temp[0],temp[1],temp[3]);
				String type2 = get_type(temp[0],temp[1],temp[3]);
				if(num2 == -1){
					WriteToFile.writefile("\t"+next_var()+" = load "+type_to_bit(type2)+", "+type_to_bit(type2)+"* %"+temp[3]+"\n");
				}
				else{
					WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num2+"\n");
					WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32*\n");
					WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
				}
				WriteToFile.writefile("\t"+next_var()+" = icmp sge i32 %_"+Integer.toString(var_c-2)+", 0\n");
				// WriteToFile.writefile("\t"+next_var()+" = icmp slt i32 %_"+Integer.toString(var_c-4)+", %_"+Integer.toString(var_c-3)+"\n");
				WriteToFile.writefile("\t"+next_var()+" = icmp slt i32 3, 4\n");

			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = icmp sge i32 "+temp[3]+", 0\n");
				WriteToFile.writefile("\t"+next_var()+" = icmp slt i32 "+temp[3]+", %_"+Integer.toString(var_temp)+"\n");
			}
			
			WriteToFile.writefile("\t"+next_var()+" = and i1 %_"+Integer.toString(var_c-3)+", %_"+Integer.toString(var_c-2)+"\n");
			WriteToFile.writefile("\tbr i1 %_"+Integer.toString(var_c-1)+", label %oob_ok_"+Integer.toString(oob_num)+", label %oob_err_"+Integer.toString(oob_num)+"\n");
			WriteToFile.writefile("\toob_err_"+Integer.toString(oob_num)+":\n");
			WriteToFile.writefile("\tcall void @throw_oob()\n");
			WriteToFile.writefile("\tbr label %oob_ok_"+Integer.toString(oob_num)+"\n");
			WriteToFile.writefile("\toob_ok_"+Integer.toString(oob_num)+":\n");
			if(!Symbol_table.isNumeric(temp[3])){
				int num2 = get_var_offset(temp[0],temp[1],temp[3]);
				String type2 = get_type(temp[0],temp[1],temp[3]);
				if(num2 == -1){
					WriteToFile.writefile("\t"+next_var()+" = load "+type_to_bit(type2)+", "+type_to_bit(type2)+"* %"+temp[3]+"\n");
				}
				else{
					WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num2+"\n");
					WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32*\n");
					WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
				}
				WriteToFile.writefile("\t"+next_var()+" = add i32 1, %_"+Integer.toString(var_c-2)+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = add i32 1, "+temp[3]+"\n");
			}
			WriteToFile.writefile("\t"+next_var()+" = getelementptr i32, i32* %_"+Integer.toString(tempvar)+", i32 %_"+Integer.toString(var_c-2)+"\n");

			WriteToFile.writefile("\tstore i32 "+Integer.toString(var_num)+", i32* %_"+Integer.toString(var_c-1)+"\n");
			oob_num++;
		}
		return null;
	}

	/**
	 * f0 -> PrimaryExpression()
	* f1 -> "-"
	* f2 -> PrimaryExpression()
	*/
	@Override
	public String visit(MinusExpression n, String argu) throws Exception {
		String op = operation;
		int num_var = var_c-1;
		operation = "load";
		String str1 = n.f0.accept(this,argu);
		int var_num = var_c-1;
		operation = "load";
		String str2 = n.f2.accept(this,argu);
		if(!Symbol_table.isNumeric(str2)){
			str2 = "%_"+Integer.toString(var_c-1);
		}
		if(!Symbol_table.isNumeric(str1)){
			str1 = "%_"+Integer.toString(var_num);
		}
		WriteToFile.writefile("\t"+next_var()+" = sub i32 "+str1+", "+str2+"\n");
		var_num = var_c-1;
		operation = "";
		if(op.equals("=")){
			String[] temp = argu.trim().split(" ");
			int num = get_var_offset(temp[0],temp[1],temp[2]);
			String type = get_type(temp[0],temp[1],temp[2]);
			if(num == -1){
				WriteToFile.writefile("\tstore "+type_to_bit(type)+" %_"+Integer.toString(var_c-1)+", "+type_to_bit(type)+"* %"+temp[2]+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32*\n");
				WriteToFile.writefile("\tstore i32 %_"+Integer.toString(var_c-3)+", i32* %_"+Integer.toString(var_c-1)+"\n");
			}
			WriteToFile.writefile("\tstore i32 %_"+Integer.toString(var_c-1)+", i32* %"+temp[2]+"\n");
		}
		else if(op.equals("array")){
			String[] temp = argu.trim().split(" ");
			// WriteToFile.writefile("\tstore i32 %_"+Integer.toString(var_c-1)+", i32* %"+temp[2]+"\n");
			int num = get_var_offset(temp[0],temp[1],temp[2]);
			String type = get_type(temp[0],temp[1],temp[2]);
			if(num == -1){
				WriteToFile.writefile("\t"+next_var()+" = load "+type_to_bit(type)+"*, "+type_to_bit(type)+"** %"+temp[2]+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32**\n");
				WriteToFile.writefile("\t"+next_var()+" = load i32*, i32** %_"+Integer.toString(var_c-2)+"\n");
			}
			int tempvar = var_c-1;
			WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
			int var_temp = var_c-1;
			if(!Symbol_table.isNumeric(temp[3])){
				int num2 = get_var_offset(temp[0],temp[1],temp[3]);
				String type2 = get_type(temp[0],temp[1],temp[3]);
				if(num2 == -1){
					WriteToFile.writefile("\t"+next_var()+" = load "+type_to_bit(type2)+", "+type_to_bit(type2)+"* %"+temp[3]+"\n");
				}
				else{
					WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num2+"\n");
					WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32*\n");
					WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
				}
				WriteToFile.writefile("\t"+next_var()+" = icmp sge i32 %_"+Integer.toString(var_c-2)+", 0\n");
				// WriteToFile.writefile("\t"+next_var()+" = icmp slt i32 %_"+Integer.toString(var_c-3)+", %_"+Integer.toString(var_c-3)+"\n");
				WriteToFile.writefile("\t"+next_var()+" = icmp slt i32 3, 4\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = icmp sge i32 "+temp[3]+", 0\n");
				WriteToFile.writefile("\t"+next_var()+" = icmp slt i32 "+temp[3]+", %_"+Integer.toString(var_temp)+"\n");
			
			}
			
			WriteToFile.writefile("\t"+next_var()+" = and i1 %_"+Integer.toString(var_c-3)+", %_"+Integer.toString(var_c-2)+"\n");
			WriteToFile.writefile("\tbr i1 %_"+Integer.toString(var_c-1)+", label %oob_ok_"+Integer.toString(oob_num)+", label %oob_err_"+Integer.toString(oob_num)+"\n");
			WriteToFile.writefile("\toob_err_"+Integer.toString(oob_num)+":\n");
			WriteToFile.writefile("\tcall void @throw_oob()\n");
			WriteToFile.writefile("\tbr label %oob_ok_"+Integer.toString(oob_num)+"\n");
			WriteToFile.writefile("\toob_ok_"+Integer.toString(oob_num)+":\n");
			if(!Symbol_table.isNumeric(temp[3])){
				int num2 = get_var_offset(temp[0],temp[1],temp[3]);
				String type2 = get_type(temp[0],temp[1],temp[3]);
				if(num2 == -1){
					WriteToFile.writefile("\t"+next_var()+" = load "+type_to_bit(type2)+", "+type_to_bit(type2)+"* %"+temp[3]+"\n");
				}
				else{
					WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num2+"\n");
					WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32*\n");
					WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
				}
				WriteToFile.writefile("\t"+next_var()+" = add i32 1, %_"+Integer.toString(var_c-2)+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = add i32 1, "+temp[3]+"\n");
			}
			WriteToFile.writefile("\t"+next_var()+" = getelementptr i32, i32* %_"+Integer.toString(tempvar)+", i32 %_"+Integer.toString(var_c-2)+"\n");

			WriteToFile.writefile("\tstore i32 "+Integer.toString(var_num)+", i32* %_"+Integer.toString(var_c-1)+"\n");
			oob_num++;
		}
		return null;
	}

	/**
	 * f0 -> PrimaryExpression()
	* f1 -> "*"
	* f2 -> PrimaryExpression()
	*/
	@Override
	public String visit(TimesExpression n, String argu) throws Exception {
		operation = "load";
		int var_num;
		String str1 = n.f0.accept(this,argu);
		var_num = var_c-1;
		String str2 = n.f2.accept(this,argu);
		boolean flg = false;
		if(!Symbol_table.isNumeric(str2)){
			str2 = "%_"+Integer.toString(var_c-1);
		}
		if(!Symbol_table.isNumeric(str1)){
			flg = true;
			str1 = "%_"+Integer.toString(var_num);
		}
		WriteToFile.writefile("\t"+next_var()+" = mul i32 "+str2+", "+str1+"\n");
		operation = "+";
		if(flg){
			n.f0.accept(this,argu);
		}
		else{
			n.f2.accept(this,argu);
		}
		operation = "";
		return null;
	}

	/**
	 * f0 -> PrimaryExpression()
	* f1 -> "["
	* f2 -> PrimaryExpression()
	* f3 -> "]"
	*/
	@Override
	public String visit(ArrayLookup n, String argu) throws Exception {
		if(check){
			operation = "";
			String num = n.f2.accept(this,argu);
			operation = "array";
			n.f0.accept(this,argu+" "+num);
			operation = "";
		}
		return null;
	}

	/**
	 * f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> "length"
	*/
	@Override
	public String visit(ArrayLength n, String argu) throws Exception {
		return n.f0.accept(this,argu) + ".length";
	}

	/**
	 * f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> Identifier()
	* f3 -> "("
	* f4 -> ( ExpressionList() )?
	* f5 -> ")"
	*/
	@Override
	public String visit(MessageSend n, String argu) throws Exception {
		String op = operation;
		if(check)
			operation = "load";
		int vvv = var_c;
		String str = n.f4.present() ? n.f4.accept(this, argu) : "";
		int vvv2 = var_c;
		if(check){
			operation="";
			int num = 0;
			String type = "";
			operation = "message";
			String class_name = n.f0.accept(this,argu);
			int var_temp = var_c-1;
			if(!operation.equals("new")){
				if(class_name.equals("this")){
					WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %this to i8***\n");
				}
				else{
					WriteToFile.writefile("\t"+next_var()+" = load i8*, i8** %"+class_name+"\n");
					WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+Integer.toString(var_c-2)+" to i8***\n");
				}
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+Integer.toString(var_c-4)+" to i8***\n");
			}
			WriteToFile.writefile("\t"+next_var()+" = load i8**, i8*** %_"+Integer.toString(var_c-2)+"\n");
			String[] temp;
			if(argu!=null){
				temp = argu.trim().split(" ");
				type = get_type(temp[0],temp[1],class_name);
				if(type.equals("this")){
					type = temp[0];
				}
				for(V_table vv : v_table){
					if(vv.name.equals(type)){
						num = vv.offset_map.get(n.f2.accept(this,null));
						if(num!=0){
							num = num / 8;
						}
					}
				}
			}
			WriteToFile.writefile("\t"+next_var()+" = getelementptr i8*, i8** %_"+Integer.toString(var_c-2)+", i32 "+Integer.toString(num)+"\n");
			WriteToFile.writefile("\t"+next_var()+" = load i8*, i8** %_"+Integer.toString(var_c-2)+"\n");

			WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+Integer.toString(var_c-2)+" to "+get_signature(type,n.f2.accept(this,argu))+"\n");
			String str2;
			if(class_name.equals("this")){
				str2 = "%this";
			}
			else{
				// System.out.println("d"+vvv);
				str2 = "%_"+Integer.toString(vvv);
			}
			if(str.equals("null")||str.equals("")){
				if(vvv2==vvv){
					WriteToFile.writefile("\t"+next_var()+" = call i32 %_"+Integer.toString(var_c-2) + "(i8* "+str2+")\n");
				}
				else
					WriteToFile.writefile("\t"+next_var()+" = call i32 %_"+Integer.toString(var_c-2) + "(i8* "+str2+", i32 %_"+Integer.toString(vvv2-1)+")\n");
				temp = argu.trim().split(" ");
				if(op.equals("=")){
					WriteToFile.writefile("\tstore i32 %_"+Integer.toString(var_c-1)+", i32* %"+temp[2]+"\n");
				}
			}
			else{
				temp = argu.trim().split(" ");
				String[] temp2 = str.split(",");
				int len = temp2.length-1;
				String str3 = "";
				int count = 0;
				for(String s : temp2){
					if(Symbol_table.isNumeric(s)){
						str3 += ", i32 "+s;
						continue;
					}
					String type2 = get_type(temp[0],temp[1],s);
					str3+=", "+type_to_bit(type2)+" %_"+Integer.toString(var_temp-len+count);
					count++;
				}
				WriteToFile.writefile("\t"+next_var()+" = call i32 %_"+Integer.toString(var_c-2) + "(i8* "+str2+str3+")\n");
				if(op.equals("=")){
					WriteToFile.writefile("\tstore i32 %_"+Integer.toString(var_c-1)+", i32* %"+temp[2]+"\n");
				}
			}
		}
		return null;
		// return n.f0.accept(this,argu) + "." + n.f2.accept(this,argu)+"(" + str + ")";
	}
	
	/**
	 * f0 -> Expression()
	 * f1 -> ExpressionTail()
	*/
	@Override
	public String visit(ExpressionList n, String argu) throws Exception {
		return n.f0.accept(this,argu) + n.f1.accept(this,argu);
	}

	/**
	 * f0 -> ( ExpressionTerm() )*
	*/
	@Override
	public String visit(ExpressionTail n, String argu) throws Exception {

		String ret = "";
		NodeListOptional varDecls = n.f0;
		for(int i =0;i<varDecls.size();++i){
			ExpressionTerm varDecl = (ExpressionTerm) varDecls.elementAt(i);
			ret = ret + ","+varDecl.f1.accept(this,argu);
		}
		return ret;
	}

	/**
	 * f0 -> ","
	* f1 -> Expression()
	*/
	@Override
	public String visit(ExpressionTerm n, String argu) throws Exception {
		//kalei expression
		return n.f1.accept(this,argu);
	}

	/**
	 * f0 -> IntegerLiteral()
	*       | TrueLiteral()
	*       | FalseLiteral()
	*       | Identifier()
	*       | ThisExpression()
	*       | ArrayAllocationExpression()
	*       | AllocationExpression()
	*       | NotExpression()
	*       | BracketExpression()
	*/
	@Override
	public String visit(PrimaryExpression n, String argu) throws Exception {
		return n.f0.accept(this, argu);
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	*/
	@Override
	public String visit(IntegerLiteral n, String argu) throws Exception {
		if(check && operation.equals("=")){
			String[] temp = argu.trim().split(" ");
			int num = get_var_offset(temp[0],temp[1],temp[2]);
			if(num == -1){
				WriteToFile.writefile("\tstore i32 "+n.f0.toString()+", i32* %"+temp[2]+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i1*\n");
				WriteToFile.writefile("\tstore i1 %_"+Integer.toString(var_c-3)+", i1* %_"+Integer.toString(var_c-1)+"\n");
			}
			operation = "";
		}
		else if(check && operation.equals("array")){
			operation = "";
			String[] temp = argu.trim().split(" ");
			WriteToFile.writefile("\t"+next_var()+" = load i32*, i32** %"+temp[2]+"\n");
			WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
			WriteToFile.writefile("\t"+next_var()+" = icmp sge i32 "+temp[3]+", 0\n");
			WriteToFile.writefile("\t"+next_var()+" = icmp slt i32 "+temp[3]+", %_"+Integer.toString(var_c-3)+"\n");
			WriteToFile.writefile("\t"+next_var()+" = and i1 %_"+Integer.toString(var_c-3)+", %_"+Integer.toString(var_c-2)+"\n");
			WriteToFile.writefile("\tbr i1 %_"+Integer.toString(var_c-1)+", label %oob_ok_"+Integer.toString(oob_num)+", label %oob_err_"+Integer.toString(oob_num)+"\n");
			WriteToFile.writefile("\toob_err_"+Integer.toString(oob_num)+":\n");
			WriteToFile.writefile("\tcall void @throw_oob()\n");
			WriteToFile.writefile("\tbr label %oob_ok_"+Integer.toString(oob_num)+"\n");
			WriteToFile.writefile("\toob_ok_"+Integer.toString(oob_num)+":\n");
			WriteToFile.writefile("\t"+next_var()+" = add i32 1, "+temp[3]+"\n");
			WriteToFile.writefile("\t"+next_var()+" = getelementptr i32, i32* %_"+Integer.toString(var_c-7)+", i32 %_"+Integer.toString(var_c-2)+"\n");
			WriteToFile.writefile("\tstore i32 "+n.f0.toString()+", i32* %_"+Integer.toString(var_c-1)+"\n");
			oob_num++;
		}
		return n.f0.toString();    
	}

	/**
	 * f0 -> "true"
	*/
	@Override
	public String visit(TrueLiteral n, String argu) throws Exception {
		if(check && operation.equals("=")){
			String[] temp = argu.trim().split(" ");
			int num = get_var_offset(temp[0],temp[1],temp[2]);
			if(num == -1){
				WriteToFile.writefile("\tstore i1 "+1+", i1* %"+temp[2]+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i1*\n");
				WriteToFile.writefile("\tstore i1 %_"+Integer.toString(var_c-3)+", i1* %_"+Integer.toString(var_c-1)+"\n");
			}
			operation = "";
		}
		return "true";    
	}

	/**
	 * f0 -> "false"
	*/
	@Override
	public String visit(FalseLiteral n, String argu) throws Exception {
		if(check && operation.equals("=")){
			String[] temp = argu.trim().split(" ");
			int num = get_var_offset(temp[0],temp[1],temp[2]);
			if(num == -1){
				WriteToFile.writefile("\tstore i1 "+0+", i1* %"+temp[2]+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i1*\n");
				WriteToFile.writefile("\tstore i1 %_"+Integer.toString(var_c-3)+", i1* %_"+Integer.toString(var_c-1)+"\n");
			}
			operation = "";
		}
		return "false";    
	}

	
	/**
	* f0 -> <IDENTIFIER>
	*/
	@Override
	public String visit(Identifier n, String argu) {
		if(check && (operation.equals("=")||operation.equals("+"))){
			String[] temp = argu.trim().split(" ");
			int num = get_var_offset(temp[0],temp[1],temp[2]);
			String type = get_type(temp[0],temp[1],temp[2]);

			if(operation.equals("=")){
				num = get_var_offset(temp[0],temp[1],n.f0.toString());
				type = get_type(temp[0],temp[1],n.f0.toString());
				if(num == -1){
					//d
					WriteToFile.writefile("\t"+next_var()+" = load "+type_to_bit(type)+", "+type_to_bit(type)+"* %"+n.f0.toString()+"\n");
				}
				else{
					WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
					WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32*\n");
					WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
				}
			}
			num = get_var_offset(temp[0],temp[1],temp[2]);
			type = get_type(temp[0],temp[1],temp[2]);
			if(num == -1){
				WriteToFile.writefile("\tstore "+type_to_bit(type)+" %_"+Integer.toString(var_c-1)+", "+type_to_bit(type)+"* %"+temp[2]+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32*\n");
				WriteToFile.writefile("\tstore i32 %_"+Integer.toString(var_c-3)+", i32* %_"+Integer.toString(var_c-1)+"\n");
			}
			operation = "";
		}
		else if(check && operation.equals("return")){
			operation = "";
			String[] temp = argu.trim().split(" ");
			int num = get_var_offset(temp[0],temp[1],n.f0.toString());
			if(num == -1){
				WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %"+n.f0.toString()+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32*\n");
				WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
			}
		}
		if(check && operation.equals("load")){
			String[] temp = argu.trim().split(" ");
			int num = get_var_offset(temp[0],temp[1],n.f0.toString());
			String type = get_type(temp[0],temp[1],n.f0.toString());
			if(num == -1){
				WriteToFile.writefile("\t"+next_var()+" = load "+type_to_bit(type)+", "+type_to_bit(type)+"* %"+n.f0.toString()+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32*\n");
				WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
			}
		}
		if(check && operation.equals("array")){
			operation = "";
			String[] temp = argu.trim().split(" ");
			//load id;
			int num = get_var_offset(temp[0],temp[1],temp[2]);
			String type = get_type(temp[0],temp[1],temp[2]);
			if(num == -1){
				if(Symbol_table.isNumeric(temp[2])){
					
				}
				else
					WriteToFile.writefile("\t"+next_var()+" = load "+type_to_bit(type)+", "+type_to_bit(type)+"* %"+temp[2]+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+temp[2]+" to i32*\n");
				WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+temp[2]+"\n");
			}
			int var_num = var_c-1;
			num = get_var_offset(temp[0],temp[1],n.f0.toString());
			type = get_type(temp[0],temp[1],n.f0.toString());
			if(num == -1){
				WriteToFile.writefile("\t"+next_var()+" = load "+type_to_bit(type)+", "+type_to_bit(type)+"* %"+n.f0.toString()+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+Integer.toString(var_c-2)+" to i32**\n");
				WriteToFile.writefile("\t"+next_var()+" = load i32*, i32** %_"+Integer.toString(var_c-2)+"\n");
			}
			// WriteToFile.writefile("\t"+next_var()+" = load i32*, i32** %"+n.f0.toString()+"\n");
			WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
			if(Symbol_table.isNumeric(temp[2])){
				WriteToFile.writefile("\t"+next_var()+" = icmp sge i32 "+temp[2]+", 0\n");
				WriteToFile.writefile("\t"+next_var()+" = icmp slt i32 "+temp[2]+", %_"+Integer.toString(var_c-3)+"\n");

			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = icmp sge i32 %_"+Integer.toString(var_num)+", 0\n");
				WriteToFile.writefile("\t"+next_var()+" = icmp slt i32 3, 4\n");
			}
			WriteToFile.writefile("\t"+next_var()+" = and i1 %_"+Integer.toString(var_c-3)+", %_"+Integer.toString(var_c-2)+"\n");
			WriteToFile.writefile("\tbr i1 %_"+Integer.toString(var_c-1)+", label %oob_ok_"+Integer.toString(oob_num)+", label %oob_err_"+Integer.toString(oob_num)+"\n");
			WriteToFile.writefile("\toob_err_"+Integer.toString(oob_num)+":\n");
			WriteToFile.writefile("\tcall void @throw_oob()\n");
			WriteToFile.writefile("\tbr label %oob_ok_"+Integer.toString(oob_num)+"\n");
			WriteToFile.writefile("\toob_ok_"+Integer.toString(oob_num)+":\n");
			if(Symbol_table.isNumeric(temp[2])){
				WriteToFile.writefile("\t"+next_var()+" = add i32 1, "+temp[2]+"\n");
			}
			else
				WriteToFile.writefile("\t"+next_var()+" = add i32 1, "+Integer.toString(var_num)+"\n");
			WriteToFile.writefile("\t"+next_var()+" = getelementptr i32, i32* %_"+Integer.toString(var_c-7)+", i32 %_"+Integer.toString(var_c-2)+"\n");
			//store
			WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
			// WriteToFile.writefile("\t"+next_var()+" = load i32, i32* %_"+Integer.toString(var_c-2)+"\n");
			oob_num++;
		}
		return n.f0.toString();   
	}

	/**
	* f0 -> "this"
	*/
	@Override
	public String visit(ThisExpression n, String argu) throws Exception {
		return "this";
	}

	/**
	 * f0 -> "new"
	* f1 -> "int"
	* f2 -> "["
	* f3 -> Expression()
	* f4 -> "]"
	*/
	@Override
	public String visit(ArrayAllocationExpression n, String argu) throws Exception {
		if(check && argu!=null){
			String[] temp = argu.trim().split(" ");
			operation = "load";
			String str = n.f3.accept(this,argu);
			operation = "";
			if(Symbol_table.isNumeric(str)){
				WriteToFile.writefile("\t"+next_var()+" = add i32 1, "+str+"\n");
			}
			else
				WriteToFile.writefile("\t"+next_var()+" = add i32 1, %_"+Integer.toString(var_c-2)+"\n");
			WriteToFile.writefile("\t"+next_var()+" = icmp sge i32 %_"+Integer.toString(var_c-2)+", 1\n");
			WriteToFile.writefile("\tbr i1 %_"+Integer.toString(var_c-1)+", label %nsz_ok_"+Integer.toString(nsz_num)+", label %nsz_err_"+Integer.toString(nsz_num)+"\n");
			WriteToFile.writefile("\tnsz_err_"+Integer.toString(nsz_num)+":\n");
			WriteToFile.writefile("\tcall void @throw_nsz()\n");
			WriteToFile.writefile("\tbr label %nsz_ok_"+Integer.toString(nsz_num)+"\n");
			WriteToFile.writefile("\tnsz_ok_"+Integer.toString(nsz_num)+":\n");
			WriteToFile.writefile("\t"+next_var()+" = call i8 * @calloc(i32 %_"+Integer.toString(var_c-3)+", i32 4)\n");
			WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+Integer.toString(var_c-2)+" to i32*\n");
			if(Symbol_table.isNumeric(str)){
				WriteToFile.writefile("\tstore i32 "+str+", i32* %_"+Integer.toString(var_c-1)+"\n");
			}
			else{
				WriteToFile.writefile("\tstore i32 "+Integer.toString(var_c-4)+", i32* %_"+Integer.toString(var_c-1)+"\n");
			}
			// WriteToFile.writefile("\tstore i32 "+n.f3.accept(this,argu)+", i32* %_"+Integer.toString(var_c-1)+"\n");
			int num = get_var_offset(temp[0],temp[1],temp[2]);
			String type = get_type(temp[0],temp[1],temp[2]);
			if(num == -1){
				// System.out.println(type_to_bit(type));
				WriteToFile.writefile("\tstore "+type_to_bit(type)+" %_"+Integer.toString(var_c-1)+", "+type_to_bit(type)+"* %"+temp[2]+"\n");
			}
			else{
				WriteToFile.writefile("\t"+next_var()+" = getelementptr i8, i8* %this, i32 "+num+"\n");
				WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+ Integer.toString(var_c-2)+" to i32**\n");
				WriteToFile.writefile("\tstore i32* %_"+Integer.toString(var_c-3)+", i32** %_"+Integer.toString(var_c-1)+"\n");
			}
			operation = "";
			// WriteToFile.writefile("\tstore i32* %_"+Integer.toString(var_c-1)+", i32** %"+temp[2]+"\n");
			nsz_num++;
		}
		return null;
		// return "new int["+n.f3.accept(this,argu)+"]";
	}

	/**
	 * f0 -> "new"
	* f1 -> Identifier()
	* f2 -> "("
	* f3 -> ")"
	*/
	@Override
	public String visit(AllocationExpression n, String argu) throws Exception {
		if(check && argu!=null){
			String op = operation;
			operation = "";
			String[] temp = argu.trim().split(" ");
			int num = 0;
			String global = "";
			for(Symbol_table table : s_table){
				if(table.name.equals(n.f1.accept(this,argu))){
					for(V_table vv : v_table){
						if(vv.name.equals(table.name)){
							global = vv.global;
						}
					}
					for (String key: table.vardecl_map.keySet()) {
						if(key!=""){
							num+=type_to_int(table.vardecl_map.get(key));
						}
					}
				}
				for(Symbol_table ext_table : table.ext_classes){
					if(ext_table.name.equals(n.f1.accept(this,argu))){
						for(V_table vv : v_table){
							if(vv.name.equals(ext_table.name)){
								global = vv.global;
							}
						}
						for (String key: table.vardecl_map.keySet()) {
							if(key!=""){
								num+=type_to_int(table.vardecl_map.get(key));
							}
						}

						for (String key: ext_table.vardecl_map.keySet()) {
							if(key!=""){
								num+=type_to_int(ext_table.vardecl_map.get(key));
							}
						}
					}
				}
			}
			num+=8;
			WriteToFile.writefile("\t"+next_var()+" = call i8* @calloc(i32 1, i32 "+num+")\n");
			WriteToFile.writefile("\t"+next_var()+" = bitcast i8* %_"+Integer.toString(var_c-2)+" to i8***\n");
			WriteToFile.writefile("\t"+next_var()+" = getelementptr "+global+", "+global+"* @."+n.f1.accept(this, argu)+"_vtable, i32 0, i32 0\n");
			WriteToFile.writefile("\tstore i8** %_"+Integer.toString(var_c-1)+", i8*** %_"+Integer.toString(var_c-2)+"\n");
			if(!op.equals("message"))
				WriteToFile.writefile("\tstore i8* %_"+Integer.toString(var_c-3)+", i8** %"+temp[2]+"\n");
			operation = "new";
		}
		return n.f1.accept(this,argu);
		// return "new "+n.f1.accept(this, argu)+"()";
	}

	/**
	 * f0 -> "!"
	* f1 -> PrimaryExpression()
	*/
	@Override
	public String visit(NotExpression n, String argu) throws Exception {
		n.f1.accept(this, argu);
		WriteToFile.writefile("\t"+next_var()+" = mul i1 %_"+Integer.toString(var_c-2)+", -1\n");
		return null;
	}

	/**
	 * f0 -> "("
	* f1 -> Expression()
	* f2 -> ")"
	*/
	@Override
	public String visit(BracketExpression n, String argu) throws Exception {
		return "("+n.f1.accept(this,argu)+")";

	}

	String input_str = "declare i8* @calloc(i32, i32)\ndeclare i32 @printf(i8*, ...)\ndeclare void @exit(i32)\n\n@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n@_cNSZ = constant [15 x i8] c\"Negative size\\0a\\00\"\n\ndefine void @print_int(i32 %i) {\n\t%_str = bitcast [4 x i8]* @_cint to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n\tret void\n}\n\ndefine void @throw_oob() {\n\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str)\n\tcall void @exit(i32 1)\n\tret void\n}\n\ndefine void @throw_nsz() {\n\t%_str = bitcast [15 x i8]* @_cNSZ to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str)\n\tcall void @exit(i32 1)\n\tret void\n}\n";
}
