class Example {
    public static void main(String[] args) {
    }
}

class A {
    int D;
    A a;
    public int foo(int i, int j) { 
        int k;
        i=5;
        return i+j; 
    }
    public int bar(){ return 1; }
}

class B extends A{
    int i;
    public int foo(int i, int j) {
        return i+j; 
    }
    public int foobar(boolean k){ return 1; }
}
