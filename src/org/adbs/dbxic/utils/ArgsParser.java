package org.adbs.dbxic.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Parsing command-line arguments.
 */

public class ArgsParser {

    private Map<String, String> optionToValue;

    public ArgsParser(String [] args) {
        this.optionToValue = new HashMap<String, String>();

        for (int i = 0; i < args.length; i++) {
            String value = "boolean-opt-active";
            if (i+1 < args.length && !args[i+1].startsWith("--")) {
                value = args[++i];
            }
            optionToValue.put(args[i], value);
        }
    }

    /**
     * hasOption: was the argument of this option provided?
     */
    public  boolean hasOption(String option) {
        return optionToValue.keySet().contains(option);
    } // hasOption()

    /**
     * getOption: get the value of the given option
     */
    public String getOption(String option) {
        return optionToValue.get(option);
    } // getOption()

    /**
     * getOption: get the value of the given option or a default value
     */
    public String getOption(String option, String defaultValue) {
        String val = optionToValue.get(option);
        return (val == null ? defaultValue : val);
    } // getOption()
	
} // ArgsParser
