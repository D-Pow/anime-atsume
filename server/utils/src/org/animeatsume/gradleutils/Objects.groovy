import java.lang.reflect.Field;

import groovy.json.JsonBuilder;

import org.gradle.api.Project;


def objToJson(Object obj) {
    return new JsonBuilder(obj).toPrettyString();
}


def getObjectProperties(Object obj) {
    return getObjectProperties(obj, false, false);
}
def getObjectProperties(Object obj, boolean asString) {
    return getObjectProperties(obj, asString, false);
}
/**
 * Alternatives for printing objects:
 *     - {@code new groovy.json.JsonBuilder(obj).toPrettyString()}
 *     - {@code groovy.json.JsonOutput.prettyPrint(objJsonString)}
 *
 * @see {@link ./src/main/java/org/animeatsume/api/utils/ObjectUtils.java#toString() GetObjectProperties}
 */
def getObjectProperties(Object obj, boolean asString, boolean recurseNestedObjects) {
    Class<?> objClass = obj.getClass();
    Field[] objFields = objClass.getDeclaredFields();

    StringBuilder sb = new StringBuilder(objClass.getName() + " {");

    Map<String, ?> objEntries = Arrays.asList(objFields).stream()
        .reduce(new HashMap<>(), (map, field) -> {
            String fieldName = field.getName();

            sb.append(fieldName).append("=");

            Object fieldValue = "Unparsed";

            try {
                field.setAccessible(true); // Make private fields readable
                fieldValue = field.get(obj);

                // Recurse if field is a dependency/non-primitive class
                // i.e. when `Class::toString()` returns something akin to "com.company.SomeClass@a1b2c3d4"
                if (recurseNestedObjects && fieldValue.toString().matches("^(org|com)[^@]*\\.\\w+@[a-z0-9]{8}\$")) {  // Note: `\\$` (Java) needs to be replaced with `\$` (Gradle)
                    fieldValue = getObjectProperties(fieldValue, asString, recurseNestedObjects);
                }
            } catch (IllegalAccessException ignored) {}

            sb.append("(")
                .append(fieldValue)
                .append(")");

            sb.append(", ");

            map.put(fieldName, fieldValue);

            return map;
        }, (mapIterationA, mapIterationB) -> {
            Map<Object, Object> partiallyCombined = new HashMap<>(mapIterationA);

            partiallyCombined.putAll(mapIterationB);

            return partiallyCombined;
        });

    sb.delete(sb.length() - 2, sb.length()); // Remove last comma+space
    sb.append("}");

    if (asString) {
        return sb.toString();
    }

    return objEntries;
}


def getGradlePropertiesAndSettings() {
    return getGradlePropertiesAndSettings(false);
}
def getGradlePropertiesAndSettings(boolean printNotReturn) {
    return getGradlePropertiesAndSettings(printNotReturn, null);
}
def getGradlePropertiesAndSettings(Project projectToInspect) {
    return getGradlePropertiesAndSettings(false, projectToInspect);
}
def getGradlePropertiesAndSettings(boolean printNotReturn, Project projectToInspect) {
    if (projectToInspect == null) {
        projectToInspect = getRootProject();
    }

    // Not sure why, but `.each { it ->` fails, thus it's omitted
    projectToInspect.properties.each {
        // Note: `$it.someField` is only for strings, `it.someField` is for normal code
        def isNull = it.value == null;

        // TODO - Add this List logic to `ObjectUtils.getObjectProperties()`
        if (
            !isNull
            && (
                it.value.getClass().isArray()
                || it.value instanceof List
                || it.value instanceof Map
                || it.value instanceof Set
                || it.value instanceof Collection
            )
        ) {
            println "$it.key -> ["

            if (it.value instanceof Map) {
                it.value.each { k, v ->
                    println "    ${k} -> ${v}"
                }
            } else {
                it.value.each { propertyValue ->
                    println "    ${propertyValue}"
                }
            }

            println "]"
        } else {
            println "$it.key -> $it.value\n"
        }
    }
}


ext {
    objToJson = this.&objToJson;
    getObjectProperties = this.&getObjectProperties;
    getGradlePropertiesAndSettings = this.&getGradlePropertiesAndSettings;
}
