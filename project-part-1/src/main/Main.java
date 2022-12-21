package main;

import java.util.*;

import java.io.FileInputStream;
import java.io.File;




public class Main {

    public static String FileToString(String filename){
	try{
	    
	File file = new File(filename);
	FileInputStream f = new FileInputStream(file);
	byte[] data = new byte[(int) file.length()];
	f.read(data);
	f.close();
	
	return new String(data, "UTF-8");
	}
	 catch(Exception ex)
	     {
		 ex.printStackTrace();
		 return null;
	     }       
    }

	public static HashMap<String, Integer> variables = new HashMap<String, Integer>();

    public static void main(String[] args){
	// String s = "x=1;y=x*2;z=x-y;x=y+z";
	Vector<Instruction> x = Parser.parse(FileToString("src/prog1"));
	for(Instruction i: x){
	    if (i instanceof Assign) {
			Assign a = (Assign) i;
			System.out.println("La variable " + a.lhs + " reçoit la valeur " + a.rhs);
			//a.lhs = Nom de la variable
			//a.lsh = Valeur de la variable (Entier ou Variable)
			if (a.rhs instanceof Entier) {
				Entier e = (Entier) a.rhs;
				variables.put(a.lhs, e.getValue());
			}
			else if (a.rhs instanceof Variable) {
				Variable v = (Variable) a.rhs;
				variables.put(a.lhs, variables.get(v.var));
			}
		}
	    else{		
		AssignOperator a = (AssignOperator) i;
		System.out.println("On effectue l'opération " + a.op + " aux valeurs " + a.t0  + " et " + a.t1 + " et on stocke le résultat dans " + a.lhs);
		//a.lhs = Nom de la variable
		//a.t0 = Valeur de la variable (Entier ou Variable)
		//a.t1 = Valeur de la variable (Entier ou Variable)
		//a.op = Opérateur
		if (a.t0 instanceof Entier && a.t1 instanceof Entier) {
			Entier e0 = (Entier) a.t0;
			Entier e1 = (Entier) a.t1;
			if (a.op.equals("+")) {
				variables.put(a.lhs, e0.getValue() + e1.getValue());
			}
			else if (a.op.equals("-")) {
				variables.put(a.lhs, e0.getValue() - e1.getValue());
			}
			else if (a.op.equals("*")) {
				variables.put(a.lhs, e0.getValue() * e1.getValue());
			}
		}
		else if (a.t0 instanceof Entier && a.t1 instanceof Variable) {
			Entier e0 = (Entier) a.t0;
			Variable v1 = (Variable) a.t1;
			if (a.op.equals("+")) {
				variables.put(a.lhs, e0.getValue() + variables.get(v1.var));
			}
			else if (a.op.equals("-")) {
				variables.put(a.lhs, e0.getValue() - variables.get(v1.var));
			}
			else if (a.op.equals("*")) {
				variables.put(a.lhs, e0.getValue() * variables.get(v1.var));
			}
		}
		else if (a.t0 instanceof Variable && a.t1 instanceof Entier) {
			Variable v0 = (Variable) a.t0;
			Entier e1 = (Entier) a.t1;
			if (a.op.equals("+")) {
				variables.put(a.lhs, variables.get(v0.var) + e1.getValue());
			}
			else if (a.op.equals("-")) {
				variables.put(a.lhs, variables.get(v0.var) - e1.getValue());
			}
			else if (a.op.equals("*")) {
				variables.put(a.lhs, variables.get(v0.var) * e1.getValue());
			}
		}
		else if (a.t0 instanceof Variable && a.t1 instanceof Variable) {
			Variable v0 = (Variable) a.t0;
			Variable v1 = (Variable) a.t1;
			if (a.op.equals("+")) {
				variables.put(a.lhs, variables.get(v0.var) + variables.get(v1.var));
			}
			else if (a.op.equals("-")) {
				variables.put(a.lhs, variables.get(v0.var) - variables.get(v1.var));
			}
			else if (a.op.equals("*")) {
				variables.put(a.lhs, variables.get(v0.var) * variables.get(v1.var));
			}
		}




	    }
	}
	//For each variable in the HashMap, print the variable name and its value
		System.out.println("Valeurs des variables à la fin du programme:");
	for (String key : variables.keySet()) {
		System.out.println("La variable " + key + " a pour valeur " + variables.get(key));
	}

    }

}
