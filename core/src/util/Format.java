package util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class Format {

    public static final char ARROW_UP = '\u02C4';
    public static final char ARROW_DOWN = '\u02C5';
    public static final char ARROW_LEFT = '<';
    public static final char ARROW_RIGHT = '>';

    private static Pattern sWhiteSpacePattern;


    public static boolean isEmpty(@Nullable CharSequence sequence) {
        return sequence == null || sequence.length() == 0;
    }

    public static boolean notEmpty(@Nullable CharSequence sequence) {
        return !isEmpty(sequence);
    }

    @NotNull
    public static Pattern getWhiteSpacePattern() {
        if (sWhiteSpacePattern == null) {
            sWhiteSpacePattern = Pattern.compile("\\s");;
        }

        return sWhiteSpacePattern;
    }

    @NotNull
    public static String removeAllWhiteSpaces(@NotNull CharSequence s) {
        return getWhiteSpacePattern().matcher(s).replaceAll("");
    }

    @NotNull
    public static String removeAllLinedComments(@NotNull String str, @NotNull String commentToken, boolean removeNewLineChar) {
        int comment_token_i = str.indexOf(commentToken);
        if (comment_token_i == -1)
            return str;

        final StringBuilder sb = new StringBuilder(str);

        do {
            int line_i = sb.indexOf("\n", comment_token_i + 1);
            if (line_i == -1) {
                sb.delete(comment_token_i, sb.length());
                break;
            }

            sb.delete(comment_token_i, line_i + (removeNewLineChar? 1: 0));
            comment_token_i = sb.indexOf(commentToken, comment_token_i + (removeNewLineChar? 0: 1));
        } while (comment_token_i != -1);

        return sb.toString();
    }
}
