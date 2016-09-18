package rs.luka.android.studygroup.misc;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luka on 3.2.16..
 */
public class TextUtils {
    private static final Map<Character, Character> latinToGreek;
    private static final Map<Character, Character> superscripts;
    private static final Map<Character, Character> subscripts;
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
        latinToGreek.put('L', 'Λ');//1st
        latinToGreek.put('l', 'λ');
        latinToGreek.put('M', 'Μ');
        latinToGreek.put('m', 'μ'); //half
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
        latinToGreek.put('u', 'ψ');//2nd
        latinToGreek.put('W', 'Ω');
        latinToGreek.put('w', 'ω');


        superscripts = new HashMap<>();
        superscripts.put('0', '⁰');
        superscripts.put('1', '¹');
        superscripts.put('2', '²');
        superscripts.put('3', '³');
        superscripts.put('4', '⁴');
        superscripts.put('5', '⁵');
        superscripts.put('6', '⁶');
        superscripts.put('7', '⁷');
        superscripts.put('8', '⁸');
        superscripts.put('9', '⁹');
        superscripts.put('+', '⁺');
        superscripts.put('-', '⁻');
        superscripts.put('=', '⁼');
        superscripts.put('(', '⁽');
        superscripts.put(')', '⁾');
        superscripts.put('A', 'ᴬ');
        superscripts.put('B', 'ᴮ');
        superscripts.put('D', 'ᴰ');
        superscripts.put('E', 'ᴱ');
        superscripts.put('G', 'ᴳ');
        superscripts.put('H', 'ᴴ');
        superscripts.put('I', 'ᴵ');
        superscripts.put('J', 'ᴶ');
        superscripts.put('K', 'ᴷ');
        superscripts.put('L', 'ᴸ');
        superscripts.put('M', 'ᴹ');
        superscripts.put('N', 'ᴺ');
        superscripts.put('O', 'ᴼ');
        superscripts.put('P', 'ᴾ');
        superscripts.put('R', 'ᴿ');
        superscripts.put('T', 'ᵀ');
        superscripts.put('U', 'ᵁ');
        superscripts.put('V', 'ⱽ');
        superscripts.put('W', 'ᵂ');
        superscripts.put('a', 'ᵃ');
        superscripts.put('b', 'ᵇ');
        superscripts.put('c', 'ᶜ');
        superscripts.put('d', 'ᵈ');
        superscripts.put('e', 'ᵉ');
        superscripts.put('f', 'ᶠ');
        superscripts.put('g', 'ᵍ');
        superscripts.put('h', 'ʰ');
        superscripts.put('i', 'ⁱ');
        superscripts.put('j', 'ʲ');
        superscripts.put('k', 'ᵏ');
        superscripts.put('l', 'ˡ');
        superscripts.put('m', 'ᵐ');
        superscripts.put('n', 'ⁿ');
        superscripts.put('o', 'ᵒ');
        superscripts.put('p', 'ᵖ');
        superscripts.put('r', 'ʳ');
        superscripts.put('s', 'ˢ');
        superscripts.put('t', 'ᵗ');
        superscripts.put('u', 'ᵘ');
        superscripts.put('v', 'ᵛ');
        superscripts.put('w', 'ʷ');
        superscripts.put('x', 'ˣ');
        superscripts.put('y', 'ʸ');
        superscripts.put('z', 'ᶻ');

        subscripts = new HashMap<>();
        subscripts.put('0', '₀');
        subscripts.put('1', '₁');
        subscripts.put('2', '₂');
        subscripts.put('3', '₃');
        subscripts.put('4', '₄');
        subscripts.put('5', '₅');
        subscripts.put('6', '₆');
        subscripts.put('7', '₇');
        subscripts.put('8', '₈');
        subscripts.put('9', '₉');
    }

    public static String replaceEscapes(String str) {
        str = str.replace("//", "∕");
        if(!str.contains("\\") && !str.contains("^") && !str.contains("_")) return str;

        StringBuilder ret = new StringBuilder(str.length());
        for(int i=0; i<str.length(); i++) {
            if(str.charAt(i) != '\\' && str.charAt(i) != '^'
               && !(str.charAt(i) == '_' && (Character.isDigit(str.charAt(i+1)) || str.charAt(i+1) == '_')))
                ret.append(str.charAt(i));
            else if(str.charAt(i) == '\\') {
                i++;
                if(latinToGreek.containsKey(str.charAt(i)))
                    ret.append(latinToGreek.get(str.charAt(i)));
                else
                    ret.append(str.charAt(i));
            } else if(str.charAt(i) == '^') {
                i++;
                if(superscripts.containsKey(str.charAt(i)))
                    ret.append(superscripts.get(str.charAt(i)));
                else
                    ret.append(str.charAt(i));
            } else {
                if (str.charAt(i + 1) == '_' && Character.isDigit(str.charAt(i + 2))) {
                    i += 2;
                    ret.append('_').append(str.charAt(i));
                } else if(Character.isDigit(str.charAt(i+1))) {
                    i++;
                    ret.append(subscripts.get(str.charAt(i)));
                } else {
                    ret.append(str.charAt(i));
                }
            }
        }
        return ret.toString();
    }
}
