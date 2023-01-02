package main;

import java.util.*;

import java.io.FileInputStream;
import java.io.File;


public class Main {

    public static String FileToString(String filename) {
        try {

            File file = new File(filename);
            FileInputStream f = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            f.read(data);
            f.close();

            return new String(data, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, Integer> variables = new HashMap<String, Integer>();
    public static Vector<Instruction> simplified = new Vector<Instruction>();


    public static void main(String[] args) {
        //String s = "x=4;x=x+1;y=x+x;z=x*y";
        String s = FileToString("src/prog1").replace(System.getProperty("line.separator"), ";").replace(" ", "");
        String s2 = FileToString("src/prog2").replace(System.getProperty("line.separator"), ";").replace(" ", "");
        String s3 = FileToString("src/prog3").replace(System.getProperty("line.separator"), ";").replace(" ", "");
        System.out.println("Programme initial 1 :");
        Vector<Instruction> x = Parser.parse(s);
        System.out.println(s + "\n");
        System.out.println("Programme initial 2 :");
        Vector<Instruction> y = Parser.parse(s2);
        System.out.println(s2 + "\n");
        System.out.println("Programme initial 3 :");
        Vector<Instruction> v = Parser.parse(s3);
        System.out.println(s2 + "\n");
        solvePro(v);
        simplifiedPro(v);
        System.out.println("Programme simplifié 3 :");
        for(Instruction i : simplified){
            System.out.println(i);
        }
        int randomPrime = RandomPrime.randomPrime();
        System.out.println("Nombre premier aléatoire : " + randomPrime + "\n");
        if (areProgEqual(x, y, randomPrime)) {
            System.out.println("Les deux programmes donnent le même résultat modulo " + randomPrime);
        } else {
            System.out.println("Les deux programmes ne donnent pas le même résultat modulo ");
        }

    }

    /**
     * QUESTION 5
     * Fonction areProgEqual qui prend en paramètre deux programmes et un nombre premier
     * (évite les entiers trop grands)
     * @param x vecteur d'instructions du programme 1
     * @param y vecteur d'instructions du programme 2
     * @param p nombre premier aléatoire
     * @return true si les deux programmes donnent le même résultat modulo p, false sinon
     * */
    public static boolean areProgEqual(Vector<Instruction> x, Vector<Instruction> y, int p){
        int a = evaluateur(x,p);
        int b = evaluateur(y,p);
        System.out.println("Evaluation du programme 1 : " + a);
        System.out.println("Evaluation du programme 2 : " + b);

        return a==b;
    }

    /**
     * QUESTION 4
     * Fonction evaluateur résous le programme en paramètre en utilisant la méthode de multiplication améliorée
     * (évite les entiers trop grands)
     * @param instructions vecteur d'instructions
     * @param p nombre premier aléatoire
     * */
    public static int evaluateur(Vector<Instruction> instructions, int p) {
        HashMap<String, Integer> variables = new HashMap<String, Integer>();
        Vector<Instruction> simplified = new Vector<Instruction>();
        //On va évaluer le programme passé en paramètre modulo p
        //Si x et y sont entre 0 et 2 puissance 30 - 1 alors (x*y) modulo p ne donne pas le bon résultat
        //C'est pourquoi on utilisera la fonction multiply définie plus bas
        for (Instruction i : instructions) {
            System.out.println(i);
            if (i instanceof Assign) {
                Assign a = (Assign) i;
                //a.lhs = Nom de la variable
                //a.lsh = Valeur de la variable (Entier ou Variable)
                if (a.rhs instanceof Entier) {
                    Entier e = (Entier) a.rhs;
                    e.setX(e.getValue() % p);
                    variables.put(a.lhs, e.getValue());
                    simplified.add(new Assign(a.lhs, e));
                } else if (a.rhs instanceof Variable) {
                    Variable v = (Variable) a.rhs;
                    variables.put(a.lhs, variables.get(v.var));
                    simplified.add(new Assign(a.lhs, v));
                }
            } else {
                AssignOperator a = (AssignOperator) i;
                if (a.t0 instanceof Entier && a.t1 instanceof Entier) {
                    Entier e0 = (Entier) a.t0;
                    e0.setX(e0.getValue() % p);
                    Entier e1 = (Entier) a.t1;
                    e1.setX(e1.getValue() % p);
                    switch (a.op) {
                        case "+" -> {
                            variables.put(a.lhs, e0.getValue()%p + e1.getValue()%p);
                            //simplifaction du 0+0
                            if (e0.getValue() == 0 && e1.getValue() == 0)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                            else if (e0.getValue() == 0) simplified.add(new Assign(a.lhs, e1));
                            else if (e1.getValue() == 0) simplified.add(new Assign(a.lhs, e0));
                            else simplified.add(new AssignOperator(a.lhs, "+", e0, e1));
                        }
                        case "-" -> {
                            variables.put(a.lhs, e0.getValue() - e1.getValue());
                            //simplification du 1-1
                            if (e0.getValue() == 1 && e1.getValue() == 1)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                            else if (e1.getValue() == 0) simplified.add(new Assign(a.lhs, e0));
                            else simplified.add(new AssignOperator(a.lhs, "-", e0, e1));
                        }
                        case "*" -> {
                            variables.put(a.lhs, multiply(e0.getValue(), e1.getValue(), p));
                            //simplification du 0 * 2 ou 3 * 0
                            if (e0.getValue() == 0 || e1.getValue() == 0)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                                //simplication du 1 * 3
                            else if (e0.getValue() == 1) simplified.add(new Assign(a.lhs, e1));
                                //simplication du 3 * 1
                            else if (e1.getValue() == 1) simplified.add(new Assign(a.lhs, e0));
                            else simplified.add(new AssignOperator(a.lhs, "*", e0, e1));
                        }
                    }
                } else if (a.t0 instanceof Entier && a.t1 instanceof Variable) {
                    Entier e0 = (Entier) a.t0;
                    e0.setX(e0.getValue() % p);
                    Variable v1 = (Variable) a.t1;
                    switch (a.op) {
                        case "+" -> {
                            variables.put(a.lhs, e0.getValue()%p + variables.get(v1.var));
                            if (e0.getValue() == 0) simplified.add(new Assign(a.lhs, v1));
                            else simplified.add(new AssignOperator(a.lhs, "+", e0, v1));
                        }
                        case "-" -> {
                            variables.put(a.lhs, e0.getValue() - variables.get(v1.var));
                            //simplification du 1 - x avec x = 1
                            if (e0.getValue() == 1 && variables.get(v1.var) == 1)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                                //simplification du 1 - x avec x = 0
                            else if (variables.get(v1.var) == 0) simplified.add(new Assign(a.lhs, e0));
                            else simplified.add(new AssignOperator(v1.var, "-", e0, v1));
                        }
                        case "*" -> {
                            variables.put(a.lhs, multiply(e0.getValue(), variables.get(v1.var), p));
                            //simplification du 0 * x
                            if (e0.getValue() == 0 || variables.get(v1.var) == 0)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                                //simplification du 1 * x
                            else if (e0.getValue() == 1) simplified.add(new Assign(a.lhs, new Entier(1)));
                                //simplification du 3 * x avec x = 1
                            else if (variables.get(v1.var) == 1) simplified.add(new Assign(a.lhs, new Entier(1)));
                            else simplified.add(new AssignOperator(v1.var, "*", e0, v1));
                        }
                    }
                } else if (a.t0 instanceof Variable && a.t1 instanceof Entier) {
                    Variable v0 = (Variable) a.t0;
                    Entier e1 = (Entier) a.t1;
                    e1.setX(e1.getValue() % p);
                    switch (a.op) {
                        case "+" -> {
                            variables.put(a.lhs, variables.get(v0.var) + e1.getValue()%p);
                            if (e1.getValue() == 0) simplified.add(new Assign(a.lhs, v0));
                            else simplified.add(new AssignOperator(a.lhs, "+", v0, e1));
                        }
                        case "-" -> {
                            variables.put(a.lhs, variables.get(v0.var) - e1.getValue());
                            //simplification du x - 1 avec x = 1
                            if (e1.getValue() == 1 && variables.get(v0.var) == 1)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                                //simplification du x - 0
                            else if (e1.getValue() == 0) simplified.add(new Assign(a.lhs, e1));
                            else simplified.add(new AssignOperator(a.lhs, "-", v0, e1));
                        }
                        case "*" -> {
                            variables.put(a.lhs, multiply(variables.get(v0.var), e1.getValue(), p));
                            //simplification du 0 * x
                            if (e1.getValue() == 0 || variables.get(v0.var) == 0)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                                //simplification du 1 * x
                            else if (e1.getValue() == 1) simplified.add(new Assign(a.lhs, new Entier(1)));
                                //simplification du 3 * x avec x = 1
                            else if (variables.get(v0.var) == 1) simplified.add(new Assign(a.lhs, new Entier(1)));
                            else simplified.add(new AssignOperator(a.lhs, "*", v0, e1));
                        }
                    }
                } else if (a.t0 instanceof Variable && a.t1 instanceof Variable) {
                    Variable v0 = (Variable) a.t0;
                    Variable v1 = (Variable) a.t1;
                    switch (a.op) {
                        case "+" -> {
                            variables.put(a.lhs, variables.get(v0.var) + variables.get(v1.var));
                            simplified.add(new AssignOperator(a.lhs, "+", v0, v1));
                        }
                        case "-" -> {
                            variables.put(a.lhs, variables.get(v0.var) - variables.get(v1.var));
                            //simplification du x - y avec x = y = 1
                            if (variables.get(v0.var) == 1 || variables.get(v1.var) == 1)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                            else simplified.add(new AssignOperator(a.lhs, "-", v0, v1));
                        }
                        case "*" -> {
                            variables.put(a.lhs, multiply(variables.get(v0.var), variables.get(v1.var), p));
                            //simplification du x * y avec x = y = 0
                            if (variables.get(v0.var) == 0 || variables.get(v1.var) == 0)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                                //simplification du x * y avec x = 1
                            else if (variables.get(v0.var) == 0 || variables.get(v1.var) == 0)
                                simplified.add(new Assign(a.lhs, v1));
                                //simplification du x * y avec y = 1
                            else if (variables.get(v0.var) == 0 || variables.get(v1.var) == 0)
                                simplified.add(new Assign(a.lhs, v0));
                            else simplified.add(new AssignOperator(a.lhs, "*", v0, v1));
                        }
                    }
                }
            }
        }
        return variables.get("x");


    }

    /**
     * QUESTION 4
     * Fonction multiply
     * @param x entier 1
     * @param y entier 2
     * @param p modulo
     * */
    public static int multiply(int x, int y, int p) {
        int result = 0;

        while (y != 0) {
            if ((y & 1) != 0) {
                result = (result + x) % p;
            }
            y = y >> 1;
            x = (2*x)%p;
        }
        return result;
    }


    /**
     * QUESTION 1
     * Fonction solvePro résous le programme en paramètre
     * @param x vecteur d'instructions
     * */
    public static void solvePro(Vector<Instruction> x) {
        for (Instruction i : x) {
            if (i instanceof Assign) {
                Assign a = (Assign) i;
                System.out.println("La variable " + a.lhs + " reçoit la valeur " + a.rhs);
                //a.lhs = Nom de la variable
                //a.lsh = Valeur de la variable (Entier ou Variable)
                if (a.rhs instanceof Entier) {
                    Entier e = (Entier) a.rhs;
                    variables.put(a.lhs, e.getValue());
                } else if (a.rhs instanceof Variable) {
                    Variable v = (Variable) a.rhs;
                    variables.put(a.lhs, variables.get(v.var));
                }
            } else {
                AssignOperator a = (AssignOperator) i;
                System.out.println("On effectue l'opération " + a.op + " aux valeurs " + a.t0 + " et " + a.t1 + " et on stocke le résultat dans " + a.lhs);
                if (a.t0 instanceof Entier && a.t1 instanceof Entier) {
                    Entier e0 = (Entier) a.t0;
                    Entier e1 = (Entier) a.t1;
                    switch (a.op) {
                        case "+" -> variables.put(a.lhs, e0.getValue() + e1.getValue());
                        case "-" -> variables.put(a.lhs, e0.getValue() - e1.getValue());
                        case "*" -> variables.put(a.lhs, e0.getValue() * e1.getValue());
                    }
                } else if (a.t0 instanceof Entier && a.t1 instanceof Variable) {
                    Entier e0 = (Entier) a.t0;
                    Variable v1 = (Variable) a.t1;
                    switch (a.op) {
                        case "+" -> variables.put(a.lhs, e0.getValue() + variables.get(v1.var));
                        case "-" -> variables.put(a.lhs, e0.getValue() - variables.get(v1.var));
                        case "*" -> variables.put(a.lhs, e0.getValue() * variables.get(v1.var));
                    }
                } else if (a.t0 instanceof Variable && a.t1 instanceof Entier) {
                    Variable v0 = (Variable) a.t0;
                    Entier e1 = (Entier) a.t1;
                    switch (a.op) {
                        case "+" -> variables.put(a.lhs, variables.get(v0.var) + e1.getValue());
                        case "-" -> variables.put(a.lhs, variables.get(v0.var) - e1.getValue());
                        case "*" -> variables.put(a.lhs, variables.get(v0.var) * e1.getValue());
                    }
                } else if (a.t0 instanceof Variable && a.t1 instanceof Variable) {
                    Variable v0 = (Variable) a.t0;
                    Variable v1 = (Variable) a.t1;
                    switch (a.op) {
                        case "+" -> variables.put(a.lhs, variables.get(v0.var) + variables.get(v1.var));
                        case "-" -> variables.put(a.lhs, variables.get(v0.var) - variables.get(v1.var));
                        case "*" -> variables.put(a.lhs, variables.get(v0.var) * variables.get(v1.var));
                    }
                }
            }
        }
    }

    /**
     * QUESTION 3
     * Fonction simplifiedPro résous le programme en paramètre en utilisant la simplification
     * @param x vecteur d'instructions
     * */
    public static void simplifiedPro(Vector<Instruction> x) {
        for (Instruction i : x) {
            System.out.println(i);
            if (i instanceof Assign) {
                Assign a = (Assign) i;
                //a.lhs = Nom de la variable
                //a.lsh = Valeur de la variable (Entier ou Variable)
                if (a.rhs instanceof Entier) {
                    Entier e = (Entier) a.rhs;
                    variables.put(a.lhs, e.getValue());
                    simplified.add(new Assign(a.lhs, e));
                } else if (a.rhs instanceof Variable) {
                    Variable v = (Variable) a.rhs;
                    variables.put(a.lhs, variables.get(v.var));
                    simplified.add(new Assign(a.lhs, v));
                }
            } else {
                AssignOperator a = (AssignOperator) i;
                if (a.t0 instanceof Entier && a.t1 instanceof Entier) {
                    Entier e0 = (Entier) a.t0;
                    Entier e1 = (Entier) a.t1;
                    switch (a.op) {
                        case "+" -> {
                            variables.put(a.lhs, e0.getValue() + e1.getValue());
                            //simplifaction du 0+0
                            if (e0.getValue() == 0 && e1.getValue() == 0)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                            else if (e0.getValue() == 0) simplified.add(new Assign(a.lhs, e1));
                            else if (e1.getValue() == 0) simplified.add(new Assign(a.lhs, e0));
                            else simplified.add(new AssignOperator(a.lhs, "+", e0, e1));
                        }
                        case "-" -> {
                            variables.put(a.lhs, e0.getValue() - e1.getValue());
                            //simplification du 1-1
                            if (e0.getValue() == 1 && e1.getValue() == 1)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                            else if (e1.getValue() == 0) simplified.add(new Assign(a.lhs, e0));
                            else simplified.add(new AssignOperator(a.lhs, "-", e0, e1));
                        }
                        case "*" -> {
                            variables.put(a.lhs, e0.getValue() * e1.getValue());
                            //simplification du 0 * 2 ou 3 * 0
                            if (e0.getValue() == 0 || e1.getValue() == 0)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                                //simplication du 1 * 3
                            else if (e0.getValue() == 1) simplified.add(new Assign(a.lhs, e1));
                                //simplication du 3 * 1
                            else if (e1.getValue() == 1) simplified.add(new Assign(a.lhs, e0));
                            else simplified.add(new AssignOperator(a.lhs, "*", e0, e1));
                        }
                    }
                } else if (a.t0 instanceof Entier && a.t1 instanceof Variable) {
                    Entier e0 = (Entier) a.t0;
                    Variable v1 = (Variable) a.t1;
                    switch (a.op) {
                        case "+" -> {
                            variables.put(a.lhs, e0.getValue() + variables.get(v1.var));
                            if (e0.getValue() == 0) simplified.add(new Assign(a.lhs, v1));
                            else simplified.add(new AssignOperator(a.lhs, "+", e0, v1));
                        }
                        case "-" -> {
                            variables.put(a.lhs, e0.getValue() - variables.get(v1.var));
                            //simplification du 1 - x avec x = 1
                            if (e0.getValue() == 1 && variables.get(v1.var) == 1)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                                //simplification du 1 - x avec x = 0
                            else if (variables.get(v1.var) == 0) simplified.add(new Assign(a.lhs, e0));
                            else simplified.add(new AssignOperator(v1.var, "-", e0, v1));
                        }
                        case "*" -> {
                            variables.put(a.lhs, e0.getValue() * variables.get(v1.var));
                            //simplification du 0 * x
                            if (e0.getValue() == 0 || variables.get(v1.var) == 0)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                                //simplification du 1 * x
                            else if (e0.getValue() == 1) simplified.add(new Assign(a.lhs, new Entier(1)));
                                //simplification du 3 * x avec x = 1
                            else if (variables.get(v1.var) == 1) simplified.add(new Assign(a.lhs, new Entier(1)));
                            else simplified.add(new AssignOperator(v1.var, "*", e0, v1));
                        }
                    }
                } else if (a.t0 instanceof Variable && a.t1 instanceof Entier) {
                    Variable v0 = (Variable) a.t0;
                    Entier e1 = (Entier) a.t1;
                    switch (a.op) {
                        case "+" -> {
                            variables.put(a.lhs, variables.get(v0.var) + e1.getValue());
                            if (e1.getValue() == 0) simplified.add(new Assign(a.lhs, v0));
                            else simplified.add(new AssignOperator(a.lhs, "+", v0, e1));
                        }
                        case "-" -> {
                            variables.put(a.lhs, variables.get(v0.var) - e1.getValue());
                            //simplification du x - 1 avec x = 1
                            if (e1.getValue() == 1 && variables.get(v0.var) == 1)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                                //simplification du x - 0
                            else if (e1.getValue() == 0) simplified.add(new Assign(a.lhs, e1));
                            else simplified.add(new AssignOperator(a.lhs, "-", v0, e1));
                        }
                        case "*" -> {
                            variables.put(a.lhs, variables.get(v0.var) * e1.getValue());
                            //simplification du 0 * x
                            if (e1.getValue() == 0 || variables.get(v0.var) == 0)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                                //simplification du 1 * x
                            else if (e1.getValue() == 1) simplified.add(new Assign(a.lhs, new Entier(1)));
                                //simplification du 3 * x avec x = 1
                            else if (variables.get(v0.var) == 1) simplified.add(new Assign(a.lhs, new Entier(1)));
                            else simplified.add(new AssignOperator(a.lhs, "*", v0, e1));
                        }
                    }
                } else if (a.t0 instanceof Variable && a.t1 instanceof Variable) {
                    Variable v0 = (Variable) a.t0;
                    Variable v1 = (Variable) a.t1;
                    switch (a.op) {
                        case "+" -> {
                            variables.put(a.lhs, variables.get(v0.var) + variables.get(v1.var));
                            simplified.add(new AssignOperator(a.lhs, "+", v0, v1));
                        }
                        case "-" -> {
                            variables.put(a.lhs, variables.get(v0.var) - variables.get(v1.var));
                            //simplification du x - y avec x = y = 1
                            if (variables.get(v0.var) == 1 || variables.get(v1.var) == 1)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                            else simplified.add(new AssignOperator(a.lhs, "-", v0, v1));
                        }
                        case "*" -> {
                            variables.put(a.lhs, variables.get(v0.var) * variables.get(v1.var));
                            //simplification du x * y avec x = y = 0
                            if (variables.get(v0.var) == 0 || variables.get(v1.var) == 0)
                                simplified.add(new Assign(a.lhs, new Entier(0)));
                                //simplification du x * y avec x = 1
                            else if (variables.get(v0.var) == 0 || variables.get(v1.var) == 0)
                                simplified.add(new Assign(a.lhs, v1));
                                //simplification du x * y avec y = 1
                            else if (variables.get(v0.var) == 0 || variables.get(v1.var) == 0)
                                simplified.add(new Assign(a.lhs, v0));
                            else simplified.add(new AssignOperator(a.lhs, "*", v0, v1));
                        }
                    }
                }
            }
        }
    }

}
