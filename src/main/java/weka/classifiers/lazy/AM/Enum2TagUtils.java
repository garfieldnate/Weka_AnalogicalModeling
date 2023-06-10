package weka.classifiers.lazy.AM;

import weka.core.SelectedTag;
import weka.core.Tag;

/**
 * In Weka, configuration with a specific set of possible values is implemented using {@link Tag}. These
 * utilities make it possible to use an enum as the set of tags for a given congif parameter.
 */
public class Enum2TagUtils {
    /**
     * Enums whose values are to be used as tags should implement this.
     */
    public interface TagInfo {
        /**
         * @return The user-facing tag description (used for the {@code readable} parameter
         */
        String getDescription();

        /**
         * @return the option string to be used to indicate the enum member
         */
        String getOptionString();
    }

    /**
     * @param enumClass The enum whose members are used as tags
     * @return Array of tags to be used for Weka configuration
     */
    public static <E extends Enum<E> & TagInfo> Tag[] getTags(Class<E> enumClass) {
        E[] values = enumClass.getEnumConstants();
        Tag[] tags = new Tag[values.length];
        for (int i = 0; i < tags.length; i++)
            tags[i] = new Tag(values[i].ordinal(), values[i].getDescription());
        return tags;
    }

    /**
     * @param enumClass The enum whose members are used as tags
     * @param tag       specifying which enum element to return. The id of this tag must match the desired element's ordinal()
     *                  value.
     * @return The selected element of this enum
     */
    public static <E extends Enum<E>> E getElement(Class<E> enumClass, SelectedTag tag) {
        int id = tag.getSelectedTag().getID();
        for (E value : enumClass.getEnumConstants()) {
            if (value.ordinal() == id) return value;
        }
        throw new IllegalArgumentException("There is no element with the specified value");
    }

    /**
     * @param enumClass The enum whose members are used as tags
     * @param option    The option specified by the user; it must match {@link TagInfo#getOptionString() getOptionString}
     *                  for one enum member
     * @return The enum member specified by the option string
     */
    public static <E extends Enum<E> & TagInfo> E getElement(Class<E> enumClass, String option) {
        for (E mdc : enumClass.getEnumConstants()) {
            if (mdc.getOptionString().equals(option)) {
                return mdc;
            }
        }
        throw new IllegalArgumentException("There is no element with the specified option string");
    }
}
