package eu.gflash.notifmod.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link Text} related utility functions.
 * @author Alex811
 */
public abstract class TextUtil {
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
     * Note: the internal structure of the result {@link Text} might be different.
     * @param text input {@link Text} to format
     * @param format the {@link Formatting} to use
     * @return the formatted text
     */
    public static Text getWithFormat(Text text, Formatting format){
        return text.copyContentOnly().fillStyle(Style.EMPTY.withFormatting(format));
    }

    /**
     * Get input {@link Text} with any content that matches the input {@link Pattern} filled with the input {@link Style}.
     * Note: the internal structure of the result {@link Text} might be different.
     * @param pattern the {@link Pattern} to match
     * @param text the {@link Text} to apply the {@code style} to
     * @param style the {@link Style} to fill matches with
     * @param onlyFillAbsent if true, only fills absent parts of {@code text}'s {@link Style} with definitions from {@code style}, otherwise, it overwrites all of it
     * @param keepEventsAndInsertion if true and {@code onlyFillAbsent} is false, the new style will maintain {@link Style#hoverEvent}, {@link Style#clickEvent} and {@link Style#insertion}
     * @param keepFont if true and {@code onlyFillAbsent} is false, the new style will maintain {@link Style#font}
     * @return the {@code text} with matched parts re-stylized
     */
    @SuppressWarnings("JavadocReference")
    public static Text matchFillStyle(Pattern pattern, Text text, Style style, boolean onlyFillAbsent, boolean keepEventsAndInsertion, boolean keepFont){
        Function<Style, Style> getMatchStyle = onlyFillAbsent ? style::withParent : origS -> {
            Style s = keepFont ? style.withFont(origS.getFont()) : style;
            return keepEventsAndInsertion ? s.withHoverEvent(origS.getHoverEvent()).withClickEvent(origS.getClickEvent()).withInsertion(origS.getInsertion()) : s;
        };
        return TextUtil.buildText(
                TextUtil.flattenToList(text).stream()
                        .map(txt -> new RegExUtil.MatchSegmentation(pattern, txt.getString()).get().stream()
                                .map(seg -> Text.of(seg.str()).copyContentOnly().fillStyle(seg.matched() ? getMatchStyle.apply(txt.getStyle()) : txt.getStyle()))
                                .collect(Collectors.toList()))
                        .map(TextUtil::buildText)
                        .collect(Collectors.toList()));
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

    /**
     * Merges the input {@link List} of {@link Text}s into a single {@link Text}.
     * @param texts {@link List} of {@link Text}s to merge
     * @return {@link Text} that includes all input {@link Text}s
     */
    public static Text buildText(List<? extends Text> texts) {return buildText(texts.toArray(Text[]::new));}

    /**
     * Flattens internal structure of a {@link Text} into a {@link List} containing all the included {@link Text}s.
     * @param text {@link Text} to flatten
     * @return {@link List} containing the included {@link Text}s
     */
    public static List<Text> flattenToList(Text text){
        ArrayList<Text> texts = new ArrayList<>();
        text.visit((s, t) -> {
            texts.add(Text.of(t).copyContentOnly().fillStyle(s));
            return Optional.empty();
        }, Style.EMPTY);
        return texts;
    }

    /**
     * Flattens internal structure of a {@link Text} into an array containing all the included {@link Text}s.
     * @param text {@link Text} to flatten
     * @return array containing the included {@link Text}s
     */
    public static Text[] flattenToArray(Text text) {return flattenToList(text).toArray(Text[]::new);}

    /**
     * Flattens internal structure of a {@link Text}.
     * @param text {@link Text} to flatten
     * @return flattened {@link Text}
     */
    public static Text flatten(Text text) {return buildText(flattenToArray(text));}
}
