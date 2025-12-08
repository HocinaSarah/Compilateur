/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.analyseur_lexicale;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 7390
 */
class Token {
    String valeur;
    String type;
    boolean erreur;

    Token(String v, String t, boolean e) {
        valeur = v;
        type = t;
        erreur = e;
    }
    
    // NOUVELLES LIGNES AJOUTÉES ICI
    public String getValeur() { return valeur; }
    public String getType() { return type; }
    public boolean estErreur() { return erreur; }

    public String toString() {
        if (erreur)
            return "ERREUR LEXICALE : " + valeur;
        else
            return valeur + " : " + type;
    }}
public class Analyseur_Lexicale {
   
    
    private static final String[] MOTS_CLES = {
        "abstract","array","as","break","callable","case","catch","class","clone",
    "const","continue","declare","default","die","do","echo","else","elseif","empty",
    "enddeclare","endfor","endforeach","endif","endswitch","endwhile","eval","exit",
    "extends","final","finally","for","foreach","function","global","goto","if",
    "implements","include","include_once","instanceof","insteadof","interface",
    "isset","list","namespace","new","print","private","protected","public",
    "require","require_once","return","static","switch","throw","trait","try","unset",
    "use","var","while","yield","true","false","Sarah","Hocina","A4"
    };

    private static final String[] OPERATEURS_MOTS = {"and", "not", "and"};

    private static final String[] OPERATEURS = {
        "===","!==","<<<","==","!=","<=",">=","++","--","&&","||",".=","->","::","**","??",
        ">>>=","<<<=","+","-","*","/","%","=","<",">","!","&","|","^","."
    };

    private static final char[] SEPARATEURS = {'(',')','{','}','[',']',';',','};

    enum Etat {
        DEBUT, IDENTIFIANT, NOMBRE, CHAINE, CARACTERE, OPERATEUR,
        COMMENTAIRE_LIGNE, COMMENTAIRE_BLOC
    }

    private static boolean estLettre(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean estChiffre(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean estEspace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    private static boolean estSeparateur(char c) {
        for (int i = 0; i < SEPARATEURS.length; i++) {
            if (SEPARATEURS[i] == c) return true;
        }
        return false;
    }

    private static boolean estDebutOperateur(char c) {
        char[] ops = {'+','-','*','/','%','=','<','>','!','&','|','^','.'};
        for (int i = 0; i < ops.length; i++) {
            if (ops[i] == c) return true;
        }
        return false;
    }

    private static boolean estMotCle(String mot) {
        for (int i = 0; i < MOTS_CLES.length; i++) {
            if (egale(MOTS_CLES[i], mot)) return true;
        }
        return false;
    }

    private static boolean estOperateurMot(String mot) {
        for (int i = 0; i < OPERATEURS_MOTS.length; i++) {
            if (egale(OPERATEURS_MOTS[i], mot)) return true;
        }
        return false;
    }

    private static boolean egale(String s1, String s2) {
        if (s1.length() != s2.length()) return false;
        for (int i = 0; i < s1.length(); i++) {
            if (s1.charAt(i) != s2.charAt(i)) return false;
        }
        return true;
    }

    private static boolean commenceParDollar(String s) {
        if (s == null || s.isEmpty()) return false;
    char first = s.charAt(0);
    if (!Character.isLetter(first) && first != '_') return false; // premier caractère

    // les autres caractères
    for (int i = 1; i < s.length(); i++) {
        char c = s.charAt(i);
        if (!Character.isLetterOrDigit(c) && c != '_') return false;
    }
    return true;
    }

    private static String lireOperateur(String text, int index) {
        for (int i = 0; i < OPERATEURS.length; i++) {
            String op = OPERATEURS[i];
            if (index + op.length() <= text.length()) {
                boolean correspond = true;
                for (int j = 0; j < op.length(); j++) {
                    if (text.charAt(index + j) != op.charAt(j)) {
                        correspond = false;
                        break;
                    }
                }
                if (correspond) return op;
            }
        }
        return null;
    }

    public static List<Token> analyserFichier(String fichierPath) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(fichierPath));
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = br.read()) != -1) sb.append((char)c);
        br.close();

        String text = sb.toString();
        List<Token> tokens = new ArrayList<>();
        String token = "";
        Etat etat = Etat.DEBUT;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            switch (etat) {
                case DEBUT:
                    
                   
                   if (estLettre(ch) || ch == '_') {
                        token = "" + ch;
                        etat = Etat.IDENTIFIANT;
                    }
                    else if (estChiffre(ch)) {
                        token = "" + ch;
                        etat = Etat.NOMBRE;
                    }
                    else if (ch == '"') {
                        token = "" + ch;
                        etat = Etat.CHAINE;
                    }
                    else if (ch == '\'') {
                        token = "" + ch;
                        etat = Etat.CARACTERE;
                    }
                    else if (ch == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                        etat = Etat.COMMENTAIRE_LIGNE;
                        i++;
                    }
                    else if (ch == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                        etat = Etat.COMMENTAIRE_BLOC;
                        i++;
                    }
                    else if (ch == '#') {
                        etat = Etat.COMMENTAIRE_LIGNE;
                    }
                    else if (estDebutOperateur(ch)) {
                        String op = lireOperateur(text, i);
                        if (op != null) {
                            tokens.add(new Token(op, "OPERATEUR", false));
                            i += op.length() - 1;
                        } else {
                            tokens.add(new Token("" + ch, "OPERATEUR", false));
                        }
                    }
                    else if (estSeparateur(ch)) {
                        tokens.add(new Token("" + ch, "SEPARATEUR", false));
                    }
                    else if (!estEspace(ch)) {
                        tokens.add(new Token("" + ch, "ERREUR", true));
                    }
                    break;

                case IDENTIFIANT:
                    if (estLettre(ch) || estChiffre(ch) || ch == '_') {
                        token += ch;
                    } else {
                        // Traiter le token accumulé
                        if (estMotCle(token)) {
    tokens.add(new Token(token, "MOT_CLE", false));
} 
else if (estOperateurMot(token)) {
    tokens.add(new Token(token, "OPERATEUR", false));
} 
else if (commenceParDollar(token)) {
    tokens.add(new Token(token, "IDENTIFIANT", false));
} 
else {
    tokens.add(new Token(token, "ERREUR", true));
}
                        token = "";
                        etat = Etat.DEBUT;

                        // Traiter le caractère actuel dans l'état DEBUT
                        if (ch == '-') {
                            token = "" + ch;
                            etat = Etat.IDENTIFIANT;
                        }
                        else if (estLettre(ch) || ch == '_') {
                            token = "" + ch;
                            etat = Etat.IDENTIFIANT;
                        }
                        else if (estChiffre(ch)) {
                            token = "" + ch;
                            etat = Etat.NOMBRE;
                        }
                        else if (ch == '"') {
                            token = "" + ch;
                            etat = Etat.CHAINE;
                        }
                        else if (ch == '\'') {
                            token = "" + ch;
                            etat = Etat.CARACTERE;
                        }
                        else if (ch == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                            etat = Etat.COMMENTAIRE_LIGNE;
                            i++;
                        }
                        else if (ch == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                            etat = Etat.COMMENTAIRE_BLOC;
                            i++;
                        }
                        else if (ch == '#') {
                            etat = Etat.COMMENTAIRE_LIGNE;
                        }
                        else if (estDebutOperateur(ch)) {
                            String op = lireOperateur(text, i);
                            if (op != null) {
                                tokens.add(new Token(op, "OPERATEUR", false));
                                i += op.length() - 1;
                            } else {
                                tokens.add(new Token("" + ch, "OPERATEUR", false));
                            }
                        }
                        else if (estSeparateur(ch)) {
                            tokens.add(new Token("" + ch, "SEPARATEUR", false));
                        }
                        else if (!estEspace(ch)) {
                            tokens.add(new Token("" + ch, "ERREUR", true));
                        }
                    }
                    break;

                case NOMBRE:
                    if (estChiffre(ch) || ch == '.' || ch == 'e' || ch == 'E' || ch == '+' || ch == '-' || ch == 'x' || ch == 'b') {
                        token += ch;
                    } else {
                        tokens.add(new Token(token, "NOMBRE", false));
                        token = "";
                        etat = Etat.DEBUT;

                        // Traiter le caractère actuel
                        if (ch == '$') {
                            token = "" + ch;
                            etat = Etat.IDENTIFIANT;
                        }
                        else if (estLettre(ch) || ch == '_') {
                            token = "" + ch;
                            etat = Etat.IDENTIFIANT;
                        }
                        else if (estChiffre(ch)) {
                            token = "" + ch;
                            etat = Etat.NOMBRE;
                        }
                        else if (ch == '"') {
                            token = "" + ch;
                            etat = Etat.CHAINE;
                        }
                        else if (ch == '\'') {
                            token = "" + ch;
                            etat = Etat.CARACTERE;
                        }
                        else if (ch == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                            etat = Etat.COMMENTAIRE_LIGNE;
                            i++;
                        }
                        else if (ch == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                            etat = Etat.COMMENTAIRE_BLOC;
                            i++;
                        }
                        else if (ch == '#') {
                            etat = Etat.COMMENTAIRE_LIGNE;
                        }
                        else if (estDebutOperateur(ch)) {
                            String op = lireOperateur(text, i);
                            if (op != null) {
                                tokens.add(new Token(op, "OPERATEUR", false));
                                i += op.length() - 1;
                            } else {
                                tokens.add(new Token("" + ch, "OPERATEUR", false));
                            }
                        }
                        else if (estSeparateur(ch)) {
                            tokens.add(new Token("" + ch, "SEPARATEUR", false));
                        }
                        else if (!estEspace(ch)) {
                            tokens.add(new Token("" + ch, "ERREUR", true));
                        }
                    }
                    break;

                case CHAINE:
                    token += ch;
                    if (ch == '"') {
                        tokens.add(new Token(token, "CHAINE", false));
                        token = "";
                        etat = Etat.DEBUT;
                    }
                    break;

                case CARACTERE:
                    token += ch;
                    if (ch == '\'') {
                        tokens.add(new Token(token, "CARACTERE", false));
                        token = "";
                        etat = Etat.DEBUT;
                    }
                    break;

                case COMMENTAIRE_LIGNE:
                    if (ch == '\n') etat = Etat.DEBUT;
                    break;

                case COMMENTAIRE_BLOC:
                    if (ch == '*' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                        etat = Etat.DEBUT;
                        i++;
                    }
                    break;
            }
        }

        // Traiter le token restant
        if (token.length() > 0) {
            if (etat == Etat.IDENTIFIANT) {
                if (commenceParDollar(token)) {
                    tokens.add(new Token(token, "IDENTIFICATEUR", false));
                } else if (estOperateurMot(token)) {
                    tokens.add(new Token(token, "OPERATEUR", false));
                } else if (estMotCle(token)) {
                    tokens.add(new Token(token, "MOT_CLE", false));
                } else {
                    tokens.add(new Token(token, "ERREUR", true));
                }
            } else if (etat == Etat.NOMBRE) {
                tokens.add(new Token(token, "NOMBRE", false));
            } else if (etat == Etat.CHAINE) {
                tokens.add(new Token(token, "CHAINE", false));
            } else if (etat == Etat.CARACTERE) {
                tokens.add(new Token(token, "CARACTERE", false));
            }
        }

        return tokens;
    }

    public static void main(String[] args) throws IOException {
  
       String fichierPath = "test.py";
    
    // 1. Analyse lexicale
    System.out.println("      ANALYSE LEXICALE    ");
    List<Token> tokens = analyserFichier(fichierPath);
    for (Token t : tokens) 
        System.out.println(t);
    
    // 2. Analyse syntaxique
    System.out.println("        ANALYSE SYNTAXIQUE      ");
    AnalyseurSyntaxique parser = new AnalyseurSyntaxique(tokens);
    try {
        parser.analyser();
    } catch (Exception e) {
        System.err.println(" Erreur: " + e.getMessage());
    }
}}


    