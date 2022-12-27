package main;

class Entier extends Value {
    Integer x;
    Entier(Integer s){
	x = s;
    }

    @Override
    public String toString(){
	return x.toString();
    }

    public int getValue() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
}
