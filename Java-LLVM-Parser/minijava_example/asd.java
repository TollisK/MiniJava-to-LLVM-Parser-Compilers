import syntaxtree.*;
import visitor.*;

import java.util.*;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class asd {
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

                Goal root = parser.Goal();

                System.err.println("Program parsed successfully.");

                MyVisitor eval = new MyVisitor();
                root.accept(eval, "table");
                root.accept(eval, "check");
                eval.Offset_check(eval.s_table);
                eval.DeleteList();
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
}

class MyVisitor extends GJDepthFirst<String, String>{

    static List <Symbol_table> s_table = new ArrayList<Symbol_table>();
    static List <Ext_class> ext_class_list = new ArrayList<Ext_class>();
    int table_index = 0;
    boolean check = true;
    public void DeleteList(){
        s_table.clear();
        ext_class_list.clear();
    }
    public void Offset_check(List<Symbol_table> s_table){
        for(Symbol_table table : s_table){
            int offset = 0;
            int offset2 = 0;
            System.out.println("-----------Class "+table.name + "-----------");
            System.out.println("--Variables---");
            for (String key: table.vardecl_map.keySet()) {
                System.out.println(table.name + "." + key + " : " + offset);
                String type = table.vardecl_map.get(key);
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
                offset2 += 8;
            }
            for(Symbol_table table2 : table.ext_classes){
                System.out.println("-----------Class "+table2.name + "-----------");
                System.out.println("--Variables---");
                for (String key: table2.vardecl_map.keySet()) {
                    System.out.println(table2.name + "." + key + " : " + offset);
                    String type = table2.vardecl_map.get(key);
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
                for(Method method : table2.method_list){
                    boolean flg = false;
                    for(Method method2 : table.method_list){
                        if(method2.name.equals(method.name)){
                            flg = true;
                        }
                    }
                    if(!flg){
                        System.out.println(table2.name + "." + method.name + " : " + offset2);
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
            
            s_table.add(new Symbol_table(classname));                                            // add symboltable
            s_table.get(table_index).put_method("main","void");
            NodeListOptional varDecls = n.f14;                                     /* Print the variable inside this class */
            for (int i = 0; i < varDecls.size(); ++i) {
                VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
                String varname = varDecl.f1.accept(this,null);
                String varType = varDecl.f0.accept(this,null);
    
                s_table.get(table_index).put_var_in_method("main", varname, varType);                           // inser variables in class table
            }
            return null;
        }
        else{
            check = true;
            table_index = 0;
            NodeListOptional varDecls = n.f14;
            for (int i = 0; i < varDecls.size(); ++i) {
                VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
                String varType = varDecl.f0.accept(this,null);
                if(varType.equals("int")||varType.equals("boolean")||varType.equals("int[]")){
                    continue;
                }
                boolean flg = false;
                for(Ext_class eclass : ext_class_list){
                    if(eclass.name.equals(varType)){
                        flg = true;
                    }
                }
                for(Symbol_table table : s_table){
                    if(table.name.equals(varType)){
                        flg = true;
                    }
                }
                if(!flg){
                    System.out.println("An error occured! No type found!");
                    System.exit(0);  
                }
            }
            n.f15.accept(this,"main"+"+"+n.f1.accept(this, null));
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
            table_index++;                                      // add a new symbol table for the new class
            s_table.add(new Symbol_table(classname));
            NodeListOptional varDecls = n.f3;                                       /* Print the variable inside this class */
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
            NodeListOptional methDecls = n.f4;                                       /* Print the variable inside this class */
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
            //Vlepei na oi metavlhtes exoyn swsto type
            NodeListOptional varDecls = n.f3;
            for (int i = 0; i < varDecls.size(); ++i) {
                VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
                String varType = varDecl.f0.accept(this,null);
                if(varType.equals("int")||varType.equals("boolean")||varType.equals("int[]")){
                    continue;
                }
                boolean flg = false;
                for(Ext_class eclass : ext_class_list){
                    if(eclass.name.equals(varType)){
                        flg = true;
                    }
                }
                for(Symbol_table table : s_table){
                    if(table.name.equals(varType)){
                        flg = true;
                    }
                }
                if(!flg){
                    System.out.println("An error occured! No type found!");
                    System.exit(0);  
                }
            }
            NodeListOptional methDecls = n.f4;                                       /* Print the variable inside this class */
            for (int i = 0; i < methDecls.size(); ++i) {
                MethodDeclaration methDecl = (MethodDeclaration) methDecls.elementAt(i);
                String argumentList = methDecl.f4.present() ? methDecl.f4.accept(this, null) : "";
                String type = methDecl.f1.accept(this, null);
                if(type.equals("int")||type.equals("boolean")||type.equals("int[]")){
                    continue;
                }
                boolean flg = false;
                for(Ext_class eclass : ext_class_list){
                    if(eclass.name.equals(type)){
                        flg = true;
                    }
                }
                for(Symbol_table table : s_table){
                    if(table.name.equals(type)){
                        flg = true;
                    }
                }
                if(!flg){
                    System.out.println("An error occured! No type found!");
                    System.exit(0);  
                }
                if(!argumentList.equals("")){
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
                        if(par_type.equals("int")||par_type.equals("boolean")||par_type.equals("int[]")){
                            continue;
                        }
                        flg = false;
                        for(Ext_class eclass : ext_class_list){
                            if(eclass.name.equals(par_type)){
                                flg = true;
                            }
                        }
                        for(Symbol_table table : s_table){
                            if(table.name.equals(par_type)){
                                flg = true;
                            }
                        }
                        if(!flg){
                            System.out.println("An error occured! No type found!");
                            System.exit(0);  
                        }
                    }
                }
                NodeListOptional varDes = methDecl.f7;         
                for (int j = 0; j < varDes.size(); ++j) {
                    VarDeclaration varDe = (VarDeclaration) varDes.elementAt(j);
                    String var_type = varDe.f0.accept(this,null);
                    if(var_type.equals("int")||var_type.equals("boolean")||var_type.equals("int[]")){
                        continue;
                    }
                    flg = false;
                    for(Ext_class eclass : ext_class_list){
                        if(eclass.name.equals(var_type)){
                            flg = true;
                        }
                    }
                    for(Symbol_table table : s_table){
                        if(table.name.equals(var_type)){
                            flg = true;
                        }
                    }
                    if(!flg){
                        System.out.println("An error occured! No type found!");
                        System.exit(0);  
                    }
                }
            }

            //Gia kathe methodo vlepei to return
            for (int i = 0; i < methDecls.size(); ++i) {
                MethodDeclaration methDecl = (MethodDeclaration) methDecls.elementAt(i);
                String meth_name = methDecl.f2.accept(this,null);
                String meth_type = methDecl.f1.accept(this,null);
                int method_index = 0;
                for(Method method : s_table.get(table_index).method_list){
                    if(method.name.equals(meth_name)){
                        break;
                    }
                    method_index++;
                }
                String str = methDecl.f10.accept(this,null);
                String[] input = str.split("\\+|\\<|&&|\\-|\\*|\\[|.length");
                for(String s : input){
                    if(str.contains("+")||str.contains("-")||str.contains("<")||str.contains("*")||str.contains(".length")){
                        if(!meth_type.equals("int")){
                            System.out.println("An error occured! Inconsistent return value!");
                            System.exit(0);  
                        }
                    }
                    else if(str.contains("[")){
                        if(!meth_type.equals("int[]")){
                            System.out.println("An error occured! Inconsistent return value!");
                            System.exit(0);  
                        }
                    }
                    else if(str.contains("&&")){
                        if(!meth_type.equals("boolean")){
                            System.out.println("An error occured! Inconsistent return value!");
                            System.exit(0);  
                        }
                    }
                    if(Symbol_table.isNumeric(s)||s.equals("false")||s.equals("true")){
                        if(Symbol_table.isNumeric(s)&&!meth_type.equals("int")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0);
                        }
                        else if(s.equals("false")||s.equals("true")){
                            if(!meth_type.equals("boolean")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0);
                            }
                        }
                        continue;
                    }
                    if(!s_table.get(table_index).method_list.get(method_index).var_map.containsKey(s)){
                        if(!s_table.get(table_index).method_list.get(method_index).param_map.containsKey(s)){
                            if(!s_table.get(table_index).vardecl_map.containsKey(s)){
                                System.out.println("An error occured! No variable found!");
                                System.exit(0);
                            }
                            else{
                                boolean flg = false;
                                for(Ext_class eclass : ext_class_list){
                                    if(s_table.get(table_index).vardecl_map.get(s).equals(eclass.name)){
                                        if(!eclass.ext_name.equals(meth_type)||!eclass.name.equals(meth_type)){
                                            System.out.println("An error occured! Inconsistent type!");
                                            System.exit(0);      
                                        }
                                        flg = true;
                                    }
                                }
                                if(!flg && !s_table.get(table_index).vardecl_map.get(s).equals(meth_type)){
                                    if(!(s_table.get(table_index).vardecl_map.get(s).equals("int[]")&&meth_type.equals("int")&&str.contains(".length"))){
                                        System.out.println("An error occured! Inconsistent type!");
                                        System.exit(0);
                                    }
                                }
                            }
                        }
                        else {
                            boolean flg = false;
                            for(Ext_class eclass : ext_class_list){
                                if(s_table.get(table_index).method_list.get(method_index).param_map.get(s).equals(eclass.name)){
                                    if(!eclass.ext_name.equals(meth_type)||!eclass.name.equals(meth_type)){
                                        System.out.println("An error occured! Inconsistent type!");
                                        System.exit(0);      
                                    }
                                    flg = true;
                                }
                            }
                            if(!flg && !s_table.get(table_index).method_list.get(method_index).param_map.get(s).equals(meth_type)){
                                if(!(s_table.get(table_index).method_list.get(method_index).param_map.get(s).equals("int[]")&&meth_type.equals("int")&&str.contains(".length"))){
                                    System.out.println("An error occured! Inconsistent type!");
                                    System.exit(0);
                                }
                            }
                        }
                    }
                    else{
                        boolean flg = false;
                        for(Ext_class eclass : ext_class_list){
                            if(s_table.get(table_index).method_list.get(method_index).var_map.get(s).equals(eclass.name)){
                                if(!eclass.ext_name.equals(meth_type)||!eclass.name.equals(meth_type)){
                                    System.out.println("An error occured! Inconsistent type!");
                                    System.exit(0);      
                                }
                                flg = true;
                            }
                        }
                        if(!flg && !s_table.get(table_index).method_list.get(method_index).var_map.get(s).equals(meth_type)){
                            if(!(s_table.get(table_index).method_list.get(method_index).var_map.get(s).equals("int[]")&&meth_type.equals("int")&&str.contains(".length"))){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0);
                            }
                        }
                    }
                }
            }
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
            int ext_index=0;
            int index = 0;
            for(Symbol_table table : s_table){
                if(table.name.equals(n.f3.accept(this,null))){
                    for(Symbol_table table2: table.ext_classes){
                        if(table2.name.equals(n.f1.accept(this,null))){
                            break;
                        }
                        ext_index++;
                    }
                    break;
                }
                index++;
            }

            NodeListOptional varDecls = n.f5;
            for (int i = 0; i < varDecls.size(); ++i) {
                VarDeclaration varDecl = (VarDeclaration) varDecls.elementAt(i);
                String varType = varDecl.f0.accept(this,null);
                if(varType.equals("int")||varType.equals("boolean")||varType.equals("int[]")){
                    continue;
                }
                boolean flg = false;
                for(Ext_class eclass : ext_class_list){
                    if(eclass.name.equals(varType)){
                        flg = true;
                    }
                }
                for(Symbol_table table : s_table){
                    if(table.name.equals(varType)){
                        flg = true;
                    }
                }
                if(!flg){
                    System.out.println("An error occured! No type found!");
                    System.exit(0);  
                }
            }
            NodeListOptional methDecls = n.f6;                                       /* Print the variable inside this class */
            for (int i = 0; i < methDecls.size(); ++i) {
                MethodDeclaration methDecl = (MethodDeclaration) methDecls.elementAt(i);
                String argumentList = methDecl.f4.present() ? methDecl.f4.accept(this, null) : "";
                String type = methDecl.f1.accept(this, null);
                if(type.equals("int")||type.equals("boolean")||type.equals("int[]")){
                    continue;
                }
                boolean flg = false;
                for(Ext_class eclass : ext_class_list){
                    if(eclass.name.equals(type)){
                        flg = true;
                    }
                }
                for(Symbol_table table : s_table){
                    if(table.name.equals(type)){
                        flg = true;
                    }
                }
                if(!flg){
                    System.out.println("An error occured! No type found!");
                    System.exit(0);  
                }
                if(!argumentList.equals("")){
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
                        if(par_type.equals("int")||par_type.equals("boolean")||par_type.equals("int[]")){
                            continue;
                        }
                        flg = false;
                        for(Ext_class eclass : ext_class_list){
                            if(eclass.name.equals(par_type)){
                                flg = true;
                            }
                        }
                        for(Symbol_table table : s_table){
                            if(table.name.equals(par_type)){
                                flg = true;
                            }
                        }
                        if(!flg){
                            System.out.println("An error occured! No type found!");
                            System.exit(0);  
                        }
                    }
                }
                NodeListOptional varDes = methDecl.f7;         
                for (int j = 0; j < varDes.size(); ++j) {
                    VarDeclaration varDe = (VarDeclaration) varDes.elementAt(j);
                    String var_type = varDe.f0.accept(this,null);
                    if(var_type.equals("int")||var_type.equals("boolean")||var_type.equals("int[]")){
                        continue;
                    }
                    flg = false;
                    for(Ext_class eclass : ext_class_list){
                        if(eclass.name.equals(var_type)){
                            flg = true;
                        }
                    }
                    for(Symbol_table table : s_table){
                        if(table.name.equals(var_type)){
                            flg = true;
                        }
                    }
                    if(!flg){
                        System.out.println("An error occured! No type found!");
                        System.exit(0);  
                    }
                }
            }

            //Gia kathe methodo vlepei to return
            for (int i = 0; i < methDecls.size(); ++i) {
                MethodDeclaration methDecl = (MethodDeclaration) methDecls.elementAt(i);
                String meth_name = methDecl.f2.accept(this,null);
                String meth_type = methDecl.f1.accept(this,null);
                int method_index = 0;
                for(Method method : s_table.get(index).ext_classes.get(ext_index).method_list){
                    if(method.name.equals(meth_name)){
                        break;
                    }
                    method_index++;
                }
                String str = methDecl.f10.accept(this,null);
                String[] input = str.split("\\+|\\<|&&|\\-|\\*|\\[|.length");
                for(String s : input){
                    if(str.contains("+")||str.contains("-")||str.contains("<")||str.contains("*")||str.contains(".length")){
                        if(!meth_type.equals("int")){
                            System.out.println("An error occured! Inconsistent return value!");
                            System.exit(0);  
                        }
                    }
                    else if(str.contains("[")){
                        if(!meth_type.equals("int[]")){
                            System.out.println("An error occured! Inconsistent return value!");
                            System.exit(0);  
                        }
                    }
                    else if(str.contains("&&")){
                        if(!meth_type.equals("boolean")){
                            System.out.println("An error occured! Inconsistent return value!");
                            System.exit(0);  
                        }
                    }
                    if(Symbol_table.isNumeric(s)||s.equals("false")||s.equals("true")){
                        continue;
                    }
                    if(!s_table.get(index).ext_classes.get(ext_index).method_list.get(method_index).var_map.containsKey(s)){
                        if(!s_table.get(index).ext_classes.get(ext_index).method_list.get(method_index).param_map.containsKey(s)){
                            if(!s_table.get(index).ext_classes.get(ext_index).vardecl_map.containsKey(s)){
                                System.out.println("An error occured! No variable found!");
                                System.exit(0);
                            }
                            else{
                                boolean flg = false;
                                for(Ext_class eclass : ext_class_list){
                                    if(s_table.get(index).ext_classes.get(ext_index).vardecl_map.get(s).equals(eclass.name)){
                                        if(!eclass.ext_name.equals(meth_type)||!eclass.name.equals(meth_type)){
                                            System.out.println("An error occured! Inconsistent type!");
                                            System.exit(0);      
                                        }
                                        flg = true;
                                    }
                                }
                                if(!flg && !s_table.get(index).ext_classes.get(ext_index).vardecl_map.get(s).equals(meth_type)){
                                    if(!(s_table.get(index).ext_classes.get(ext_index).vardecl_map.get(s).equals("int[]")&&meth_type.equals("int")&&str.contains(".length"))){
                                        System.out.println("An error occured! Inconsistent type!");
                                        System.exit(0);
                                    }
                                }
                            }
                        }
                        else {
                            boolean flg = false;
                            for(Ext_class eclass : ext_class_list){
                                if(s_table.get(index).ext_classes.get(ext_index).method_list.get(method_index).param_map.get(s).equals(eclass.name)){
                                    if(!eclass.ext_name.equals(meth_type)||!eclass.name.equals(meth_type)){
                                        System.out.println("An error occured! Inconsistent type!");
                                        System.exit(0);      
                                    }
                                    flg = true;
                                }
                            }
                            if(!flg && !s_table.get(index).ext_classes.get(ext_index).method_list.get(method_index).param_map.get(s).equals(meth_type)){
                                if(!(s_table.get(index).ext_classes.get(ext_index).method_list.get(method_index).param_map.get(s).equals("int[]")&&meth_type.equals("int")&&str.contains(".length"))){
                                    System.out.println("An error occured! Inconsistent type!");
                                    System.exit(0);
                                }
                            }
                        }
                    }
                    else{
                        boolean flg = false;
                        for(Ext_class eclass : ext_class_list){
                            if(s_table.get(index).ext_classes.get(ext_index).method_list.get(method_index).var_map.get(s).equals(eclass.name)){
                                if(!eclass.ext_name.equals(meth_type)||!eclass.name.equals(meth_type)){
                                    System.out.println("An error occured! Inconsistent type!");
                                    System.exit(0);      
                                }
                                flg = true;
                            }
                        }
                        if(!flg && !s_table.get(index).ext_classes.get(ext_index).method_list.get(method_index).var_map.get(s).equals(meth_type)){
                            if(!(s_table.get(index).ext_classes.get(ext_index).method_list.get(method_index).var_map.get(s).equals("int[]")&&meth_type.equals("int")&&str.contains(".length"))){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0);
                            }
                        }
                    }
                }
            }
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
        String myType = n.f0.accept(this, null);
        String myName = n.f1.accept(this, null);
        return myType + " " + myName;
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
            n.f8.accept(this,n.f2.accept(this,null)+"+"+argu);
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
        n.f1.accept(this,argu);
        return " ";
    }

    /**
     * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    @Override
    public String visit(AssignmentStatement n, String argu) throws Exception {
        String[] str = argu.split("\\+");
        String method_name = str[0];
        String class_name = str[1];
        String var_type ="";
        //Tsekarei identifier
        boolean ext_flg = false;
        for(Ext_class eclass : ext_class_list){
            if(eclass.name.equals(class_name)){
                for(Symbol_table table : s_table){
                    if(table.name.equals(eclass.ext_name)){
                        var_type = table.Id_check(n.f0.accept(this,null),method_name,table.name,ext_class_list,s_table);
                        ext_flg = true;
                    }
                }
            }
        }
        if(!ext_flg){
            for(Symbol_table table : s_table){
                if(table.name.equals(class_name)){
                    var_type = table.Id_check(n.f0.accept(this,null),method_name, "", ext_class_list,s_table);
                }
            }
        }
        //kalei expression
        String var_type2;
        String temp = n.f2.accept(this,argu + "+" + var_type);
        if(!temp.contains("(")&&!temp.contains("-")&&!temp.contains("+")&&!temp.contains("<")&&!temp.contains("*")&&!temp.contains("[")&&!temp.contains(".length")&&!temp.contains("&&")&&!temp.contains("this")){
            boolean e_flg = false;
            for(Ext_class eclass : ext_class_list){
                if(eclass.name.equals(class_name)){
                    for(Symbol_table table : s_table){
                        if(table.name.equals(eclass.ext_name)){
                            var_type2 = table.Id_check(temp,method_name,table.name,ext_class_list,s_table);
                            if(!var_type.equals(var_type2)){
                                boolean f = true;
                                for(Ext_class e_class : ext_class_list){
                                    if(e_class.name.equals(var_type)||e_class.name.equals(var_type2)){
                                        if(e_class.ext_name.equals(var_type)||e_class.ext_name.equals(var_type2)){
                                            f = false;
                                        }
                                    }
                                }
                                if(f){
                                    System.out.println("An error occured! Wrong type!");
                                    System.exit(0); 
                                }
                            }
                            e_flg = true;
                        }
                    }
                }
            }
            if(!e_flg){
                for(Symbol_table table : s_table){
                    if(table.name.equals(class_name)){
                        var_type2 = table.Id_check(temp,method_name, "", ext_class_list,s_table);
                        if(!var_type.equals(var_type2)){
                            boolean f = true;
                            for(Ext_class e_class : ext_class_list){
                                if(e_class.name.equals(var_type)||e_class.name.equals(var_type2)){
                                    if(e_class.ext_name.equals(var_type)||e_class.ext_name.equals(var_type2)){
                                        f = false;
                                    }
                                }
                            }
                            if(f){
                                System.out.println("An error occured! Wrong type!");
                                System.exit(0); 
                            }
                        }
                    }
                }
            }
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

        String[] str = argu.split("\\+");
        String method_name = str[0];
        String class_name = str[1];
        String var_type = "";
        boolean ext_flg = false;
        for(Ext_class eclass : ext_class_list){
            if(eclass.name.equals(class_name)){
                for(Symbol_table table : s_table){
                    if(table.name.equals(eclass.ext_name)){
                        var_type = table.Id_check(n.f0.accept(this,null),method_name,table.name,ext_class_list,s_table);
                        if(!var_type.equals("int[]")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                        ext_flg = true;
                    }
                }
            }
        }
        if(!ext_flg){
            for(Symbol_table table : s_table){
                if(table.name.equals(class_name)){
                    var_type = table.Id_check(n.f0.accept(this,null),method_name, "", ext_class_list,s_table);
                    if(!var_type.equals("int[]")){
                        System.out.println("An error occured! Inconsistent type!");
                        System.exit(0); 
                    }
                }
            }
        }

        n.f2.accept(this,argu + "+" + "int");
        n.f5.accept(this,argu + "+" + "int");
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
        n.f2.accept(this, argu + "+" + "boolean");
        n.f4.accept(this,argu);
        n.f6.accept(this,argu);
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
        n.f2.accept(this,argu + "+" + "boolean");
        n.f4.accept(this,argu);
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
        n.f2.accept(this,argu + "+" + "int");
        return " ";
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
        if(argu == null){
            return n.f0.accept(this,argu) + "&&" + n.f2.accept(this,argu);
        }
        String[] str = argu.split("\\+");
        String method_name = str[0];
        String class_name = str[1];
        String var_type = "";
        String temp ="";
        boolean ext_flg = false;
        for(Ext_class eclass : ext_class_list){
            if(eclass.name.equals(class_name)){
                for(Symbol_table table : s_table){
                    if(table.name.equals(eclass.ext_name)){
                        temp = n.f0.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                            var_type = table.Id_check(n.f0.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("boolean")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        temp = n.f2.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                            var_type = table.Id_check(n.f2.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("boolean")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        ext_flg = true;
                    }
                }
            }
        }
        if(!ext_flg){
            for(Symbol_table table : s_table){
                if(table.name.equals(class_name)){
                    temp = n.f0.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                        var_type = table.Id_check(n.f0.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("boolean")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                    temp = n.f2.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                        var_type = table.Id_check(n.f2.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("boolean")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                }
            }
        }
        return n.f0.accept(this,argu) + "&&" + n.f2.accept(this,argu);
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(CompareExpression n, String argu) throws Exception {
        if(argu == null){
            return n.f0.accept(this,argu) + "<" + n.f2.accept(this,argu);
        }
        String[] str = argu.split("\\+");
        String method_name = str[0];
        String class_name = str[1];
        String var_type = "";   
        String temp ="";
        boolean ext_flg = false;
        for(Ext_class eclass : ext_class_list){
            if(eclass.name.equals(class_name)){
                for(Symbol_table table : s_table){
                    if(table.name.equals(eclass.ext_name)){
                        temp = n.f0.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                            var_type = table.Id_check(n.f0.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("int")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        temp = n.f2.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                            var_type = table.Id_check(n.f2.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("int")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        ext_flg = true;
                    }
                }
            }
        }
        if(!ext_flg){
            for(Symbol_table table : s_table){
                if(table.name.equals(class_name)){
                    temp = n.f0.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                        var_type = table.Id_check(n.f0.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("int")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                    temp = n.f2.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                        var_type = table.Id_check(n.f2.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("int")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                }
            }
        }
        return n.f0.accept(this,argu) + "<" + n.f2.accept(this,argu);
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(PlusExpression n, String argu) throws Exception {
        if(argu == null){
            return n.f0.accept(this,argu) + "+" + n.f2.accept(this,argu);
        }
        String[] str = argu.split("\\+");
        String method_name = str[0];
        String class_name = str[1];
        String var_type = "";
        String temp;
        boolean ext_flg = false;
        for(Ext_class eclass : ext_class_list){
            if(eclass.name.equals(class_name)){
                for(Symbol_table table : s_table){
                    if(table.name.equals(eclass.ext_name)){
                        temp = n.f0.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                            var_type = table.Id_check(n.f0.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("int")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        temp = n.f2.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                            var_type = table.Id_check(n.f2.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("int")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        ext_flg = true;
                    }
                }
            }
        }
        if(!ext_flg){
            for(Symbol_table table : s_table){
                if(table.name.equals(class_name)){
                    temp = n.f0.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                        var_type = table.Id_check(n.f0.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("int")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                    temp = n.f2.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                        var_type = table.Id_check(n.f2.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("int")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                }
            }
        }
        return n.f0.accept(this,argu) + "+" + n.f2.accept(this,argu);
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(MinusExpression n, String argu) throws Exception {
        if(argu == null){
            return n.f0.accept(this,argu) + "-" + n.f2.accept(this,argu);
        }
        String[] str = argu.split("\\+");
        String method_name = str[0];
        String class_name = str[1];
        String var_type = "";
        String temp = "";
        boolean ext_flg = false;
        for(Ext_class eclass : ext_class_list){
            if(eclass.name.equals(class_name)){
                for(Symbol_table table : s_table){
                    if(table.name.equals(eclass.ext_name)){
                        temp = n.f0.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")){
                            var_type = table.Id_check(n.f0.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("int")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        temp = n.f2.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")){
                            var_type = table.Id_check(n.f2.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("int")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        ext_flg = true;
                    }
                }
            }
        }
        if(!ext_flg){
            for(Symbol_table table : s_table){
                if(table.name.equals(class_name)){
                    temp = n.f0.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")){
                        var_type = table.Id_check(n.f0.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("int")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                    temp = n.f2.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")){
                        var_type = table.Id_check(n.f2.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("int")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                }
            }
        }
        return n.f0.accept(this,argu) + "-" + n.f2.accept(this,argu);
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(TimesExpression n, String argu) throws Exception {
        if(argu == null){
            return n.f0.accept(this,argu) + "*" + n.f2.accept(this,argu);
        }
        String[] str = argu.split("\\+");
        String method_name = str[0];
        String class_name = str[1];
        String var_type = "";
        String temp = "";
        boolean ext_flg = false;
        for(Ext_class eclass : ext_class_list){
            if(eclass.name.equals(class_name)){
                for(Symbol_table table : s_table){
                    if(table.name.equals(eclass.ext_name)){
                        temp = n.f0.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")){
                            var_type = table.Id_check(n.f0.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("int")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        temp = n.f2.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")){
                            var_type = table.Id_check(n.f2.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("int")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        ext_flg = true;
                    }
                }
            }
        }
        if(!ext_flg){
            for(Symbol_table table : s_table){
                if(table.name.equals(class_name)){
                    temp = n.f0.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")){
                        var_type = table.Id_check(n.f0.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("int")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                    temp = n.f2.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")){
                        var_type = table.Id_check(n.f2.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("int")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                }
            }
        }
        return n.f0.accept(this,argu) + "*" + n.f2.accept(this,argu);
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    @Override
    public String visit(ArrayLookup n, String argu) throws Exception {
        if(argu == null){
            return n.f0.accept(this,argu) + "[" + n.f2.accept(this,argu);
        }
        String[] str = argu.split("\\+");
        String method_name = str[0];
        String class_name = str[1];
        String var_type = "";
        String temp = "";
        boolean ext_flg = false;
        for(Ext_class eclass : ext_class_list){
            if(eclass.name.equals(class_name)){
                for(Symbol_table table : s_table){
                    if(table.name.equals(eclass.ext_name)){
                        temp = n.f0.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                            var_type = table.Id_check(n.f0.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("int[]")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        temp = n.f2.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new")&&!temp.contains("!")){
                            var_type = table.Id_check(n.f2.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("int")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        ext_flg = true;
                    }
                }
            }
        }
        if(!ext_flg){
            for(Symbol_table table : s_table){
                if(table.name.equals(class_name)){
                    temp = n.f0.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")){
                        var_type = table.Id_check(n.f0.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("int[]")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                    temp = n.f2.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")){
                        var_type = table.Id_check(n.f2.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("int")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                }
            }
        }
        return n.f0.accept(this,argu) + "[" + n.f2.accept(this,argu);
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
    public String visit(ArrayLength n, String argu) throws Exception {
        if(argu == null){
            return n.f0.accept(this,argu) + ".length";
        }
        String[] str = argu.split("\\+");
        String method_name = str[0];
        String class_name = str[1];
        String var_type = "";
        String temp = "";
        boolean ext_flg = false;
        for(Ext_class eclass : ext_class_list){
            if(eclass.name.equals(class_name)){
                for(Symbol_table table : s_table){
                    if(table.name.equals(eclass.ext_name)){
                        temp = n.f0.accept(this,argu);
                        if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")){
                            var_type = table.Id_check(n.f0.accept(this,argu),method_name,table.name, ext_class_list,s_table);
                            if(!var_type.equals("int[]")){
                                System.out.println("An error occured! Inconsistent type!");
                                System.exit(0); 
                            }
                        }
                        ext_flg = true;
                    }
                }
            }
        }
        if(!ext_flg){
            for(Symbol_table table : s_table){
                if(table.name.equals(class_name)){
                    temp = n.f0.accept(this,argu);
                    if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")){
                        var_type = table.Id_check(n.f0.accept(this,argu),method_name, "", ext_class_list,s_table);
                        if(!var_type.equals("int[]")){
                            System.out.println("An error occured! Inconsistent type!");
                            System.exit(0); 
                        }
                    }
                }
            }
        }
        //checkarei primary expression
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
        if(argu == null){
            String str = n.f4.present() ? n.f4.accept(this, argu) : "";
            return n.f0.accept(this,argu) + "." + n.f2.accept(this,argu)+"(" + str + ")";
        }
        String[] stri = argu.split("\\+");
        String method_name = stri[0];
        String class_name = stri[1];
        String var_type = "";
        String temp = "";
        boolean flg = false;
        String message_class_name = "";
        temp = n.f0.accept(this,argu);
        if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")&&!temp.contains("this")){
            for(Symbol_table table : s_table){
                if(class_name.equals(table.name)){
                    var_type = table.Id_check(n.f0.accept(this,argu),method_name, "", ext_class_list,s_table);
                }
            }
            boolean ext_flag = false;
            for(Ext_class eclass : ext_class_list){
                if(eclass.name.equals(var_type)||eclass.name.equals(temp)){
                    message_class_name = eclass.name;
                    flg = true;
                }
            }
            if(!ext_flag){
                for(Symbol_table table2 : s_table){
                    if(temp.equals(table2.name) || var_type.equals(table2.name)){
                        message_class_name = table2.name;
                        flg = true;
                    }
                }
            }
            if(!flg){
                System.out.println("An error occured! No class found!");
                System.exit(0); 
            }
        }
        if(temp.equals("this")){
            message_class_name = class_name;
        }
        Method meth_temp = null;
        if(!message_class_name.equals("")){
            for(Symbol_table table : s_table){
                if(table.name.equals(message_class_name)){
                    boolean flag = false;
                    for(Method method : table.method_list){
                        if(method.name.equals(n.f2.accept(this,argu))){
                            meth_temp = method;
                            flag = true;
                        }
                    }
                    if(!flag){
                        System.out.println("An error occured! No Method found!");
                        System.exit(0); 
                    }
                }
            }
        }

        String str = n.f4.present() ? n.f4.accept(this, argu) : ""; //to poly ena stoixeio
        if(Symbol_table.isNumeric(str)){
            return n.f0.accept(this,argu) + "." + n.f2.accept(this,argu)+"(" + str + ")";
        }
        Object[] values = null;
        if(!str.equals("")){
            String [] string = str.split("\\,");
            int counter = 0;
            String type;
            for(String strr : string){
                if(strr.contains("(")||strr.contains("-")||strr.contains("+")||strr.contains("<")||strr.contains("*")||strr.contains("[")||strr.contains(".length")||strr.contains("&&")||strr.contains("this")){
                    counter++;
                    continue;
                }
                //An epistrfei int tote prepei na eiani kai int

                // checkaroyme to strr
                for(Symbol_table table : s_table){
                    if(table.name.equals(class_name)){
                        type = table.Id_check(strr,method_name, "", ext_class_list,s_table);
                        values = meth_temp.param_map.values().toArray();
                        flg = false;
                        for(Object val : values){
                            if(type.equals(val.toString())){
                                flg = true;
                            }
                            for(Ext_class eclass : ext_class_list){
                                if(eclass.name.equals(type)){
                                    if(eclass.ext_name.equals(val.toString())){
                                        flg = true;
                                    }
                                }
                            }
                        }
                        if(!flg){
                            System.out.println("An error occured! Wrong parameters");
                            System.exit(0); 
                        }
                    }
                }
                counter++;
            }
            if(values != null){
                if(counter != values.length){
                    System.out.println("An error occured! Wrong parameters number");
                    System.exit(0); 
                }
            }
        }
        return n.f0.accept(this,argu) + "." + n.f2.accept(this,argu)+"(" + str + ")";
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
        return n.f0.toString();    
    }

    /**
     * f0 -> "true"
    */
    @Override
    public String visit(TrueLiteral n, String argu) throws Exception {
        return "true";    
    }

    /**
     * f0 -> "false"
    */
    @Override
    public String visit(FalseLiteral n, String argu) throws Exception {
        return "false";    
    }

    
    /**
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, String argu) {
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
        //kalei expression
        // n.f3.accept(this,argu);
        return "new int["+n.f3.accept(this,argu+"+int")+"]";
    }

    /**
     * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    @Override
    public String visit(AllocationExpression n, String argu) throws Exception {
        //checkarei identifier
        String[] str = argu.split("\\+");
        String method_name = str[0];
        String class_name = str[1];
        String var_type = "";
        boolean flg = false;
        for(Symbol_table table : s_table){
            if(table.name.equals(n.f1.accept(this,argu))){
                flg = true;
                // var_type = table.Id_check(n.f1.accept(this,null),method_name, "", ext_class_list);
            }
        }
        for(Ext_class etable : ext_class_list){
            if(etable.name.equals(n.f1.accept(this,argu))){
                flg = true;
                // var_type = table.Id_check(n.f1.accept(this,null),method_name, "", ext_class_list);
            }
        }
        if(!flg){
            System.out.println("An error occured! Inconsistent type!");
            System.exit(0); 
        }
        return "new "+n.f1.accept(this, argu)+"()";
    }

    /**
     * f0 -> "!"
    * f1 -> PrimaryExpression()
    */
    @Override
    public String visit(NotExpression n, String argu) throws Exception {
        //checkarei primaryexpression
        String[] str = argu.split("\\+");
        String method_name = str[0];
        String class_name = str[1];
        String var_type = "";
        String temp ="";
        for(Symbol_table table : s_table){
            if(table.name.equals(class_name)){
                temp = n.f1.accept(this,argu);
                if(!temp.contains("(")&&!temp.contains("new ")&&!temp.contains("! ")&&!temp.contains("this")){
                    var_type = table.Id_check(n.f1.accept(this,argu),method_name, "", ext_class_list,s_table);
                    if(!var_type.equals("boolean")){
                        System.out.println("An error occured! Inconsistent type!");
                        System.exit(0); 
                    }
                }
            }
        }
        return "! "+n.f1.accept(this, argu);
    }

    /**
     * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    @Override
    public String visit(BracketExpression n, String argu) throws Exception {
        //kalei to expression

        return "("+n.f1.accept(this,argu)+")";

    }
}
