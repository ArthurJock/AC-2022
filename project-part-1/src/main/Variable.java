package main;

class Variable extends Value{
    String var;
    Variable(String s){
	var = s;
    }
    @Override
    public String toString(){
	return "Variable " + var;
    }

    public int getValue() {
        System.out.println("Variable " + var + " is not initialized");
        return 0;
    }
}
