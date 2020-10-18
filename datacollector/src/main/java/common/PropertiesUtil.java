package common;

import java.util.Properties;

public class PropertiesUtil {

    /**
     *
     * @param propsArray: Properties Array, the later properties will override the previous ones
     * @return
     */
    static public Properties newProperties(final Properties... propsArray) {
        Properties newProperties = new Properties();
        for(Properties properties: propsArray) {
            if(properties != null) {
                for(String key: properties.stringPropertyNames()) {
                    newProperties.put(key, properties.getProperty(key));
                }
            }
        }
        return newProperties;
    }
}
