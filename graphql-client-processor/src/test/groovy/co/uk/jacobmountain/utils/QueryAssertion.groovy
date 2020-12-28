package co.uk.jacobmountain.utils

import groovy.util.logging.Slf4j

import static org.junit.Assert.assertEquals

@Slf4j
class QueryAssertion {

    static String clean(String query) {
        query.split("\n").collect { it.trim() }.join(" ").trim()
    }

    static List<String> split(String query) {
        return [
                query.find("(query|mutation|subscription)\\s*").trim(), // finds the type
                query.find("\\((\\\$\\w+:\\s+\\w+,?\\s*)+\\)"),         // finds the args
                query.find("\\{.*")                                     // finds the selections
        ]
    }

    static void assertArgs(String expected = "", String actual) {
        actual = actual ?: "()"
        expected = expected ?: "()"
        assertEquals("Args are incorrect", "(", actual.substring(0, 1))
        assertEquals("Args are incorrect", ")", actual.substring(actual.length() - 1))
        assertEquals(
                "Args are incorrect",
                expected.substring(1, expected.length() - 1).split(", ") as Set,
                actual.substring(1, expected.length() - 1).split(", ") as Set
        )
    }

    static void assertQueriesAreEqual(String expected, String result) {
        log.info("Asserting equal: ")
        expected = clean(expected)
        result = clean(result)
        log.info("\t{}", expected)
        log.info("\t{}", result)
        def a = split(expected)
        def b = split(result)
        assertEquals("Type is incorrect", a[0], b[0])
        assertArgs(a[1], b[1])
        assertEquals("Fields are incorrect", a[2], b[2])
    }

}
