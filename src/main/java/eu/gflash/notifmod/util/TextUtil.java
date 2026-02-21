package eu.gflash.notifmod.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * {@link Component} related utility functions.
 * @author Alex811
 */
public abstract class TextUtil {
    public static final Component EMPTY = Component.nullToEmpty("");

    /**
     * Get an {@link Object} as a formatted {@link Component}.
     * @param text an {@link Object} with a suitable {@link String} representation.
     * @param format the {@link ChatFormatting} to use
     * @return the formatted text
     */
    public static Component getWithFormat(Object text, ChatFormatting format){
        return getWithFormat(Component.nullToEmpty(String.valueOf(text)), format);
    }

    /**
     * Get formatted copy of a {@link Component}.
     * Note: the internal structure of the result {@link Component} might be different.
     * @param text input {@link Component} to format
     * @param format the {@link ChatFormatting} to use
     * @return the formatted text
     */
    public static Component getWithFormat(Component text, ChatFormatting format){
        return text.plainCopy().withStyle(Style.EMPTY.applyFormat(format));
    }

    /**
     * Get input {@link Component} with any content that matches the input {@link Pattern} filled with the input {@link Style}.
     * Note: the internal structure of the result {@link Component} might be different.
     * @param pattern the {@link Pattern} to match
     * @param text the {@link Component} to apply the {@code style} to
     * @param style the {@link Style} to fill matches with
     * @param onlyFillAbsent if true, only fills absent parts of {@code text}'s {@link Style} with definitions from {@code style}, otherwise, it overwrites all of it
     * @param keepEventsAndInsertion if true and {@code onlyFillAbsent} is false, the new style will maintain {@link Style#hoverEvent}, {@link Style#clickEvent} and {@link Style#insertion}
     * @param keepFont if true and {@code onlyFillAbsent} is false, the new style will maintain {@link Style#font}
     * @return the {@code text} with matched parts re-stylized
     */
    @SuppressWarnings("JavadocReference")
    public static Component matchFillStyle(Pattern pattern, Component text, Style style, boolean onlyFillAbsent, boolean keepEventsAndInsertion, boolean keepFont){
        Function<Style, Style> getMatchStyle = onlyFillAbsent ? style::applyTo : origS -> {
            Style s = keepFont ? style.withFont(origS.getFont()) : style;
            return keepEventsAndInsertion ? s.withHoverEvent(origS.getHoverEvent()).withClickEvent(origS.getClickEvent()).withInsertion(origS.getInsertion()) : s;
        };
        return TextUtil.buildText(
                TextUtil.flattenToList(text).stream()
                        .map(txt -> new RegExUtil.MatchSegmentation(pattern, txt.getString()).get().stream()
                                .map(seg -> Component.nullToEmpty(seg.str()).plainCopy().withStyle(seg.matched() ? getMatchStyle.apply(txt.getStyle()) : txt.getStyle()))
                                .collect(Collectors.toList()))
                        .map(TextUtil::buildText)
                        .collect(Collectors.toList()));
    }

    /**
     * Merges the input {@link Component}s into a single {@link Component}.
     * @param texts {@link Component}s to merge
     * @return {@link Component} that includes all input {@link Component}s
     */
    public static Component buildText(Component... texts){
        MutableComponent result = Component.empty();
        Stream.of(texts).forEach(result::append);
        return result;
    }

    /**
     * Merges the input {@link List} of {@link Component}s into a single {@link Component}.
     * @param texts {@link List} of {@link Component}s to merge
     * @return {@link Component} that includes all input {@link Component}s
     */
    public static Component buildText(List<? extends Component> texts) {return buildText(texts.toArray(Component[]::new));}

    /**
     * Flattens internal structure of a {@link Component} into a {@link List} containing all the included {@link Component}s.
     * @param text {@link Component} to flatten
     * @return {@link List} containing the included {@link Component}s
     */
    public static List<Component> flattenToList(Component text){
        ArrayList<Component> texts = new ArrayList<>();
        text.visit((s, t) -> {
            texts.add(Component.nullToEmpty(t).plainCopy().withStyle(s));
            return Optional.empty();
        }, Style.EMPTY);
        return texts;
    }

    /**
     * Flattens internal structure of a {@link Component} into an array containing all the included {@link Component}s.
     * @param text {@link Component} to flatten
     * @return array containing the included {@link Component}s
     */
    public static Component[] flattenToArray(Component text) {return flattenToList(text).toArray(Component[]::new);}

    /**
     * Flattens internal structure of a {@link Component}.
     * @param text {@link Component} to flatten
     * @return flattened {@link Component}
     */
    public static Component flatten(Component text) {return buildText(flattenToArray(text));}
}
