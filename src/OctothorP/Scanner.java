package OctothorP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static OctothorP.TokenType.*;
import static OctothorP.OctothorP.error;
import static Utils.Textual.*;

public class Scanner {
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fn",    FN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("let",    LET);
        keywords.put("while",  WHILE);
    }
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    // initial state of scanner
    private int start = 0;
    private int current = 0;
    private int line = 1;
    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!itIsTheEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '/': addToken(SLASH); break;
            // comment => ignore line
            case '#': while (peek() != '\n' && !itIsTheEnd()) advance(); break;
            // require looking ahead
            case '!': addToken(lookAhead('=') ? BANG_EQUAL : BANG);break;
            case '=': addToken(lookAhead('=') ? EQUAL_EQUAL : EQUAL);break;
            case '<': addToken(lookAhead('=') ? LESS_EQUAL : LESS);break;
            case '>': addToken(lookAhead('=') ? GREATER_EQUAL : GREATER); break;
            case ' ':
            case '\r':
            case '\t':
                // OctothoP this
                break;
            case '\n': line++; break;
            // string literals
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    // number literals
                    // TODO: Allow for leading decimal point
                    number();
                } else if (isAlpha(c)) {
                    // reserved keywords
                    identifier();
                }else {
                    error(line, "Unexpected character.");
                }
        }
    }
    private void string() {
        while (peek() != '"' && !itIsTheEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (itIsTheEnd()) {
            error(line, "Unterminated string.");
            return;
        }
        // ignore closing #
        advance();

        // Sting cleanup
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance();
        // look for fraction
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    private boolean itIsTheEnd() {
        return current >= source.length();
    }
    private char advance() {
        return source.charAt(current++);
    }
    private boolean lookAhead(char aheadChar) {
        if (itIsTheEnd()) return false;
        // conditional advance
        if (source.charAt(current) != aheadChar) {
            return false;
        }
        current++;
        return true;
    }
    private char peek() {
        if (itIsTheEnd()) return '\0';
        return source.charAt(current);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
