package rs.luka.android.studygroup.misc;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luka on 3.2.16..
 */
public class TextUtils {
    private static final Map<Character, Character> latinToGreek;
    static {
        latinToGreek = new HashMap<>();
        latinToGreek.put('A', 'Α');
        latinToGreek.put('a', 'α');
        latinToGreek.put('B', 'Β');
        latinToGreek.put('b', 'β');
        latinToGreek.put('G', 'Γ');
        latinToGreek.put('g', 'γ');
        latinToGreek.put('D', 'Δ');
        latinToGreek.put('d', 'δ');
        latinToGreek.put('E', 'Ε');
        latinToGreek.put('e', 'ε');
        latinToGreek.put('Z', 'Ζ');
        latinToGreek.put('z', 'ζ');
        latinToGreek.put('H', 'Η');
        latinToGreek.put('h', 'η');
        latinToGreek.put('Q', 'Θ');
        latinToGreek.put('q', 'θ');
        latinToGreek.put('I', 'Ι');
        latinToGreek.put('i', 'ι');
        latinToGreek.put('K', 'Κ');
        latinToGreek.put('k', 'κ');
        latinToGreek.put('L', 'Λ');
        latinToGreek.put('l', 'λ');
        latinToGreek.put('M', 'Μ');
        latinToGreek.put('m', 'μ');
        latinToGreek.put('N', 'Ν');
        latinToGreek.put('n', 'ν');
        latinToGreek.put('X', 'Ξ');
        latinToGreek.put('x', 'ξ');
        latinToGreek.put('O', 'Ο');
        latinToGreek.put('o', 'ο');
        latinToGreek.put('P', 'Π');
        latinToGreek.put('p', 'π');
        latinToGreek.put('R', 'Ρ');
        latinToGreek.put('r', 'ρ');
        latinToGreek.put('S', 'Σ');
        latinToGreek.put('s', 'σ');
        latinToGreek.put('T', 'Τ');
        latinToGreek.put('t', 'τ');
        latinToGreek.put('Y', 'Υ');
        latinToGreek.put('y', 'υ');
        latinToGreek.put('F', 'Φ');
        latinToGreek.put('f', 'φ');
        latinToGreek.put('C', 'Χ');
        latinToGreek.put('c', 'χ');
        latinToGreek.put('U', 'Ψ');
        latinToGreek.put('u', 'ψ');
        latinToGreek.put('W', 'Ω');
        latinToGreek.put('w', 'ω');
    }

    public static String replaceGreekEscapes(String str) {
        if(!str.contains("\\")) return str;
        StringBuilder ret = new StringBuilder(str.length());
        for(int i=0; i<str.length(); i++) {
            if(str.charAt(i) != '\\') ret.append(str.charAt(i));
            else {
                i++;
                if(latinToGreek.containsKey(str.charAt(i)))
                    ret.append(latinToGreek.get(str.charAt(i)));
                else
                    ret.append(str.charAt(i));
            }
        }
        return ret.toString();
    }
}
