package com.mycompany.analyseur_lexicale;

import java.util.ArrayList;
import java.util.List;

public class AnalyseurSyntaxique{
    private List<Token> tokens;
    private int position;
    private Token currentToken;
    private List<String> erreurs;
    private boolean erreurTrouvee;
    
    public AnalyseurSyntaxique(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        this.erreurs = new ArrayList<>();
        this.erreurTrouvee = false;
        if (!tokens.isEmpty()) {
            this.currentToken = tokens.get(0);
        }
    }
    
    private void consommer() {
        position++;
        if (position < tokens.size()) {
            currentToken = tokens.get(position);
        } else {
            currentToken = null;
        }
    }
    
    private void ajouterErreur(String message) {
        erreurs.add("ERREUR SYNTAXIQUE [Position " + position + "] : " + message);
        erreurTrouvee = true;
    }
    
    private void match(String type) {
        if (currentToken == null) {
            ajouterErreur("Fin de fichier inattendue. Attendu: " + type);
            return;
        }
        if (!currentToken.getType().equals(type)) {
            ajouterErreur("Attendu: " + type + ", Trouve: " + currentToken.getType() + 
                         " ('" + currentToken.getValeur() + "')");
            // NE PAS ARRETER - on essaie de recuperer
            recupererErreur();
            return;
        }
        consommer();
    }
    
    private void matchMotCle(String motCle) {
        if (currentToken == null) {
            ajouterErreur("Fin de fichier inattendue. Attendu: " + motCle);
            return;
        }
        if (!currentToken.getType().equals("MOT_CLE") || 
            !currentToken.getValeur().equals(motCle)) {
            ajouterErreur("Attendu mot-cle: " + motCle + ", Trouve: " + 
                         currentToken.getValeur());
            recupererErreur();
            return;
        }
        consommer();
    }
    
    private void matchSeparateur(char sep) {
        if (currentToken == null) {
            ajouterErreur("Fin de fichier inattendue. Attendu: " + sep);
            return;
        }
        if (!currentToken.getType().equals("SEPARATEUR") || 
            currentToken.getValeur().charAt(0) != sep) {
            ajouterErreur("Attendu separateur: '" + sep + "', Trouve: '" + 
                         currentToken.getValeur() + "'");
            recupererErreur();
            return;
        }
        consommer();
    }
    
    // Strategie de recuperation d'erreur
    private void recupererErreur() {
        // Avancer jusqu'au prochain point de synchronisation
        while (currentToken != null) {
            String valeur = currentToken.getValeur();
            // Points de synchronisation: separateurs importants
            if (valeur.equals(";") || valeur.equals("}") || valeur.equals("{")) {
                consommer();
                return;
            }
            // Mots-cles de structure
            if (currentToken.getType().equals("MOT_CLE")) {
                if (valeur.equals("if") || valeur.equals("while") || 
                    valeur.equals("for") || valeur.equals("foreach")) {
                    return;
                }
            }
            consommer();
        }
    }
    
    public void programme() {
        while (currentToken != null && !currentToken.getValeur().equals("}")) {
            try {
                instruction();
            } catch (Exception e) {
                ajouterErreur(e.getMessage());
                recupererErreur();
            }
        }
    }
    
    private void instruction() {
        if (currentToken == null) return;
        
        String type = currentToken.getType();
        String valeur = currentToken.getValeur();
        
        if (type.equals("IDENTIFICATEUR")) {
            affectation();
        } else if (type.equals("MOT_CLE")) {
            switch (valeur) {
                case "if":
                    condition();
                    break;
                case "for":
                case "while":
                case "foreach":
                    boucle();
                    break;
                case "print":
                    affichage();
                    break;
                default:
                    ajouterErreur("Mot-cle inattendu: " + valeur);
                    consommer();
            }
        } else if (type.equals("SEPARATEUR") && valeur.equals("{")) {
            bloc();
        } else {
            ajouterErreur("Instruction invalide: " + valeur);
            consommer();
        }
    }
    
    private void affectation() {
        match("IDENTIFICATEUR");
        if (currentToken != null && currentToken.getType().equals("OPERATEUR") && 
            currentToken.getValeur().equals("=")) {
            consommer();
            expression();
        } else {
            ajouterErreur("Attendu '=' apres l'identificateur");
        }
    }
    
    private void condition() {
        matchMotCle("if");
        expression();
        matchSeparateur(':');
        bloc();
        
        if (currentToken != null && currentToken.getType().equals("MOT_CLE") && 
            currentToken.getValeur().equals("else")) {
            matchMotCle("else");
            matchSeparateur(':');
            bloc();
        }
    }
    
    private void boucle() {
        String typeB = currentToken.getValeur();
        
        if (typeB.equals("for")) {
            boucleFor();
        } else if (typeB.equals("while")) {
            boucleWhile();
        } else if (typeB.equals("foreach")) {
            boucleForeach();
        }
    }
    
    private void boucleFor() {
        matchMotCle("for");
        match("IDENTIFICATEUR");
        matchMotCle("in");
        matchMotCle("range");
        matchSeparateur('(');
        arguments();
        matchSeparateur(')');
        matchSeparateur(':');
        bloc();
    }
    
    private void boucleWhile() {
        matchMotCle("while");
        expression();
        matchSeparateur(':');
        bloc();
    }
    
    private void boucleForeach() {
        matchMotCle("foreach");
        match("IDENTIFICATEUR");
        matchMotCle("in");
        expression();
        matchSeparateur(':');
        bloc();
    }
    
    private void bloc() {
        matchSeparateur('{');
        programme();
        matchSeparateur('}');
    }
    
    private void affichage() {
        matchMotCle("print");
        matchSeparateur('(');
        arguments();
        matchSeparateur(')');
    }
    
    private void arguments() {
        if (currentToken != null && !currentToken.getValeur().equals(")")) {
            expression();
            argumentsPrime();
        }
    }
    
    private void argumentsPrime() {
        if (currentToken != null && currentToken.getType().equals("SEPARATEUR") && 
            currentToken.getValeur().equals(",")) {
            matchSeparateur(',');
            expression();
            argumentsPrime();
        }
    }
    
    private void expression() {
        terme();
        expressionPrime();
    }
    
    private void expressionPrime() {
        if (currentToken != null && currentToken.getType().equals("OPERATEUR")) {
            String op = currentToken.getValeur();
            if (op.equals("+") || op.equals("-") || op.equals(">") || 
                op.equals("<") || op.equals("==") || op.equals("!=") || 
                op.equals("<=") || op.equals(">=")) {
                consommer();
                terme();
                expressionPrime();
            }
        }
    }
    
    private void terme() {
        facteur();
        termePrime();
    }
    
    private void termePrime() {
        if (currentToken != null && currentToken.getType().equals("OPERATEUR")) {
            String op = currentToken.getValeur();
            if (op.equals("*") || op.equals("/")) {
                consommer();
                facteur();
                termePrime();
            }
        }
    }
    
    private void facteur() {
        if (currentToken == null) {
            ajouterErreur("Expression attendue, fin de fichier trouvee");
            return;
        }
        
        String type = currentToken.getType();
        
        if (type.equals("NOMBRE") || type.equals("CHAINE") || type.equals("CARACTERE")) {
            consommer();
        } else if (type.equals("IDENTIFICATEUR")) {
            consommer();
            if (currentToken != null && currentToken.getType().equals("SEPARATEUR") && 
                currentToken.getValeur().equals("(")) {
                matchSeparateur('(');
                arguments();
                matchSeparateur(')');
            }
        } else if (type.equals("SEPARATEUR") && currentToken.getValeur().equals("(")) {
            matchSeparateur('(');
            expression();
            matchSeparateur(')');
        } else {
            ajouterErreur("Facteur invalide: " + currentToken.getValeur());
            consommer();
        }
    }
    
    public void analyser() {
        System.out.println("\nANALYSE SYNTAXIQUE ");
        programme();
        
        if (erreurs.isEmpty()) {
            System.out.println(" Analyse syntaxique reussie !");
            System.out.println("  Aucune erreur detectee.");
        } else {
            System.out.println(" Analyse terminee avec " + erreurs.size() + " erreur(s) :");
            System.out.println("─");
            for (int i = 0; i < erreurs.size(); i++) {
                System.out.println((i + 1) + ". " + erreurs.get(i));
            }
            System.out.println("─");
        }
    }
    
    public List<String> getErreurs() {
        return erreurs;
    }
    
    public boolean aDesErreurs() {
        return erreurTrouvee;
    }
}