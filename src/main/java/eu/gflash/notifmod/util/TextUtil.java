package eu.gflash.notifmod.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.stream.Stream;

/**
 * {@link Text} related utility functions.
 * @author Alex811
 */
public class TextUtil {
    public static final Text EMPTY = Text.of("");

    /**
     * Get an {@link Object} as a formatted {@link Text}.
     * @param text an {@link Object} with a suitable {@link String} representation.
     * @param format the {@link Formatting} to use
     * @return the formatted text
     */
    public static Text getWithFormat(Object text, Formatting format){
        return getWithFormat(Text.of(String.valueOf(text)), format);
    }

    /**
     * Get formatted copy of a {@link Text}.
     * Note: the internal structure of the {@link Text} will be different.
     * @param text input {@link Text} to format
     * @param format the {@link Formatting} to use
     * @return the formatted text
     */
    public static Text getWithFormat(Text text, Formatting format){
        return buildText(text.copyContentOnly().getWithStyle(Style.EMPTY.withFormatting(format)).toArray(Text[]::new));
    }

    /**
     * Merges the input {@link Text}s into a single {@link Text}.
     * @param texts {@link Text}s to merge
     * @return {@link Text} that includes all input {@link Text}s
     */
    public static Text buildText(Text... texts){
        MutableText result = Text.empty();
        Stream.of(texts).forEach(result::append);
        return result;
    }
}
