package eu.gflash.notifmod.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RegEx related utility functions.
 * @author Alex811
 */
public abstract class RegExUtil {
    /**
     * Class that provides a way to divide an input {@link String}, according to an input {@link Pattern}.
     * The result will contain both matched and non-matched parts of the input, accompanied by whether they were a match.
     */
    public static class MatchSegmentation{
        private final Matcher matcher;
        private final String whole;
        private final List<MatchSegment> segments = new ArrayList<>();

        public MatchSegmentation(String pattern, String whole) {this(Pattern.compile(pattern), whole);}
        public MatchSegmentation(Pattern pattern, String whole) {
            this.matcher = pattern.matcher(whole);
            this.whole = whole;
        }

        /**
         * Divide {@link MatchSegmentation#whole} according given {@link Pattern}.
         * @return {@link List} of {@link MatchSegment} containing both matched and non-matched parts.
         * @see MatchSegment
         * @see MatchSegmentation#MatchSegmentation(Pattern, String)
         * @see MatchSegmentation#MatchSegmentation(String, String)
         */
        public List<MatchSegment> get(){
            if(whole == null || !segments.isEmpty()) return segments;
            int prevPos = 0;
            while(matcher.find()){
                addMatchSeg(prevPos, matcher.start(), false);
                addMatchSeg(matcher.start(), matcher.end(), true);
                prevPos = matcher.end();
            }
            addMatchSeg(prevPos, -1, false);
            return segments;
        }

        private void addMatchSeg(int start, int end, boolean matched){
            String seg = whole.substring(start, end < 0 ? whole.length() : end);
            if(!seg.isEmpty()) segments.add(new MatchSegment(seg, matched));
        }

        /**
         * Part of {@link MatchSegmentation#get()}'s result.
         * @param str the part of the {@link MatchSegmentation#whole}
         * @param matched whether it was a match
         * @see MatchSegmentation
         * @see MatchSegmentation#get()
         */
        public record MatchSegment(String str, boolean matched){
            @Override
            public String toString() {
                return (matched ? "" : "NON-") + "MATCHED: \"" + str + "\"";
            }
        }
    }
}
