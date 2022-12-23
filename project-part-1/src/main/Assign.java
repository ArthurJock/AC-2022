package main;

class Assign extends Instruction{
    String lhs;
    Value rhs;
    Assign(String s, Value x){
	lhs = s;
	rhs = x;
    }

    public String toString(){
        return this.lhs+" = "+this.rhs;
    }
}
